package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.content.glare.GlareAddress;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Compact server-authoritative state shown by an open launchpad menu. */
public record LaunchpadSnapshot(long revision, LaunchpadConfiguration configuration, boolean assembled,
        boolean skyClear, int fuelAmountMb, int fuelCapacityMb, int fuelRequiredMb,
        List<LaunchpadFailureReason> failures, UUID claimedFlightId, int landedCargoItems,
        List<InboundFlight> inboundFlights) {
    public static final int MAX_INBOUND_FLIGHTS = 64;

    public LaunchpadSnapshot {
        failures = List.copyOf(failures);
        inboundFlights = List.copyOf(inboundFlights);
    }

    public static LaunchpadSnapshot capture(LaunchpadControllerBlockEntity controller) {
        ServerLevel level = (ServerLevel) controller.getLevel();
        PugFlightService.LaunchInspection inspection = PugFlightService.inspectLaunch(controller);
        List<InboundFlight> inbound = new ArrayList<>();
        PugSavedData.get(level).getFlights().stream()
                .filter(flight -> flight.destination().dimension().equals(level.dimension())
                        && flight.destination().controllerPos().equals(controller.getBlockPos()))
                .sorted(Comparator.comparing(PugFlightRecord::id))
                .limit(MAX_INBOUND_FLIGHTS)
                .map(InboundFlight::fromRecord)
                .forEach(inbound::add);
        PugEntity claimed = controller.getClaimedPug();
        int landedItems = 0;
        if (claimed != null) {
            landedItems = claimed.copyCargo().stream().mapToInt(stack -> stack.getCount()).sum();
            if (inbound.size() < MAX_INBOUND_FLIGHTS) {
                inbound.add(new InboundFlight(claimed.getUUID(), claimed.getFlightState(), 1, 1));
            }
        }
        return new LaunchpadSnapshot(level.getGameTime(), controller.getConfiguration(), controller.isAssembled(),
                controller.isSkyClearCached(), controller.getFuelTank().getFluidAmount(),
                controller.getFuelTank().getCapacity(), inspection.fuelRequiredMb(), inspection.failures(),
                controller.getClaimedFlightId(), landedItems, inbound);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarLong(revision);
        buf.writeEnum(configuration.mode());
        configuration.localAddress().write(buf);
        configuration.destinationAddress().write(buf);
        buf.writeEnum(configuration.launchCondition());
        buf.writeVarInt(configuration.timerSeconds());
        buf.writeBoolean(assembled);
        buf.writeBoolean(skyClear);
        buf.writeVarInt(fuelAmountMb);
        buf.writeVarInt(fuelCapacityMb);
        buf.writeVarInt(fuelRequiredMb);
        buf.writeVarInt(failures.size());
        failures.forEach(buf::writeEnum);
        buf.writeBoolean(claimedFlightId != null);
        if (claimedFlightId != null) buf.writeUUID(claimedFlightId);
        buf.writeVarInt(landedCargoItems);
        buf.writeVarInt(inboundFlights.size());
        inboundFlights.forEach(flight -> flight.write(buf));
    }

    public static LaunchpadSnapshot read(RegistryFriendlyByteBuf buf) {
        long revision = buf.readVarLong();
        LaunchpadConfiguration configuration = new LaunchpadConfiguration(buf.readEnum(LaunchpadMode.class),
                GlareAddress.read(buf), GlareAddress.read(buf), buf.readEnum(LaunchCondition.class), buf.readVarInt());
        boolean assembled = buf.readBoolean();
        boolean skyClear = buf.readBoolean();
        int fuelAmount = buf.readVarInt();
        int fuelCapacity = buf.readVarInt();
        int fuelRequired = buf.readVarInt();
        int failureCount = Math.min(buf.readVarInt(), LaunchpadFailureReason.values().length);
        List<LaunchpadFailureReason> failures = new ArrayList<>(failureCount);
        for (int index = 0; index < failureCount; index++) failures.add(buf.readEnum(LaunchpadFailureReason.class));
        UUID claim = buf.readBoolean() ? buf.readUUID() : null;
        int landedCargo = buf.readVarInt();
        int inboundCount = Math.min(buf.readVarInt(), MAX_INBOUND_FLIGHTS);
        List<InboundFlight> inbound = new ArrayList<>(inboundCount);
        for (int index = 0; index < inboundCount; index++) inbound.add(InboundFlight.read(buf));
        return new LaunchpadSnapshot(revision, configuration, assembled, skyClear, fuelAmount, fuelCapacity,
                fuelRequired, failures, claim, landedCargo, inbound);
    }

    public int queuedCount() {
        return (int) inboundFlights.stream().filter(flight -> flight.state() == PugFlightState.QUEUED).count();
    }

    public record InboundFlight(UUID id, PugFlightState state, int elapsedTicks, int totalTicks) {
        static InboundFlight fromRecord(PugFlightRecord record) {
            return new InboundFlight(record.id(), record.state(), record.elapsedTravelTicks(),
                    Math.max(1, record.totalTravelTicks()));
        }

        float progress() {
            return Math.clamp((float) elapsedTicks / Math.max(1, totalTicks), 0.0F, 1.0F);
        }

        void write(RegistryFriendlyByteBuf buf) {
            buf.writeUUID(id);
            buf.writeEnum(state);
            buf.writeVarInt(elapsedTicks);
            buf.writeVarInt(totalTicks);
        }

        static InboundFlight read(RegistryFriendlyByteBuf buf) {
            return new InboundFlight(buf.readUUID(), buf.readEnum(PugFlightState.class), buf.readVarInt(),
                    Math.max(1, buf.readVarInt()));
        }
    }
}
