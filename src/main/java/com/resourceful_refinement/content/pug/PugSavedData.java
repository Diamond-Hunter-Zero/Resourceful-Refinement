package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.content.glare.GlareAddress;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Global, overworld-backed authority for assembled launchpad endpoints. */
public final class PugSavedData extends SavedData {
    private static final String DATA_NAME = "resourceful_refinement_pug_system";

    private final Map<LaunchpadId, PadRecord> pads = new HashMap<>();
    private final Map<GlareAddress, LinkedHashSet<LaunchpadId>> receiversByAddress = new HashMap<>();
    private final Map<UUID, PugFlightRecord> flights = new HashMap<>();

    public static SavedData.Factory<PugSavedData> factory() {
        return new SavedData.Factory<>(PugSavedData::new, PugSavedData::load);
    }

    public static PugSavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.dimension().equals(Level.OVERWORLD)
                ? level : level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static PugSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    private static PugSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PugSavedData data = new PugSavedData();
        ListTag entries = tag.getList("Pads", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            PadRecord.load(entries.getCompound(index)).ifPresent(record -> data.pads.put(record.id(), record));
        }
        ListTag flightEntries = tag.getList("Flights", Tag.TAG_COMPOUND);
        for (int index = 0; index < flightEntries.size(); index++) {
            PugFlightRecord.load(flightEntries.getCompound(index), registries)
                    .ifPresent(record -> data.flights.put(record.id(), record));
        }
        data.rebuildIndexes();
        return data;
    }

    static PugSavedData loadForTests(CompoundTag tag, HolderLookup.Provider registries) {
        return load(tag, registries);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        pads.values().stream()
                .sorted((first, second) -> first.id().toString().compareTo(second.id().toString()))
                .map(PadRecord::save)
                .forEach(entries::add);
        tag.put("Pads", entries);
        ListTag flightEntries = new ListTag();
        flights.values().stream().sorted(java.util.Comparator.comparing(PugFlightRecord::id))
                .map(record -> record.save(registries)).forEach(flightEntries::add);
        tag.put("Flights", flightEntries);
        return tag;
    }

    public boolean upsert(PadRecord record) {
        PadRecord previous = pads.put(record.id(), record);
        if (record.equals(previous)) return false;
        if (previous != null) unindex(previous);
        index(record);
        setDirty();
        return true;
    }

    public Optional<PadRecord> remove(LaunchpadId id) {
        PadRecord removed = pads.remove(id);
        if (removed != null) {
            unindex(removed);
            setDirty();
        }
        return Optional.ofNullable(removed);
    }

    public Optional<PadRecord> getPad(LaunchpadId id) {
        return Optional.ofNullable(pads.get(id));
    }

    public boolean contains(LaunchpadId id) {
        return pads.containsKey(id);
    }

    public Collection<PadRecord> getPads() {
        return List.copyOf(pads.values());
    }

    public List<PadRecord> getPadsInDimension(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
        return pads.values().stream().filter(record -> record.id().dimension().equals(dimension)).toList();
    }

    public List<PadRecord> findReceivers(GlareAddress address) {
        LinkedHashSet<LaunchpadId> ids = receiversByAddress.get(address);
        if (ids == null) return List.of();
        List<PadRecord> records = new ArrayList<>(ids.size());
        for (LaunchpadId id : ids) {
            PadRecord record = pads.get(id);
            if (record != null) records.add(record);
        }
        return List.copyOf(records);
    }

    public boolean putFlight(PugFlightRecord record) {
        PugFlightRecord previous = flights.put(record.id(), record);
        if (record.equals(previous)) return false;
        setDirty();
        return true;
    }

    public Optional<PugFlightRecord> getFlight(UUID id) {
        return Optional.ofNullable(flights.get(id));
    }

    public Collection<PugFlightRecord> getFlights() {
        return List.copyOf(flights.values());
    }

    public Optional<PugFlightRecord> removeFlight(UUID id) {
        PugFlightRecord removed = flights.remove(id);
        if (removed != null) setDirty();
        return Optional.ofNullable(removed);
    }

    private void rebuildIndexes() {
        receiversByAddress.clear();
        pads.values().forEach(this::index);
    }

    private void index(PadRecord record) {
        if (record.mode().canReceive() && record.address().isComplete()) {
            receiversByAddress.computeIfAbsent(record.address(), ignored -> new LinkedHashSet<>()).add(record.id());
        }
    }

    private void unindex(PadRecord record) {
        if (!record.mode().canReceive() || !record.address().isComplete()) return;
        LinkedHashSet<LaunchpadId> ids = receiversByAddress.get(record.address());
        if (ids == null) return;
        ids.remove(record.id());
        if (ids.isEmpty()) receiversByAddress.remove(record.address());
    }

    public record PadRecord(LaunchpadId id, LaunchpadMode mode, GlareAddress address, boolean skyClear) {
        public PadRecord {
            java.util.Objects.requireNonNull(id, "id");
            java.util.Objects.requireNonNull(mode, "mode");
            java.util.Objects.requireNonNull(address, "address");
        }

        public LaunchpadEndpoint endpoint() {
            return new LaunchpadEndpoint(id.dimension(), id.controllerPos(), address, mode);
        }

        CompoundTag save() {
            CompoundTag tag = id.save();
            tag.putString("Mode", mode.name());
            tag.put("Address", address.save());
            tag.putBoolean("SkyClear", skyClear);
            return tag;
        }

        static Optional<PadRecord> load(CompoundTag tag) {
            Optional<LaunchpadId> id = LaunchpadId.load(tag);
            if (id.isEmpty()) return Optional.empty();
            LaunchpadMode mode;
            try {
                mode = LaunchpadMode.valueOf(tag.getString("Mode"));
            } catch (IllegalArgumentException ignored) {
                mode = LaunchpadMode.SEND;
            }
            GlareAddress address = tag.contains("Address") ? GlareAddress.load(tag.getCompound("Address"))
                    : GlareAddress.empty();
            return Optional.of(new PadRecord(id.get(), mode, address, tag.getBoolean("SkyClear")));
        }
    }
}
