package com.resourceful_refinement.content.combustion_chamber;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock.FACING;

public class CombustionChamberBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation, IRotate {

    public static final int TANK_CAPACITY = 1000;
    public static final int MAX_CHAIN_LENGTH = 16;

    public static TagKey<Fluid> PASSIVE_FUEL_FLUID_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "passive_fuel"));
    public static TagKey<Fluid> HEATED_FUEL_FLUID_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "heated_fuel"));
    public static TagKey<Fluid> SUPERHEATED_FUEL_FLUID_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "superheated_fuel"));

    private boolean hasRunInitialChecks = false;
    private int currentFuelState = -1;
    private int localColdestHeatSource = 0;
    private int coldestHeatSource = 0;
    private boolean localRedstonePowered = false;
    private boolean chainRedstonePowered = false;
    private boolean isUnderPerforming = false;
    private BlockPos controllerPos;
    private int chainIndex = 0;
    private int chainSize = 1;
    protected ScrollOptionBehaviour<RotationDirection> movementDirection;

    private final IFluidHandler chainInputHandler = new ChainInputFluidHandler();

    public final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
            if (level != null && !level.isClientSide) {
                handleFuelStateChanged();
            }
        }
    };


    // -------------------------------------------------------------------------
    //  Block Entity Definition
    // -------------------------------------------------------------------------
    public CombustionChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        controllerPos = pos;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        movementDirection = new ScrollOptionBehaviour<>(RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new RotationDirectionSlot());
        movementDirection.onlyActiveWhen(this::isOutputEngine);
        movementDirection.withCallback($ -> onDirectionChanged());
        behaviours.add(movementDirection);
    }


    // -------------------------------------------------------------------------
    //  Server Logic
    // -------------------------------------------------------------------------
    @Override
    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!hasRunInitialChecks) {
            updateConnectivity();
            updateHeatAdjacency();
            updateRedstonePower();
            hasRunInitialChecks = true;
        }

        if (isController()) {
            int newFuelState = getChainFuelState();
            if (newFuelState != currentFuelState || getGeneratedSpeed() != getSpeed()) {
                updateSpeedAndStressOutput(newFuelState);
            }
        }

        consumeFuel();
    }

    private int getCombustionFuelState(FluidTank tank) {
        if (tank.isEmpty()) {
            return 0;
        } else if (tank.getFluid().is(PASSIVE_FUEL_FLUID_TAG)) {
            return 1;
        } else if (tank.getFluid().is(HEATED_FUEL_FLUID_TAG)) {
            return 2;
        } else if (tank.getFluid().is(SUPERHEATED_FUEL_FLUID_TAG)) {
            return 3;
        }

        return 0;
    }

    private int getChainFuelState() {
        CombustionChamberBlockEntity controller = getController();
        if (controller == null) {
            return getCombustionFuelState(inputTank);
        }

        List<CombustionChamberBlockEntity> members = controller.getChainMembers();
        if (members.isEmpty()) {
            return getCombustionFuelState(controller.inputTank);
        }

        int highestFuelState = 0;
        for (CombustionChamberBlockEntity member : members) {
            int memberFuelState = getCombustionFuelState(member.inputTank);
            if (memberFuelState > highestFuelState) {
                highestFuelState = memberFuelState;
            }
        }

        return highestFuelState;
    }

    private void handleFuelStateChanged() {
        CombustionChamberBlockEntity controller = getController();
        if (controller == null || level == null || level.isClientSide) {
            return;
        }

        int newFuelState = controller.getChainFuelState();
        boolean stateChanged = newFuelState != controller.currentFuelState;
        boolean speedChanged = controller.getGeneratedSpeed() != controller.getSpeed();

        if (stateChanged || speedChanged) {
            controller.updateSpeedAndStressOutput(newFuelState);
        }

        if (stateChanged || speedChanged || controller != this) {
            controller.syncChainToClients();
        }
    }

    public void updateConnectivity() {
        if (level == null || level.isClientSide) return;

        BlockPos back = findChainBack(worldPosition);
        List<BlockPos> row = collectRow(back);
        if (row.isEmpty()) return;

        for (int segmentStart = 0; segmentStart < row.size(); segmentStart += MAX_CHAIN_LENGTH) {
            int segmentEnd = Math.min(segmentStart + MAX_CHAIN_LENGTH, row.size());
            List<BlockPos> chain = new ArrayList<>(row.subList(segmentStart, segmentEnd));
            applyChain(chain);

            if (level.getBlockEntity(chain.get(0)) instanceof CombustionChamberBlockEntity controller) {
                controller.updateChainHeat();
                controller.updateChainRedstone();
                controller.updateChainRotation();
            }
        }
    }

    private BlockPos findChainBack(BlockPos start) {
        Direction facing = getBlockState().getValue(FACING);
        BlockPos current = start;
        while (isAlignedChamber(current.relative(facing.getOpposite()))) {
            current = current.relative(facing.getOpposite());
        }
        return current;
    }

    private List<BlockPos> collectRow(BlockPos back) {
        Direction facing = getBlockState().getValue(FACING);
        List<BlockPos> row = new ArrayList<>();
        BlockPos current = back;
        while (isAlignedChamber(current)) {
            row.add(current.immutable());
            current = current.relative(facing);
        }
        return row;
    }

    private boolean isAlignedChamber(BlockPos pos) {
        if (level == null || !(level.getBlockEntity(pos) instanceof CombustionChamberBlockEntity chamber)) {
            return false;
        }
        return chamber.getBlockState().hasProperty(FACING)
                && chamber.getBlockState().getValue(FACING) == getBlockState().getValue(FACING);
    }

    private void applyChain(List<BlockPos> chain) {
        BlockPos controller = chain.get(0);
        FluidStack controllerFluid = FluidStack.EMPTY;
        if (level.getBlockEntity(controller) instanceof CombustionChamberBlockEntity controllerBe) {
            controllerFluid = controllerBe.inputTank.getFluid().copy();
        }

        for (int i = 0; i < chain.size(); i++) {
            BlockPos currentPos = chain.get(i);
            if (!(level.getBlockEntity(currentPos) instanceof CombustionChamberBlockEntity chamber)) {
                continue;
            }

            chamber.controllerPos = controller;
            chamber.chainIndex = i;
            chamber.chainSize = chain.size();
            chamber.refreshLocalRedstonePower();

            if (i > 0 && !chamber.inputTank.isEmpty()
                    && (controllerFluid.isEmpty() || !FluidStack.isSameFluidSameComponents(chamber.inputTank.getFluid(), controllerFluid))) {
                chamber.inputTank.setFluid(FluidStack.EMPTY);
            }

            chamber.setChanged();
            chamber.sendData();
        }
    }

    public void updateHeatAdjacency() {
        if (level == null || level.isClientSide) return;

        int newColdestSource = HeatUtilities.GetColdestAdjacentHeatSource(level, worldPosition);
        if (newColdestSource != localColdestHeatSource) {
            localColdestHeatSource = newColdestSource;
        }
        updateChainHeat();
    }

    public void updateRedstonePower() {
        if (level == null || level.isClientSide) return;

        refreshLocalRedstonePower();
        updateChainRedstone();
    }

    private void refreshLocalRedstonePower() {
        if (level == null || level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered != localRedstonePowered) {
            localRedstonePowered = powered;
            setChanged();
            sendData();
        }
    }

    private void updateChainHeat() {
        CombustionChamberBlockEntity controller = getController();
        if (controller == null) return;

        int chainHeat = controller.getChainMembers().stream()
                .mapToInt(member -> member.localColdestHeatSource)
                .max()
                .orElse(controller.localColdestHeatSource);

        for (CombustionChamberBlockEntity member : controller.getChainMembers()) {
            if (member.coldestHeatSource != chainHeat) {
                member.coldestHeatSource = chainHeat;
                member.setChanged();
                member.sendData();
            }
        }

        controller.updateSpeedAndStressOutput(controller.getChainFuelState());
        controller.syncChainToClients();
    }

    private void updateChainRedstone() {
        CombustionChamberBlockEntity controller = getController();
        if (controller == null) return;

        boolean powered = controller.getChainMembers().stream()
                .anyMatch(member -> member.localRedstonePowered);

        for (CombustionChamberBlockEntity member : controller.getChainMembers()) {
            if (member.chainRedstonePowered != powered) {
                member.chainRedstonePowered = powered;
                member.setChanged();
                member.sendData();
            }
        }

        controller.updateSpeedAndStressOutput(controller.getChainFuelState());
        controller.syncChainToClients();
    }

    private void updateChainRotation() {
        CombustionChamberBlockEntity output = getOutputEngine();
        if (output != null) {
            output.updateGeneratedRotation();
        }
    }

    public boolean isController() {
        return controllerPos == null || controllerPos.equals(worldPosition);
    }

    public CombustionChamberBlockEntity getController() {
        if (level == null || controllerPos == null || controllerPos.equals(worldPosition)) {
            return this;
        }
        if (level.getBlockEntity(controllerPos) instanceof CombustionChamberBlockEntity controller) {
            return controller;
        }
        return this;
    }

    private List<CombustionChamberBlockEntity> getChainMembers() {
        List<CombustionChamberBlockEntity> members = new ArrayList<>();
        if (level == null) return members;

        CombustionChamberBlockEntity controller = getController();
        Direction facing = controller.getBlockState().getValue(FACING);
        for (int i = 0; i < Math.max(1, controller.chainSize); i++) {
            BlockPos pos = controller.worldPosition.relative(facing, i);
            if (level.getBlockEntity(pos) instanceof CombustionChamberBlockEntity chamber
                    && chamber.getController() == controller) {
                members.add(chamber);
            }
        }

        if (members.isEmpty()) {
            members.add(this);
        }
        return members;
    }

    public boolean isOutputEngine() {
        return chainIndex == chainSize - 1;
    }

    private CombustionChamberBlockEntity getOutputEngine() {
        CombustionChamberBlockEntity controller = getController();
        if (level == null || controller == null) return this;

        Direction facing = controller.getBlockState().getValue(FACING);
        BlockPos outputPos = controller.worldPosition.relative(facing, Math.max(0, controller.chainSize - 1));
        if (level.getBlockEntity(outputPos) instanceof CombustionChamberBlockEntity output) {
            return output;
        }
        return controller;
    }

    public IFluidHandler getFluidHandler(Direction side) {
        if (!isController() || !getBlockState().hasProperty(FACING)) return null;
        return side == getBlockState().getValue(FACING).getOpposite() ? chainInputHandler : null;
    }

    private void consumeFuel() {
        int localFuelState = getCombustionFuelState(inputTank);
        CombustionChamberBlockEntity controller = getController();
        float generatedSpeed = controller != null ? controller.getControllerGeneratedSpeed() : getControllerGeneratedSpeed();
        if (localFuelState <= 0 || generatedSpeed == 0 || chainRedstonePowered) {
            return;
        }
        if (localFuelState == 3 && coldestHeatSource >= ExtendedHeatCondition.NONE.getBlazeHeatEnergy()) {
            return;
        }

        // Play audio
        if (chainIndex % 4 == 0 && level.getGameTime() % 2 == 0)
            level.playSound(null, worldPosition, AllSoundEvents.CRUSHING_3.getMainEvent(), SoundSource.BLOCKS, 0.225f, 0.1f + level.random.nextFloat() * 0.05f);

        // Consume fuel from tank
        inputTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
        setChanged();
    }


    // -------------------------------------------------------------------------
    //  Kinetic Properties
    // -------------------------------------------------------------------------
    @Override
    public boolean hasShaftTowards(LevelReader levelReader, BlockPos blockPos, BlockState blockState, Direction direction) {
        return isOutputEngine() && direction == blockState.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return blockState.getValue(FACING).getAxis();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!isOutputEngine()) {
            return 0;
        }

        CombustionChamberBlockEntity controller = getController();
        return controller != null ? controller.getControllerGeneratedSpeed() : 0;
    }

    private float getControllerGeneratedSpeed() {
        if (chainRedstonePowered) {
            return 0;
        }

        float baseSpeed = 0f;
        boolean isCurrentlyUnderperforming = true;
        isUnderPerforming = true;

        if (currentFuelState >= 3) {
            if (coldestHeatSource <= ExtendedHeatCondition.CHILLED.getBlazeHeatEnergy()) {
                baseSpeed = getMaxGeneratedSpeed();
                isCurrentlyUnderperforming = false;
            } else if (coldestHeatSource <= ExtendedHeatCondition.COOLED.getBlazeHeatEnergy()) {
                baseSpeed = getMaxGeneratedSpeed() * 0.5f;
                isCurrentlyUnderperforming = true;
            } else {
                baseSpeed = 0f;
                isCurrentlyUnderperforming = true;
            }
        } else if (currentFuelState == 2) {
            if (coldestHeatSource <= ExtendedHeatCondition.COOLED.getBlazeHeatEnergy()) {
                baseSpeed = getMaxGeneratedSpeed();
                isCurrentlyUnderperforming = false;
            } else {
                baseSpeed = getMaxGeneratedSpeed() * 0.5f;
                isCurrentlyUnderperforming = true;
            }
        } else if (currentFuelState == 1) {
            baseSpeed = getMaxGeneratedSpeed();
            isCurrentlyUnderperforming = false;
        }

        if (isCurrentlyUnderperforming != isUnderPerforming) {
            isUnderPerforming = isCurrentlyUnderperforming;
            if (level != null && !level.isClientSide) {
                setChanged();
            }
        }

        return baseSpeed * getOutputRotationDirectionMultiplier();
    }

    private float getOutputRotationDirectionMultiplier() {
        CombustionChamberBlockEntity output = getOutputEngine();
        if (output == null || output.movementDirection == null) {
            return 1;
        }
        return output.movementDirection.get() == RotationDirection.CLOCKWISE ? 1 : -1;
    }

    private void onDirectionChanged() {
        if (level == null || level.isClientSide) {
            return;
        }
        updateGeneratedRotation();
        CombustionChamberBlockEntity controller = getController();
        if (controller != null && controller != this) {
            controller.updateSpeedAndStressOutput(controller.getChainFuelState());
        }
    }

    private float getMaxGeneratedSpeed() {
        return getMaxGeneratedSpeed(currentFuelState);
    }

    private float getMaxGeneratedSpeed(int fuelState) {
        if (fuelState >= 3) {
            return 48f;
        } else if (fuelState == 2) {
            return 32f;
        } else if (fuelState == 1) {
            return 16f;
        }

        return 0;
    }

    private void updateSpeedAndStressOutput(int newFuelState) {
        currentFuelState = newFuelState;
        CombustionChamberBlockEntity output = getOutputEngine();
        if (output != null) {
            output.updateGeneratedRotation();
        }
    }

    private void syncChainToClients() {
        CombustionChamberBlockEntity controller = getController();
        if (controller == null) {
            return;
        }

        for (CombustionChamberBlockEntity member : controller.getChainMembers()) {
            member.setChanged();
            member.sendData();
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!isOutputEngine()) {
            return 0;
        }

        CombustionChamberBlockEntity controller = getController();
        return controller != null ? controller.getChainStressCapacity() : 0;
    }

    private float getChainStressCapacity() {
        if (chainRedstonePowered) {
            return 0;
        }

        float totalStress = 0;
        for (CombustionChamberBlockEntity member : getChainMembers()) {
            totalStress += getStressCapacityForFuelState(getCombustionFuelState(member.inputTank));
        }
        return totalStress;
    }

    private float getStressCapacityForFuelState(int fuelState) {
        float maxSpeed = getMaxGeneratedSpeed(fuelState);
        if (fuelState <= 0 || maxSpeed == 0) {
            return 0;
        }

        return (fuelState * fuelState * 512) / maxSpeed;
    }

    public float getRenderedEngineSpeed() {
        CombustionChamberBlockEntity controller = getController();
        return controller != null ? controller.getControllerGeneratedSpeed() : getSpeed();
    }


    // -------------------------------------------------------------------------
    //  Client Visuals
    // -------------------------------------------------------------------------
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CombustionChamberBlockEntity controller = getController();
        CombustionChamberBlockEntity displaySource = controller != null ? controller : this;

        tooltip.add(Component.literal("     Combustion Chamber:"));
        if (displaySource.chainSize > 1) {
            tooltip.add(Component.literal("Linked Engines: " + displaySource.chainSize));
        }
        if (chainRedstonePowered) {
            tooltip.add(Component.literal("§cDeactivated by redstone signal"));
        }
        tooltip.add(Component.literal("§7" + inputTank.getFluid().getHoverName().getString() + " §8(" + (int) (((float) inputTank.getFluidAmount() / TANK_CAPACITY) * 100) + "%)"));
        float displayedSpeed = displaySource.getControllerGeneratedSpeed();
        tooltip.add(Component.literal("§7Generating §b" + String.format("%,d", (int) (displaySource.getChainStressCapacity() * Math.abs(displayedSpeed))) + "su §8at " + (int) displayedSpeed + " RPM"));

        if (displaySource.currentFuelState > 1 && displaySource.isUnderPerforming) {
            tooltip.add(Component.literal(""));

            if (displaySource.currentFuelState == 2)
            {
                tooltip.add(Component.literal("§cEngine underperforming!"));
                tooltip.add(Component.literal("§cSupply a ")
                        .append(Component.literal(ExtendedHeatCondition.COOLED.getSerializedName()).withColor(ExtendedHeatCondition.COOLED.getColor()))
                        .append("§c radiator"));
            }
            else if (displaySource.currentFuelState == 3)
            {
                if (displaySource.getControllerGeneratedSpeed() == 0)
                {
                    tooltip.add(Component.literal("§cEngine overheated!"));
                    tooltip.add(Component.literal("§cSupply a ")
                            .append(Component.literal(ExtendedHeatCondition.CHILLED.getSerializedName()).withColor(ExtendedHeatCondition.CHILLED.getColor()))
                            .append("§c or ")
                            .append(Component.literal(ExtendedHeatCondition.COOLED.getSerializedName()).withColor(ExtendedHeatCondition.COOLED.getColor()))
                            .append("§c radiator"));
                }
                else
                {
                    tooltip.add(Component.literal("§cEngine underperforming!"));
                    tooltip.add(Component.literal("§cSupply a ")
                            .append(Component.literal(ExtendedHeatCondition.CHILLED.getSerializedName()).withColor(ExtendedHeatCondition.CHILLED.getColor()))
                            .append("§c radiator"));
                }
            }
        }
        return true;
    }


    // -------------------------------------------------------------------------
    //  NBT Persistence & Networking
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("CurrentFuelState", currentFuelState);
        tag.putInt("LocalColdestHeatSource", localColdestHeatSource);
        tag.putInt("ColdestHeatSource", coldestHeatSource);
        tag.putBoolean("LocalRedstonePowered", localRedstonePowered);
        tag.putBoolean("ChainRedstonePowered", chainRedstonePowered);
        tag.putBoolean("IsUnderPerforming", isUnderPerforming);
        tag.putInt("ChainIndex", chainIndex);
        tag.putInt("ChainSize", chainSize);
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        currentFuelState = tag.getInt("CurrentFuelState");
        localColdestHeatSource = tag.getInt("LocalColdestHeatSource");
        coldestHeatSource = tag.getInt("ColdestHeatSource");
        localRedstonePowered = tag.getBoolean("LocalRedstonePowered");
        chainRedstonePowered = tag.getBoolean("ChainRedstonePowered");
        isUnderPerforming = tag.getBoolean("IsUnderPerforming");
        chainIndex = tag.getInt("ChainIndex");
        chainSize = tag.contains("ChainSize") ? tag.getInt("ChainSize") : 1;
        controllerPos = tag.contains("ControllerPos") ? BlockPos.of(tag.getLong("ControllerPos")) : worldPosition;
    }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }


    // -------------------------------------------------------------------------
    //  Rotation Direction Handling
    // -------------------------------------------------------------------------
    private static class RotationDirectionSlot extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
            if (!state.hasProperty(FACING)) {
                return null;
            }
            Direction facing = state.getValue(FACING);
            Vec3 location = VecHelper.voxelSpace(8, 13.5, 16);
            return VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(facing), Axis.Y);
        }

        @Override
        public void rotate(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
            TransformStack.of(ms)
                    .rotateYDegrees(AngleHelper.horizontalAngle(facing) + 180);
        }

        @Override
        public boolean shouldRender(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
            if (!super.shouldRender(level, pos, state)) {
                return false;
            }
            return level.getBlockEntity(pos) instanceof CombustionChamberBlockEntity chamber && chamber.isOutputEngine();
        }

        @Override
        public boolean testHit(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
            return shouldRender(level, pos, state) && super.testHit(level, pos, state, localHit);
        }
    }


    // -------------------------------------------------------------------------
    //  Fluid Handling
    // -------------------------------------------------------------------------
    private class ChainInputFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return inputTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY * Math.max(1, chainSize);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;

            List<CombustionChamberBlockEntity> fillable = getChainMembers().stream()
                    .filter(member -> member.inputTank.isEmpty()
                            || FluidStack.isSameFluidSameComponents(member.inputTank.getFluid(), resource))
                    .sorted(Comparator.comparingInt(member -> member.inputTank.getFluidAmount()))
                    .toList();

            if (fillable.isEmpty()) {
                return 0;
            }

            int fillableAmount = 0;
            for (CombustionChamberBlockEntity member : fillable) {
                fillableAmount += member.inputTank.fill(copyWithAmount(resource, resource.getAmount()), FluidAction.SIMULATE);
            }

            int toFill = Math.min(resource.getAmount(), fillableAmount);
            if (action == FluidAction.SIMULATE) {
                return toFill;
            }

            int remaining = toFill;
            while (remaining > 0) {
                List<CombustionChamberBlockEntity> eligible = fillable.stream()
                        .filter(member -> member.inputTank.isEmpty()
                                || FluidStack.isSameFluidSameComponents(member.inputTank.getFluid(), resource))
                        .filter(member -> member.inputTank.getCapacity() - member.inputTank.getFluidAmount() > 0)
                        .sorted(Comparator.comparingInt(member -> member.inputTank.getFluidAmount()))
                        .toList();

                if (eligible.isEmpty()) {
                    break;
                }

                int share = Math.max(1, (int) Math.ceil(remaining / (double) eligible.size()));
                boolean filledAny = false;
                for (CombustionChamberBlockEntity member : eligible) {
                    if (remaining <= 0) break;
                    int inserted = member.inputTank.fill(copyWithAmount(resource, Math.min(share, remaining)), FluidAction.EXECUTE);
                    remaining -= inserted;
                    filledAny |= inserted > 0;
                }

                if (!filledAny) {
                    break;
                }
            }

            updateSpeedAndStressOutput(getChainFuelState());
            syncChainToClients();
            return toFill - remaining;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }

        private FluidStack copyWithAmount(FluidStack stack, int amount) {
            FluidStack copy = stack.copy();
            copy.setAmount(amount);
            return copy;
        }
    }
}
