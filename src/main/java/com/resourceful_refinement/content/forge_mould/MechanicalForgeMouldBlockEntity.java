package com.resourceful_refinement.content.forge_mould;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipe;
import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipe;
import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipeInput;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.List;
import java.util.Optional;

public class MechanicalForgeMouldBlockEntity extends KineticBlockEntity {

    public static final int TANK_CAPACITY = 2000;
    private FilteringBehaviour filtering;
    public boolean runFalseAnimation = false;
    public float falseAnimationExtension = 0f;
    public RunningState falseAnimState = RunningState.IDLE;

    public static final float INGOT_MOULD_BREAK_CHANCE = 0.25f;
    public static final float SHAFT_MOULD_BREAK_CHANCE = 0.01f;

    public final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            if (state == RunningState.IDLE) checkRecipeValidity();
            syncData();
        }
    };
    public final ItemStackHandler inputInv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            if (state == RunningState.IDLE) checkRecipeValidity();
            syncData();
        }
    };


    public enum RunningState {
        IDLE, EXTENDING, IMPACTING, RETRACTING
    }

    public int timer;
    public RunningState state = RunningState.IDLE;
    public float extensionProgress = 0; // 0 to 1
    private net.minecraft.world.item.crafting.Recipe<MechanicalForgeMouldRecipeInput> lastRecipe;
    
    private net.minecraft.world.level.block.Block lastWorkspaceBlock;
    private ItemStack lastWorkspaceItem = ItemStack.EMPTY;

    public void clearRecipeCache() {
        lastRecipe = null;
    }

    public boolean checkRecipeValidity() {
        clearRecipeCache();
        return canProcess();
    }

    public MechanicalForgeMouldBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        filtering = new FilteringBehaviour(this, new ForgeMouldValueBox())
                .withCallback(stack -> {
                    if (level != null && !level.isClientSide) {
                        if (state == RunningState.IDLE) checkRecipeValidity();
                        syncData();
                    }
                })
                .forRecipes();
        behaviours.add(filtering);
    }

    public static class ForgeMouldValueBox extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            // Positioned on back face, near top edge
            // Use -0.01 to ensure it's slightly outside the back face
            return rotateHorizontally(state, VecHelper.voxelSpace(8, 11.5, 0.01));
        }

        @Override
        public void rotate(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, PoseStack poseStack) {
            float yRot = blockState.getValue(MechanicalForgeMouldBlock.FACING).toYRot();
            TransformStack.of(poseStack).rotateYDegrees(-yRot);
        }
    }

    // -------------------------------------------------------------------------
    // BE Behaviour
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();

        if (level == null) return;

        if (state == RunningState.IDLE) {
            if (!level.isClientSide) {
                BlockPos target = worldPosition.below(2);
                net.minecraft.world.level.block.Block currentWorkspaceBlock = level.getBlockState(target).getBlock();
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.UP);
                ItemStack currentWorkspaceItem = ItemStack.EMPTY;
                if (handler != null && handler.getSlots() > 0) {
                    currentWorkspaceItem = handler.getStackInSlot(0).copy();
                }

                boolean workspaceChanged = false;
                if (currentWorkspaceBlock != lastWorkspaceBlock) {
                    workspaceChanged = true;
                } else if (!ItemStack.matches(lastWorkspaceItem, currentWorkspaceItem)) {
                    workspaceChanged = true;
                }

                if (workspaceChanged) {
                    lastWorkspaceBlock = currentWorkspaceBlock;
                    lastWorkspaceItem = currentWorkspaceItem;
                    checkRecipeValidity();
                }
            }

            if (getSpeed() == 0) return;
            
            if (!level.isClientSide) {
                if (inputTank.isEmpty() && inputInv.getStackInSlot(0).isEmpty()) return;

                if (canProcess()) {
                    state = RunningState.EXTENDING;
                    extensionProgress = 0;
                    sendData();
                }
            }
            return;
        }

        float speed = Math.abs(getSpeed() / 128f) * 0.1f; // Adjust as needed
        if (speed <= 0) speed = 0.01f;

        switch (state) {
            case EXTENDING:
                extensionProgress += speed;
                if (extensionProgress >= 1) {
                    extensionProgress = 1;
                    if (!level.isClientSide) {
                        if (!checkRecipeValidity()) {
                            abort();
                            return;
                        }
                        state = RunningState.IMPACTING;
                        // Start timer from recipe
                        if (lastRecipe instanceof MechanicalForgeMouldRecipe recipe) {
                            timer = recipe.getProcessingDuration();
                        } else if (lastRecipe instanceof com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe coating) {
                            timer = coating.getProcessingDuration();
                        } else {
                            timer = 100;
                        }
                        spawnImpactParticles();
                        sendData();
                    }
                }
                break;
            case IMPACTING:
                if (!level.isClientSide) {
                    // Check if space below is still valid/empty
                    if (isObstructed()) {
                        abort();
                        return;
                    }

                    if (timer > 0) {
                        timer -= getProcessingSpeed();
                        if (timer <= 0) {
                            if (!checkRecipeValidity()) {
                                abort();
                                return;
                            }
                        // Check target handler occupancy before processing output
                if (isTargetOccupied()) {
                    abort();
                    return;
                }
                process();
                            state = RunningState.RETRACTING;
                            sendData();
                        }
                    }
                }
                break;
            case RETRACTING:
                extensionProgress -= speed;
                if (extensionProgress <= 0) {
                    extensionProgress = 0;
                    if (!level.isClientSide) {
                        state = RunningState.IDLE;
                        checkRecipeValidity();
                        sendData();
                    }
                }
                break;
        }
    }

    private boolean canProcess() {
        if (inputTank.isEmpty()) return false;

        BlockPos target = worldPosition.below(2);
        boolean isCastingDepot = level.getBlockState(target).getBlock() instanceof com.resourceful_refinement.content.casting_depot.CastingDepotBlock;

        // Check target handler occupancy before processing
        IItemHandler targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.UP);
        ItemStack depotItem = ItemStack.EMPTY;
        boolean hasItem = false;
        if (targetHandler != null && targetHandler.getSlots() > 0) {
            depotItem = targetHandler.getStackInSlot(0);
            hasItem = !depotItem.isEmpty();
        }

        MechanicalForgeMouldRecipeInput input = new MechanicalForgeMouldRecipeInput(
                inputInv.getStackInSlot(0),
                inputTank.getFluid(),
                isCastingDepot,
                depotItem
        );

        // REQUIRE a workspace (depot or belt) below
        if (targetHandler == null) return false;

        if (lastRecipe == null || !lastRecipe.matches(input, level)) {
            Optional<? extends RecipeHolder<?>> recipe = Optional.empty();

            // 1. Try to find a Coating recipe if the depot is occupied
            if (hasItem) {
                recipe = level.getRecipeManager()
                        .getRecipeFor(ModRecipeTypes.COATING_TYPE.get(), input, level);
            }
            
            // 2. If no coating recipe found (or depot empty), try standard Forge Mould recipe
            if (recipe.isEmpty()) {
                recipe = level.getRecipeManager()
                        .getRecipeFor(ModRecipeTypes.MECHANICAL_FORGE_MOULD_TYPE.get(), input, level);
                
                // If we found a standard recipe but the depot is occupied, it's an invalid state (can't forge on top of an item)
                if (recipe.isPresent() && hasItem) {
                    return false;
                }
            }

            if (recipe.isEmpty()) {
                return false;
            }

            net.minecraft.world.item.crafting.Recipe<?> foundRecipe = recipe.get().value();

            if (foundRecipe instanceof MechanicalForgeMouldRecipe mfmr && !mfmr.matchesFilter(filtering)) {
                return false;
            }

            lastRecipe = (net.minecraft.world.item.crafting.Recipe<MechanicalForgeMouldRecipeInput>) foundRecipe;
        }

        // Space between (worldPosition.below(1)) MUST be empty
        if (!level.getBlockState(worldPosition.below()).isAir()) return false;

        return true;
    }

    private boolean isObstructed() {
        // If anything enters the gap during impact, it's obstructed
        if (!level.getBlockState(worldPosition.below()).isAir()) return true;

        // Also ensure the workstation is still present
        BlockPos target = worldPosition.below(2);
        if (level.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.UP) == null) return true;

        // If a recipe is running and it requires casting, ensure we are still above a casting depot
        if (lastRecipe instanceof MechanicalForgeMouldRecipe recipe && recipe.isCasting()) {
            if (!(level.getBlockState(target).getBlock() instanceof com.resourceful_refinement.content.casting_depot.CastingDepotBlock)) {
                return true;
            }
        }

        return false;
    }

    private void process() {
        if (lastRecipe instanceof com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe coating) {
            inputTank.drain(coating.getFluidIngredient().amount(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            
            if (!coating.getItemIngredient().isEmpty()) {
                int requiredItems = coating.getIngredients().size();
                ItemStack itemInSlot = inputInv.getStackInSlot(0);
                boolean isIngotMould = itemInSlot.is(ModItems.SHAFT_MOULD.get());
                boolean isShaftMould = itemInSlot.is(ModItems.INGOT_MOULD.get());

                // Moulds have a low chance to be consumed; other items are always consumed
                if ((!isIngotMould && !isShaftMould)
                        || (isIngotMould && level.random.nextFloat() < INGOT_MOULD_BREAK_CHANCE)
                        || (isShaftMould && level.random.nextFloat() < SHAFT_MOULD_BREAK_CHANCE)) {
                    inputInv.extractItem(0, requiredItems, false);
                }
            }

            BlockPos target = worldPosition.below(2);
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.UP);
            if (handler != null && handler.getSlots() > 0) {
                ItemStack depotItem = handler.getStackInSlot(0);
                if (!depotItem.isEmpty()) {
                    depotItem.set(com.resourceful_refinement.registry.ModDataComponents.COATING_DATA.get(), new com.resourceful_refinement.content.coating.CoatingData(coating.getCoatingType(), coating.getCoatingType().getMaxDurability()));
                }
            }
            spawnProcessParticles();
        } else if (lastRecipe instanceof MechanicalForgeMouldRecipe recipe) {
            // Consume inputs
            inputTank.drain(recipe.getFluidIngredients().get(0).amount(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            
            int requiredItems = recipe.getIngredients().size();
            if (requiredItems > 0) {
                ItemStack itemInSlot = inputInv.getStackInSlot(0);
                boolean isIngotMould = itemInSlot.is(ModItems.SHAFT_MOULD.get());
                boolean isShaftMould = itemInSlot.is(ModItems.INGOT_MOULD.get());

                // Moulds have a low chance to be consumed; other items are always consumed
                if ((!isIngotMould && !isShaftMould)
                        || (isIngotMould && level.random.nextFloat() < INGOT_MOULD_BREAK_CHANCE)
                        || (isShaftMould && level.random.nextFloat() < SHAFT_MOULD_BREAK_CHANCE)) {
                    inputInv.extractItem(0, requiredItems, false);
                }
            }

            // Spawn result
            ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
            BlockPos target = worldPosition.below(2);
            
            // Try to put on belt/depot
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.UP);
            if (handler != null) {
                // Actually insert the item (we already checked for occupancy in isTargetOccupied)
                net.neoforged.neoforge.items.ItemHandlerHelper.insertItem(handler, result, false);
            }

            // Spawn steam particles
            spawnProcessParticles();
        }
    }

    private void abort() {
        state = RunningState.RETRACTING;
        timer = 0;
        spawnAbortParticles();
        sendData();
    }

    private void spawnImpactParticles() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                worldPosition.getX() + 0.5, worldPosition.getY() - 1, worldPosition.getZ() + 0.5, 
                6, 0.2, 0.1, 0.2, 0.05);
        }
    }

    private void spawnProcessParticles() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    worldPosition.getX() + 0.5, worldPosition.getY() - 1, worldPosition.getZ() + 0.5,
                    6, 0.2, 0.1, 0.2, 0.035);
        }
    }

    private void spawnAbortParticles() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                worldPosition.getX() + 0.5, worldPosition.getY() - 0.5, worldPosition.getZ() + 0.5, 
                10, 0.2, 0.1, 0.2, 0.02);
        }
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Mechanical Forge Mould"));

        float speed = Math.abs(getSpeed());
        tooltip.add(Component.literal("     §b" + (int)(speed * ModStressValues.SIEVE_STRESS) + "su §8at current speed"));

        // Show contents
        ItemStack invItem = inputInv.getStackInSlot(0);
        if (!invItem.isEmpty())
        {
            tooltip.add(Component.literal("§8     -> §7" + invItem.getCount() + " x " + invItem.getHoverName().getString()));
        }

        FluidStack tankAFluid = inputTank.getFluid();
        if (tankAFluid.isEmpty())
            tooltip.add(Component.literal("§9Tank A: §8empty"));
        else
            tooltip.add(Component.literal("§9Tank A: §7" + tankAFluid.getAmount() + "mb " + tankAFluid.getHoverName().getString()));

        return true;
    }

    /**
     * Returns true if the target handler (block below) already contains items.
     */
    private boolean isTargetOccupied() {
        BlockPos target = worldPosition.below(2);
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.UP);
        if (handler == null) return false;
        
        boolean hasItem = false;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                hasItem = true;
                break;
            }
        }
        
        if (lastRecipe instanceof com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe) {
            return !hasItem; // If coating, it MUST have an item. If it doesn't, it's invalid (abort).
        }
        return hasItem; // Standard recipe requires NO item.
    }

    // -------------------------------------------------------------------------
    // NBT management
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putInt("Timer", timer);
        tag.putString("State", state.name());
        tag.putFloat("Extension", extensionProgress);
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("InputInv", inputInv.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        timer = tag.getInt("Timer");
        if (tag.contains("State")) state = RunningState.valueOf(tag.getString("State"));
        extensionProgress = tag.getFloat("Extension");
        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        inputInv.deserializeNBT(registries, tag.getCompound("InputInv"));
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

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -2, 0);
    }

    // -------------------------------------------------------------------------
    // Ponder Utilities
    // -------------------------------------------------------------------------

    public void TriggerFalseAnimation()
    {
        falseAnimState = RunningState.IDLE;
        runFalseAnimation = true;
        falseAnimationExtension = 0f;
        sendData();
    }

    public boolean IsRunningFalseAnimation() {return runFalseAnimation;}
}
