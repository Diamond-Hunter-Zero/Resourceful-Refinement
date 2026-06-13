package com.resourceful_refinement.content.combustion_chamber;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

import static com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock.FACING;

public class CombustionChamberBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation, IRotate {

    public static final int TANK_CAPACITY = 1000;

    public static TagKey<Fluid> PASSIVE_FUEL_FLUID_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "passive_fuel"));
    public static TagKey<Fluid> HEATED_FUEL_FLUID_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "heated_fuel"));
    public static TagKey<Fluid> SUPERHEATED_FUEL_FLUID_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "superheated_fuel"));

    private int currentFuelState = -1;
    private int coldestHeatSource = 0;
    private boolean isUnderPerforming = false;

    public final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };


    // -------------------------------------------------------------------------
    // Block Entity instantiation
    // -------------------------------------------------------------------------

    public CombustionChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {

    }


    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Combustion Chamber:"));
        tooltip.add(Component.literal( "§7" + inputTank.getFluid().getHoverName().getString() + " §8(" + (int)(((float)inputTank.getFluidAmount() / TANK_CAPACITY)*100) + "%)"));
        tooltip.add(Component.literal("§7Generating §b" + String.format("%,d", (int)capacity) + "su §8at " + (int)getSpeed() + " RPM"));

        if (currentFuelState > 1 && isUnderPerforming) {
            tooltip.add(Component.literal(""));

            if (currentFuelState == 2)
            {
                tooltip.add(Component.literal("§cEngine underperforming!"));
            tooltip.add(Component.literal("§cSupply a ")
                    .append(Component.literal(ExtendedHeatCondition.COOLED.getSerializedName()).withColor(ExtendedHeatCondition.COOLED.getColor()))
                    .append("§c radiator"));
            }
            else if (currentFuelState == 3)
            {
                if (speed == 0)
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
    // Block Entity logic
    // -------------------------------------------------------------------------
    public void tick() {
        if (level != null && !level.isClientSide()) {

            // Only run the mechanism if we have fluid
            int newFuelState = getCombustionFuelState(inputTank);
            if (newFuelState != currentFuelState || getGeneratedSpeed() != getSpeed())
            {
                updateSpeedAndStressOutput(newFuelState);
            }

            if (currentFuelState > 0) {
                // Don't consume fuel if overclocked engine is offline
                if (currentFuelState == 3 && coldestHeatSource >= ExtendedHeatCondition.NONE.getBlazeHeatEnergy())
                    return;

                // Consume the fluid
                inputTank.drain(1, IFluidHandler.FluidAction.EXECUTE);

                // Mark block as changed to update client and power network
                setChanged();
            }
        }
    }

    private int getCombustionFuelState(FluidTank tank)
    {
        if (tank.isEmpty())
            return 0;
        else if (tank.getFluid().is(PASSIVE_FUEL_FLUID_TAG))
            return 1;
        else if (tank.getFluid().is(HEATED_FUEL_FLUID_TAG))
            return 2;
        else if (tank.getFluid().is(SUPERHEATED_FUEL_FLUID_TAG))
            return 3;

        return 0;
    }

    public void updateHeatAdjacency()
    {
        int newColdestSource = HeatUtilities.GetColdestAdjacentHeatSource(level, this.getBlockPos());
        if (newColdestSource != coldestHeatSource)
        {
            coldestHeatSource = newColdestSource;
            this.updateGeneratedRotation();
        }
    }


    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("CurrentFuelState", currentFuelState);
        tag.putBoolean("IsUnderPerforming", isUnderPerforming);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        currentFuelState = tag.getInt("CurrentFuelState");
        isUnderPerforming = tag.getBoolean("IsUnderPerforming");
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


    // -------------------------------------------------------------------------
    // Kinetic network properties
    // -------------------------------------------------------------------------
    @Override
    public boolean hasShaftTowards(LevelReader levelReader, BlockPos blockPos, BlockState blockState, Direction direction) {
        return direction == blockState.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return blockState.getValue(FACING).getAxis();
    }

    @Override
    public float getGeneratedSpeed()
    {
        float baseSpeed = 0f;
        boolean isCurrentlyUnderperforming = true;
        isUnderPerforming = true;

        if (currentFuelState >= 3)
        {
            if (coldestHeatSource <= ExtendedHeatCondition.CHILLED.getBlazeHeatEnergy())
            {
                baseSpeed = getMaxGeneratedSpeed();
                isCurrentlyUnderperforming = false;
            }
            else if (coldestHeatSource <= ExtendedHeatCondition.COOLED.getBlazeHeatEnergy())
            {
                baseSpeed = getMaxGeneratedSpeed() * 0.5f;
                isCurrentlyUnderperforming = true;
            }
            else
            {
                baseSpeed = 0f;
                isCurrentlyUnderperforming = true;
            }
        }
        else if (currentFuelState == 2)
        {
            if (coldestHeatSource <= ExtendedHeatCondition.COOLED.getBlazeHeatEnergy())
            {
                baseSpeed = getMaxGeneratedSpeed();
                isCurrentlyUnderperforming = false;
            }
            else
            {
                baseSpeed = getMaxGeneratedSpeed() * 0.5f;
                isCurrentlyUnderperforming = true;
            }
        }
        else if (currentFuelState == 1)
        {
            baseSpeed = getMaxGeneratedSpeed();
            isCurrentlyUnderperforming = false;
        }

        if (isCurrentlyUnderperforming != isUnderPerforming)
        {
            isUnderPerforming = isCurrentlyUnderperforming;
            if (level != null && !level.isClientSide)
                setChanged();
        }

        return baseSpeed;
    }

    private float getMaxGeneratedSpeed()
    {
        if (currentFuelState >= 3)
            return 48f;
        else if (currentFuelState == 2)
            return 32f;
        else if (currentFuelState == 1)
            return 16f;

        return 0;
    }

    private void updateSpeedAndStressOutput(int newFuelState)
    {
        currentFuelState = newFuelState;
        this.updateGeneratedRotation();
    }

    @Override
    public float calculateAddedStressCapacity()
    {
        float baseStress = (currentFuelState * currentFuelState * 512)/getMaxGeneratedSpeed();
        if (isUnderPerforming && currentFuelState < 3)
            baseStress *= 0.5f;

        return baseStress;
    }

    /*@Override
    public float calculateStressApplied()
    {
        return 256 + (int) (512 * (currentFuelState -1 + Math.pow(1.25f, currentFuelState -1)));
    }

    // Return speed (RPM)
    @Override
    public float getSpeed() {
        if (currentFuelState >= 2)
            return 48f;
        else if (currentFuelState == 1)
            return 32f;

        return 0;
    }*/
}
