package com.resourceful_refinement.content.coating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record CoatingData(CoatingType type, int integrity) {

    public static final Codec<CoatingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CoatingType.CODEC.fieldOf("type").forGetter(CoatingData::type),
            Codec.INT.fieldOf("integrity").forGetter(CoatingData::integrity)
    ).apply(instance, CoatingData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoatingData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeEnum(data.type());
                buf.writeInt(data.integrity());
            },
            buf -> new CoatingData(buf.readEnum(CoatingType.class), buf.readInt())
    );
}
