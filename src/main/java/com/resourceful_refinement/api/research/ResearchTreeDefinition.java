package com.resourceful_refinement.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.Item;

import java.util.Objects;

/**
 * Static datapack-authored metadata for a research tree folder.
 * This is descriptive content only; it is not a research node and has no unlock state.
 */
public record ResearchTreeDefinition(Component name, Component description, Item icon) {
    public static final Codec<ResearchTreeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ResearchTreeDefinition::name),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(ResearchTreeDefinition::description),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(ResearchTreeDefinition::icon)
    ).apply(instance, ResearchTreeDefinition::new));

    public ResearchTreeDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(icon, "icon");
    }
}
