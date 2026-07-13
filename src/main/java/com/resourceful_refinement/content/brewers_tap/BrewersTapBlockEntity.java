package com.resourceful_refinement.content.brewers_tap;

import com.resourceful_refinement.content.brewers_tap.recipe.BrewersTapRecipe;
import com.resourceful_refinement.content.brewers_tap.recipe.BrewersTapRecipeInput;
import com.resourceful_refinement.content.brewers_tap.FlavourType;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.content.research.ResearchRecipeGate;
import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class BrewersTapBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final int DEFAULT_PROCESSING_TIME = 40;

    public final ItemStackHandler flavourInv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            clearRecipeCache();
            syncData();
        }
    };

    private int timer;
    private int processingDuration = DEFAULT_PROCESSING_TIME;
    private BrewersTapRecipe lastRecipe;
    private ResourceLocation displayedRecipeId;
    private boolean processingBeltItem;
    private int beltProcessSegment = -1;


    // -------------------------------------------------------------------------
    // Block Entity Logic
    // -------------------------------------------------------------------------
    public BrewersTapBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrewersTapBlockEntity be) {
        be.tick();
        be.tickServer();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (!isValveOpen()) {
            if (timer > 0) {
                timer = 0;
                clearRecipeCache();
                syncData();
            }
            return;
        }

        if (timer > 0) {
            tickDepotProcessing();
            return;
        }

        if (processingBeltItem) {
            return;
        }

        if (canProcess()) {
            startProcessing();
        }
    }

    public boolean isValveOpen() {
        return level != null && BrewersTapBlock.isValveOpen(getBlockState());
    }

    public void onValveStateChanged(boolean open) {
        if (!open) {
            timer = 0;
            clearRecipeCache();
        }
        syncData();
    }

    public void onFlavourItemChanged() {
        clearRecipeCache();
        syncData();
    }

    private void startProcessing() {
        processingDuration = lastRecipe != null ? Math.max(1, lastRecipe.getProcessingDuration()) : DEFAULT_PROCESSING_TIME;
        timer = processingDuration;
        level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.35F, 1.4F);
        syncData();
    }

    private void tickDepotProcessing() {
        if (timer % 5 == 0) {
            spawnPourParticles();
        }

        timer--;
        if (timer > 0) {
            return;
        }

        if (canProcess()) {
            process();
        } else {
            clearRecipeCache();
            syncData();
        }
    }

    public static Boolean handleBeltProcessing(BeltBlockEntity belt, TransportedItemStack transported, float nextOffset, boolean blocking) {
        if (belt == null || belt.getLevel() == null || belt.getLevel().isClientSide || transported == null || transported.stack.isEmpty()) {
            return null;
        }

        BrewersTapBlockEntity tap = findBeltTap(belt, transported, nextOffset);
        if (tap == null) {
            return null;
        }

        if (!tap.processingBeltItem) {
            if (blocking || !tap.isValveOpen() || tap.timer > 0 || !tap.canProcessWorkItem(transported.stack)) {
                return null;
            }

            tap.beltProcessSegment = findBeltSegment(belt, transported, nextOffset);
            tap.processingBeltItem = true;
            tap.startProcessing();
        } else if (tap.beltProcessSegment < 0 || !tap.canProcessWorkItem(transported.stack)) {
            tap.cancelBeltProcessing();
            return null;
        }

        tap.holdBeltItemAtTap(belt, transported);
        if (tap.timer > 0) {
            if (tap.timer % 5 == 0) {
                tap.spawnPourParticles();
            }
            tap.timer--;
            tap.syncData();
            return false;
        }

        tap.completeBeltProcessing(belt, transported);
        return false;
    }

    private static BrewersTapBlockEntity findBeltTap(BeltBlockEntity belt, TransportedItemStack transported, float nextOffset) {
        int activeSegment = findBeltSegment(belt, transported, nextOffset);
        if (activeSegment < 0 || belt.getLevel() == null) {
            return null;
        }

        BlockPos tapPos = BeltHelper.getPositionForOffset(belt, activeSegment).above();
        BlockEntity blockEntity = belt.getLevel().getBlockEntity(tapPos);
        return blockEntity instanceof BrewersTapBlockEntity tap ? tap : null;
    }

    private static int findBeltSegment(BeltBlockEntity belt, TransportedItemStack transported, float nextOffset) {
        int currentSegment = (int) transported.beltPosition;
        int nextSegment = (int) nextOffset;
        int minSegment = Math.max(0, Math.min(currentSegment, nextSegment) - 1);
        int maxSegment = Math.min(belt.beltLength - 1, Math.max(currentSegment, nextSegment) + 1);

        for (int segment = minSegment; segment <= maxSegment; segment++) {
            float center = segment + 0.5F;
            float from = transported.beltPosition;
            float to = nextOffset;
            if (Math.abs(from - center) <= 0.51F || Math.min(from, to) <= center && Math.max(from, to) >= center) {
                return segment;
            }
        }

        return -1;
    }

    private void completeBeltProcessing(BeltBlockEntity belt, TransportedItemStack transported) {
        ItemStack result = processWorkItem(transported.stack);
        if (result.isEmpty()) {
            cancelBeltProcessing();
            return;
        }

        ItemStack heldRemainder = transported.stack.copy();
        transported.stack = result;
        placeBeltItemAtTap(transported);

        BeltInventory inventory = getBeltInventory(belt);
        if (inventory != null && !heldRemainder.isEmpty()) {
            TransportedItemStack heldStack = transported.copy();
            heldStack.stack = heldRemainder;
            placeBeltItemAtTap(heldStack);
            inventory.addItem(heldStack);
        }

        spawnFinishParticles();
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6F, 1.0F);
        processingBeltItem = false;
        beltProcessSegment = -1;
        clearRecipeCache();
        syncData();
    }

    private void holdBeltItemAtTap(BeltBlockEntity belt, TransportedItemStack transported) {
        placeBeltItemAtTap(transported);
        transported.locked = true;
        belt.notifyUpdate();
    }

    private void placeBeltItemAtTap(TransportedItemStack transported) {
        float position = beltProcessSegment + 0.5F;
        transported.beltPosition = position;
        transported.prevBeltPosition = position;
    }

    private void cancelBeltProcessing() {
        processingBeltItem = false;
        beltProcessSegment = -1;
        timer = 0;
        clearRecipeCache();
        syncData();
    }

    private boolean canProcess() {
        if (level == null) {
            return false;
        }

        TargetItem target = getTargetItem();
        if (target == null || target.stack().isEmpty()) {
            return false;
        }

        if (level.getBlockEntity(target.pos()) instanceof BeltBlockEntity) {
            return false;
        }

        return canProcessWorkItem(target.stack());
    }

    private boolean canProcessWorkItem(ItemStack workStack) {
        if (level == null || workStack.isEmpty()) {
            return false;
        }

        IFluidHandler source = getAttachedFluidHandler();
        if (source == null) {
            return false;
        }

        FluidStack sourceFluid = getAvailableFluid(source);
        if (sourceFluid.isEmpty()) {
            return false;
        }

        BrewersTapRecipeInput input = new BrewersTapRecipeInput(workStack.copyWithCount(1), sourceFluid, flavourInv.getStackInSlot(0));
        if (lastRecipe == null || !lastRecipe.matches(input, level)) {
            Optional<RecipeHolder<BrewersTapRecipe>> recipe = level.getRecipeManager()
                    .getRecipeFor(ModRecipeTypes.BREWERS_TAP_TYPE.get(), input, level);
            if (recipe.isEmpty()) {
                lastRecipe = null;
                displayedRecipeId = null;
                return false;
            }

            lastRecipe = recipe.get().value();
            displayedRecipeId = recipe.get().id();
            if (!ResearchRecipeGate.canUseServerRecipe(level, displayedRecipeId)) {
                clearRecipeCache();
                return false;
            }
        } else if (!ResearchRecipeGate.canUseServerRecipe(level, displayedRecipeId)) {
            clearRecipeCache();
            return false;
        }

        ItemStack result = lastRecipe.getResultItem(level.registryAccess());
        return !result.isEmpty() && canDrainRecipeFluid(lastRecipe, source, sourceFluid);
    }

    private void process() {
        if (level == null || lastRecipe == null) {
            return;
        }

        TargetItem target = getTargetItem();
        IFluidHandler source = getAttachedFluidHandler();
        if (target == null || source == null) {
            clearRecipeCache();
            syncData();
            return;
        }

        ItemStack result = processWorkItem(target.stack());
        if (result.isEmpty()) {
            clearRecipeCache();
            syncData();
            return;
        }

        ItemStack extracted = target.handler().extractItem(target.slot(), 1, false);
        if (extracted.isEmpty()) {
            clearRecipeCache();
            syncData();
            return;
        }

        ItemStack remainder = insertProcessingOutput(target, result);
        if (!remainder.isEmpty()) {
            Containers.dropItemStack(level, target.pos().getX() + 0.5, target.pos().getY() + 1.0, target.pos().getZ() + 0.5, remainder);
        }

        spawnFinishParticles();
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6F, 1.0F);
        clearRecipeCache();
        syncData();
    }

    private ItemStack processWorkItem(ItemStack workStack) {
        if (level == null || lastRecipe == null || workStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        IFluidHandler source = getAttachedFluidHandler();
        if (source == null) {
            return ItemStack.EMPTY;
        }

        FluidStack sourceFluid = getAvailableFluid(source);
        if (!canDrainRecipeFluid(lastRecipe, source, sourceFluid)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = createResultStack(lastRecipe.getResultItem(level.registryAccess()));
        Optional<FlavourType> flavour = lastRecipe.getMatchingFlavour(flavourInv.getStackInSlot(0));
        flavour.ifPresent(type -> result.set(ModDataComponents.FLAVOUR.get(), type));

        SizedFluidIngredient fluidIngredient = lastRecipe.getFluidIngredients().get(0);
        FluidStack toDrain = sourceFluid.copy();
        toDrain.setAmount(fluidIngredient.amount());
        FluidStack drained = source.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty() || drained.getAmount() != fluidIngredient.amount()) {
            return ItemStack.EMPTY;
        }

        workStack.shrink(1);
        flavour.ifPresent(tag -> flavourInv.extractItem(0, 1, false));
        return result;
    }

    private ItemStack createResultStack(ItemStack recipeResult) {
        ItemStack result = new ItemStack(recipeResult.getItem(), recipeResult.getCount());
        result.applyComponents(recipeResult.getComponentsPatch());
        return result;
    }

    private ItemStack insertProcessingOutput(TargetItem target, ItemStack result) {
        if (level != null && level.getBlockEntity(target.pos()) instanceof DepotBlockEntity depot) {
            ItemStack depotRemainder = insertIntoDepotOutputBuffer(depot, result);
            if (depotRemainder.getCount() != result.getCount()) {
                depot.notifyUpdate();
                return depotRemainder;
            }
        }

        return ItemHandlerHelper.insertItem(target.handler(), result, false);
    }

    private ItemStack insertIntoDepotOutputBuffer(DepotBlockEntity depot, ItemStack result) {
        DepotBehaviour behaviour = depot.getBehaviour(DepotBehaviour.TYPE);
        if (behaviour == null) {
            return result;
        }

        try {
            Field outputBufferField = DepotBehaviour.class.getDeclaredField("processingOutputBuffer");
            outputBufferField.setAccessible(true);
            Object outputBuffer = outputBufferField.get(behaviour);
            if (outputBuffer instanceof ItemStackHandler itemHandler) {
                return ItemHandlerHelper.insertItemStacked(itemHandler, result, false);
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return result;
    }

    private boolean canDrainRecipeFluid(BrewersTapRecipe recipe, IFluidHandler source, FluidStack sourceFluid) {
        if (recipe.getFluidIngredients().isEmpty() || sourceFluid.isEmpty()) {
            return false;
        }

        SizedFluidIngredient fluidIngredient = recipe.getFluidIngredients().get(0);
        if (!fluidIngredient.test(sourceFluid)) {
            return false;
        }

        FluidStack toDrain = sourceFluid.copy();
        toDrain.setAmount(fluidIngredient.amount());
        FluidStack drained = source.drain(toDrain, IFluidHandler.FluidAction.SIMULATE);
        return !drained.isEmpty() && drained.getAmount() == fluidIngredient.amount() && fluidIngredient.test(drained);
    }

    private TargetItem getTargetItem() {
        if (level == null) {
            return null;
        }

        BlockPos targetPos = worldPosition.below();
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, Direction.UP);
        if (handler == null) {
            return null;
        }

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return new TargetItem(targetPos, handler, slot, stack.copy());
            }
        }

        return null;
    }

    private BeltInventory getBeltInventory(BeltBlockEntity belt) {
        if (belt == null) {
            return null;
        }

        BeltBlockEntity controller = belt.getControllerBE();
        if (controller == null) {
            controller = belt;
        }

        return controller.getInventory();
    }

    private IFluidHandler getAttachedFluidHandler() {
        if (level == null) {
            return null;
        }

        Direction pipeFace = BrewersTapBlock.getPipeFace(getBlockState());
        BlockPos sourcePos = worldPosition.relative(pipeFace);
        BlockEntity sourceBe = level.getBlockEntity(sourcePos);
        if (sourceBe instanceof DistilleryBlockEntity distillery) {
            DistilleryBlockEntity controller = distillery.getController();
            return controller != null ? controller.outputTank : null;
        }

        return level.getCapability(Capabilities.FluidHandler.BLOCK, sourcePos, pipeFace.getOpposite());
    }

    private FluidStack getAvailableFluid(IFluidHandler source) {
        if (source == null) {
            return FluidStack.EMPTY;
        }

        for (int tank = 0; tank < source.getTanks(); tank++) {
            FluidStack stack = source.getFluidInTank(tank);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return FluidStack.EMPTY;
    }

    private void clearRecipeCache() {
        lastRecipe = null;
        displayedRecipeId = null;
    }

    private void spawnPourParticles() {
        if (level instanceof ServerLevel serverLevel) {

            IFluidHandler source = getAttachedFluidHandler();
            FluidStack particleFluid = getAvailableFluid(source);

            serverLevel.sendParticles(
                    new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), particleFluid.copy()),
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.275, worldPosition.getZ() + 0.5,
                    3, 0.02, 0.0, 0.02, 0.03);
        }
    }

    private void spawnFinishParticles() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    worldPosition.getX() + 0.5, worldPosition.getY() - 0.25, worldPosition.getZ() + 0.5,
                    8, 0.18, 0.05, 0.18, 0.04);
        }
    }

    public boolean hasFlavourItem()
    {
        return !flavourInv.getStackInSlot(0).isEmpty();
    }


    // -------------------------------------------------------------------------
    // Data Persistence
    // -------------------------------------------------------------------------
    private void syncData() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Timer", timer);
        tag.putInt("ProcessingDuration", processingDuration);
        tag.putBoolean("ProcessingBeltItem", processingBeltItem);
        tag.putInt("BeltProcessSegment", beltProcessSegment);
        tag.put("FlavourInv", flavourInv.serializeNBT(registries));
        if (displayedRecipeId != null) {
            tag.putString("DisplayedRecipe", displayedRecipeId.toString());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        timer = tag.getInt("Timer");
        processingDuration = tag.getInt("ProcessingDuration");
        processingBeltItem = tag.getBoolean("ProcessingBeltItem");
        beltProcessSegment = tag.contains("BeltProcessSegment") ? tag.getInt("BeltProcessSegment") : -1;
        if (processingDuration <= 0) {
            processingDuration = DEFAULT_PROCESSING_TIME;
        }
        if (tag.contains("FlavourInv")) {
            flavourInv.deserializeNBT(registries, tag.getCompound("FlavourInv"));
        }
        displayedRecipeId = tag.contains("DisplayedRecipe") ? ResourceLocation.tryParse(tag.getString("DisplayedRecipe")) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        write(tag, registries, true);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    // -------------------------------------------------------------------------
    // Client Visuals
    // -------------------------------------------------------------------------
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Brewer's Tap:"));
        if (isValveOpen())
            tooltip.add(Component.literal("§a- Valve open -"));
        else
            tooltip.add(Component.literal("§c- Valve closed -"));

        ItemStack flavour = flavourInv.getStackInSlot(0);
        tooltip.add(flavour.isEmpty()
                ? Component.literal("Flavour: empty")
                : Component.literal("Flavour: " + flavour.getCount() + " x " + flavour.getHoverName().getString()));

        IFluidHandler source = getAttachedFluidHandler();
        FluidStack fluid = source != null ? getAvailableFluid(source) : FluidStack.EMPTY;
        tooltip.add(fluid.isEmpty()
                ? Component.literal("Source: empty")
                : Component.literal("Source: " + fluid.getAmount() + "mb " + fluid.getHoverName().getString()));

        if (timer > 0) {
            tooltip.add(Component.literal("Processing: " + (processingDuration - timer) + "/" + processingDuration + "t"));
        }

        return true;
    }

    private record TargetItem(BlockPos pos, IItemHandler handler, int slot, ItemStack stack) {
        private ItemStack singleItem() {
            return stack.copyWithCount(1);
        }
    }
}
