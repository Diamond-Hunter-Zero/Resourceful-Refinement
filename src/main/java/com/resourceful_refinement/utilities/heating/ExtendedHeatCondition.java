package com.resourceful_refinement.utilities.heating;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;


public enum ExtendedHeatCondition implements StringRepresentable {

    CHILLED("chilled", 0xBFF2F5, -500, -750),
    COOLED("cooled", 0x1A83C9, -50, -350),
    NONE("none", 16777215, 50, 0),
    PASSIVE("passive", 0xFF86C43B, 333, 250),
    HEATED("heated", 15237888, 666, 500),
    SUPERHEATED("superheated", 6067176, 1000, 750);

    private final String name;
    private int color;
    private int maxHeatEnergy;
    private int targetHeatEnergy;
    public static final Codec<ExtendedHeatCondition> CODEC = StringRepresentable.fromEnum(ExtendedHeatCondition::values);
    public static final StreamCodec<ByteBuf, ExtendedHeatCondition> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ExtendedHeatCondition.class);

    private ExtendedHeatCondition(String name, int color, int maxHeatEnergy, int targetHeatEnergy) {
        this.name = name;
        this.color = color;
        this.maxHeatEnergy = maxHeatEnergy;
        this.targetHeatEnergy = targetHeatEnergy;
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

    public int getMaxHeatEnergy() { return this.maxHeatEnergy; }
    public int getTargetHeatEnergy() { return this.targetHeatEnergy; }
}
