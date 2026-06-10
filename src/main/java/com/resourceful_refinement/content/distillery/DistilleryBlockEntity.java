package com.resourceful_refinement.content.distillery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.distillery.recipe.DistilleryRecipe;
import com.resourceful_refinement.content.distillery.recipe.DistilleryRecipeInput;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipe;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.utilities.GoggleUtilities;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DistilleryBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {


    public static final int TANK_CAPACITY = 8000;
    public static final int MAX_STACK_HEIGHT = 8;
    public static final int MIN_STACK_HEIGHT = 2;
    private FilteringBehaviour filtering;

    public int timer;
    private DistilleryRecipe lastRecipe;
    private ResourceLocation displayedRecipeId;

    // Multiblock data
    public int stackSize = 1;
    public int stackIndex = 0;
    public BlockPos controllerPos;
    public int heatLevel = 0;
    private String processErrorString = "";

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
    public final ItemStackHandler inputInv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };


    // -------------------------------------------------------------------------
    // Block Entity Logic
    // -------------------------------------------------------------------------
    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.DISTILLERY_BE.get(), pos, state);
    }

    public DistilleryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isController() {
        return controllerPos != null && controllerPos.equals(worldPosition);
    }

    public DistilleryBlockEntity getController() {
        if (isController()) return this;
        if (level == null || controllerPos == null) return null;
        if (level.getBlockEntity(controllerPos) instanceof DistilleryBlockEntity be) {
            return be;
        }
        return null;
    }

    public DistilleryBlockEntity getTopBlock() {
        if (level == null || controllerPos == null) return this;
        BlockPos topPos = controllerPos.above(stackSize - 1);
        if (level.getBlockEntity(topPos) instanceof DistilleryBlockEntity be) {
            return be;
        }
        return this;
    }

    public void updateConnectivity() {
        if (level == null || level.isClientSide) return;

        // Update stack information
        BlockPos columnBottom = findColumnBottom(worldPosition);
        List<BlockPos> column = collectColumn(columnBottom);
        if (column.isEmpty()) return;

        for (int segmentStart = 0; segmentStart < column.size(); segmentStart += MAX_STACK_HEIGHT) {
            int segmentSize = Math.min(MAX_STACK_HEIGHT, column.size() - segmentStart);
            applyStackSegment(column, segmentStart, segmentSize);
        }

        // Update heat conditions
        int newHeatingLevel = getHeatingLevel();
        if (newHeatingLevel != heatLevel)
        {
            heatLevel = newHeatingLevel;
            setChanged();
            sendData();
        }
    }

    private BlockPos findColumnBottom(BlockPos start) {
        BlockPos bottom = start;
        while (level.getBlockEntity(bottom.below()) instanceof DistilleryBlockEntity) {
            bottom = bottom.below();
        }
        return bottom;
    }

    private List<BlockPos> collectColumn(BlockPos bottom) {
        List<BlockPos> column = new ArrayList<>();
        BlockPos current = bottom;
        while (level.getBlockEntity(current) instanceof DistilleryBlockEntity) {
            column.add(current.immutable());
            current = current.above();
        }
        return column;
    }

    private void applyStackSegment(List<BlockPos> column, int segmentStart, int segmentSize) {
        BlockPos controllerPos = column.get(segmentStart);
        if (!(level.getBlockEntity(controllerPos) instanceof DistilleryBlockEntity controller)) {
            return;
        }

        Direction controllerFacing = controller.getBlockState().getValue(DistilleryBlock.FACING);

        for (int i = 0; i < segmentSize; i++) {
            BlockPos currentPos = column.get(segmentStart + i);
            if (!(level.getBlockEntity(currentPos) instanceof DistilleryBlockEntity distillery)) {
                continue;
            }

            boolean wasController = distillery.controllerPos != null && distillery.controllerPos.equals(distillery.worldPosition);
            boolean assignmentChanged = distillery.stackIndex != i
                    || distillery.stackSize != segmentSize
                    || distillery.controllerPos == null
                    || !distillery.controllerPos.equals(controllerPos);

            if (i == 0 && wasController && distillery.timer > 0 && distillery.lastRecipe != null) {
                // Check if recipe is now invalidated due to stack-size change, or due to a change in heating level
            }

            if (i > 0 && assignmentChanged) {
                clearMemberInventory(distillery, currentPos);
            }

            distillery.controllerPos = controllerPos;
            distillery.stackSize = segmentSize;
            distillery.stackIndex = i;

            int modelType = 2;
            if (segmentSize == 1)
                modelType = 0;
            else if (i == 0)
                modelType = 1;
            else if (i == segmentSize - 1)
                modelType = 3;

            if (distillery.getBlockState().getValue(DistilleryBlock.FACING) != controllerFacing
                    || distillery.getBlockState().getValue(DistilleryBlock.MODEL_TYPE) != modelType) {
                level.setBlockAndUpdate(currentPos, distillery.getBlockState().setValue(DistilleryBlock.FACING, controllerFacing).setValue(DistilleryBlock.MODEL_TYPE, modelType));
            }

            distillery.setChanged();
            distillery.sendData();
        }
    }

    private void clearMemberInventory(DistilleryBlockEntity distillery, BlockPos pos) {
        distillery.inputTank.setFluid(FluidStack.EMPTY);
        distillery.outputTank.setFluid(FluidStack.EMPTY);
        for (int slot = 0; slot < distillery.inputInv.getSlots(); slot++) {
            ItemStack stack = distillery.inputInv.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                distillery.inputInv.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }


    // -------------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------------
    public FilteringBehaviour getFiltering() {
        return filtering;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new FilteringBehaviour(this, new DistilleryBlockEntity.DistilleryFilterValueBox())
                .withCallback(stack -> {
                    DistilleryBlockEntity controller = getController();
                    if (controller != null && level != null && !level.isClientSide) {
                        controller.lastRecipe = null; // Reset recipe matching on controller when ANY filter in stack changes
                        controller.syncData();
                    }
                })
                .forRecipes();
        behaviours.add(filtering);
    }

    public static class DistilleryFilterValueBox extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            if (level.getBlockEntity(pos) instanceof DistilleryBlockEntity be) {
                if (be.stackIndex != be.stackSize - 1) return null;
            }
            // Positioned on top face (Y=16), near the front edge (Z=13 for SOUTH facing)
            return rotateHorizontally(state, VecHelper.voxelSpace(8, 16.01, 15));
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            // Rotate to lie flat on the top face
            TransformStack.of(ms).rotateXDegrees(90);

            Direction facing = state.getValue(DistilleryBlock.FACING);
            if (facing.getAxis() == Direction.Axis.Z)
                TransformStack.of(ms).rotateZDegrees(facing.toYRot()+180);
            else
                TransformStack.of(ms).rotateZDegrees(-facing.toYRot());
        }
    }


    // -------------------------------------------------------------------------
    // Processing Logic
    // -------------------------------------------------------------------------
    @Override
    public void tick() {
        super.tick();

        // Only the controller processes the recipe
        if (!isController()) {
            timer = 0; // Ensure non-controllers don't have a timer
            return;
        }


        // Decrement timer
        if (timer > 0) {

            // Check progress of ongoing recipe conditions
            if (lastRecipe != null && !canProcess())
                return;

            timer -= 1;

            if (level != null && level.isClientSide) {
                return;
            }

            if (timer <= 0) {
                process();
            }

            return;
        }


        // If all inventories/tanks are empty, exit
        if (level == null || level.isClientSide) return;
        if (inputTank.isEmpty() && inputInv.getStackInSlot(0).isEmpty()) {
            if (lastRecipe != null || displayedRecipeId != null) {
                lastRecipe = null;
                displayedRecipeId = null;
                timer = 0;
                sendData();
            }
            return;
        }

        // Find any matching recipes for current controller state
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inputInv.getSlots(); i++) {
            ItemStack stack = inputInv.getStackInSlot(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        DistilleryRecipeInput input = new DistilleryRecipeInput(items, List.of(inputTank.getFluid()));

        if (lastRecipe == null || !lastRecipe.matches(input, level)) {
            Optional<RecipeHolder<DistilleryRecipe>> recipe = level.getRecipeManager()
                    .getRecipeFor(ModRecipeTypes.DISTILLERY_TYPE.get(), input, level);

            // Use top block's filter for matching
            if (recipe.isEmpty() || !recipe.get().value().matchesFilter(getTopBlock().getFiltering())) {
                timer = 20; // wait before checking again
                lastRecipe = null;
                displayedRecipeId = null;
                sendData();
                return;
            }

            lastRecipe = recipe.get().value();
            displayedRecipeId = recipe.get().id();
            timer = lastRecipe.getProcessingDuration();
            if (timer <= 0) timer = 20;
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

        // Ensure we can process (enough space in outputs, enough fluid in input, valid heat/height)
        if (!canProcess()) {
            return;
        }

        timer = lastRecipe.getProcessingDuration();
        sendData();
    }

    private void process() {
        if (!canProcess()) return;

        // Drain input
        if (lastRecipe.getCombinedIngredients() != null && !lastRecipe.getCombinedIngredients().isEmpty()) {
            for (SizedIngredient sizedIngredient : lastRecipe.getCombinedIngredients()) {
                int amountToDrain = sizedIngredient.count();

                for (int slot = 0; slot < inputInv.getSlots(); slot++) {
                    ItemStack slotStack = inputInv.getStackInSlot(slot);
                    if (sizedIngredient.test(slotStack)) {
                        int drainedAmount = Math.min(amountToDrain, slotStack.getCount());
                        inputInv.extractItem(slot, drainedAmount, false);
                        amountToDrain -= drainedAmount;

                        if (amountToDrain <= 0)
                            break;
                    }
                }
            }
        }

        if (!lastRecipe.getFluidIngredients().isEmpty())
        {
            SizedFluidIngredient fluidIngredient = lastRecipe.getFluidIngredients().getFirst();
            inputTank.drain(fluidIngredient.amount(), IFluidHandler.FluidAction.EXECUTE);
        }

        // Fill output fluid
        if (!lastRecipe.getFluidResults().isEmpty()) {
            FluidStack resultFluid = lastRecipe.getFluidResults().getFirst().copy();
            outputTank.fill(resultFluid, IFluidHandler.FluidAction.EXECUTE);
        }

        setChanged();
        sendData();
    }

    private boolean canProcess() {
        if (lastRecipe == null)
        {
            processErrorString = "";
            return false;
        }

        if (!lastRecipe.matchesFilter(getTopBlock().getFiltering()))
        {
            processErrorString = "No recipes match filter";
            return false;
        }

        // Check input fluid amount
        if (!lastRecipe.getFluidIngredients().isEmpty())
        {
            SizedFluidIngredient fluidIngredient = lastRecipe.getFluidIngredients().getFirst();
            if (!fluidIngredient.test(inputTank.getFluid())) {
                processErrorString = "Insufficient fluid";
                return false;
            }
        }

        // Check input items
        if (lastRecipe.getCombinedIngredients() != null && !lastRecipe.getCombinedIngredients().isEmpty()) {
            SizedIngredient itemIngredient = lastRecipe.getCombinedIngredients().getFirst();
            ItemStack inputSlot = inputInv.getStackInSlot(0);
            if (!itemIngredient.test(inputSlot)) {
                processErrorString = "Insufficient items";
                return false;
            }
        }

        // Check output fluid space
        if (!lastRecipe.getFluidResults().isEmpty()) {
            FluidStack resultFluid = lastRecipe.getFluidResults().getFirst();
            int filled = outputTank.fill(resultFluid, IFluidHandler.FluidAction.SIMULATE);
            if (filled != resultFluid.getAmount()) {
                processErrorString = "Output tank full";
                return false; // not enough space
            }
        }

        // Check heat
        if (lastRecipe.getRequiredHeatCondition() != HeatUtilities.ConvertHeatLevelToExtendedCondition(heatLevel))
        {
            processErrorString = "Distillery must be " + lastRecipe.getRequiredHeatCondition().getSerializedName();
            return false;
        }

        // Check stack height
        if (lastRecipe.getRequiredHeight() != stackSize)
        {
            processErrorString = "Distillery must be " + lastRecipe.getRequiredHeight() + " blocks high";
            return false;
        }

        processErrorString = "";
        return true;
    }

    public DistilleryRecipe getLocalRecipe()
    {
        if (level == null)
            return null;

        if (!level.isClientSide)
            return lastRecipe;

        DistilleryRecipe recipeToDisplay = lastRecipe;
        if (recipeToDisplay == null && displayedRecipeId != null) {
            var holder = level.getRecipeManager().byKey(displayedRecipeId);
            if (holder.isPresent() && holder.get().value() instanceof DistilleryRecipe fpr) {
                recipeToDisplay = fpr;
            }
        }
        return  recipeToDisplay;
    }

    public int getHeatingLevel()
    {
        if (level == null || getController() == null)
            return -1;

        if (!isController())
            return getController().getHeatingLevel();

        BlockPos blockBelow = this.getBlockPos().below();
        return HeatUtilities.GetExtendedHeatLevel(level, blockBelow);
    }

    public float getProgressionFactor()
    {
        if (lastRecipe == null) return 0;

        int recipeDuration = lastRecipe.getProcessingDuration();
        return (1f - (float) timer/recipeDuration);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        if (!isController())
        {
            DistilleryBlockEntity controller = getController();
            if (controller == null)
                return true;
            return getController().addToGoggleTooltip(tooltip, isPlayerSneaking);
        }

        tooltip.add(Component.literal("     Distillery:"));

        FluidStack tankInputFluid = inputTank.getFluid();
        FluidStack tankOutputFluid = outputTank.getFluid();
        ItemStack itemA = inputInv.getStackInSlot(0);

        if (tankInputFluid.isEmpty())
            tooltip.add(Component.literal("Input: §8empty"));
        else
            tooltip.add(Component.literal("Input: §7" + tankInputFluid.getAmount() + "mb " + tankInputFluid.getHoverName().getString()));

        if (!itemA.isEmpty())
            tooltip.add(Component.literal("§8-> §7" + itemA.getHoverName().getString() + " x " + itemA.getCount()));

        tooltip.add(Component.literal(""));
        if (tankOutputFluid.isEmpty())
            tooltip.add(Component.literal("Output: §8empty"));
        else
            tooltip.add(Component.literal("Output: §7" + tankOutputFluid.getAmount() + "mb " + tankOutputFluid.getHoverName().getString()));

        tooltip.add(Component.literal("§7Heating: ").append(HeatUtilities.GetHeatTitle(heatLevel)).withColor(HeatUtilities.GetHeatColour(heatLevel)));

        if (displayedRecipeId != null && lastRecipe != null)
        {
            float progressFactor = getProgressionFactor();
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("§c" + GoggleUtilities.BuildTextProgressBar(progressFactor)
                    + " §c" + GoggleUtilities.FormatTicksToTime((int)((1f - progressFactor) * lastRecipe.getProcessingDuration()))));
            if (!canProcess())
            {
                tooltip.add(Component.literal("§c" + processErrorString));
            }
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
        tag.putInt("HeatLevel", heatLevel);
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries,  new CompoundTag()));
        tag.put("InputInv", inputInv.serializeNBT(registries));
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
        heatLevel = tag.getInt("HeatLevel");
        if (tag.contains("ControllerPos")) {
            controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        }
        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        outputTank.readFromNBT(registries,  tag.getCompound("OutputTank"));
        inputInv.deserializeNBT(registries, tag.getCompound("InputInv"));
        if (tag.contains("DisplayedRecipe")) {
            displayedRecipeId = ResourceLocation.tryParse(tag.getString("DisplayedRecipe"));
        } else {
            displayedRecipeId = null;
        }

        // Locally cache current recipe on client
        if (level != null && level.isClientSide)
            lastRecipe = getLocalRecipe();
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
