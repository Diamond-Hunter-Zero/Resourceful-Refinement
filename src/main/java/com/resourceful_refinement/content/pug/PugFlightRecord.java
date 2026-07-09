package com.resourceful_refinement.content.pug;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Optional;

/** Immutable persistent-flight contract. NBT persistence is added with the global PUG service in Phase 3. */
public record PugFlightRecord(UUID id, List<ItemStack> cargo, LaunchpadEndpoint source,
        LaunchpadEndpoint destination, int totalTravelTicks, int elapsedTravelTicks, PugFlightState state) {
    public PugFlightRecord {
        id = Objects.requireNonNull(id, "id");
        cargo = List.copyOf(Objects.requireNonNull(cargo, "cargo").stream().map(ItemStack::copy).toList());
        source = Objects.requireNonNull(source, "source");
        destination = Objects.requireNonNull(destination, "destination");
        state = Objects.requireNonNull(state, "state");
        if (cargo.size() > 6 || totalTravelTicks < 0 || elapsedTravelTicks < 0
                || elapsedTravelTicks > totalTravelTicks) {
            throw new IllegalArgumentException("Invalid PUG flight record");
        }
    }

    @Override
    public List<ItemStack> cargo() {
        return cargo.stream().map(ItemStack::copy).toList();
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.put("Source", saveEndpoint(source));
        tag.put("Destination", saveEndpoint(destination));
        tag.putInt("TotalTravelTicks", totalTravelTicks);
        tag.putInt("ElapsedTravelTicks", elapsedTravelTicks);
        tag.putString("State", state.name());
        ListTag cargoTag = new ListTag();
        for (ItemStack stack : cargo) cargoTag.add(stack.saveOptional(registries));
        tag.put("Cargo", cargoTag);
        return tag;
    }

    public static Optional<PugFlightRecord> load(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.hasUUID("Id")) return Optional.empty();
        Optional<LaunchpadEndpoint> source = loadEndpoint(tag.getCompound("Source"));
        Optional<LaunchpadEndpoint> destination = loadEndpoint(tag.getCompound("Destination"));
        if (source.isEmpty() || destination.isEmpty()) return Optional.empty();
        PugFlightState state;
        try {
            state = PugFlightState.valueOf(tag.getString("State"));
        } catch (IllegalArgumentException ignored) {
            state = PugFlightState.IN_TRANSIT;
        }
        List<ItemStack> cargo = new java.util.ArrayList<>(CARGO_LIMIT);
        ListTag cargoTag = tag.getList("Cargo", Tag.TAG_COMPOUND);
        for (int index = 0; index < cargoTag.size() && cargo.size() < CARGO_LIMIT; index++) {
            ItemStack stack = ItemStack.parseOptional(registries, cargoTag.getCompound(index));
            if (!stack.isEmpty()) cargo.add(stack);
        }
        int total = Math.max(0, tag.getInt("TotalTravelTicks"));
        int elapsed = Math.clamp(tag.getInt("ElapsedTravelTicks"), 0, total);
        return Optional.of(new PugFlightRecord(tag.getUUID("Id"), cargo, source.get(), destination.get(), total,
                elapsed, state));
    }

    private static final int CARGO_LIMIT = 6;

    static CompoundTag saveEndpoint(LaunchpadEndpoint endpoint) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", endpoint.dimension().location().toString());
        tag.put("ControllerPos", NbtUtils.writeBlockPos(endpoint.controllerPos()));
        tag.put("Address", endpoint.address().save());
        tag.putString("Mode", endpoint.mode().name());
        return tag;
    }

    static Optional<LaunchpadEndpoint> loadEndpoint(CompoundTag tag) {
        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("Dimension"));
        Optional<BlockPos> pos = NbtUtils.readBlockPos(tag, "ControllerPos");
        if (dimensionId == null || pos.isEmpty()) return Optional.empty();
        LaunchpadMode mode;
        try {
            mode = LaunchpadMode.valueOf(tag.getString("Mode"));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
        return Optional.of(new LaunchpadEndpoint(ResourceKey.create(Registries.DIMENSION, dimensionId), pos.get(),
                tag.contains("Address") ? com.resourceful_refinement.content.glare.GlareAddress.load(
                        tag.getCompound("Address")) : com.resourceful_refinement.content.glare.GlareAddress.empty(),
                mode));
    }

    public PugFlightRecord withProgress(int elapsedTicks, PugFlightState state) {
        return new PugFlightRecord(id, cargo, source, destination, totalTravelTicks,
                Math.clamp(elapsedTicks, 0, totalTravelTicks), state);
    }
}
