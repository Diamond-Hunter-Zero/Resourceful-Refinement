package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class PugEntity extends Entity {
    public static final double ASCENT_INITIAL_SPEED = 0.04D;
    public static final double ASCENT_ACCELERATION = 0.018D;
    public static final double ASCENT_SPEED = 0.75D;
    public static final double DESCENT_SPEED = 0.6D;
    public static final double DESCENT_MIN_SPEED = 0.035D;
    public static final double DESCENT_ACCELERATION = 0.025D;
    public static final double HOVER_HEIGHT = 0.5D;
    public static final double HOVER_AMPLITUDE = 0.12D;
    public static final int HOVER_TICKS = 72;
    public static final int HOVER_CYCLES = 3;
    public static final int MINIMUM_LANDED_TICKS = 200;
    public static final double CRASH_DESCENT_SPEED = 1.25D;
    private static final EntityDataAccessor<Integer> FLIGHT_STATE = SynchedEntityData.defineId(PugEntity.class,
            EntityDataSerializers.INT);

    public final ItemStackHandler cargo = new ItemStackHandler(LaunchpadControllerBlockEntity.CARGO_SLOTS);
    private LaunchpadEndpoint source;
    private LaunchpadEndpoint destination;
    private int totalTravelTicks;
    private BlockPos crashLandingPos;
    private boolean crashLanded;
    private double verticalVelocity;
    private DescentStage descentStage = DescentStage.APPROACH;
    private int hoverTicks;
    private int landedTicks;
    private double clientTargetX;
    private double clientTargetY;
    private double clientTargetZ;
    private float clientTargetYRot;
    private float clientTargetXRot;
    private int clientLerpSteps;

    public PugEntity(EntityType<? extends PugEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public PugEntity(Level level) {
        this(ModEntities.PUG.get(), level);
    }

    public PugFlightState getFlightState() {
        int ordinal = entityData.get(FLIGHT_STATE);
        PugFlightState[] values = PugFlightState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : PugFlightState.DOCKED;
    }

    public void setFlightState(PugFlightState state) {
        entityData.set(FLIGHT_STATE, state.ordinal());
        boolean crashed = state == PugFlightState.CRASHED;
        noPhysics = !crashed || !crashLanded;
        setNoGravity(true);
    }

    public void dockAt(LaunchpadControllerBlockEntity controller) {
        LaunchpadConfiguration configuration = controller.getConfiguration();
        source = new LaunchpadEndpoint(controller.getLevel().dimension(), controller.getBlockPos(),
                configuration.localAddress(), configuration.mode());
        destination = null;
        totalTravelTicks = 0;
        verticalVelocity = 0;
        descentStage = DescentStage.APPROACH;
        hoverTicks = 0;
        landedTicks = 0;
        clearCargo();
        setFlightState(PugFlightState.DOCKED);
        Vec3 dock = PugFlightService.padCenter(controller);
        setPos(dock.x, dock.y, dock.z);
        setDeltaMovement(Vec3.ZERO);
        Direction padFacing = controller.getBlockState().getValue(LaunchpadControllerBlock.FACING);
        setYRot(padFacing.toYRot());

    }

    public void beginAscent(LaunchpadEndpoint source, LaunchpadEndpoint destination, int totalTravelTicks,
            List<ItemStack> stacks) {
        this.source = source;
        this.destination = destination;
        this.totalTravelTicks = Math.max(0, totalTravelTicks);
        setCargo(stacks);
        setFlightState(PugFlightState.ASCENDING);
        verticalVelocity = ASCENT_INITIAL_SPEED;
        setDeltaMovement(0, verticalVelocity, 0);
    }

    public void beginDescent(PugFlightRecord record, Vec3 spawnPosition) {
        source = record.source();
        destination = record.destination();
        totalTravelTicks = record.totalTravelTicks();
        setCargo(record.cargo());
        setFlightState(PugFlightState.DESCENDING);
        descentStage = DescentStage.APPROACH;
        hoverTicks = 0;
        landedTicks = 0;
        verticalVelocity = -DESCENT_SPEED;
        setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
        setDeltaMovement(0, verticalVelocity, 0);
    }

    public void beginCrash(PugFlightRecord record, BlockPos landingPos, double spawnY) {
        source = record.source();
        destination = record.destination();
        totalTravelTicks = record.totalTravelTicks();
        setCargo(record.cargo());
        beginCrash(landingPos, spawnY);
    }

    private void beginCrash(BlockPos landingPos, double spawnY) {
        crashLandingPos = landingPos.immutable();
        crashLanded = false;
        verticalVelocity = -CRASH_DESCENT_SPEED;
        setFlightState(PugFlightState.CRASHED);
        setPos(landingPos.getX() + 0.5D, Math.max(spawnY, landingPos.getY()), landingPos.getZ() + 0.5D);
        setDeltaMovement(0, verticalVelocity, 0);
    }

    public LaunchpadEndpoint getSourceEndpoint() {
        return source;
    }

    public LaunchpadEndpoint getDestinationEndpoint() {
        return destination;
    }

    public BlockPos getCrashLandingPos() {
        return crashLandingPos;
    }

    public boolean hasCrashLanded() {
        return crashLanded;
    }

    public double getVerticalVelocity() {
        return verticalVelocity;
    }

    public boolean isHovering() {
        return getFlightState() == PugFlightState.DESCENDING && descentStage == DescentStage.HOVER;
    }

    public int getHoverTicks() {
        return hoverTicks;
    }

    public int getLandedTicks() {
        return landedTicks;
    }

    public boolean hasCargo() {
        for (int slot = 0; slot < cargo.getSlots(); slot++) {
            if (!cargo.getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    public List<ItemStack> copyCargo() {
        java.util.ArrayList<ItemStack> stacks = new java.util.ArrayList<>();
        for (int slot = 0; slot < cargo.getSlots(); slot++) {
            ItemStack stack = cargo.getStackInSlot(slot);
            if (!stack.isEmpty()) stacks.add(stack.copy());
        }
        return List.copyOf(stacks);
    }

    private void setCargo(List<ItemStack> stacks) {
        clearCargo();
        for (int slot = 0; slot < stacks.size() && slot < cargo.getSlots(); slot++) {
            cargo.setStackInSlot(slot, stacks.get(slot).copy());
        }
    }

    private void clearCargo() {
        for (int slot = 0; slot < cargo.getSlots(); slot++) cargo.setStackInSlot(slot, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            tickClientInterpolation();
            return;
        }
        if (!(level() instanceof ServerLevel level)) return;
        switch (getFlightState()) {
            case DOCKED -> tickDocked(level);
            case ASCENDING -> tickAscending(level);
            case DESCENDING -> tickDescending(level);
            case UNLOADING -> tickUnloading(level);
            case CRASHED -> tickCrashed();
            case IN_TRANSIT, QUEUED -> discard();
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        if (!level().isClientSide) {
            super.lerpTo(x, y, z, yRot, xRot, steps);
            return;
        }
        clientTargetX = x;
        clientTargetY = y;
        clientTargetZ = z;
        clientTargetYRot = yRot;
        clientTargetXRot = xRot;
        clientLerpSteps = Math.max(3, steps);
    }

    private void tickClientInterpolation() {
        if (clientLerpSteps <= 0) return;
        double amount = 1.0D / clientLerpSteps;
        setPos(Mth.lerp(amount, getX(), clientTargetX), Mth.lerp(amount, getY(), clientTargetY),
                Mth.lerp(amount, getZ(), clientTargetZ));
        setYRot((float) Mth.rotLerp(amount, getYRot(), clientTargetYRot));
        setXRot((float) Mth.lerp(amount, getXRot(), clientTargetXRot));
        clientLerpSteps--;
    }

    private void tickDocked(ServerLevel level) {
        LaunchpadControllerBlockEntity controller = getController(level, source);
        if (controller == null || !controller.isAssembled() || !controller.getConfiguration().mode().canSend()) {
            discard();
            return;
        }
        Vec3 dock = PugFlightService.padCenter(controller);
        setPos(dock.x, dock.y, dock.z);
        setDeltaMovement(Vec3.ZERO);
    }

    private void tickAscending(ServerLevel level) {
        if (PugSavedData.get(level).getFlight(getUUID()).isPresent()) {
            discard();
            return;
        }
        if (source == null || destination == null || !hasCargo()) {
            setFlightState(PugFlightState.CRASHED);
            return;
        }
        double ceiling = level.getMaxBuildHeight() - getBbHeight();
        verticalVelocity = Math.min(ASCENT_SPEED, verticalVelocity + ASCENT_ACCELERATION);
        double nextY = Math.min(ceiling, getY() + verticalVelocity);
        setPos(getX(), nextY, getZ());
        setDeltaMovement(0, verticalVelocity, 0);
        if (nextY >= ceiling) {
            PugSavedData.get(level).putFlight(new PugFlightRecord(getUUID(), copyCargo(), source, destination,
                    totalTravelTicks, 0, PugFlightState.IN_TRANSIT));
            discard();
        }
    }

    private void tickDescending(ServerLevel level) {
        LaunchpadControllerBlockEntity controller = getController(level, destination);
        if (controller == null || !PugFlightService.matchesDestination(controller, destination)) {
            if (controller != null) controller.releaseClaim(getUUID());
            beginCrash(PugFlightService.chooseCrashLanding(level, destination.controllerPos()), getY());
            return;
        }
        Vec3 landing = PugFlightService.padCenter(controller);
        switch (descentStage) {
            case APPROACH -> tickDescentApproach(landing);
            case HOVER -> tickDescentHover(controller, level, landing);
            case FINAL_DROP -> tickFinalDrop(controller, level, landing);
        }
    }

    private void tickDescentApproach(Vec3 landing) {
        double hoverY = landing.y + HOVER_HEIGHT;
        double distance = Math.max(0, getY() - hoverY);
        if (distance <= 0.001D) {
            descentStage = DescentStage.HOVER;
            hoverTicks = 0;
            verticalVelocity = 0;
            setPos(landing.x, hoverY, landing.z);
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        double desiredSpeed = Mth.clamp(distance * 0.07D, DESCENT_MIN_SPEED, DESCENT_SPEED);
        double currentSpeed = Math.abs(verticalVelocity);
        double nextSpeed = currentSpeed < desiredSpeed
                ? Math.min(desiredSpeed, currentSpeed + DESCENT_ACCELERATION)
                : Math.max(desiredSpeed, currentSpeed - DESCENT_ACCELERATION);
        verticalVelocity = -nextSpeed;
        double nextY = Math.max(hoverY, getY() + verticalVelocity);
        setPos(landing.x, nextY, landing.z);
        setDeltaMovement(0, verticalVelocity, 0);
        if (nextY <= hoverY) {
            descentStage = DescentStage.HOVER;
            hoverTicks = 0;
            verticalVelocity = 0;
            setDeltaMovement(Vec3.ZERO);
        }
    }

    private void tickDescentHover(LaunchpadControllerBlockEntity controller, ServerLevel level, Vec3 landing) {
        hoverTicks++;
        double angle = Math.PI * 2.0D * HOVER_CYCLES * hoverTicks / HOVER_TICKS;
        double y = landing.y + HOVER_HEIGHT + Math.sin(angle) * HOVER_AMPLITUDE;
        setPos(landing.x, y, landing.z);
        setDeltaMovement(Vec3.ZERO);
        if (hoverTicks >= HOVER_TICKS) {
            if (!controller.hasClearSky()) {
                controller.releaseClaim(getUUID());
                beginCrash(PugFlightService.chooseCrashLanding(level, destination.controllerPos()), getY());
                return;
            }
            descentStage = DescentStage.FINAL_DROP;
            verticalVelocity = -0.04D;
            setPos(landing.x, landing.y + HOVER_HEIGHT, landing.z);
        }
    }

    private void tickFinalDrop(LaunchpadControllerBlockEntity controller, ServerLevel level, Vec3 landing) {
        if (!controller.hasClearSky()) {
            controller.releaseClaim(getUUID());
            beginCrash(PugFlightService.chooseCrashLanding(level, destination.controllerPos()), getY());
            return;
        }
        verticalVelocity = Math.max(-0.18D, verticalVelocity - 0.012D);
        double nextY = Math.max(landing.y, getY() + verticalVelocity);
        setPos(landing.x, nextY, landing.z);
        setDeltaMovement(0, verticalVelocity, 0);
        if (nextY <= landing.y) {
            setPos(landing.x, landing.y, landing.z);
            setDeltaMovement(Vec3.ZERO);
            verticalVelocity = 0;
            landedTicks = 0;
            setFlightState(PugFlightState.UNLOADING);
        }
    }

    private void tickUnloading(ServerLevel level) {
        LaunchpadControllerBlockEntity controller = getController(level, destination);
        if (controller == null || !PugFlightService.matchesDestination(controller, destination)) {
            if (controller != null) controller.releaseClaim(getUUID());
            beginCrash(PugFlightService.chooseCrashLanding(level, destination.controllerPos()), getY());
            return;
        }
        Vec3 landing = PugFlightService.padCenter(controller);
        setPos(landing.x, landing.y, landing.z);
        setDeltaMovement(Vec3.ZERO);
        landedTicks++;
        for (int cargoSlot = 0; cargoSlot < cargo.getSlots(); cargoSlot++) {
            ItemStack remainder = cargo.getStackInSlot(cargoSlot).copy();
            for (int padSlot = 0; padSlot < controller.inventory.getSlots() && !remainder.isEmpty(); padSlot++) {
                remainder = controller.inventory.insertItem(padSlot, remainder, false);
            }
            cargo.setStackInSlot(cargoSlot, remainder);
        }
        if (!hasCargo() && landedTicks >= MINIMUM_LANDED_TICKS) {
            controller.releaseClaim(getUUID());
            discard();
        }
    }

    private void tickCrashed() {
        if (!hasCargo()) {
            discard();
            return;
        }
        if (crashLandingPos == null) {
            crashLandingPos = blockPosition();
            crashLanded = true;
        }
        if (crashLanded) {
            noPhysics = false;
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        double nextY = Math.max(crashLandingPos.getY(), getY() - CRASH_DESCENT_SPEED);
        setPos(crashLandingPos.getX() + 0.5D, nextY, crashLandingPos.getZ() + 0.5D);
        setDeltaMovement(0, -CRASH_DESCENT_SPEED, 0);
        if (nextY <= crashLandingPos.getY()) {
            crashLanded = true;
            noPhysics = false;
            setDeltaMovement(Vec3.ZERO);
        }
    }

    private static LaunchpadControllerBlockEntity getController(ServerLevel level, LaunchpadEndpoint endpoint) {
        if (endpoint == null || !endpoint.dimension().equals(level.dimension())) return null;
        return level.getBlockEntity(endpoint.controllerPos()) instanceof LaunchpadControllerBlockEntity controller
                ? controller : null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FLIGHT_STATE, PugFlightState.DOCKED.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        int ordinal = tag.getInt("FlightState");
        setFlightState(ordinal >= 0 && ordinal < PugFlightState.values().length
                ? PugFlightState.values()[ordinal] : PugFlightState.DOCKED);
        source = PugFlightRecord.loadEndpoint(tag.getCompound("Source")).orElse(null);
        destination = PugFlightRecord.loadEndpoint(tag.getCompound("Destination")).orElse(null);
        totalTravelTicks = Math.max(0, tag.getInt("TotalTravelTicks"));
        verticalVelocity = tag.contains("VerticalVelocity") ? tag.getDouble("VerticalVelocity")
                : switch (getFlightState()) {
                    case ASCENDING -> ASCENT_INITIAL_SPEED;
                    case DESCENDING -> -DESCENT_SPEED;
                    default -> 0.0D;
                };
        try {
            descentStage = DescentStage.valueOf(tag.getString("DescentStage"));
        } catch (IllegalArgumentException ignored) {
            descentStage = DescentStage.APPROACH;
        }
        hoverTicks = Math.clamp(tag.getInt("HoverTicks"), 0, HOVER_TICKS);
        landedTicks = Math.clamp(tag.getInt("LandedTicks"), 0, MINIMUM_LANDED_TICKS);
        crashLandingPos = tag.contains("CrashLandingPos")
                ? NbtUtils.readBlockPos(tag, "CrashLandingPos").orElse(null) : null;
        crashLanded = tag.getBoolean("CrashLanded");
        if (getFlightState() == PugFlightState.CRASHED) {
            noPhysics = !crashLanded;
            setNoGravity(true);
        }
        HolderLookup.Provider registries = level().registryAccess();
        if (tag.contains("Cargo")) cargo.deserializeNBT(registries, tag.getCompound("Cargo"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("FlightState", getFlightState().ordinal());
        if (source != null) tag.put("Source", PugFlightRecord.saveEndpoint(source));
        if (destination != null) tag.put("Destination", PugFlightRecord.saveEndpoint(destination));
        tag.putInt("TotalTravelTicks", totalTravelTicks);
        tag.putDouble("VerticalVelocity", verticalVelocity);
        tag.putString("DescentStage", descentStage.name());
        tag.putInt("HoverTicks", hoverTicks);
        tag.putInt("LandedTicks", landedTicks);
        if (crashLandingPos != null) tag.put("CrashLandingPos", NbtUtils.writeBlockPos(crashLandingPos));
        tag.putBoolean("CrashLanded", crashLanded);
        tag.put("Cargo", cargo.serializeNBT(level().registryAccess()));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (getFlightState() != PugFlightState.CRASHED) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        boolean movedAny = false;
        for (int slot = 0; slot < cargo.getSlots(); slot++) {
            ItemStack stored = cargo.getStackInSlot(slot);
            if (stored.isEmpty()) continue;
            ItemStack remainder = stored.copy();
            int before = remainder.getCount();
            player.getInventory().add(remainder);
            movedAny |= remainder.getCount() < before;
            cargo.setStackInSlot(slot, remainder);
        }
        if (!hasCargo()) discard();
        return movedAny ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getFlightState() != PugFlightState.CRASHED || isRemoved() || isInvulnerableTo(source)) return false;
        if (!level().isClientSide) {
            for (int slot = 0; slot < cargo.getSlots(); slot++) {
                ItemStack stack = cargo.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level(), getX(), getY(), getZ(), stack.copy());
                    cargo.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
            discard();
        }
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return getFlightState() != PugFlightState.CRASHED || super.isInvulnerableTo(source);
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return getFlightState() == PugFlightState.CRASHED;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    private enum DescentStage {
        APPROACH,
        HOVER,
        FINAL_DROP
    }
}
