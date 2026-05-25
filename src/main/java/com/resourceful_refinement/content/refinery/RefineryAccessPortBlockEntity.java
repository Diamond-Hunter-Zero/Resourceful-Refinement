package com.resourceful_refinement.content.refinery;

import com.resourceful_refinement.content.refinery.recipe.FluidRefineryRecipe;
import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.particle.FluidParticleData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller block entity for the Fluid Refinery multiblock.
 * Stores all multiblock state: assembly flag, structure layout,
 * fluid tanks, item inventory, fuel/heat, and crafting progress.
 */
public class RefineryAccessPortBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    // --- Constants ---
    public static final int MAX_HEIGHT = 8;
    public static final int DEFAULT_TANK_CAPACITY = 1000;
    public static final int INPUT_SLOTS = 2;
    public static final int FUEL_SLOTS = 1;

    // --- Assembly state ---
    private boolean assembled = false;
    private int structureHeight = 0;
    public int renderFalseRefinery = 0;
    /** Positions of all proxy blocks (world-space), stored for disassembly. */
    private final List<BlockPos> proxyPositions = new ArrayList<>();
    /** Parallel list: original BlockStates that were replaced by proxies. */
    private final List<BlockState> originalStates = new ArrayList<>();

    // --- Storage ---
    public final FluidTank inputTankA = new FluidTank(DEFAULT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };
    public final FluidTank inputTankB = new FluidTank(DEFAULT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };
    public final FluidTank outputTank = new FluidTank(DEFAULT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };
    public final ItemStackHandler itemInput = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };
    public final ItemStackHandler fuelSlot  = new ItemStackHandler(FUEL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    // --- Crafting / heat ---
    private int craftingProgress = 0;
    private int burnTimeRemaining = 0;
    private double refineryRotationAngle = 0;
    private int speed_requirement = 32;
    // Heat levels: 0 = NONE, 1 = HEATED, 2 = SUPERHEATED
    private int heatLevel = 0;
    private int requiredHeat = 0;

    private FilteringBehaviour filtering;
    private int activeRecipeDuration = 0; // Synced for client visuals

    private final IItemHandler fuelHandler = new ItemStackHandler(1) {
        @Override
        public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            int burnTime = stack.getBurnTime(RecipeType.SMELTING);
            if (burnTime <= 0) return stack;

            boolean isBlazeCake = false;
            try {
                isBlazeCake = stack.is(com.simibubi.create.AllItems.BLAZE_CAKE.get());
            } catch (Exception ignored) {}

            // Conditions: not already superheated (unless cake), and not already has 500s fuel
            if (heatLevel == 2 && !isBlazeCake) return stack;
            if (burnTimeRemaining >= 10000 && !isBlazeCake) return stack;

            if (!simulate) {
                ItemStack consumed = stack.copy();
                consumed.setCount(1);
                addFuel(consumed, null);
            }
            
            ItemStack result = stack.copy();
            result.shrink(1);
            return result;
        }
    };

    @Nullable
    private RecipeHolder<FluidRefineryRecipe> currentRecipe;

    public RefineryAccessPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new FilteringBehaviour(this, new RefineryFilterValueBox())
                .withCallback(stack -> {
                    if (level != null && !level.isClientSide) {
                        currentRecipe = null; // Reset recipe matching when filter changes
                        requiredHeat = 0;
                        syncData();
                    }
                })
                .forRecipes();
        behaviours.add(filtering);
    }

    public static class RefineryFilterValueBox extends CenteredSideValueBoxTransform {
        public RefineryFilterValueBox() {
            super((state, dir) -> true);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return true; // We check this in shouldRender
        }

        @Override
        public boolean shouldRender(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
            BlockEntity be = level.getBlockEntity(pos);
            int dx, dy, dz;
            BlockPos controllerPos;

            if (be instanceof RefineryProxyBlockEntity proxy) {
                controllerPos = proxy.getControllerPos();
                dx = proxy.getDx();
                dy = proxy.getDy();
                dz = proxy.getDz();
            } else if (be instanceof RefineryKineticProxyBlockEntity kProxy) {
                controllerPos = kProxy.getControllerPos();
                dx = kProxy.getDx();
                dy = kProxy.getDy();
                dz = kProxy.getDz();
            } else {
                return false;
            }

            // Only allow on the middle-front block (dx=0, dy=1, dz=0)
            if (!(dx == 0 && dy == 1 && dz == 0)) return false;

            Direction facing = Direction.SOUTH;
            BlockState controllerState = level.getBlockState(controllerPos);
            if (controllerState != null && controllerState.hasProperty(RefineryAccessPortBlock.FACING)) {
                facing = controllerState.getValue(RefineryAccessPortBlock.FACING);
            }

            return getSide() == facing;
        }

        @Override
        public Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
            Direction facing = Direction.SOUTH;
            BlockPos controllerPos = pos;

            if (level.getBlockEntity(pos) instanceof RefineryProxyBlockEntity proxy) {
                controllerPos = proxy.getControllerPos();
            } else if (level.getBlockEntity(pos) instanceof RefineryKineticProxyBlockEntity kProxy) {
                controllerPos = kProxy.getControllerPos();
            }

            BlockState controllerState = level.getBlockState(controllerPos);
            if (controllerState != null && controllerState.hasProperty(RefineryAccessPortBlock.FACING)) {
                facing = controllerState.getValue(RefineryAccessPortBlock.FACING);
            }

            this.fromSide(facing);
            Vec3 offset = super.getLocalOffset(level, pos, state);

            // FilteringBehaviour.testHit() calls this with the controller's position.
            // Since the slot is visually on the block above (dy=1), we must add 1.0 to the Y offset
            // when checked against the controller's local space.
            if (pos.equals(controllerPos)) {
                offset = offset.add(0, 1, 0);
            }

            offset = offset.add(0, -0.3, 0);
            return offset;
        }

    }

    // -------------------------------------------------------------------------
    // Capability Delegation
    // -------------------------------------------------------------------------

    public @Nullable IFluidHandler getFluidHandlerForProxy(int dx, int dy, int dz, Direction side) {
        if (!assembled) return null;
        // User wants only the top face of the back top corners to be accessible.
        if (side == Direction.UP && dy == structureHeight - 1 && dz == 2) {
            if (dx == -1) return inputTankA;
            if (dx == 1)  return inputTankB;
        }
        return null;
    }

    public @Nullable IItemHandler getItemHandlerForProxy(int dx, int dy, int dz, Direction side) {
        if (!assembled) return null;

        // Side faces of the 4 corner blocks on the bottom layer accept fuel
        if (dy == 0 && side != Direction.UP && side != Direction.DOWN) {
            if ((dx == -1 || dx == 1) && (dz == 0 || dz == 2)) {
                return fuelHandler;
            }
        }

        // Item Vaults (top front corners)
        if (dy == structureHeight - 1 && dz == 0) {
            if (dx == -1 || dx == 1) {
                // Not top or front-facing (facing is the direction the Access Port faces out)
                Direction facing = getBlockState().getValue(RefineryAccessPortBlock.FACING);
                if (side != Direction.UP && side != facing) {
                    return itemInput;
                }
            }
        }
        return null;
    }

    // ... (rest of class)

    // -------------------------------------------------------------------------
    // Server tick (called from RefineryAccessPortBlock.getTicker)
    // -------------------------------------------------------------------------
    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) return;
        if (!assembled) return;

        tickFuel();
        tickCrafting();

        if (craftingProgress > 0) {
            spawnCraftingParticles();
        }
    }

    public static void serverTick(net.minecraft.world.level.Level level,
                                   BlockPos pos,
                                   BlockState state,
                                   RefineryAccessPortBlockEntity be) {
        be.tick();
    }

    public boolean addFuel(ItemStack stack, @Nullable Player player) {
        if (!assembled) return false;

        int burnTime = stack.getBurnTime(RecipeType.SMELTING);
        if (burnTime <= 0) return false;

        boolean isBlazeCake = false;
        try {
            isBlazeCake = stack.is(com.simibubi.create.AllItems.BLAZE_CAKE.get());
        } catch (Exception ignored) {}

        if (heatLevel == 2 && !isBlazeCake) return false;
        if (burnTimeRemaining >= 10000 && !isBlazeCake) return false;

        if (player != null && !player.isCreative()) {
            stack.shrink(1);
        }

        if (isBlazeCake) {
            heatLevel = 2;
            burnTimeRemaining = Math.max(burnTimeRemaining, burnTime);
        } else {
            if (heatLevel < 2) {
                heatLevel = 1;
                burnTimeRemaining = Math.min(burnTimeRemaining + burnTime, 10000);
            }
        }

        updateProxyHeatLevels();
        syncData();
        return true;
    }

    private void tickFuel() {
        if (burnTimeRemaining > 0) {
            burnTimeRemaining--;
            if (burnTimeRemaining == 0) {
                heatLevel = 0; // cool down
                updateProxyHeatLevels();
            }
            syncData();
            return;
        }
        // Try to consume next fuel item
        var stack = fuelSlot.getStackInSlot(0);
        if (stack.isEmpty()) return;

        int burnTime = stack.getBurnTime(RecipeType.SMELTING);
        if (burnTime <= 0) return;

        // Check for blaze cake (superheated)
        boolean isSuperheated = false;
        try {
            // Attempt to reference Create's BlazeCake item; fallback gracefully
            isSuperheated = stack.is(com.simibubi.create.AllItems.BLAZE_CAKE.get());
        } catch (Exception ignored) {}

        fuelSlot.extractItem(0, 1, false);
        burnTimeRemaining = burnTime;
        heatLevel = isSuperheated ? 2 : 1;
        updateProxyHeatLevels();
        syncData();
    }

    private void tickCrafting() {
        if (level == null || level.isClientSide) return;

        // Requires rotational speed to process
        float speed = 0;
        for (BlockPos proxyPos : proxyPositions) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(proxyPos);
            if (be instanceof RefineryKineticProxyBlockEntity kineticProxy) {
                speed = kineticProxy.getSpeed();
                if (speed != 0) {break;};
            }
        }
        if (Math.abs(speed) < getSpeedRequirement()) return;
        refineryRotationAngle = (refineryRotationAngle - speed * 0.3f)%360;
        syncData();

        // Get item and fluid inputs
        List<net.minecraft.world.item.ItemStack> items = new ArrayList<>();
        for (int i = 0; i < itemInput.getSlots(); i++) {
            net.minecraft.world.item.ItemStack stack = itemInput.getStackInSlot(i);
            if (!stack.isEmpty()) items.add(stack);
        }

        List<FluidStack> fluids = new ArrayList<>();
        if (!inputTankA.isEmpty()) fluids.add(inputTankA.getFluid());
        if (!inputTankB.isEmpty()) fluids.add(inputTankB.getFluid());

        com.resourceful_refinement.content.refinery.recipe.FluidRefineryRecipeInput recipeInput = 
            new com.resourceful_refinement.content.refinery.recipe.FluidRefineryRecipeInput(items, fluids);

        if (currentRecipe != null) {
            if (!currentRecipe.value().matches(recipeInput, level)) {
                currentRecipe = null;
                activeRecipeDuration = 0;
                craftingProgress = 0;
                requiredHeat = 0;
                syncData();
            }
        }

        if (currentRecipe == null) {
            java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<com.resourceful_refinement.content.refinery.recipe.FluidRefineryRecipe>> match = 
                level.getRecipeManager().getRecipeFor(com.resourceful_refinement.registry.ModRecipeTypes.FLUID_REFINERY_TYPE.get(), recipeInput, level);
            
            if (match.isPresent()) {
                if (match.get().value().matchesFilter(filtering)) {
                    currentRecipe = match.get();
                    craftingProgress = 0;
                    activeRecipeDuration = (int)(currentRecipe.value().getProcessingDuration() / StructureProcessingSpeedModifier());
                    if (activeRecipeDuration <= 0) activeRecipeDuration = 100;
                    requiredHeat = currentRecipe.value().getRequiredHeat().ordinal();
                    syncData();
                }
            }
            
            if (currentRecipe == null) {
                if (craftingProgress > 0) {
                    craftingProgress = 0;
                    syncData();
                }
                return;
            }
        }

        boolean heatSatisfied = isHeatSatisfied();
        if (!heatSatisfied) return;

        java.util.List<FluidStack> results = currentRecipe.value().getFluidResults();
        if (results.isEmpty()) return;
        FluidStack resultFluid = results.get(0);
        int fillAmount = outputTank.fill(resultFluid, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
        if (fillAmount < resultFluid.getAmount()) return;

        craftingProgress++;
        
        int duration = (int)(currentRecipe.value().getProcessingDuration() / StructureProcessingSpeedModifier());
        if (duration <= 0) duration = 100;

        if (craftingProgress >= duration) {
            outputTank.fill(resultFluid.copy(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

            for (int i = 0; i < currentRecipe.value().getIngredients().size(); i++) {
                net.minecraft.world.item.crafting.Ingredient ingredient = currentRecipe.value().getIngredients().get(i);
                for (int slot = 0; slot < itemInput.getSlots(); slot++) {
                    net.minecraft.world.item.ItemStack slotStack = itemInput.getStackInSlot(slot);
                    if (ingredient.test(slotStack)) {
                        itemInput.extractItem(slot, 1, false);
                        break;
                    }
                }
            }

            for (int i = 0; i < currentRecipe.value().getFluidIngredients().size(); i++) {
                var fluidIngredient = currentRecipe.value().getFluidIngredients().get(i);
                int amountToDrain = fluidIngredient.amount();
                
                if (fluidIngredient.test(inputTankA.getFluid()) && inputTankA.getFluidAmount() >= amountToDrain) {
                    inputTankA.drain(amountToDrain, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                } else if (fluidIngredient.test(inputTankB.getFluid()) && inputTankB.getFluidAmount() >= amountToDrain) {
                    inputTankB.drain(amountToDrain, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                }
            }

            craftingProgress = 0;
            currentRecipe = null;
            activeRecipeDuration = 0;
            requiredHeat = 0;
            syncData();
        }
    }

    private boolean isHeatSatisfied() {
        HeatCondition requiredHeat = currentRecipe.value().getRequiredHeat();
        boolean heatSatisfied = false;
        if (requiredHeat == HeatCondition.NONE) heatSatisfied = true;
        else if (requiredHeat == HeatCondition.HEATED && heatLevel >= 1) heatSatisfied = true;
        else if (requiredHeat == HeatCondition.SUPERHEATED && heatLevel >= 2) heatSatisfied = true;
        return heatSatisfied;
    }

    // -------------------------------------------------------------------------
    // Assembly helpers
    // -------------------------------------------------------------------------
    public boolean isAssembled() { return assembled; }
    public int getStructureHeight() { return structureHeight; }

    public void onAssembled(int height, List<BlockPos> proxies, List<BlockState> originals) {
        this.assembled = true;
        this.structureHeight = height;
        this.proxyPositions.clear();
        this.proxyPositions.addAll(proxies);
        this.originalStates.clear();
        this.originalStates.addAll(originals);

        // Dynamically set tank capacity
        int tankCapacity = getDynamicTankCapacity();
        this.inputTankA.setCapacity(tankCapacity);
        this.inputTankB.setCapacity(tankCapacity);
        this.outputTank.setCapacity(tankCapacity);

        // Sync to client
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void onDisassembled() {
        this.assembled = false;
        this.structureHeight = 0;
        this.proxyPositions.clear();
        this.originalStates.clear();

        // Reset tank capacity
        int tankCapacity = getDynamicTankCapacity();
        this.inputTankA.setCapacity(DEFAULT_TANK_CAPACITY);
        this.inputTankB.setCapacity(DEFAULT_TANK_CAPACITY);
        this.outputTank.setCapacity(DEFAULT_TANK_CAPACITY);

        // Sync to client
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public List<BlockPos> getProxyPositions() { return proxyPositions; }

    private int getSpeedRequirement()
    {
        return (structureHeight - 2) * 32;
    }

    private int getDynamicTankCapacity()
    {
        return (structureHeight - 2) * 1000;
    }

    private float StructureProcessingSpeedModifier()
    {
        return 0.75f + (structureHeight - 3) * 0.25f;
    }

    // -------------------------------------------------------------------------
    // Inventory Management
    // -------------------------------------------------------------------------
    public void tryClearInventory(Player player) {
        if (level == null || level.isClientSide) return;

        boolean extracted = false;
        // Clear input items
        for (int i = 0; i < itemInput.getSlots(); i++) {
            ItemStack stack = itemInput.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack taken = itemInput.extractItem(i, stack.getCount(), false);
                player.getInventory().placeItemBackInInventory(taken);
                extracted = true;
            }
        }
        // Clear fuel slot
        for (int i = 0; i < fuelSlot.getSlots(); i++) {
            ItemStack stack = fuelSlot.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack taken = fuelSlot.extractItem(i, stack.getCount(), false);
                player.getInventory().placeItemBackInInventory(taken);
                extracted = true;
            }
        }

        if (extracted) {
            syncData();
        }
    }

    // -------------------------------------------------------------------------
    // Status chat output (Deprecated - replaced by goggle tooltip)
    // -------------------------------------------------------------------------
    public void debugPrintStatus(Player player) {
        player.displayClientMessage(Component.literal("§6[Refinery] §rAssembled — Height: " + structureHeight), false);
        player.displayClientMessage(Component.literal(
                "§6  Tank A: §r" + formatFluid(inputTankA.getFluid()) +
                " §6| Tank B: §r" + formatFluid(inputTankB.getFluid())), false);

        StringBuilder itemsStr = new StringBuilder("§6  Items: §r");
        boolean first = true;
        for (int i = 0; i < itemInput.getSlots(); i++) {
            ItemStack stack = itemInput.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (!first) itemsStr.append(", ");
                itemsStr.append(stack.getCount()).append("x ").append(stack.getHoverName().getString());
                first = false;
            }
        }
        if (first) itemsStr.append("Empty");
        player.displayClientMessage(Component.literal(itemsStr.toString()), false);

        player.displayClientMessage(Component.literal(
                "§6  Output: §r" + formatFluid(outputTank.getFluid())), false);
        player.displayClientMessage(Component.literal(
                "§6  Heat: §r" + heatLevelName() +
                " §6| Burn: §r" + burnTimeRemaining + "t"), false);
        
        if (!filtering.getFilter().isEmpty()) {
             player.displayClientMessage(Component.literal("§6  Filter: §r" + filtering.getFilter().getHoverName().getString()), false);
        }
    }

    private String formatFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return "Empty";
        return fluid.getAmount() + "mB " + fluid.getFluid().getFluidType().getDescription().getString();
    }

    private String heatLevelName() {
        return switch (heatLevel) {
            case 1 -> "§eHEATED";
            case 2 -> "§cSUPERHEATED";
            default -> "§7None";
        };
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        if (!assembled) {
            tooltip.add(Component.literal("     Fluid Refinery Outlet:"));
            tooltip.add(Component.literal("§7Right-click to assemble when"));
            tooltip.add(Component.literal("§7valid structure is ready."));
            return true;
        }
        tooltip.add(Component.literal("     Fluid Refinery Outlet:"));

        // Speed
        float speed = 0;
        for (BlockPos proxyPos : proxyPositions) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(proxyPos);
            if (be instanceof RefineryKineticProxyBlockEntity kineticProxy) {
                speed = Math.abs(kineticProxy.getSpeed());
                if (speed > 0) break;
            }
        }

        if (speed < getSpeedRequirement())
            tooltip.add(Component.literal("     §cMinimum RPM: §r" + getSpeedRequirement()));
        else
            tooltip.add(Component.literal("     §b" + (int)(speed * ModStressValues.REFINERY_STRESS) + "su §8at current speed"));
        tooltip.add(Component.literal(""));

        // Heating
        if (burnTimeRemaining > 0)
        {
            if (heatLevel == 1)
                tooltip.add(Component.literal("§7HEATED for " + (int)(burnTimeRemaining/20f) + "s"));
            else if (heatLevel == 2)
                tooltip.add(Component.literal("§7SUPERHEATED for " + (int)(burnTimeRemaining/20f) + "s"));
        }

        // Item inputs
        ItemStack itemA = itemInput.getStackInSlot(0);
        ItemStack itemB = itemInput.getStackInSlot(1);

        if (!itemB.isEmpty() && !itemA.isEmpty() && ItemStack.isSameItem(itemA, itemB))
        {
            tooltip.add(Component.literal("§8-> §7" + itemA.getItem().getName(itemA).getString() + " x " + (itemA.getCount() + itemB.getCount())));
        }
        else
        {
            if (!itemA.isEmpty())
                tooltip.add(Component.literal("§8-> §7" + itemA.getHoverName().getString() + " x " + itemA.getCount()));

            if (!itemB.isEmpty())
                tooltip.add(Component.literal("§8-> §7" + itemB.getHoverName().getString() + " x " + itemB.getCount()));
        }


        // Tanks
        FluidStack tankAFluid = inputTankA.getFluid();
        if (tankAFluid.isEmpty())
            tooltip.add(Component.literal("§9Tank A: §8empty"));
        else
            tooltip.add(Component.literal("§9Tank A: §7" + tankAFluid.getAmount() + "mb " + tankAFluid.getHoverName().getString()));

        FluidStack tankBFluid = inputTankB.getFluid();
        if (tankBFluid.isEmpty())
            tooltip.add(Component.literal("§9Tank B: §8empty"));
        else
            tooltip.add(Component.literal("§9Tank B: §7" + tankBFluid.getAmount() + "mb " + tankBFluid.getHoverName().getString()));

        FluidStack tankOutputFluid = outputTank.getFluid();
        if (tankOutputFluid.isEmpty())
            tooltip.add(Component.literal("§6Output: §8empty"));
        else
            tooltip.add(Component.literal("§6Output: §7" + tankOutputFluid.getAmount() + "mb " + tankOutputFluid.getHoverName().getString()));

        // Progress
        if (activeRecipeDuration > 0) {

            if (!tankOutputFluid.isEmpty() && tankOutputFluid.getAmount() >= outputTank.getCapacity())
                tooltip.add(Component.literal("§cOutput Tank Full!"));
            else if (heatLevel < requiredHeat)
            {
                if (requiredHeat == 1)
                    tooltip.add(Component.literal("§cRefinery must be HEATED"));
                else if (requiredHeat == 2)
                    tooltip.add(Component.literal("§cRefinery must be SUPERHEATED"));
            }
            else
            {
                float progress = Math.min(1.0f, craftingProgress / (float) activeRecipeDuration);
                int filledBlocks = (int) (progress * 8);
                StringBuilder bar = new StringBuilder("Progress: ");
                for (int i = 0; i < 8; i++) {
                    if (i < filledBlocks) bar.append("█");
                    else bar.append(" _");
                }
                tooltip.add(Component.literal("§c" + bar.toString()));
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
        tag.putBoolean("assembled", assembled);
        tag.putInt("structureHeight", structureHeight);
        tag.putInt("heatLevel", heatLevel);
        tag.putInt("requiredHeat", requiredHeat);
        tag.putInt("burnTimeRemaining", burnTimeRemaining);
        tag.putInt("craftingProgress", craftingProgress);
        tag.putInt("activeRecipeDuration", activeRecipeDuration);
        tag.putDouble("refineryRotationAngle", refineryRotationAngle);
        tag.putInt("renderFalseRefinery", renderFalseRefinery);

        // Proxy positions
        ListTag proxyList = new ListTag();
        for (int i = 0; i < proxyPositions.size(); i++) {
            CompoundTag entry = new CompoundTag();
            entry.put("pos", NbtUtils.writeBlockPos(proxyPositions.get(i)));
            entry.put("state", NbtUtils.writeBlockState(originalStates.get(i)));
            proxyList.add(entry);
        }
        tag.put("proxyData", proxyList);

        // Tanks
        tag.put("inputTankA", inputTankA.writeToNBT(registries, new CompoundTag()));
        tag.put("inputTankB", inputTankB.writeToNBT(registries, new CompoundTag()));
        tag.put("outputTank",  outputTank.writeToNBT(registries,  new CompoundTag()));

        // Item handlers
        tag.put("itemInput", itemInput.serializeNBT(registries));
        tag.put("fuelSlot",  fuelSlot.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        assembled = tag.getBoolean("assembled");
        structureHeight = tag.getInt("structureHeight");
        heatLevel = tag.getInt("heatLevel");
        requiredHeat = tag.getInt("requiredHeat");
        burnTimeRemaining = tag.getInt("burnTimeRemaining");
        craftingProgress = tag.getInt("craftingProgress");
        activeRecipeDuration = tag.getInt("activeRecipeDuration");
        refineryRotationAngle = tag.getDouble("refineryRotationAngle");
        renderFalseRefinery = tag.getInt("renderFalseRefinery");

        // Dynamically set tank capacities based on the loaded structure height if assembled
        int dynamicCapacity = assembled ? getDynamicTankCapacity() : DEFAULT_TANK_CAPACITY;
        inputTankA.setCapacity(dynamicCapacity);
        inputTankB.setCapacity(dynamicCapacity);
        outputTank.setCapacity(dynamicCapacity);

        proxyPositions.clear();
        originalStates.clear();
        ListTag proxyList = tag.getList("proxyData", Tag.TAG_COMPOUND);
        for (int i = 0; i < proxyList.size(); i++) {
            CompoundTag entry = proxyList.getCompound(i);
            proxyPositions.add(NbtUtils.readBlockPos(entry, "pos").orElse(BlockPos.ZERO));
            originalStates.add(NbtUtils.readBlockState(registries.lookup(net.minecraft.core.registries.Registries.BLOCK).orElseThrow(), entry.getCompound("state")));
        }

        inputTankA.readFromNBT(registries, tag.getCompound("inputTankA"));
        inputTankB.readFromNBT(registries, tag.getCompound("inputTankB"));
        outputTank.readFromNBT(registries,  tag.getCompound("outputTank"));

        itemInput.deserializeNBT(registries, tag.getCompound("itemInput"));
        fuelSlot.deserializeNBT(registries,  tag.getCompound("fuelSlot"));
    }

    // -------------------------------------------------------------------------
    // Network sync (for client-side rendering)
    // -------------------------------------------------------------------------
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        write(tag, registries, true);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getHeatLevel() { return heatLevel; }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public List<BlockState> getOriginalStates() { return originalStates; }

    private void updateProxyHeatLevels() {
        if (level == null || level.isClientSide) return;
        Direction facing = getBlockState().getValue(RefineryAccessPortBlock.FACING);
        // The 4 bottom corners
        int[][] corners = { {-1, 0}, {1, 0}, {-1, 2}, {1, 2} };
        for (int[] off : corners) {
            BlockPos pos = RefineryStructureHelper.toWorldPos(worldPosition, facing, off[0], 0, off[1]);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof RefineryProxyBlock && state.hasProperty(RefineryProxyBlock.HEAT_LEVEL)) {
                if (state.getValue(RefineryProxyBlock.HEAT_LEVEL) != heatLevel) {
                    level.setBlockAndUpdate(pos, state.setValue(RefineryProxyBlock.HEAT_LEVEL, heatLevel));
                }
            }
        }
    }

    public double getRotationAngle() {return refineryRotationAngle; }

    // -------------------------------------------------------------------------
    // Visuals / Particles
    // -------------------------------------------------------------------------

    private void spawnCraftingParticles() {
        if (level == null || !(level instanceof ServerLevel serverLevel) || !assembled) return;
        
        FluidStack topFluid = getTopFluid();
        if (topFluid.isEmpty()) return;

        Direction facing = getBlockState().getValue(RefineryAccessPortBlock.FACING);
        BlockPos structureCenterPos = RefineryStructureHelper.toWorldPos(worldPosition, facing, 0, 0, 1);
        
        float fluidLevel = getFluidLevelOffset();
        
        // Increase particle count when heated
        int particleCount = (heatLevel > 0) ? 3 : 1;
        
        for (int i = 0; i < particleCount; i++) {
            double x = structureCenterPos.getX() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2.7;
            double y = worldPosition.getY() + fluidLevel + 0.1; // Add slight offset to be above surface
            double z = structureCenterPos.getZ() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2.7;

            // Base fluid splash particle
            serverLevel.sendParticles(new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), topFluid),
                    x, y, z,
                    0,
                    (serverLevel.random.nextDouble() - 0.5) * 0.05,
                    0.05 + serverLevel.random.nextDouble() * 0.05,
                    (serverLevel.random.nextDouble() - 0.5) * 0.05,
                    1);
            
            // Bubbles (more frequent when heated)
            float bubbleChance = (heatLevel > 0) ? 0.4f : 0.15f;
            if (serverLevel.random.nextFloat() < bubbleChance) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE,
                        x, y, z,
                        0,
                        (serverLevel.random.nextDouble() - 0.5) * 0.02,
                        0.05 + serverLevel.random.nextDouble() * 0.1,
                        (serverLevel.random.nextDouble() - 0.5) * 0.02,
                        1);
            }
            
            // Optional: Steam particles for heated refinery
            /*if (heatLevel > 0 && serverLevel.random.nextFloat() < 0.1f) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        x, y, z,
                        0,
                        0.0, 0.1, 0.0,
                        0.25);
            }*/
        }
    }

    private float getFluidLevelOffset() {
        float totalHeight = structureHeight - 1.0625f;
        float tankHeightLimit = totalHeight / 3f;
        float currentY = 0.9375f;
        
        if (!outputTank.isEmpty()) currentY += (float) outputTank.getFluidAmount() / outputTank.getCapacity() * tankHeightLimit;
        if (!inputTankA.isEmpty()) currentY += (float) inputTankA.getFluidAmount() / inputTankA.getCapacity() * tankHeightLimit;
        if (!inputTankB.isEmpty()) currentY += (float) inputTankB.getFluidAmount() / inputTankB.getCapacity() * tankHeightLimit;
        
        return currentY;
    }

    private FluidStack getTopFluid() {
        if (!inputTankB.isEmpty()) return inputTankB.getFluid();
        if (!inputTankA.isEmpty()) return inputTankA.getFluid();
        if (!outputTank.isEmpty()) return outputTank.getFluid();
        return FluidStack.EMPTY;
    }

    public void setFalseRenderingLevel(int renderLevel)
    {
        renderFalseRefinery = renderLevel;
        sendData();
    }
}
