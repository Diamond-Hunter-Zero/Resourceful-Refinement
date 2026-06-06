package com.resourceful_refinement.content.coating;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum CoatingType implements StringRepresentable {
    //LUMINITE("luminite", 0xFFFF00, 500),
    //OBSIDIAN("obsidian", 0x221133, 1000),
    OBSIDIANITE("Obsidianite", 0x9532a8, 160, "+2 armor"),
    QUICKSILVER("Quicksilver", 0xC8E3E0, 128, "Haste II"),
    DURASTEEL("Durasteel", 0x286152, 320, "+25% knockback resist"),
    LIQUIDLUCK("Liquid Luck", 0xE8BD31, 96, "Raises Fortune level"),
    UPLIFT("Uplift", 0xE065AF, 128, "Slow Fall"),
    CONDUCTION("Conduction", 0xEB9B2D, 192, "Thunderstruck?..."),
    GLOOPY("Gloopy", 0xB4CC58, 128, "Get Glooped!");

    public static final Codec<CoatingType> CODEC = StringRepresentable.fromEnum(CoatingType::values);

    private final String name;
    private final String description;
    private final int color;
    private final int maxDurability;

    CoatingType(String name, int color, int maxDurability, String desc) {
        this.name = name;
        this.color = color;
        this.maxDurability = maxDurability;
        this.description = desc;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public int getColor() {
        return this.color;
    }

    public int getMaxDurability() {
        return this.maxDurability;
    }
}
