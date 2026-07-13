package com.resourceful_refinement.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * Static datapack-authored definition for a research node.
 * The node ID is derived from its datapack path by the reload manager; unlock and progression state live in SavedData.
 */
public record ResearchNodeDefinition(Component title, Component description, ItemStack icon,
                                     List<ResourceLocation> parents, List<ResearchReward> rewards,
                                     List<ResourceLocation> recipes, List<Item> items,
                                     ResearchRequirement requirements) {
    public static final Codec<ResearchNodeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("title").forGetter(ResearchNodeDefinition::title),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(ResearchNodeDefinition::description),
            ResearchReward.CODEC.xmap(ResearchReward::toStack, stack -> new ResearchReward(stack.getItem(), stack.getCount()))
                    .fieldOf("icon").forGetter(ResearchNodeDefinition::icon),
            ResourceLocation.CODEC.listOf().optionalFieldOf("parents", List.of()).forGetter(ResearchNodeDefinition::parents),
            ResearchReward.CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(ResearchNodeDefinition::rewards),
            ResourceLocation.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(ResearchNodeDefinition::recipes),
            BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("items", List.of())
                    .forGetter(ResearchNodeDefinition::items),
            ResearchRequirement.CODEC.optionalFieldOf("requirements", ResearchRequirement.EMPTY)
                    .forGetter(ResearchNodeDefinition::requirements)
    ).apply(instance, ResearchNodeDefinition::new));

    public ResearchNodeDefinition {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(icon, "icon");
        parents = List.copyOf(Objects.requireNonNull(parents, "parents"));
        rewards = List.copyOf(Objects.requireNonNull(rewards, "rewards"));
        recipes = List.copyOf(Objects.requireNonNull(recipes, "recipes"));
        items = List.copyOf(Objects.requireNonNull(items, "items"));
        Objects.requireNonNull(requirements, "requirements");
        icon = icon.copy();
    }

    /**
     * Recipe IDs locked by this node. Machines and player crafting checks resolve these IDs at runtime.
     */
    @Override
    public List<ResourceLocation> recipes() {
        return recipes;
    }

    /**
     * Item IDs whose producing recipes should be locked by this node.
     * The reload manager expands these to concrete recipe IDs from the server recipe manager.
     */
    @Override
    public List<Item> items() {
        return items;
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }
}
