package com.resourceful_refinement.utilities.heating;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;


public enum ExtendedHeatCondition implements StringRepresentable {

    CHILLED("chilled", 0xBFF2F5),
    COOLED("cooled", 0x1A83C9),
    NONE("none", 16777215),
    PASSIVE("passive", 0xFF86C43B),
    HEATED("heated", 15237888),
    SUPERHEATED("superheated", 6067176);

    private final String name;
    private int color;
    public static final Codec<ExtendedHeatCondition> CODEC = StringRepresentable.fromEnum(ExtendedHeatCondition::values);
    public static final StreamCodec<ByteBuf, ExtendedHeatCondition> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ExtendedHeatCondition.class);

    private ExtendedHeatCondition(String name, int color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public String getTranslationKey() {
        return "recipe.heat_requirement." + this.getSerializedName();
    }

    public int getColor() {
        return this.color;
    }
}
