package com.resourceful_refinement.content.milking_station;

import com.resourceful_refinement.content.milking_station.recipe.MilkingStationRecipe;
import com.resourceful_refinement.content.milking_station.recipe.MilkingStationRecipeInput;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class MilkingStationBlockEntity extends KineticBlockEntity {

    public static final int OUTPUT_TANK_CAPACITY = 4000;

    public final ItemStackHandler outputInv = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    public final FluidTank outputTank = new FluidTank(OUTPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    public final IItemHandler outputItemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return outputInv.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return outputInv.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return outputInv.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return outputInv.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };

    public final IFluidHandler outputFluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return outputTank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return outputTank.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return outputTank.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return outputTank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return outputTank.drain(maxDrain, action);
        }
    };

    private ResourceLocation capturedEntityType;
    private CompoundTag capturedEntityData;
    private int timer;
    private MilkingStationRecipe lastRecipe;
    private ResourceLocation displayedRecipeId;
    private UUID seatedPlayerId;
    private transient Entity previewEntity;
    private transient String previewEntityKey;

    public MilkingStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean hasCapturedEntity() {
        return capturedEntityType != null && capturedEntityData != null;
    }

    public ResourceLocation getCapturedEntityType() {
        return capturedEntityType;
    }

    public boolean hasSeatedPlayer() {
        return seatedPlayerId != null;
    }

    public boolean canSeatPlayer() {
        return !hasCapturedEntity() && !hasSeatedPlayer();
    }

    public void setSeatedPlayer(Player player) {
        seatedPlayerId = player.getUUID();
        capturedEntityType = null;
        capturedEntityData = null;
        clearPreviewEntity();
        timer = 0;
        lastRecipe = null;
        displayedRecipeId = null;
        syncData();
    }

    public void clearSeatedPlayer() {
        if (seatedPlayerId == null) {
            return;
        }
        seatedPlayerId = null;
        timer = 0;
        lastRecipe = null;
        displayedRecipeId = null;
        syncData();
    }

    public boolean captureEntity(Mob mob) {
        if (level == null || level.isClientSide || hasCapturedEntity() || hasSeatedPlayer() || !mob.isAlive()) {
            return false;
        }

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (entityId == null) {
            return false;
        }

        if (mob instanceof Leashable leashable && leashable.isLeashed()) {
            leashable.dropLeash(false, false);
        }

        CompoundTag entityData = new CompoundTag();
        mob.saveWithoutId(entityData);
        entityData.putString("id", entityId.toString());

        capturedEntityType = entityId;
        capturedEntityData = entityData;
        clearPreviewEntity();
        timer = 0;
        lastRecipe = null;
        displayedRecipeId = null;

        mob.discard();
        syncData();
        return true;
    }

    public boolean releaseCapturedEntity(Player leashHolder, boolean leashToPlayer) {
        if (!(level instanceof ServerLevel serverLevel) || !hasCapturedEntity()) {
            return false;
        }

        Optional<Entity> loaded = loadCapturedEntity(serverLevel);
        if (loaded.isEmpty()) {
            clearCapturedEntity();
            return false;
        }

        Entity entity = loaded.get();
        entity.setUUID(java.util.UUID.randomUUID());
        entity.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 1.05, worldPosition.getZ() + 0.5);
        entity.setDeltaMovement(0, 0, 0);
        entity.setYRot(getBlockState().getValue(MilkingStationBlock.FACING).toYRot());
        entity.setXRot(0);

        if (!serverLevel.addFreshEntity(entity)) {
            return false;
        }

        if (leashToPlayer && leashHolder != null && entity instanceof Leashable leashable) {
            leashable.setLeashedTo(leashHolder, true);
        }

        clearCapturedEntity();
        return true;
    }

    private Optional<Entity> loadCapturedEntity(ServerLevel serverLevel) {
        if (capturedEntityData == null) {
            return Optional.empty();
        }

        CompoundTag entityData = capturedEntityData.copy();
        if (!entityData.contains("id") && capturedEntityType != null) {
            entityData.putString("id", capturedEntityType.toString());
        }

        Entity entity = EntityType.loadEntityRecursive(entityData, serverLevel, Function.identity());
        return Optional.ofNullable(entity);
    }

    private void clearCapturedEntity() {
        capturedEntityType = null;
        capturedEntityData = null;
        seatedPlayerId = null;
        clearPreviewEntity();
        timer = 0;
        lastRecipe = null;
        displayedRecipeId = null;
        syncData();
    }

    public Entity getPreviewEntity() {
        if (level == null || !level.isClientSide || !hasCapturedEntity()) {
            return null;
        }

        String key = capturedEntityData.toString();
        if (previewEntity != null && key.equals(previewEntityKey)) {
            return previewEntity;
        }

        CompoundTag entityData = capturedEntityData.copy();
        if (!entityData.contains("id") && capturedEntityType != null) {
            entityData.putString("id", capturedEntityType.toString());
        }

        previewEntity = EntityType.loadEntityRecursive(entityData, level, Function.identity());
        previewEntityKey = key;

        if (previewEntity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        return previewEntity;
    }

    private void clearPreviewEntity() {
        previewEntity = null;
        previewEntityKey = null;
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        validateSeatedPlayer();

        if (!hasActiveRecipeEntity()) {
            clearRecipeProgress();
            return;
        }

        if (getSpeed() == 0) {
            return;
        }

        if (timer > 0) {
            timer -= getProcessingSpeed();
            if (timer <= 0) {
                process();
            }
            sendData();
            return;
        }

        MilkingStationRecipe recipe = resolveRecipe();
        if (recipe == null) {
            timer = 20;
            sendData();
            return;
        }

        if (!canProcess(recipe)) {
            return;
        }

        timer = Math.max(1, recipe.getProcessingDuration());
        sendData();
    }

    private MilkingStationRecipe resolveRecipe() {
        ResourceLocation activeEntityType = getActiveRecipeEntityType();
        if (activeEntityType == null || level == null) {
            clearRecipeProgress();
            return null;
        }

        MilkingStationRecipeInput input = new MilkingStationRecipeInput(activeEntityType);
        if (lastRecipe != null && lastRecipe.matches(input, level)) {
            return lastRecipe;
        }

        Optional<RecipeHolder<MilkingStationRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.MILKING_STATION_TYPE.get(), input, level);
        if (recipe.isEmpty()) {
            lastRecipe = null;
            displayedRecipeId = null;
            return null;
        }

        lastRecipe = recipe.get().value();
        displayedRecipeId = recipe.get().id();
        return lastRecipe;
    }

    private boolean canProcess(MilkingStationRecipe recipe) {
        for (FluidStack fluidResult : recipe.getFluidResults()) {
            FluidStack result = fluidResult.copy();
            int filled = outputTank.fill(result, IFluidHandler.FluidAction.SIMULATE);
            if (filled != result.getAmount()) {
                return false;
            }
        }

        for (ProcessingOutput output : recipe.getRollableResults()) {
            ItemStack result = output.getStack().copy();
            if (result.isEmpty()) {
                continue;
            }
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(outputInv, result, true);
            if (!remainder.isEmpty()) {
                return false;
            }
        }

        return !recipe.getFluidResults().isEmpty() || !recipe.getRollableResults().isEmpty();
    }

    private void process() {
        MilkingStationRecipe recipe = resolveRecipe();
        if (recipe == null || !canProcess(recipe)) {
            timer = 0;
            return;
        }

        for (FluidStack fluidResult : recipe.getFluidResults()) {
            outputTank.fill(fluidResult.copy(), IFluidHandler.FluidAction.EXECUTE);
        }

        for (ItemStack result : recipe.rollResults(level.random)) {
            if (!result.isEmpty()) {
                ItemHandlerHelper.insertItemStacked(outputInv, result.copy(), false);
            }
        }

        timer = 0;
        setChanged();
        sendData();
    }

    private void clearRecipeProgress() {
        if (timer == 0 && lastRecipe == null && displayedRecipeId == null) {
            return;
        }
        timer = 0;
        lastRecipe = null;
        displayedRecipeId = null;
        sendData();
    }

    private boolean hasActiveRecipeEntity() {
        return getActiveRecipeEntityType() != null;
    }

    private ResourceLocation getActiveRecipeEntityType() {
        if (hasCapturedEntity()) {
            return capturedEntityType;
        }
        if (hasSeatedPlayer()) {
            return BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER);
        }
        return null;
    }

    private void validateSeatedPlayer() {
        if (!(level instanceof ServerLevel serverLevel) || seatedPlayerId == null) {
            return;
        }

        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(seatedPlayerId);
        if (player == null || !player.isPassenger()
                || !(player.getVehicle() instanceof MilkingStationSeatEntity seat)
                || !seat.blockPosition().equals(worldPosition)) {
            clearSeatedPlayer();
        }
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float speed = Math.abs(getSpeed());

        tooltip.add(Component.literal("     Milking Station:"));
        tooltip.add(Component.literal("     \u00a7b" + (int) (speed * ModStressValues.MILKING_STATION_STRESS) + "su \u00a78at current speed"));
        if (hasCapturedEntity()) {
            tooltip.add(Component.literal("     \u00a77Holding: \u00a78" + capturedEntityType));
            if (timer > 0) {
                tooltip.add(Component.literal("     \u00a77Progress: \u00a78" + timer + " ticks remaining"));
            }
        } else if (hasSeatedPlayer()) {
            tooltip.add(Component.literal("     \u00a77Holding: \u00a78minecraft:player"));
            if (timer > 0) {
                tooltip.add(Component.literal("     \u00a77Progress: \u00a78" + timer + " ticks remaining"));
            }
        } else {
            tooltip.add(Component.literal(speed == 0 ? "     \u00a78Idle" : "     \u00a77Awaiting captured entity"));
        }
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Timer", timer);
        if (hasCapturedEntity()) {
            tag.putString("CapturedEntityType", capturedEntityType.toString());
            tag.put("CapturedEntityData", capturedEntityData.copy());
        }
        if (displayedRecipeId != null) {
            tag.putString("DisplayedRecipe", displayedRecipeId.toString());
        }
        if (seatedPlayerId != null) {
            tag.putUUID("SeatedPlayer", seatedPlayerId);
        }
        tag.put("OutputInv", outputInv.serializeNBT(registries));
        tag.put("OutputTank", outputTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        timer = tag.getInt("Timer");
        capturedEntityType = tag.contains("CapturedEntityType")
                ? ResourceLocation.tryParse(tag.getString("CapturedEntityType"))
                : null;
        capturedEntityData = tag.contains("CapturedEntityData")
                ? tag.getCompound("CapturedEntityData").copy()
                : null;
        clearPreviewEntity();
        displayedRecipeId = tag.contains("DisplayedRecipe")
                ? ResourceLocation.tryParse(tag.getString("DisplayedRecipe"))
                : null;
        seatedPlayerId = tag.hasUUID("SeatedPlayer") ? tag.getUUID("SeatedPlayer") : null;
        if (tag.contains("OutputInv")) {
            outputInv.deserializeNBT(registries, tag.getCompound("OutputInv"));
        }
        if (tag.contains("OutputTank")) {
            outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
        } else {
            outputTank.setFluid(FluidStack.EMPTY);
        }
    }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
