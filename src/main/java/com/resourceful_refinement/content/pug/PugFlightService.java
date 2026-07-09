package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PugFlightService {
    private static final int ENTITY_RECOVERY_GRACE_TICKS = 40;

    private PugFlightService() {}

    public static void tickController(LaunchpadControllerBlockEntity controller) {
        if (!(controller.getLevel() instanceof ServerLevel level)) return;
        maintainDockedPug(level, controller);
        if (controller.isAssembled() && controller.getConfiguration().mode().canSend() && controller.hasCargo()) {
            attemptLaunch(controller);
        }
        validateClaim(level, controller);
    }

    private static void maintainDockedPug(ServerLevel level, LaunchpadControllerBlockEntity controller) {
        boolean shouldBeDocked = controller.isAssembled() && controller.getConfiguration().mode().canSend()
                && controller.hasCargo();
        PugEntity docked = controller.getDockedPug();
        if (!shouldBeDocked) {
            if (docked != null) docked.discard();
            if (controller.getDockedPugId() != null) controller.clearDockedPug(null);
            return;
        }
        if (docked != null) {
            controller.resetMissingDockedPugTicks();
            return;
        }
        if (controller.getDockedPugId() != null
                && controller.incrementMissingDockedPugTicks() <= ENTITY_RECOVERY_GRACE_TICKS) return;
        controller.clearDockedPug(null);
        PugEntity pug = new PugEntity(level);
        pug.dockAt(controller);
        if (level.addFreshEntity(pug)) controller.setDockedPug(pug);
    }

    private static void validateClaim(ServerLevel level, LaunchpadControllerBlockEntity controller) {
        UUID claim = controller.getClaimedFlightId();
        if (claim == null) return;
        if (level.getEntity(claim) instanceof PugEntity pug
                && (pug.getFlightState() == PugFlightState.DESCENDING
                || pug.getFlightState() == PugFlightState.UNLOADING)) {
            controller.resetMissingClaimedPugTicks();
            return;
        }
        if (PugSavedData.get(level).getFlight(claim).isPresent()) {
            controller.resetMissingClaimedPugTicks();
            return;
        }
        if (controller.incrementMissingClaimedPugTicks() > ENTITY_RECOVERY_GRACE_TICKS) {
            controller.releaseClaim(claim);
        }
    }

    public static LaunchAttemptResult attemptLaunch(LaunchpadControllerBlockEntity controller) {
        if (!(controller.getLevel() instanceof ServerLevel level) || !controller.isAssembled()) {
            return LaunchAttemptResult.failed(LaunchpadFailureReason.INCOMPLETE_ASSEMBLY);
        }
        if (!controller.getConfiguration().mode().canSend()) {
            return LaunchAttemptResult.failed(LaunchpadFailureReason.WRONG_MODE);
        }
        if (!controller.hasCargo()) return LaunchAttemptResult.failed(LaunchpadFailureReason.NO_CARGO);
        List<SelectedCargo> selected = selectCargo(controller);
        if (selected.isEmpty()) return LaunchAttemptResult.failed(LaunchpadFailureReason.NO_CARGO);
        LaunchpadFailureReason conditionFailure = getConditionFailure(controller, selected);
        if (conditionFailure != LaunchpadFailureReason.NONE) return LaunchAttemptResult.failed(conditionFailure);
        if (!controller.hasClearSky()) {
            return LaunchAttemptResult.failed(LaunchpadFailureReason.SOURCE_SKY_OBSTRUCTED);
        }

        PugDestinationResult destinationResult = PugService.resolveDestination(level.getServer(),
                controller.getConfiguration().destinationAddress());
        if (!destinationResult.found()) {
            return LaunchAttemptResult.failed(switch (destinationResult.status()) {
                case INCOMPLETE_ADDRESS -> LaunchpadFailureReason.INCOMPLETE_DESTINATION_ADDRESS;
                case AMBIGUOUS -> LaunchpadFailureReason.DESTINATION_AMBIGUOUS;
                case SKY_OBSTRUCTED -> LaunchpadFailureReason.DESTINATION_SKY_OBSTRUCTED;
                default -> LaunchpadFailureReason.DESTINATION_MISSING;
            });
        }

        PugRouteQuote route = PugRouteCalculator.quote(level.dimension(), controller.getBlockPos(),
                destinationResult.endpoint().dimension(), destinationResult.endpoint().controllerPos(),
                PugRouteParameters.fromServerConfig());
        if (controller.fuelTank.getFluidAmount() < route.fuelCostMb()) {
            return LaunchAttemptResult.failed(LaunchpadFailureReason.INSUFFICIENT_FUEL);
        }

        maintainDockedPug(level, controller);
        PugEntity pug = controller.getDockedPug();
        if (pug == null) return LaunchAttemptResult.failed(LaunchpadFailureReason.PAD_OCCUPIED);
        for (SelectedCargo selectedCargo : selected) {
            ItemStack simulated = controller.inventory.extractItem(selectedCargo.slot(),
                    selectedCargo.stack().getCount(), true);
            if (!ItemStack.isSameItemSameComponents(simulated, selectedCargo.stack())
                    || simulated.getCount() != selectedCargo.stack().getCount()) {
                return LaunchAttemptResult.failed(LaunchpadFailureReason.NO_CARGO);
            }
        }

        List<ItemStack> cargo = selected.stream().map(SelectedCargo::stack).map(ItemStack::copy).toList();
        LaunchpadConfiguration configuration = controller.getConfiguration();
        LaunchpadEndpoint source = new LaunchpadEndpoint(level.dimension(), controller.getBlockPos(),
                configuration.localAddress(), configuration.mode());
        for (SelectedCargo selectedCargo : selected) {
            controller.inventory.extractItem(selectedCargo.slot(), selectedCargo.stack().getCount(), false);
        }
        controller.fuelTank.drain(route.fuelCostMb(), IFluidHandler.FluidAction.EXECUTE);
        controller.setRoundRobinCursor((selected.getLast().slot() + 1) % LaunchpadControllerBlockEntity.INVENTORY_SLOTS);
        controller.clearDockedPug(pug.getUUID());
        pug.beginAscent(source, destinationResult.endpoint(), route.travelTicks(), cargo);
        if (configuration.launchCondition() == LaunchCondition.REDSTONE) controller.consumePendingRedstoneLaunch();
        if (configuration.launchCondition() == LaunchCondition.TIMER) controller.resetLaunchTimer();
        level.playSound(null, controller.getBlockPos(), ModSounds.PUG_LAUNCH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return LaunchAttemptResult.launched(pug.getUUID(), route.fuelCostMb());
    }

    private static LaunchpadFailureReason getConditionFailure(LaunchpadControllerBlockEntity controller,
            List<SelectedCargo> selected) {
        return switch (controller.getConfiguration().launchCondition()) {
            case WHEN_FULL -> selected.size() == LaunchpadControllerBlockEntity.CARGO_SLOTS
                    && selected.stream().allMatch(entry -> entry.stack().getCount() == entry.stack().getMaxStackSize())
                    ? LaunchpadFailureReason.NONE : LaunchpadFailureReason.PUG_NOT_FULL;
            case REDSTONE -> controller.hasPendingRedstoneLaunch()
                    ? LaunchpadFailureReason.NONE : LaunchpadFailureReason.WAITING_FOR_REDSTONE;
            case TIMER -> controller.isTimerReady()
                    ? LaunchpadFailureReason.NONE : LaunchpadFailureReason.WAITING_FOR_TIMER;
        };
    }

    public static LaunchInspection inspectLaunch(LaunchpadControllerBlockEntity controller) {
        List<LaunchpadFailureReason> failures = new ArrayList<>();
        if (!(controller.getLevel() instanceof ServerLevel level) || !controller.isAssembled()) {
            return new LaunchInspection(List.of(LaunchpadFailureReason.INCOMPLETE_ASSEMBLY), 0);
        }
        if (!controller.getConfiguration().mode().canSend()) {
            return new LaunchInspection(List.of(LaunchpadFailureReason.WRONG_MODE), 0);
        }

        List<SelectedCargo> selected = selectCargo(controller);
        if (selected.isEmpty()) failures.add(LaunchpadFailureReason.NO_CARGO);
        else {
            LaunchpadFailureReason condition = getConditionFailure(controller, selected);
            if (condition != LaunchpadFailureReason.NONE) failures.add(condition);
        }
        if (!controller.hasClearSky()) failures.add(LaunchpadFailureReason.SOURCE_SKY_OBSTRUCTED);

        PugDestinationResult destination = PugService.resolveDestination(level.getServer(),
                controller.getConfiguration().destinationAddress());
        int fuelRequired = 0;
        if (!destination.found()) {
            failures.add(switch (destination.status()) {
                case INCOMPLETE_ADDRESS -> LaunchpadFailureReason.INCOMPLETE_DESTINATION_ADDRESS;
                case AMBIGUOUS -> LaunchpadFailureReason.DESTINATION_AMBIGUOUS;
                case SKY_OBSTRUCTED -> LaunchpadFailureReason.DESTINATION_SKY_OBSTRUCTED;
                default -> LaunchpadFailureReason.DESTINATION_MISSING;
            });
        } else {
            PugRouteQuote route = PugRouteCalculator.quote(level.dimension(), controller.getBlockPos(),
                    destination.endpoint().dimension(), destination.endpoint().controllerPos(),
                    PugRouteParameters.fromServerConfig());
            fuelRequired = route.fuelCostMb();
            if (controller.fuelTank.getFluidAmount() < fuelRequired) {
                failures.add(LaunchpadFailureReason.INSUFFICIENT_FUEL);
            }
        }
        return new LaunchInspection(List.copyOf(failures), fuelRequired);
    }

    private static List<SelectedCargo> selectCargo(LaunchpadControllerBlockEntity controller) {
        List<SelectedCargo> selected = new ArrayList<>(LaunchpadControllerBlockEntity.CARGO_SLOTS);
        int cursor = controller.getRoundRobinCursor();
        for (int offset = 0; offset < LaunchpadControllerBlockEntity.INVENTORY_SLOTS
                && selected.size() < LaunchpadControllerBlockEntity.CARGO_SLOTS; offset++) {
            int slot = (cursor + offset) % LaunchpadControllerBlockEntity.INVENTORY_SLOTS;
            ItemStack stack = controller.inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) selected.add(new SelectedCargo(slot, stack.copy()));
        }
        return List.copyOf(selected);
    }

    public static void tick(MinecraftServer server) {
        PugSavedData data = PugSavedData.get(server);
        for (PugFlightRecord flight : data.getFlights()) {
            switch (flight.state()) {
                case IN_TRANSIT -> advanceFlight(data, flight);
                case QUEUED -> tryDispatchArrival(server, data, flight);
                default -> {
                    // Physical states are authoritative entities, never simulated records.
                }
            }
        }
    }

    private static void advanceFlight(PugSavedData data, PugFlightRecord flight) {
        int elapsed = Math.min(flight.totalTravelTicks(), flight.elapsedTravelTicks() + 1);
        PugFlightState state = elapsed >= flight.totalTravelTicks()
                ? PugFlightState.QUEUED : PugFlightState.IN_TRANSIT;
        data.putFlight(flight.withProgress(elapsed, state));
    }

    private static void tryDispatchArrival(MinecraftServer server, PugSavedData data, PugFlightRecord flight) {
        LaunchpadEndpoint destination = flight.destination();
        ServerLevel level = server.getLevel(destination.dimension());
        if (level == null) return;
        PugChunkLoading.ensureControllerChunk(level, destination.controllerPos());
        level.getChunk(destination.controllerPos());

        if (level.getEntity(flight.id()) instanceof PugEntity existing) {
            if (existing.getFlightState() == PugFlightState.DESCENDING
                    || existing.getFlightState() == PugFlightState.UNLOADING
                    || existing.getFlightState() == PugFlightState.CRASHED) {
                data.removeFlight(flight.id());
            }
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(destination.controllerPos());
        if (!(blockEntity instanceof LaunchpadControllerBlockEntity controller)
                || !matchesDestination(controller, destination) || !controller.hasClearSky()) {
            if (blockEntity instanceof LaunchpadControllerBlockEntity controller) {
                PugService.syncController(controller);
            } else {
                PugService.unregister(level, destination.controllerPos());
            }
            dispatchCrash(level, data, flight);
            return;
        }
        if (!controller.tryClaim(flight.id())) return;

        PugEntity pug = new PugEntity(level);
        pug.setUUID(flight.id());
        Vec3 center = padCenter(controller);
        Vec3 spawn = new Vec3(center.x, Math.max(center.y, level.getMaxBuildHeight() - pug.getBbHeight()), center.z);
        pug.beginDescent(flight, spawn);
        if (level.addFreshEntity(pug)) {
            data.removeFlight(flight.id());
        } else {
            controller.releaseClaim(flight.id());
        }
    }

    private static void dispatchCrash(ServerLevel level, PugSavedData data, PugFlightRecord flight) {
        PugEntity pug = new PugEntity(level);
        pug.setUUID(flight.id());
        BlockPos landing = chooseCrashLanding(level, flight.destination().controllerPos());
        double spawnY = Math.max(landing.getY(), level.getMaxBuildHeight() - pug.getBbHeight());
        pug.beginCrash(flight, landing, spawnY);
        if (level.addFreshEntity(pug)) data.removeFlight(flight.id());
    }

    static BlockPos chooseCrashLanding(ServerLevel level, BlockPos intended) {
        int dx;
        int dz;
        do {
            dx = level.random.nextInt(5) - 2;
            dz = level.random.nextInt(5) - 2;
        } while ((dx == 0 && dz == 0) || dx * dx + dz * dz > 4);
        int x = intended.getX() + dx;
        int z = intended.getZ() + dz;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int landingY = Math.max(intended.getY() + 1, surfaceY);
        landingY = Math.clamp(landingY, level.getMinBuildHeight(), level.getMaxBuildHeight() - 2);
        return new BlockPos(x, landingY, z);
    }

    static boolean matchesDestination(LaunchpadControllerBlockEntity controller, LaunchpadEndpoint destination) {
        LaunchpadConfiguration configuration = controller.getConfiguration();
        return controller.isAssembled() && controller.validateAssembly() && configuration.mode().canReceive()
                && configuration.localAddress().isComplete()
                && configuration.localAddress().equals(destination.address())
                && controller.getBlockPos().equals(destination.controllerPos())
                && controller.getLevel().dimension().equals(destination.dimension());
    }

    public static Vec3 padCenter(LaunchpadControllerBlockEntity controller) {
        Direction facing = controller.getBlockState().getValue(LaunchpadControllerBlock.FACING);
        BlockPos center = LaunchpadStructure.position(controller.getBlockPos(), facing, 0, 1);
        return Vec3.atBottomCenterOf(center).add(0, 1, 0);
    }

    private record SelectedCargo(int slot, ItemStack stack) {}

    public record LaunchInspection(List<LaunchpadFailureReason> failures, int fuelRequiredMb) {
        public LaunchInspection {
            failures = List.copyOf(failures);
        }
    }
}
