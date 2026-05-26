package com.resourceful_refinement.content.gel_splatter;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

public class GelPropertiesManager {

    private static final Map<Fluid, GelType> FLUID_TO_GEL = new HashMap<>();
    public static final int GEL_AMMO_COST = 5;
    public static final int POTION_AMMO_COST = 200;

    static {
        FLUID_TO_GEL.put(Fluids.WATER, GelType.CLEANSE);
        FLUID_TO_GEL.put(Fluids.FLOWING_WATER, GelType.CLEANSE);
    }

    /**
     * Rebuilds gel-type lookups from loaded fluid tags. Call on tag reload so mod fluids
     * resolve correctly (including flowing variants, which are not always in tag JSON).
     */
    public static void rebuildFromTags() {
        FLUID_TO_GEL.put(Fluids.WATER, GelType.CLEANSE);
        FLUID_TO_GEL.put(Fluids.FLOWING_WATER, GelType.CLEANSE);

        for (GelType type : GelType.values()) {
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, type.getTagLocation());
            BuiltInRegistries.FLUID.getTag(tagKey).ifPresent(set -> {
                for (Holder<Fluid> holder : set) {
                    mapFluidAndFlowingVariant(holder.value(), type);
                }
            });
        }
    }

    public static void onTagsUpdated(TagsUpdatedEvent event) {
        rebuildFromTags();
    }

    /**
     * Resolves the GelType for a given Fluid by checking hardcoded mappings, cached tag lookups,
     * then live tag queries. Defaults to GelType.INERT if no specific matches are found.
     */
    public static GelType getGelType(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return GelType.INERT;
        }

        Fluid resolved = resolveForLookup(fluid);

        GelType cached = FLUID_TO_GEL.get(resolved);
        if (cached != null) {
            return cached;
        }
        cached = FLUID_TO_GEL.get(fluid);
        if (cached != null) {
            return cached;
        }

        for (GelType type : GelType.values()) {
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, type.getTagLocation());
            if (resolved.is(tagKey)) {
                return type;
            }
        }

        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(resolved);
        if (fluidId != null && fluidId.getPath().contains("potion")) {
            return GelType.POTION;
        }

        return GelType.INERT;
    }

    public static int getGelAmmoCost(Fluid fluid)
    {
        if (getGelType(fluid) == GelType.POTION)
            return POTION_AMMO_COST;
        return GEL_AMMO_COST;
    }

    public static Fluid resolveSourceFluid(Fluid fluid) {
        return resolveForLookup(fluid);
    }

    private static Fluid resolveForLookup(Fluid fluid) {
        if (fluid instanceof FlowingFluid flowing) {
            Fluid source = flowing.getSource();
            if (source != null && source != Fluids.EMPTY) {
                return source;
            }
        }
        return fluid;
    }

    /** First gel type wins (enum order), matching prior tag-iteration behaviour. */
    private static void mapFluidAndFlowingVariant(Fluid fluid, GelType type) {
        if (!FLUID_TO_GEL.containsKey(fluid)) {
            FLUID_TO_GEL.put(fluid, type);
        }

        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id == null) {
            return;
        }

        if (id.getPath().startsWith("flowing_")) {
            ResourceLocation sourceId = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(), id.getPath().substring("flowing_".length()));
            BuiltInRegistries.FLUID.getHolder(sourceId).ifPresent(holder ->
                    FLUID_TO_GEL.putIfAbsent(holder.value(), type));
        } else {
            ResourceLocation flowingId = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(), "flowing_" + id.getPath());
            BuiltInRegistries.FLUID.getHolder(flowingId).ifPresent(holder ->
                    FLUID_TO_GEL.putIfAbsent(holder.value(), type));
        }
    }
}
