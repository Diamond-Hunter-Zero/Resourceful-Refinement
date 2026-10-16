package com.resourceful_refinement.content.drill_pylon;

import com.resourceful_refinement.content.drill_pylon.recipe.DrillPylonRecipe;
import com.resourceful_refinement.content.drill_pylon.recipe.DrillPylonRecipeInput;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.glare.GlareLuxCalculator;
import com.resourceful_refinement.content.glare.GlareOperationStatus;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.content.glare.IGlareNode;
import com.resourceful_refinement.content.glare.IGlareReceiver;
import com.resourceful_refinement.content.glare.lux.LuxTransceiverBlockEntity;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshot;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshotProvider;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.utilities.GoggleUtilities;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DrillPylonHeadBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation, IGlareNode, IGlareReceiver, GlareNetworkSnapshotProvider {
    public record AssemblyResult(boolean success, String reason) {
        public static AssemblyResult ok() {
            return new AssemblyResult(true, "");
        }

        public static AssemblyResult fail(String reason) {
            return new AssemblyResult(false, reason);
        }
    }

    public static final int MIN_RPM = 64;
    public static final int INVENTORY_SLOT_COUNT = 4;
    private static final int STANDALONE_DRILL_INTERVAL = 20;
    private static final int DRILL_PARTICLES_PER_TICK = 8;
    private static final int STANDALONE_TARGET_COUNT = 9;

    // Structure layout, as y offsets from the head. The head sits one layer above the bud, in the drill bit's
    // clearance layer; corner girder legs run from the bud's layer to the top, and the body is the 3x3 casing
    // ring stack above the head.
    public static final int LEG_BOTTOM_Y = -1;
    public static final int LEG_TOP_Y = 3;
    public static final int BODY_BOTTOM_Y = 1;
    public static final int BODY_TOP_Y = 3;
    public static final int OUTPUT_Y = 1;
    public static final int KINETIC_Y = 2;
    public static final int LUX_SOCKET_Y = 3;

    private boolean assembled;
    private Direction front = Direction.NORTH;
    private int progress;
    private int standaloneDrillTimer;
    private int allocatedLux;
    private UUID networkId;
    private GlareOperationStatus operationStatus = GlareOperationStatus.ONLINE;
    private int syncedLuxCapacity;
    private int syncedLuxAllocated;
    private boolean syncedOverloaded;
    private int[] syncedLuxHistory = new int[0];
    private ResourceLocation displayedRecipeId;
    private DrillPylonRecipe lastRecipe;
    private Item lastSourceItem = Items.AIR;
    private boolean drilling;

    // Server-only standalone breaking overlay state; one breaker id per 3x3 target
    private int[] breakerIds;
    private int[] breakingStages;

    // Client-only animation state, advanced by DrillPylonRenderer from the synced drilling flag
    private DrillAnimationPhase animPhase;
    private float animPhaseStart;
    private float animSpinOffset;

    public final ItemStackHandler outputInv = new ItemStackHandler(INVENTORY_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    public DrillPylonHeadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            tickDrillParticles();
            return;
        }

        if (!assembled) {
            tickStandaloneDrill();
            return;
        }

        tickAssembledPylon();
    }

    private void tickAssembledPylon() {
        Item sourceItem = resolveSourceItem();
        if (sourceItem != lastSourceItem) {
            lastSourceItem = sourceItem;
            lastRecipe = null;
            displayedRecipeId = null;
        }

        DrillPylonRecipe recipe = findActiveRecipe(sourceItem);
        if (recipe == null) {
            progress = 0;
            setAllocatedLux(0);
            setDrilling(false);
            return;
        }

        int duration = getRecipeDuration(recipe);
        int requiredLux = GlareLuxCalculator.sampleVariableLux(recipe.getLuxCurveArray(), progress, duration);
        setAllocatedLux(requiredLux);

        if (getProxySpeed() < MIN_RPM || !hasLuxReady(recipe)) {
            setDrilling(false);
            return;
        }

        progress++;
        if (progress < duration) {
            setDrilling(true);
            syncData();
            return;
        }

        List<ItemStack> rolledResults = new ArrayList<>(recipe.rollResults(level.random));
        if (!canFitResults(rolledResults)) {
            progress = duration;
            setDrilling(false);
            syncData();
            return;
        }

        for (ItemStack result : rolledResults) {
            if (!result.isEmpty()) {
                ItemHandlerHelper.insertItemStacked(outputInv, result.copy(), false);
            }
        }
        progress = 0;
        setDrilling(true);
        syncData();
    }

    private void setDrilling(boolean value) {
        if (drilling == value) return;
        drilling = value;
        syncData();
    }

    public boolean isDrilling() {
        return assembled && drilling;
    }

    /**
     * Client-only: while the bit is in its operating spin, sprays debris particles textured from the block
     * being drilled outward around the bit's tip.
     */
    private void tickDrillParticles() {
        if (!isDrilling() || animPhase != DrillAnimationPhase.RUNNING) return;

        BlockPos drilledPos = worldPosition.below();
        BlockState drilledState = level.getBlockState(drilledPos);
        if (drilledState.isAir()) return;

        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, drilledState).setPos(drilledPos);
        double tipX = worldPosition.getX() + 0.5;
        double tipY = worldPosition.getY() + 0.05;
        double tipZ = worldPosition.getZ() + 0.5;
        for (int i = 0; i < DRILL_PARTICLES_PER_TICK; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double speed = 0.15 + level.random.nextDouble() * 0.2;
            double radius = 0.2 + level.random.nextDouble() * 0.3;
            level.addParticle(particle,
                    tipX + Math.cos(angle) * radius, tipY, tipZ + Math.sin(angle) * radius,
                    Math.cos(angle) * speed, 0.2 + level.random.nextDouble() * 0.25, Math.sin(angle) * speed);
        }
    }

    private void tickStandaloneDrill() {
        if (Math.abs(getSpeed()) < MIN_RPM) {
            standaloneDrillTimer = 0;
            clearBreakingProgress();
            return;
        }
        standaloneDrillTimer++;
        boolean breakNow = standaloneDrillTimer >= STANDALONE_DRILL_INTERVAL;
        // Crack stages 0-9 across the interval, matching the vanilla/Create breaking overlay
        int stage = Math.min(9, standaloneDrillTimer * 10 / STANDALONE_DRILL_INTERVAL);

        Direction facing = getBlockState().getValue(DrillPylonHeadBlock.FACING);
        BlockPos center = worldPosition.relative(facing);
        Direction.Axis axis = facing.getAxis();
        int index = 0;
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++, index++) {
                BlockPos target = offsetInPlane(center, axis, a, b);
                BlockState state = level.getBlockState(target);
                boolean breakable = !state.isAir() && state.getDestroySpeed(level, target) >= 0;
                if (breakNow && breakable) {
                    level.destroyBlock(target, true);
                }
                setBreakingStage(index, target, breakable && !breakNow ? stage : -1);
            }
        }
        if (breakNow) standaloneDrillTimer = 0;
    }

    private void setBreakingStage(int index, BlockPos pos, int stage) {
        if (breakerIds == null) {
            breakerIds = new int[STANDALONE_TARGET_COUNT];
            for (int i = 0; i < breakerIds.length; i++) {
                breakerIds[i] = BlockBreakingKineticBlockEntity.NEXT_BREAKER_ID.decrementAndGet();
            }
            breakingStages = new int[STANDALONE_TARGET_COUNT];
            Arrays.fill(breakingStages, -1);
        }
        if (breakingStages[index] == stage) return;
        breakingStages[index] = stage;
        level.destroyBlockProgress(breakerIds[index], pos, stage);
    }

    private void clearBreakingProgress() {
        if (breakingStages == null || level == null || level.isClientSide) return;
        Direction facing = getBlockState().getValue(DrillPylonHeadBlock.FACING);
        BlockPos center = worldPosition.relative(facing);
        int index = 0;
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++, index++) {
                setBreakingStage(index, offsetInPlane(center, facing.getAxis(), a, b), -1);
            }
        }
    }

    @Override
    public void invalidate() {
        clearBreakingProgress();
        super.invalidate();
    }

    static BlockPos offsetInPlane(BlockPos center, Direction.Axis axis, int a, int b) {
        return switch (axis) {
            case X -> center.offset(0, a, b);
            case Y -> center.offset(a, 0, b);
            case Z -> center.offset(a, b, 0);
        };
    }

    private DrillPylonRecipe findActiveRecipe(Item sourceItem) {
        if (sourceItem == null || sourceItem == Items.AIR) return null;
        DrillPylonRecipeInput input = new DrillPylonRecipeInput(sourceItem);
        if (lastRecipe != null && lastRecipe.matches(input, level)) return lastRecipe;

        RecipeHolder<DrillPylonRecipe> holder = level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.DRILL_PYLON_TYPE.get(), input, level)
                .orElse(null);
        if (holder == null) return null;
        lastRecipe = holder.value();
        displayedRecipeId = holder.id();
        syncData();
        return lastRecipe;
    }

    private Item resolveSourceItem() {
        if (level.getBlockEntity(worldPosition.below()) instanceof CrystalFissureBudBlockEntity bud) {
            return bud.getSourceItem();
        }
        return Items.AIR;
    }

    private int getRecipeDuration(DrillPylonRecipe recipe) {
        int duration = recipe.getProcessingDuration();
        return duration <= 0 ? 100 : duration;
    }

    private boolean canFitResults(List<ItemStack> results) {
        ItemStackHandler trial = new ItemStackHandler(INVENTORY_SLOT_COUNT);
        for (int i = 0; i < INVENTORY_SLOT_COUNT; i++) {
            trial.setStackInSlot(i, outputInv.getStackInSlot(i).copy());
        }
        for (ItemStack result : results) {
            if (!result.isEmpty() && !ItemHandlerHelper.insertItemStacked(trial, result.copy(), false).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasLuxReady(DrillPylonRecipe recipe) {
        if (!recipe.requiresLux()) return true;
        if (!(level instanceof ServerLevel server)) return false;
        if (networkId == null || operationStatus != GlareOperationStatus.ONLINE) return false;
        return !GlareService.getAllLinks(server, getGlareNodePos()).isEmpty();
    }

    public float getProxySpeed() {
        if (level == null || !assembled) return Math.abs(getSpeed());
        float speed = 0;
        for (Direction side : List.of(front.getClockWise(), front.getCounterClockWise())) {
            BlockPos pos = worldPosition.above(KINETIC_Y).relative(side);
            if (level.getBlockEntity(pos) instanceof DrillPylonKineticProxyBlockEntity proxy) {
                speed = Math.max(speed, Math.abs(proxy.getSpeed()));
            }
        }
        return speed;
    }

    public boolean isAssembled() {
        return assembled;
    }

    public Direction getFront() {
        return front;
    }

    public AssemblyResult tryAssemble(Player player) {
        if (assembled) return AssemblyResult.ok();
        if (level == null) return AssemblyResult.fail("No level is available");
        if (!(level.getBlockEntity(worldPosition.below()) instanceof CrystalFissureBudBlockEntity)) {
            return AssemblyResult.fail("Drill Pylon Head must be directly above a Crystal Fissure Bud");
        }

        Direction assembledFront = player.getDirection().getOpposite();
        AssemblyResult validation = validateStructure(assembledFront);
        if (!validation.success()) return validation;

        clearBreakingProgress();
        standaloneDrillTimer = 0;
        front = assembledFront;
        assembled = true;
        progress = 0;
        convertStructureToProxies();
        if (level instanceof ServerLevel server) {
            GlareService.onNodeLoaded(server, this);
        }
        refreshAdjacentLuxTransceiver();
        syncData();
        return AssemblyResult.ok();
    }

    private AssemblyResult validateStructure(Direction assembledFront) {
        for (int y = BODY_BOTTOM_Y; y <= BODY_TOP_Y; y++) {
            BlockPos center = worldPosition.above(y);
            if (!AllBlocks.GEARBOX.has(level.getBlockState(center))) {
                return AssemblyResult.fail("Missing gearbox at layer " + y);
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos pos = worldPosition.offset(dx, y, dz);
                    if (!AllBlocks.BRASS_CASING.has(level.getBlockState(pos))) {
                        return AssemblyResult.fail("Missing brass casing at ring layer " + y);
                    }
                }
            }
        }

        for (int y = LEG_BOTTOM_Y; y <= LEG_TOP_Y; y++) {
            for (int dx : new int[]{-2, 2}) {
                for (int dz : new int[]{-2, 2}) {
                    BlockPos pos = worldPosition.offset(dx, y, dz);
                    if (!AllBlocks.METAL_GIRDER.has(level.getBlockState(pos))) {
                        return AssemblyResult.fail("Missing corner girder at height " + y);
                    }
                }
            }
        }

        if (assembledFront.getAxis() == Direction.Axis.Y) {
            return AssemblyResult.fail("Front direction must be horizontal");
        }
        return AssemblyResult.ok();
    }

    private void convertStructureToProxies() {
        for (int y = BODY_BOTTOM_Y; y <= BODY_TOP_Y; y++) {
            BlockPos center = worldPosition.above(y);
            if (y == KINETIC_Y) {
                convertToKineticProxy(center);
            } else {
                convertToProxy(center, roleFor(center));
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos pos = worldPosition.offset(dx, y, dz);
                    DrillPylonProxyRole role = roleFor(pos);
                    if (role == DrillPylonProxyRole.KINETIC) {
                        convertToKineticProxy(pos);
                    } else {
                        convertToProxy(pos, role);
                    }
                }
            }
        }

        for (int y = LEG_BOTTOM_Y; y <= LEG_TOP_Y; y++) {
            for (int dx : new int[]{-2, 2}) {
                for (int dz : new int[]{-2, 2}) {
                    convertToProxy(worldPosition.offset(dx, y, dz), DrillPylonProxyRole.STRUCTURE);
                }
            }
        }
    }

    private DrillPylonProxyRole roleFor(BlockPos pos) {
        BlockPos output = worldPosition.above(OUTPUT_Y).relative(front);
        if (pos.equals(output)) return DrillPylonProxyRole.OUTPUT;
        BlockPos left = worldPosition.above(KINETIC_Y).relative(front.getCounterClockWise());
        BlockPos right = worldPosition.above(KINETIC_Y).relative(front.getClockWise());
        if (pos.equals(left) || pos.equals(right)) return DrillPylonProxyRole.KINETIC;
        if (pos.equals(getLuxSocketProxyPos())) return DrillPylonProxyRole.LUX_SOCKET;
        return DrillPylonProxyRole.STRUCTURE;
    }

    private BlockPos getLuxSocketProxyPos() {
        return worldPosition.above(LUX_SOCKET_Y);
    }

    private void refreshAdjacentLuxTransceiver() {
        if (!(level instanceof ServerLevel server)) return;
        BlockPos transceiverPos = getLuxSocketProxyPos().above();
        if (!server.isLoaded(transceiverPos)) return;
        if (server.getBlockEntity(transceiverPos) instanceof LuxTransceiverBlockEntity transceiver) {
            transceiver.refreshSocketLink();
        }
    }

    private void convertToProxy(BlockPos pos, DrillPylonProxyRole role) {
        BlockState oldState = level.getBlockState(pos);
        level.setBlock(pos, ModBlocks.DRILL_PYLON_PROXY.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof DrillPylonProxyBlockEntity proxy) {
            proxy.setControllerData(worldPosition, pos.getX() - worldPosition.getX(), pos.getY() - worldPosition.getY(), pos.getZ() - worldPosition.getZ(), role);
            proxy.setStoredState(oldState);
        }
    }

    private void convertToKineticProxy(BlockPos pos) {
        BlockState oldState = level.getBlockState(pos);
        BlockState newState = ModBlocks.DRILL_PYLON_KINETIC_PROXY.get().defaultBlockState()
                .setValue(RotatedPillarKineticBlock.AXIS, front.getClockWise().getAxis());
        level.setBlock(pos, newState, 3);
        if (level.getBlockEntity(pos) instanceof DrillPylonKineticProxyBlockEntity proxy) {
            proxy.setControllerData(worldPosition, pos.getX() - worldPosition.getX(), pos.getY() - worldPosition.getY(), pos.getZ() - worldPosition.getZ());
            proxy.setStoredState(oldState);
        }
    }

    public void disassemble() {
        if (!assembled || level == null) return;
        assembled = false;
        progress = 0;
        drilling = false;
        setAllocatedLux(0);
        if (level instanceof ServerLevel server) {
            GlareService.removeAllLinks(server, getGlareNodePos());
            GlareService.onNodeRemoved(server, getGlareNodePos());
        }

        for (int y = BODY_BOTTOM_Y; y <= BODY_TOP_Y; y++) {
            restoreFromProxy(worldPosition.above(y));
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    restoreFromProxy(worldPosition.offset(dx, y, dz));
                }
            }
        }

        for (int y = LEG_BOTTOM_Y; y <= LEG_TOP_Y; y++) {
            for (int dx : new int[]{-2, 2}) {
                for (int dz : new int[]{-2, 2}) {
                    restoreFromProxy(worldPosition.offset(dx, y, dz));
                }
            }
        }
        refreshAdjacentLuxTransceiver();
        syncData();
    }

    private void restoreFromProxy(BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DrillPylonProxyBlockEntity proxy) {
            BlockState stored = proxy.getStoredState();
            level.removeBlockEntity(pos);
            level.setBlock(pos, stored == null || stored.isAir() ? Blocks.AIR.defaultBlockState() : stored, 3);
        } else if (level.getBlockEntity(pos) instanceof DrillPylonKineticProxyBlockEntity proxy) {
            BlockState stored = proxy.getStoredState();
            level.removeBlockEntity(pos);
            level.setBlock(pos, stored == null || stored.isAir() ? Blocks.AIR.defaultBlockState() : stored, 3);
        } else if (level.getBlockState(pos).is(ModBlocks.DRILL_PYLON_PROXY.get()) || level.getBlockState(pos).is(ModBlocks.DRILL_PYLON_KINETIC_PROXY.get())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    public boolean isOutputProxySide(DrillPylonProxyBlockEntity proxy, Direction side) {
        return assembled && proxy.getRole() == DrillPylonProxyRole.OUTPUT && side == front;
    }

    public boolean isLuxSocketSide(DrillPylonProxyBlockEntity proxy, Direction side) {
        return assembled && proxy.getRole() == DrillPylonProxyRole.LUX_SOCKET && side == Direction.UP;
    }

    public ItemStackHandler getOutputInventory() {
        return outputInv;
    }

    private void setAllocatedLux(int value) {
        int next = Math.max(0, value);
        if (allocatedLux == next) return;
        allocatedLux = next;
        if (level instanceof ServerLevel server && assembled) {
            GlareService.updateNodeState(server, this);
            refreshSyncedGlareSummary(server);
        }
        syncData();
    }

    @Override
    public int getMaxGlareLinks() {
        return 1;
    }

    @Override
    public boolean allowsManualGlareLinks() {
        return false;
    }

    @Override
    public DimensionalNodePos getGlareNodePos() {
        return DimensionalNodePos.of(level, worldPosition);
    }

    @Override
    public Vec3 getGlareLinkEndpoint() {
        return Vec3.atCenterOf(getLuxSocketProxyPos());
    }

    @Override
    public void onGlareNetworkChanged(ServerLevel level, UUID networkId) {
        this.networkId = networkId;
        refreshSyncedGlareSummary(level);
        syncData();
    }

    @Override
    public void onGlareLinksChanged(ServerLevel level) {
        refreshSyncedGlareSummary(level);
        syncData();
    }

    @Override
    public int getAllocatedLux() {
        return assembled ? allocatedLux : 0;
    }

    @Override
    public GlareOperationStatus getGlareOperationStatus() {
        return operationStatus;
    }

    @Override
    public void setGlareOperationStatus(GlareOperationStatus status) {
        operationStatus = status;
        if (level instanceof ServerLevel server) {
            GlareService.updateNodeState(server, this);
            refreshSyncedGlareSummary(server);
        }
        syncData();
    }

    @Override
    public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) {
        operationStatus = status;
        if (level instanceof ServerLevel server) refreshSyncedGlareSummary(server);
        syncData();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (assembled && level instanceof ServerLevel server) {
            GlareService.onNodeLoaded(server, this);
            refreshSyncedGlareSummary(server);
            refreshAdjacentLuxTransceiver();
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("Assembled", assembled);
        tag.putString("Front", front.getName());
        tag.putInt("Progress", progress);
        tag.putBoolean("Drilling", drilling);
        tag.putInt("StandaloneDrillTimer", standaloneDrillTimer);
        tag.putInt("AllocatedLux", allocatedLux);
        tag.putString("OperationStatus", operationStatus.name());
        tag.putInt("GlareLuxCapacity", syncedLuxCapacity);
        tag.putInt("GlareLuxAllocated", syncedLuxAllocated);
        tag.putBoolean("GlareOverloaded", syncedOverloaded);
        tag.putIntArray("GlareLuxHistory", syncedLuxHistory);
        tag.put("OutputInv", outputInv.serializeNBT(registries));
        if (networkId != null) tag.putUUID("GlareNetwork", networkId);
        if (displayedRecipeId != null) tag.putString("DisplayedRecipe", displayedRecipeId.toString());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        assembled = tag.getBoolean("Assembled");
        front = Direction.byName(tag.getString("Front"));
        if (front == null || front.getAxis() == Direction.Axis.Y) front = Direction.NORTH;
        progress = tag.getInt("Progress");
        drilling = tag.getBoolean("Drilling");
        standaloneDrillTimer = tag.getInt("StandaloneDrillTimer");
        allocatedLux = tag.getInt("AllocatedLux");
        try {
            operationStatus = GlareOperationStatus.valueOf(tag.getString("OperationStatus"));
        } catch (IllegalArgumentException ignored) {
            operationStatus = GlareOperationStatus.ONLINE;
        }
        syncedLuxCapacity = tag.getInt("GlareLuxCapacity");
        syncedLuxAllocated = tag.getInt("GlareLuxAllocated");
        syncedOverloaded = tag.getBoolean("GlareOverloaded");
        syncedLuxHistory = tag.getIntArray("GlareLuxHistory");
        if (tag.contains("OutputInv")) outputInv.deserializeNBT(registries, tag.getCompound("OutputInv"));
        networkId = tag.hasUUID("GlareNetwork") ? tag.getUUID("GlareNetwork") : null;
        displayedRecipeId = tag.contains("DisplayedRecipe") ? ResourceLocation.tryParse(tag.getString("DisplayedRecipe")) : null;
    }

    private void refreshSyncedGlareSummary(ServerLevel server) {
        syncedLuxCapacity = 0;
        syncedLuxAllocated = 0;
        syncedOverloaded = false;
        syncedLuxHistory = new int[0];
        if (networkId != null) {
            GlareService.getNetwork(server, networkId).ifPresent(network -> {
                syncedLuxCapacity = network.luxCapacity;
                syncedLuxAllocated = network.luxAllocated;
                syncedOverloaded = network.overloaded;
                syncedLuxHistory = network.luxHistory.stream().mapToInt(Integer::intValue).toArray();
            });
        }
    }

    @Override
    public GlareNetworkSnapshot getSyncedGlareNetworkSnapshot() {
        return GlareNetworkSnapshot.of(networkId != null, syncedLuxAllocated, syncedLuxCapacity, syncedLuxHistory,
                syncedOverloaded ? GlareOperationStatus.OVERLOADED : operationStatus, syncedOverloaded);
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (!assembled) return super.getRenderBoundingBox();
        return new AABB(
                worldPosition.getX() - 2, worldPosition.getY() + LEG_BOTTOM_Y, worldPosition.getZ() - 2,
                worldPosition.getX() + 3, worldPosition.getY() + LEG_TOP_Y + 1, worldPosition.getZ() + 3
        );
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Drill Pylon:"));
        if (!assembled) {
            tooltip.add(Component.literal("     \u00a77Unassembled; right-click when built."));
            tooltip.add(Component.literal("     \u00a7b" + (int) (Math.abs(getSpeed()) * ServerConfig.DRILL_PYLON_HEAD_STRESS.getAsInt()) + "su \u00a78at current speed"));
            return true;
        }

        float speed = getProxySpeed();
        tooltip.add(Component.literal(speed < MIN_RPM ? "     \u00a7cRequired RPM: " + MIN_RPM : "     \u00a75Current RPM: \u00a7r" + (int) speed));
        tooltip.add(Component.literal("     \u00a7dLux: \u00a7r" + allocatedLux + " (" + operationStatus.name().toLowerCase(java.util.Locale.ROOT) + ")"));
        DrillPylonRecipe recipe = lastRecipe;
        if (recipe == null) recipe = findActiveRecipe(resolveSourceItem());
        if (recipe != null) {
            tooltip.add(Component.literal("     \u00a78" + GoggleUtilities.BuildTextProgressBar(progress, getRecipeDuration(recipe))));
        } else {
            tooltip.add(Component.literal("     \u00a7cNo recipe for fissure source."));
        }
        for (int i = 0; i < INVENTORY_SLOT_COUNT; i++) {
            ItemStack stack = outputInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                tooltip.add(Component.literal("\u00a78     -> \u00a77" + stack.getCount() + " x " + stack.getHoverName().getString()));
            }
        }
        return true;
    }

    public DrillAnimationPhase getAnimPhase() {
        return animPhase;
    }

    public float getAnimPhaseStart() {
        return animPhaseStart;
    }

    public float getAnimSpinOffset() {
        return animSpinOffset;
    }

    public void setAnimState(DrillAnimationPhase phase, float phaseStart, float spinOffset) {
        animPhase = phase;
        animPhaseStart = phaseStart;
        animSpinOffset = spinOffset;
    }

    private void syncData() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
