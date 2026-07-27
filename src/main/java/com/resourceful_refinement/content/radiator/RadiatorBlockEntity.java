package com.resourceful_refinement.content.radiator;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.createmod.catnip.data.Couple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

import static com.resourceful_refinement.content.radiator.RadiatorBlock.HEAT_STATE;
import static net.minecraft.world.level.block.DirectionalBlock.FACING;

public class RadiatorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, BoilerHeater {

    private static final float FLUID_CONSUMPTION = 0.25f;
    public static final int TANK_CAPACITY = 50;
    public static final int MIN_TANK_FILL_THRESHOLD = 25;
    public static final int HEAT_GROWTH = 5;
    public static final int HEAT_DECAY = 8;

    public static final int HEAT_STATE_OFFSET = 3;

    private int heatTemperature = 0; // Visual/internal heat energy (-1000 to 1000)

    // Flow rate limiting fields (transient / tick-by-tick)
    private int fluidReceivedCurrentTick = 0;
    private int fluidReceivedLastTick = 0;
    private int fluidDrainedCurrentTick = 0;

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[4];
    private final int[] cookingTime = new int[4];
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmokingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.SMOKING);

    public final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    public RadiatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {
        list.add(new RadiatorFluidTransportBehaviour(this));
    }

    public boolean isValidFace(Direction face) {
        BlockState state = getBlockState();
        if (!state.hasProperty(RadiatorBlock.FACING)) return false;
        return face.getAxis() == state.getValue(RadiatorBlock.FACING).getAxis();
    }


    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------
    public static boolean isEffectiveFluid(FluidStack stack) {
        return getHeatStateForFluid(stack) != ExtendedHeatCondition.NONE;
    }

    public static ExtendedHeatCondition getHeatStateForFluid(FluidStack stack) {
        return HeatUtilities.GetCoolantConditionFromFluid(stack);
    }

    private static int getConsumeAmount(Level level, ExtendedHeatCondition fluidCoolantType)
    {
        double fluidConsumption = 0;
        if (fluidCoolantType == ExtendedHeatCondition.CHILLED)
            fluidConsumption = ServerConfig.CHILLED_COOLANT_CONSUMPTION.get();
        else if (fluidCoolantType == ExtendedHeatCondition.COOLED)
            fluidConsumption = ServerConfig.COOLED_COOLANT_CONSUMPTION.get();
        else if (fluidCoolantType == ExtendedHeatCondition.PASSIVE)
            fluidConsumption = ServerConfig.PASSIVE_COOLANT_CONSUMPTION.get();
        else if (fluidCoolantType == ExtendedHeatCondition.HEATED)
            fluidConsumption = ServerConfig.HEATED_COOLANT_CONSUMPTION.get();
        else if (fluidCoolantType == ExtendedHeatCondition.SUPERHEATED)
            fluidConsumption = ServerConfig.SUPERHEATED_COOLANT_CONSUMPTION.get();


        if (fluidConsumption >= 1)
            return (int)Math.round(fluidConsumption);
        else if (level.random.nextFloat() <= fluidConsumption)
            return 1;

        return 0;
    }

    public static ExtendedHeatCondition getHeatConditionFromEnergy(int energy)
    {
        if (energy <= ExtendedHeatCondition.CHILLED.getMaxHeatEnergy())
            return ExtendedHeatCondition.CHILLED;
        else if (energy <= ExtendedHeatCondition.COOLED.getMaxHeatEnergy())
            return ExtendedHeatCondition.COOLED;
        else if (energy <= ExtendedHeatCondition.NONE.getMaxHeatEnergy())
            return ExtendedHeatCondition.NONE;
        else if (energy <= ExtendedHeatCondition.PASSIVE.getMaxHeatEnergy())
            return ExtendedHeatCondition.PASSIVE;
        else if (energy <= ExtendedHeatCondition.HEATED.getMaxHeatEnergy())
            return ExtendedHeatCondition.HEATED;
        else
            return ExtendedHeatCondition.SUPERHEATED;
    }

    public static int getHeatGainDelta(int currentEnergy, ExtendedHeatCondition targetState)
    {
        int delta = (targetState.getTargetHeatEnergy() - currentEnergy);
        if (delta == 0) return 0;

        return (int) Math.signum(delta) * Math.clamp(Math.abs(delta), 0, HEAT_GROWTH);
    }

    // Maps ExtendedHeatCondition to the BlazeBurner HeatLevel used by the basin
    public static BlazeBurnerBlock.HeatLevel getBlazeHeatLevelForCondition(ExtendedHeatCondition condition) {
        return switch (condition) {
            case PASSIVE     -> BlazeBurnerBlock.HeatLevel.SMOULDERING;
            case HEATED      -> BlazeBurnerBlock.HeatLevel.KINDLED;
            case SUPERHEATED -> BlazeBurnerBlock.HeatLevel.SEETHING;
            default          -> BlazeBurnerBlock.HeatLevel.NONE;
        };
    }


    // -------------------------------------------------------------------------
    // Server Fluid Logic
    // -------------------------------------------------------------------------
    // Server tick handling decay, setBlock (visual only), and active output push
    public static void serverTick(Level level, BlockPos pos, BlockState state, RadiatorBlockEntity be) {
        // Roll over tick-by-tick rate trackers (clamped to 1 so radiators always try to drain themselves)
        be.fluidReceivedLastTick = Math.max(1, be.fluidReceivedCurrentTick);
        be.fluidReceivedCurrentTick = 0;
        be.fluidDrainedCurrentTick = 0;

        // Process flat-rate consumption and heat updates per tick
        FluidStack fluidInTank = be.tank.getFluid();
        if (!fluidInTank.isEmpty() && isEffectiveFluid(fluidInTank)) {
            ExtendedHeatCondition targetState = getHeatStateForFluid(fluidInTank);
            int coolantConsumption = getConsumeAmount(level, targetState);
            if (fluidInTank.getAmount() >= coolantConsumption) {

                // Deduct flat rate directly from the tank
                be.tank.drain(coolantConsumption, IFluidHandler.FluidAction.EXECUTE);

                be.heatTemperature += getHeatGainDelta(be.heatTemperature, targetState);
                be.setChanged();
            } else {
                // Cannot afford the flat rate, do not consume anything and decay instead
                be.decayHeat(level);
            }
        } else {
            be.decayHeat(level);
        }

        // Map to block state (HEAT_STATE for visuals, HEAT_LEVEL for basin/boiler compat)
        int currentHeatState = state.getValue(HEAT_STATE) + HEAT_STATE_OFFSET;
        ExtendedHeatCondition currentHeatCondition = getHeatConditionFromEnergy(be.heatTemperature);
        int targetHeatState = currentHeatCondition.getBlazeHeatLevel() + HEAT_STATE_OFFSET;
        BlazeBurnerBlock.HeatLevel targetBlazeLevel = getBlazeHeatLevelForCondition(currentHeatCondition);
        BlazeBurnerBlock.HeatLevel currentBlazeLevel = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);

        if (currentHeatState != targetHeatState || currentBlazeLevel != targetBlazeLevel) {
            // Use flag 2 (Block.UPDATE_CLIENTS) to avoid neighbor pipe updates wiping pressure
            level.setBlock(pos, state
                    .setValue(HEAT_STATE, targetHeatState)
                    .setValue(BlazeBurnerBlock.HEAT_LEVEL, targetBlazeLevel), 3);
        }

        // Active output pushing (for direct connections to tanks/machines without pipes)
        // Only push if the radiator has more than the minimum tank fill threshold
        if (be.tank.getFluidAmount() > MIN_TANK_FILL_THRESHOLD && state.hasProperty(RadiatorBlock.FACING)) {
            Direction facing = state.getValue(RadiatorBlock.FACING);
            BlockPos targetPos = pos.relative(facing);
            IFluidHandler targetHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, facing.getOpposite());
            if (targetHandler != null) {
                int maxPush = Math.max(0, be.fluidReceivedLastTick - be.fluidDrainedCurrentTick);
                if (maxPush > 0) {
                    FluidStack toDrain = be.tank.getFluid().copy();
                    toDrain.setAmount(Math.min(toDrain.getAmount(), maxPush));

                    int filled = targetHandler.fill(toDrain, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        FluidStack drained = be.tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                        if (!drained.isEmpty()) {
                            targetHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            be.fluidDrainedCurrentTick += drained.getAmount();
                        }
                    }
                }
            }
        }

        // Run block freezing logic
        freezeTick(state, level, pos, level.random);

        // Cook items sitting on the radiator
        be.cookTick();
    }

    private void decayHeat(Level level) {
        if (heatTemperature != 0 && level.random.nextInt(4) == 0) {
            heatTemperature = (int)(Math.signum(heatTemperature) * Math.max(Math.abs(heatTemperature) - HEAT_DECAY, 0));
            setChanged();
            syncData();
        }
    }

    public boolean isHeated() {
        // Passive, Heated, or Superheated
        return this.heatTemperature > ExtendedHeatCondition.NONE.getMaxHeatEnergy();
    }

    /** Returns the current heat condition of this radiator based on its internal heat energy. */
    public ExtendedHeatCondition getHeatCondition() {
        return getHeatConditionFromEnergy(this.heatTemperature);
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public Optional<RecipeHolder<SmokingRecipe>> getCookableRecipe(ItemStack stack) {
        return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SingleRecipeInput(stack), this.level);
    }

    public boolean placeFood(@Nullable LivingEntity entity, ItemStack food, int cookTime) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack = this.items.get(i);
            if (itemstack.isEmpty()) {
                this.cookingTime[i] = cookTime * 8;
                this.cookingProgress[i] = 0;
                this.items.set(i, food.consumeAndReturn(1, entity));
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), Context.of(entity, this.getBlockState()));
                this.syncData();
                return true;
            }
        }
        return false;
    }

    public void cookTick() {
        if (level == null || level.isClientSide) return;

        boolean isHeated = isHeated();
        if (!isHeated) {
            return;
        }

        boolean changed = false;
        boolean cookedFood = false;
        for (int i = 0; i < items.size(); ++i) {
            ItemStack itemstack = items.get(i);
            if (!itemstack.isEmpty()) {
                cookedFood = true;
                cookingProgress[i]++;
                if (cookingProgress[i] >= cookingTime[i]) {
                    SingleRecipeInput recipeInput = new SingleRecipeInput(itemstack);
                    ItemStack resultStack = quickCheck.getRecipeFor(recipeInput, level)
                            .map(recipe -> recipe.value().assemble(recipeInput, level.registryAccess()))
                            .orElse(itemstack);

                    if (resultStack.isItemEnabled(level.enabledFeatures())) {
                        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, resultStack.copy());
                        items.set(i, ItemStack.EMPTY);
                        changed = true;
                    }
                }
            }
        }

        if (cookedFood)
            particleTick(level, getBlockPos(), getBlockState(), this);


        if (changed) {
            syncData();
        }
    }

    public static void particleTick(Level level, BlockPos pos, BlockState state, RadiatorBlockEntity blockEntity) {
        RandomSource randomsource = level.random;
        if (randomsource.nextFloat() < 0.75f)
            return;

        if (level instanceof ServerLevel serverLevel)
        {
            int l = ((Direction)state.getValue(FACING)).get2DDataValue();
            for(int j = 0; j < blockEntity.items.size(); ++j) {
                if (!((ItemStack)blockEntity.items.get(j)).isEmpty() && randomsource.nextFloat() < 0.2F) {
                    Direction direction = Direction.from2DDataValue(Math.floorMod(j + l, 4));
                    double d0 = (double)pos.getX() + (double)0.5F - (double)((float)direction.getStepX() * 0.25F) + (double)((float)direction.getClockWise().getStepX() * 0.25F);
                    double d1 = (double)pos.getY() + (double)1F;
                    double d2 = (double)pos.getZ() + (double)0.5F - (double)((float)direction.getStepZ() * 0.25F) + (double)((float)direction.getClockWise().getStepZ() * 0.25F);

                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            d0, d1, d2,
                            0, 0, 0.25, 0, 5.0E-4);
                }
            }
        }
    }


    // Exposes a contextual capability handler tailored to the accessed face.
    @Nullable
    public IFluidHandler getFluidHandler(@NotNull Direction side) {
        if (!isValidFace(side)) return null;
        return new RadiatorProxyFluidHandler(side);
    }

    private static void freezeTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (!level.isClientSide()) {
            if (state.getValue(HEAT_STATE) != ExtendedHeatCondition.CHILLED.getBlazeHeatLevel() + HEAT_STATE_OFFSET)
                return;

            if (random.nextFloat() > 0.0075f)
                return;

            List<FreezeableFluidPos> adjacentFluidSources = new ArrayList<>();

            // Find all adjacent fluids
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = pos.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacentPos);

                // Check if it's a water state or lava source
                if (adjacentState.getFluidState().is(Fluids.WATER) && level.getFluidState(adjacentPos).isSource() &&
                        (!adjacentState.hasProperty(BlockStateProperties.WATERLOGGED) || !adjacentState.getValue(BlockStateProperties.WATERLOGGED)))
                    adjacentFluidSources.add(new FreezeableFluidPos(adjacentPos, 0));
                else if (adjacentState.getFluidState().is(Fluids.WATER) && !level.getFluidState(adjacentPos).isSource() &&
                        (!adjacentState.hasProperty(BlockStateProperties.WATERLOGGED) || !adjacentState.getValue(BlockStateProperties.WATERLOGGED)))
                    adjacentFluidSources.add(new FreezeableFluidPos(adjacentPos, 1));
                else if (adjacentState.getFluidState().is(Fluids.LAVA) && level.getFluidState(adjacentPos).isSource())
                    adjacentFluidSources.add(new FreezeableFluidPos(adjacentPos, 2));
            }

            // Randomly select one and freeze it
            if (!adjacentFluidSources.isEmpty()) {
                FreezeableFluidPos fluidToFreeze = adjacentFluidSources.get(random.nextInt(adjacentFluidSources.size()));

                if (fluidToFreeze.type == 0)
                    level.setBlockAndUpdate(fluidToFreeze.pos, Blocks.ICE.defaultBlockState());
                else if (fluidToFreeze.type == 1)
                    level.setBlockAndUpdate(fluidToFreeze.pos, Blocks.POWDER_SNOW.defaultBlockState());
                else if (fluidToFreeze.type == 2)
                    level.setBlockAndUpdate(fluidToFreeze.pos, Blocks.DEEPSLATE.defaultBlockState());
            }
        }
    }

    // Create BoilerHeater interface method
    // Returns heat level for fluid-tank boilers (0 = passive, 1 = heated, 2 = superheated, -1 = none)
    @Override
    public float getHeat(Level level, BlockPos blockPos, BlockState blockState) {
        ExtendedHeatCondition condition = getHeatConditionFromEnergy(heatTemperature);
        return switch (condition) {
            case CHILLED     -> -3;
            case COOLED      -> -2;
            case PASSIVE     -> BoilerHeater.PASSIVE_HEAT;  // 0
            case HEATED      -> 1;
            case SUPERHEATED -> 2;
            default          -> BoilerHeater.NO_HEAT;       // -1
        };
    }

    private record FreezeableFluidPos(BlockPos pos, int type) {}


    // -------------------------------------------------------------------------
    //  Client Visuals
    // -------------------------------------------------------------------------
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Radiator:"));

        BlockState state = getBlockState();

        ExtendedHeatCondition radiatorHeat = getHeatConditionFromEnergy(heatTemperature);
        tooltip.add(Component.literal("§7Heat: ").append(Component.literal(radiatorHeat.getSerializedName()).withColor(radiatorHeat.getColor())).append(" §8(" + heatTemperature + "° H)"));
        tooltip.add(Component.literal("§7Flow Status: §8" + (fluidReceivedLastTick != 1 ? "Operational" : "Stagnant")));

        return true;
    }


    // -------------------------------------------------------------------------
    // NBT Persistence
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("HeatLevel", heatTemperature);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("FluidReceivedLastTick", fluidReceivedLastTick);
        ContainerHelper.saveAllItems(tag, this.items, true, registries);
        tag.putIntArray("CookingTimes", this.cookingProgress);
        tag.putIntArray("CookingTotalTimes", this.cookingTime);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        heatTemperature = tag.getInt("HeatLevel");
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        fluidReceivedLastTick = tag.getInt("FluidReceivedLastTick");
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
        if (tag.contains("CookingTimes", 11)) {
            int[] aint = tag.getIntArray("CookingTimes");
            System.arraycopy(aint, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, aint.length));
        }
        if (tag.contains("CookingTotalTimes", 11)) {
            int[] aint1 = tag.getIntArray("CookingTotalTimes");
            System.arraycopy(aint1, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, aint1.length));
        }
    }

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }


    // -------------------------------------------------------------------------
    // FluidTransport Implementation
    // -------------------------------------------------------------------------
    public class RadiatorFluidTransportBehaviour extends FluidTransportBehaviour {
        public RadiatorFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public void tick() {
            super.tick();
            if (level == null || level.isClientSide) return;
            BlockState state = getBlockState();
            if (!state.hasProperty(RadiatorBlock.FACING)) return;
            Direction facing = state.getValue(RadiatorBlock.FACING);

            if (interfaces != null) {
                for (java.util.Map.Entry<Direction, PipeConnection> entry : interfaces.entrySet()) {
                    Direction side = entry.getKey();
                    Couple<Float> pressure = entry.getValue().getPressure();
                    if (side == facing) {
                        // Push face: apply pressure based on how much fluid we received last tick
                        float targetPressure = 2.0f * fluidReceivedLastTick;
                        //float targetPressure = Math.max(2.0f * fluidReceivedLastTick, 2f);
                        pressure.set(true, targetPressure);
                        pressure.set(false, 0f);
                    } else {
                        // Pull face: no active pressure generated by the radiator
                        pressure.set(true, 0f);
                        pressure.set(false, 0f);
                    }
                }
            }
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            if (!state.hasProperty(RadiatorBlock.FACING)) return false;
            Direction facing = state.getValue(RadiatorBlock.FACING);
            return direction.getAxis() == facing.getAxis();
        }
    }

    private class RadiatorProxyFluidHandler implements IFluidHandler {
        private final Direction accessedSide;

        public RadiatorProxyFluidHandler(Direction accessedSide) {
            this.accessedSide = accessedSide;
        }

        private boolean isInput() {
            BlockState state = getBlockState();
            if (!state.hasProperty(RadiatorBlock.FACING)) return false;
            return accessedSide == state.getValue(RadiatorBlock.FACING).getOpposite();
        }

        private boolean isOutput() {
            BlockState state = getBlockState();
            if (!state.hasProperty(RadiatorBlock.FACING)) return false;
            return accessedSide == state.getValue(RadiatorBlock.FACING);
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        @NotNull
        public FluidStack getFluidInTank(int tankSlot) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int tankSlot) {
            return tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tankSlot, @NotNull FluidStack stack) {
            return tank.isFluidValid(tankSlot, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isInput()) return 0;
            if (resource.isEmpty()) return 0;

            int filled = tank.fill(resource, action);

            if (filled > 0) {
                if (action.execute()) {
                    fluidReceivedCurrentTick += filled;
                    setChanged();
                }
                return filled;
            }
            return 0;
        }

        @Override
        @NotNull
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!isOutput()) return FluidStack.EMPTY; // Can only drain from the output face
            if (resource.isEmpty()) return FluidStack.EMPTY;

            int maxDrain = Math.max(0, fluidReceivedLastTick - fluidDrainedCurrentTick);
            if (tank.getFluidAmount() - maxDrain < MIN_TANK_FILL_THRESHOLD)
                maxDrain = Math.min(maxDrain, tank.getFluidAmount() - MIN_TANK_FILL_THRESHOLD);
            if (maxDrain <= 0) return FluidStack.EMPTY;

            FluidStack toDrain = resource.copy();
            toDrain.setAmount(Math.min(toDrain.getAmount(), maxDrain));

            FluidStack drained = tank.drain(toDrain, action);
            if (!drained.isEmpty() && action.execute()) {
                fluidDrainedCurrentTick += drained.getAmount();
                setChanged();
            }
            return drained;
        }

        @Override
        @NotNull
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (!isOutput()) return FluidStack.EMPTY; // Can only drain from the output face
            if (maxDrain <= 0) return FluidStack.EMPTY;

            int allowedDrain = Math.max(0, fluidReceivedLastTick - fluidDrainedCurrentTick);
            if (tank.getFluidAmount() - allowedDrain < MIN_TANK_FILL_THRESHOLD)
                allowedDrain = Math.min(allowedDrain, tank.getFluidAmount() - MIN_TANK_FILL_THRESHOLD);
            if (allowedDrain <= 0) return FluidStack.EMPTY;


            int toDrainAmount = Math.min(maxDrain, allowedDrain);
            FluidStack drained = tank.drain(toDrainAmount, action);
            if (!drained.isEmpty() && action.execute()) {
                fluidDrainedCurrentTick += drained.getAmount();
                setChanged();
            }
            return drained;
        }
    }
}
