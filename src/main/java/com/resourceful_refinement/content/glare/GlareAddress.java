package com.resourceful_refinement.content.glare;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record GlareAddress(ResourceLocation first, ResourceLocation second, ResourceLocation third) {
    public static GlareAddress empty() {
        ResourceLocation air = BuiltInRegistries.ITEM.getKey(Items.AIR);
        return new GlareAddress(air, air, air);
    }

    public static GlareAddress of(Item first, Item second, Item third) {
        return new GlareAddress(BuiltInRegistries.ITEM.getKey(first), BuiltInRegistries.ITEM.getKey(second), BuiltInRegistries.ITEM.getKey(third));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("First", first.toString());
        tag.putString("Second", second.toString());
        tag.putString("Third", third.toString());
        return tag;
    }

    public static GlareAddress load(CompoundTag tag) {
        return new GlareAddress(
                parseOrAir(tag.getString("First")),
                parseOrAir(tag.getString("Second")),
                parseOrAir(tag.getString("Third"))
        );
    }

    private static ResourceLocation parseOrAir(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id == null ? BuiltInRegistries.ITEM.getKey(Items.AIR) : id;
    }
}
