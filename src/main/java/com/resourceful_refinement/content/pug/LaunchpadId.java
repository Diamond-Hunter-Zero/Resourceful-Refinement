package com.resourceful_refinement.content.pug;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

public record LaunchpadId(ResourceKey<Level> dimension, BlockPos controllerPos) {
    public LaunchpadId {
        dimension = Objects.requireNonNull(dimension, "dimension");
        controllerPos = Objects.requireNonNull(controllerPos, "controllerPos").immutable();
    }

    public static LaunchpadId of(Level level, BlockPos controllerPos) {
        return new LaunchpadId(level.dimension(), controllerPos);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", dimension.location().toString());
        tag.put("ControllerPos", NbtUtils.writeBlockPos(controllerPos));
        return tag;
    }

    public static Optional<LaunchpadId> load(CompoundTag tag) {
        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("Dimension"));
        Optional<BlockPos> pos = NbtUtils.readBlockPos(tag, "ControllerPos");
        if (dimensionId == null || pos.isEmpty()) return Optional.empty();
        return Optional.of(new LaunchpadId(ResourceKey.create(Registries.DIMENSION, dimensionId), pos.get()));
    }
}
