package com.resourceful_refinement.content.glare;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record GlareNodePos(ResourceKey<Level> levelKey, BlockPos pos) {
    public static final Codec<GlareNodePos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.xmap(id -> ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id), ResourceKey::location).fieldOf("level").forGetter(GlareNodePos::levelKey),
            BlockPos.CODEC.fieldOf("pos").forGetter(GlareNodePos::pos)
    ).apply(instance, GlareNodePos::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GlareNodePos> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeResourceLocation(value.levelKey.location());
                BlockPos.STREAM_CODEC.encode(buf, value.pos);
            },
            buf -> new GlareNodePos(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, buf.readResourceLocation()), BlockPos.STREAM_CODEC.decode(buf))
    );

    public static GlareNodePos of(Level level, BlockPos pos) {
        return new GlareNodePos(level.dimension(), pos.immutable());
    }

    public String toShortString() {
        return levelKey.location() + " " + pos.toShortString();
    }
}
