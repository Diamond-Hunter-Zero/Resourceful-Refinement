package com.resourceful_refinement.content.glare;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record GlareTargetsData(List<GlareNodePos> targets) {
    public static final int MAX_TARGETS = 8;
    public static final GlareTargetsData EMPTY = new GlareTargetsData(List.of());
    public static final Codec<GlareTargetsData> CODEC = GlareNodePos.CODEC.listOf(0, MAX_TARGETS).xmap(GlareTargetsData::new, GlareTargetsData::targets);
    public static final StreamCodec<RegistryFriendlyByteBuf, GlareTargetsData> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeVarInt(value.targets.size());
                for (GlareNodePos target : value.targets) {
                    GlareNodePos.STREAM_CODEC.encode(buf, target);
                }
            },
            buf -> {
                int size = Math.min(buf.readVarInt(), MAX_TARGETS);
                List<GlareNodePos> targets = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    targets.add(GlareNodePos.STREAM_CODEC.decode(buf));
                }
                return new GlareTargetsData(targets);
            }
    );

    public GlareTargetsData {
        targets = List.copyOf(targets.stream().distinct().limit(MAX_TARGETS).toList());
    }

    public GlareTargetsData withAdded(GlareNodePos pos) {
        ArrayList<GlareNodePos> copy = new ArrayList<>(targets);
        copy.remove(pos);
        copy.add(pos);
        while (copy.size() > MAX_TARGETS) {
            copy.remove(0);
        }
        return new GlareTargetsData(copy);
    }

    public boolean isEmpty() {
        return targets.isEmpty();
    }
}
