package com.resourceful_refinement.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Static datapack-authored item reward for a research node.
 * Runtime unlock state is stored separately by the server research SavedData.
 */
public record ResearchReward(Item item, int count) {
    public static final int MAX_COUNT = 64;

    public static final Codec<ResearchReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ResearchReward::item),
            Codec.intRange(1, MAX_COUNT).optionalFieldOf("count", 1).forGetter(ResearchReward::count)
    ).apply(instance, ResearchReward::new));

    public ResearchReward {
        Objects.requireNonNull(item, "item");
        if (count < 1 || count > MAX_COUNT) {
            throw new IllegalArgumentException("Research reward count must be between 1 and " + MAX_COUNT);
        }
    }

    public ItemStack toStack() {
        return new ItemStack(item, count);
    }
}
