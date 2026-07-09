package com.resourceful_refinement.content.glare;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record DimensionalNodePos(ResourceKey<Level> levelKey, BlockPos pos) {
    public static final Codec<DimensionalNodePos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.xmap(id -> ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id), ResourceKey::location).fieldOf("level").forGetter(DimensionalNodePos::levelKey),
            BlockPos.CODEC.fieldOf("pos").forGetter(DimensionalNodePos::pos)
    ).apply(instance, DimensionalNodePos::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionalNodePos> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeResourceLocation(value.levelKey.location());
                BlockPos.STREAM_CODEC.encode(buf, value.pos);
            },
            buf -> new DimensionalNodePos(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, buf.readResourceLocation()), BlockPos.STREAM_CODEC.decode(buf))
    );

    public static DimensionalNodePos of(Level level, BlockPos pos) {
        return new DimensionalNodePos(level.dimension(), pos.immutable());
    }

    public String toShortString() {
        return levelKey.location() + " " + pos.toShortString();
    }

    public static void writePos(CompoundTag tag, String prefix, DimensionalNodePos pos) {
        tag.putString(prefix + "Level", pos.levelKey().location().toString());
        tag.putInt(prefix + "X", pos.pos().getX());
        tag.putInt(prefix + "Y", pos.pos().getY());
        tag.putInt(prefix + "Z", pos.pos().getZ());
    }

    public static Optional<DimensionalNodePos> readPos(CompoundTag tag, String prefix) {
        if (!tag.contains(prefix + "Level")) {
            return Optional.empty();
        }
        ResourceLocation levelId = ResourceLocation.tryParse(tag.getString(prefix + "Level"));
        if (levelId == null) {
            return Optional.empty();
        }
        ResourceKey<Level> levelKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, levelId);
        return Optional.of(new DimensionalNodePos(levelKey, new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"))));
    }
}
