package com.resourceful_refinement.content.sieve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipe;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipe;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipeInput;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MechanicalFluidSieveBlockEntity extends KineticBlockEntity {

    public static final int TANK_CAPACITY = 4000;
    public static final int MAX_STACK_HEIGHT = 4;
    private FilteringBehaviour filtering;

    public final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };
    public final FluidTank outputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    public final ItemStackHandler outputInv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    public int timer;
    private MechanicalSieveRecipe lastRecipe;
    private ResourceLocation displayedRecipeId;

    // Multiblock data
    public int stackSize = 1;
    public int stackIndex = 0;
    public BlockPos controllerPos;

    public MechanicalFluidSieveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        controllerPos = pos;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        filtering = new FilteringBehaviour(this, new SieveFilterValueBox())
                .withCallback(stack -> {
                    MechanicalFluidSieveBlockEntity controller = getController();
                    if (controller != null && level != null && !level.isClientSide) {
                        controller.lastRecipe = null; // Reset recipe matching on controller when ANY filter in stack changes
                        controller.syncData();
                    }
                })
                .forRecipes();
        behaviours.add(filtering);
    }

    public static class SieveFilterValueBox extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            if (level.getBlockEntity(pos) instanceof MechanicalFluidSieveBlockEntity be) {
                if (be.stackIndex != be.stackSize - 1) return null;
            }
            // Positioned on top face (Y=16), near the front edge (Z=13 for SOUTH facing)
            return rotateHorizontally(state, VecHelper.voxelSpace(8, 16.01, 15));
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            // Rotate to lie flat on the top face
            TransformStack.of(ms).rotateXDegrees(90);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Only the controller processes the recipe
        if (!isController()) {
            timer = 0; // Ensure non-controllers don't have a timer
            return;
        }

        if (getSpeed() == 0) return;

        if (timer > 0) {
            timer -= getProcessingSpeed();

            if (lastRecipe != null) {
                spawnProcessingParticles();
            }

            if (level != null && level.isClientSide) {
                return;
            }

            if (timer <= 0) {
                process();
            }
            return;
        }

        if (level == null || level.isClientSide) return;
        if (inputTank.isEmpty()) {
            if (lastRecipe != null || displayedRecipeId != null) {
                lastRecipe = null;
                displayedRecipeId = null;
                timer = 0;
                sendData();
            }
            return;
        }

        MechanicalSieveRecipeInput input = new MechanicalSieveRecipeInput(List.of(inputTank.getFluid()));

        if (lastRecipe == null || !lastRecipe.matches(input, level)) {
            Optional<RecipeHolder<MechanicalSieveRecipe>> recipe = level.getRecipeManager()
                    .getRecipeFor(ModRecipeTypes.MECHANICAL_SIEVE_TYPE.get(), input, level);

            // Use top block's filter for matching
            if (recipe.isEmpty() || !recipe.get().value().matchesFilter(getTopBlock().getFiltering())) {
                timer = 100; // wait before checking again
                lastRecipe = null;
                displayedRecipeId = null;
                sendData();
                return;
            }

            lastRecipe = recipe.get().value();
            displayedRecipeId = recipe.get().id();
            timer = scaledProcessingDuration(lastRecipe.getProcessingDuration(), stackSize);
            if (timer <= 0) timer = 200;
            sendData();
            return;
        }

        // Use top block's filter for matching
        if (!lastRecipe.matchesFilter(getTopBlock().getFiltering())) {
            lastRecipe = null;
            displayedRecipeId = null;
            sendData();
            return;
        }

        // Ensure we can process (enough space in outputs, enough fluid in input)
        if (!canProcess()) {
            return;
        }

        timer = scaledProcessingDuration(lastRecipe.getProcessingDuration(), stackSize);
        sendData();
    }

    private boolean canProcess() {
        if (lastRecipe == null) return false;
        
        // Check input fluid amount
        SizedFluidIngredient fluidIngredient = lastRecipe.getFluidIngredients().get(0);
        if (!fluidIngredient.test(inputTank.getFluid())) {
            return false;
        }

        // Check output fluid space
        if (!lastRecipe.getFluidResults().isEmpty()) {
            FluidStack resultFluid = lastRecipe.getFluidResults().get(0);
            int filled = outputTank.fill(resultFluid, IFluidHandler.FluidAction.SIMULATE);
            if (filled != resultFluid.getAmount()) {
                return false; // not enough space
            }
        }

        return true;
    }

    private void process() {
        if (!canProcess()) return;

        // Drain input
        SizedFluidIngredient fluidIngredient = lastRecipe.getFluidIngredients().get(0);
        inputTank.drain(fluidIngredient.amount(), IFluidHandler.FluidAction.EXECUTE);

        // Fill output fluid
        if (!lastRecipe.getFluidResults().isEmpty()) {
            FluidStack resultFluid = lastRecipe.getFluidResults().get(0).copy();
            outputTank.fill(resultFluid, IFluidHandler.FluidAction.EXECUTE);
        }

        // Roll item output
        for (int i = 0; i < stackSize; i++) {
            List<ItemStack> rolled = lastRecipe.rollResults(level.random);
            boolean gotItem = false;
            for (ItemStack stack : rolled) {
                if (!stack.isEmpty()) {
                    ItemHandlerHelper.insertItemStacked(outputInv, stack, false);
                    gotItem = true;
                }
            }
            if (gotItem) break; // Stop rolling if we got an item
        }

        setChanged();
        sendData();
    }

    public FilteringBehaviour getFiltering() {
        return filtering;
    }

    public boolean isController() {
        return controllerPos != null && controllerPos.equals(worldPosition);
    }

    public MechanicalFluidSieveBlockEntity getController() {
        if (isController()) return this;
        if (level == null || controllerPos == null) return null;
        if (level.getBlockEntity(controllerPos) instanceof MechanicalFluidSieveBlockEntity be) {
            return be;
        }
        return null;
    }

    public MechanicalFluidSieveBlockEntity getTopBlock() {
        if (level == null || controllerPos == null) return this;
        BlockPos topPos = controllerPos.above(stackSize - 1);
        if (level.getBlockEntity(topPos) instanceof MechanicalFluidSieveBlockEntity be) {
            return be;
        }
        return this;
    }

    public void updateConnectivity() {
        if (level == null || level.isClientSide) return;

        BlockPos columnBottom = findColumnBottom(worldPosition);
        List<BlockPos> column = collectColumn(columnBottom);
        if (column.isEmpty()) return;

        for (int segmentStart = 0; segmentStart < column.size(); segmentStart += MAX_STACK_HEIGHT) {
            int segmentSize = Math.min(MAX_STACK_HEIGHT, column.size() - segmentStart);
            applyStackSegment(column, segmentStart, segmentSize);
        }
    }

    private BlockPos findColumnBottom(BlockPos start) {
        BlockPos bottom = start;
        while (level.getBlockEntity(bottom.below()) instanceof MechanicalFluidSieveBlockEntity) {
            bottom = bottom.below();
        }
        return bottom;
    }

    private List<BlockPos> collectColumn(BlockPos bottom) {
        List<BlockPos> column = new ArrayList<>();
        BlockPos current = bottom;
        while (level.getBlockEntity(current) instanceof MechanicalFluidSieveBlockEntity) {
            column.add(current.immutable());
            current = current.above();
        }
        return column;
    }

    private void applyStackSegment(List<BlockPos> column, int segmentStart, int segmentSize) {
        BlockPos controllerPos = column.get(segmentStart);
        if (!(level.getBlockEntity(controllerPos) instanceof MechanicalFluidSieveBlockEntity controller)) {
            return;
        }

        Direction controllerFacing = controller.getBlockState().getValue(MechanicalFluidSieveBlock.FACING);

        for (int i = 0; i < segmentSize; i++) {
            BlockPos currentPos = column.get(segmentStart + i);
            if (!(level.getBlockEntity(currentPos) instanceof MechanicalFluidSieveBlockEntity sieve)) {
                continue;
            }

            boolean wasController = sieve.controllerPos != null && sieve.controllerPos.equals(sieve.worldPosition);
            boolean assignmentChanged = sieve.stackIndex != i
                    || sieve.stackSize != segmentSize
                    || sieve.controllerPos == null
                    || !sieve.controllerPos.equals(controllerPos);

            if (i == 0 && wasController && sieve.timer > 0 && sieve.lastRecipe != null && sieve.stackSize != segmentSize) {
                preserveRecipeProgress(sieve, sieve.stackSize, segmentSize);
            }

            if (i > 0 && assignmentChanged) {
                clearMemberInventory(sieve, currentPos);
            }

            sieve.controllerPos = controllerPos;
            sieve.stackSize = segmentSize;
            sieve.stackIndex = i;

            if (sieve.getBlockState().getValue(MechanicalFluidSieveBlock.FACING) != controllerFacing) {
                level.setBlockAndUpdate(currentPos, sieve.getBlockState().setValue(MechanicalFluidSieveBlock.FACING, controllerFacing));
            }

            sieve.setChanged();
            sieve.sendData();
        }
    }

    private void preserveRecipeProgress(MechanicalFluidSieveBlockEntity controller, int oldStackSize, int newStackSize) {
        int baseDuration = controller.lastRecipe.getProcessingDuration();
        if (baseDuration <= 0) {
            baseDuration = 200;
        }

        int oldTotalDuration = scaledProcessingDuration(baseDuration, oldStackSize);
        int newTotalDuration = scaledProcessingDuration(baseDuration, newStackSize);
        if (oldTotalDuration <= 0 || newTotalDuration <= 0) {
            return;
        }

        float remainingRatio = controller.timer / (float) oldTotalDuration;
        controller.timer = Mth.ceil(remainingRatio * newTotalDuration);
    }

    private static int scaledProcessingDuration(int baseDuration, int stackSize) {
        return (int) (baseDuration * (1 + 0.25 * (stackSize - 1)));
    }

    private void clearMemberInventory(MechanicalFluidSieveBlockEntity sieve, BlockPos pos) {
        sieve.inputTank.setFluid(FluidStack.EMPTY);
        sieve.outputTank.setFluid(FluidStack.EMPTY);
        for (int slot = 0; slot < sieve.outputInv.getSlots(); slot++) {
            ItemStack stack = sieve.outputInv.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                sieve.outputInv.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    private void spawnProcessingParticles() {
        if (inputTank.isEmpty() || level == null || level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Use a copy to avoid race conditions with the tank being modified in another thread (e.g. Netty encoding)
        FluidStack fluid = inputTank.getFluid().copy();
        if (fluid.isEmpty()) return;
        
        // Spawn particles at the top of the entire stack
        BlockPos topPos = controllerPos.above(stackSize - 1);

        // Spawn 1-2 particles per tick on average
        for (int i = 0; i < 2; i++) {
            double x = topPos.getX() + 0.5;
            double y = topPos.getY() + 0.625 + (level.random.nextDouble() * 2 - 1) * 0.15; // top of the sieve
            double z = topPos.getZ() + 0.5;

            // Radial movement
            double angle = level.random.nextDouble() * 2 * Math.PI;
            double speed = level.random.nextDouble() * 0.1 + 0.05;

            // Check front face to avoid spawning particles there
            Direction facing = getBlockState().getValue(MechanicalFluidSieveBlock.FACING);
            float facingAngle = facing.toYRot(); // degrees
            float particleAngle = (float) Math.toDegrees(angle);

            float diff = Math.abs(Mth.wrapDegrees(particleAngle - facingAngle));
            if (diff < 45) continue; // Skip particles jumping towards the front (90 degree arc)

            double vx = Math.sin(angle) * speed;
            double vz = Math.cos(angle) * speed;
            double vy = level.random.nextDouble() * 0.1 + 0.1; // jump up
            // Apply an offset to ensure the particles don't get stuck inside the block
            x += vx * 7;
            z += vz * 7;

            // Using count=0 allows us to specify velocity via dx, dy, dz
            serverLevel.sendParticles(new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), fluid),
                    x, y, z, 0, vx, vy, vz, 0.625);
        }
    }

    public boolean hasIncompatibleOutput() {
        if (level == null || inputTank.isEmpty()) {
            return false;
        }

        boolean hasOutputFluid = !outputTank.isEmpty();
        boolean hasOutputItem = !outputInv.getStackInSlot(0).isEmpty();
        if (!hasOutputFluid && !hasOutputItem) {
            return false;
        }

        MechanicalSieveRecipeInput recipeInput = new MechanicalSieveRecipeInput(List.of(inputTank.getFluid()));
        List<RecipeHolder<MechanicalSieveRecipe>> recipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.MECHANICAL_SIEVE_TYPE.get());
        
        boolean foundMatchingRecipe = false;
        boolean anyRecipeCompatible = false;

        for (RecipeHolder<MechanicalSieveRecipe> holder : recipes) {
            MechanicalSieveRecipe recipe = holder.value();
            if (recipe.matches(recipeInput, level)) {
                foundMatchingRecipe = true;
                
                boolean isFluidCompatible = true;
                if (hasOutputFluid) {
                    if (!recipe.getFluidResults().isEmpty()) {
                        FluidStack resultFluid = recipe.getFluidResults().get(0);
                        if (outputTank.getFluid().getFluid() != resultFluid.getFluid()) {
                            isFluidCompatible = false;
                        }
                    }
                }

                boolean isItemCompatible = true;
                if (hasOutputItem) {
                    if (recipe.getRollableResults().isEmpty()) {
                        isItemCompatible = false;
                    } else {
                        boolean matchedAnyItem = false;
                        for (var output : recipe.getRollableResults()) {
                            if (outputInv.getStackInSlot(0).getItem() ==  output.getStack().getItem()
                            && outputInv.getStackInSlot(0).getCount() + output.getStack().getCount() <= output.getStack().getItem().getDefaultMaxStackSize()) {
                                matchedAnyItem = true;
                                break;
                            }
                        }
                        if (!matchedAnyItem) {
                            isItemCompatible = false;
                        }
                    }
                }

                if (isFluidCompatible && isItemCompatible) {
                    anyRecipeCompatible = true;
                    break;
                }
            }
        }

        return foundMatchingRecipe && !anyRecipeCompatible;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        // Delegate to controller
        if (!isController())
        {
            return getController().addToGoggleTooltip(tooltip, isPlayerSneaking);
        }

        if (stackSize > 1)
            tooltip.add(Component.literal("     Mechanical Sieve §7[" + stackSize + " Sieves]:"));
        else
            tooltip.add(Component.literal("     Mechanical Sieve §7[" + stackSize + " Sieve]:"));

        float speed = Math.abs(getSpeed());
        tooltip.add(Component.literal("     §b" + (int)(speed * ModStressValues.SIEVE_STRESS) + "su §8at current speed"));

        if (hasIncompatibleOutput()) {
            tooltip.add(Component.literal("     §cBlockage: Empty sieve contents first!"));
        }

        MechanicalSieveRecipe recipeToDisplay = lastRecipe;
        if (recipeToDisplay == null && displayedRecipeId != null && level != null) {
            var holder = level.getRecipeManager().byKey(displayedRecipeId);
            if (holder.isPresent() && holder.get().value() instanceof MechanicalSieveRecipe fpr) {
                recipeToDisplay = fpr;
            }
        }

        // Show intake/outtake
        if (recipeToDisplay != null) {
            int baseDuration = recipeToDisplay.getProcessingDuration();
            if (baseDuration <= 0) baseDuration = 100;

            // Input
            if (!recipeToDisplay.getFluidIngredients().isEmpty()) {
                var ingredient = recipeToDisplay.getFluidIngredients().get(0);
                float rate = ((float)ingredient.amount() / baseDuration) * 20f;

                // Get fluid name from ingredient if tank is empty or doesn't match
                String fluidName = "Required Fluid";
                if (!inputTank.isEmpty() && ingredient.test(inputTank.getFluid())) {
                    fluidName = inputTank.getFluid().getHoverName().getString();
                } else {
                    var matching = ingredient.ingredient().getStacks();
                    if (matching.length > 0) {
                        fluidName = matching[0].getHoverName().getString();
                    }
                }

                tooltip.add(Component.literal("     §9Intake: §r" + String.format("%.0f", rate) + "mB/s (" + fluidName + ")"));
            }

            // Output
            if (!recipeToDisplay.getFluidResults().isEmpty()) {
                var result = recipeToDisplay.getFluidResults().get(0);
                float rate = ((float)result.getAmount() / baseDuration) * 20f;
                tooltip.add(Component.literal("     §6Outtake: §r" + String.format("%.0f", rate) + "mB/s (" + result.getHoverName().getString() + ")"));
            }
        }

        // Show contents
        ItemStack invItem = outputInv.getStackInSlot(0);
        if (!invItem.isEmpty())
        {
            tooltip.add(Component.literal("§8     -> §7" + invItem.getCount() + " x " + invItem.getHoverName().getString()));
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putInt("Timer", timer);
        tag.putInt("StackSize", stackSize);
        tag.putInt("StackIndex", stackIndex);
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries,  new CompoundTag()));
        tag.put("OutputInv", outputInv.serializeNBT(registries));
        if (displayedRecipeId != null) {
            tag.putString("DisplayedRecipe", displayedRecipeId.toString());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        timer = tag.getInt("Timer");
        stackSize = tag.getInt("StackSize");
        stackIndex = tag.getInt("StackIndex");
        if (tag.contains("ControllerPos")) {
            controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        }
        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        outputTank.readFromNBT(registries,  tag.getCompound("OutputTank"));
        outputInv.deserializeNBT(registries, tag.getCompound("OutputInv"));
        if (tag.contains("DisplayedRecipe")) {
            displayedRecipeId = ResourceLocation.tryParse(tag.getString("DisplayedRecipe"));
        } else {
            displayedRecipeId = null;
        }
    }

    // -------------------------------------------------------------------------
    // Network sync (for client-side rendering)
    // -------------------------------------------------------------------------
    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
