package com.resourceful_refinement.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Objects;

/**
 * Static datapack-authored item and fluid costs required to unlock a research node.
 */
public record ResearchRequirement(List<ItemRequirement> items, List<FluidRequirement> fluids) {
    public static final int BUCKET_AMOUNT = 1000;
    public static final ResearchRequirement EMPTY = new ResearchRequirement(List.of(), List.of());

    public static final Codec<ResearchRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(ResearchRequirement::items),
            FluidRequirement.CODEC.listOf().optionalFieldOf("fluids", List.of()).forGetter(ResearchRequirement::fluids)
    ).apply(instance, ResearchRequirement::new));

    public ResearchRequirement {
        items = List.copyOf(Objects.requireNonNull(items, "items"));
        fluids = List.copyOf(Objects.requireNonNull(fluids, "fluids"));
    }

    public boolean isEmpty() {
        return items.isEmpty() && fluids.isEmpty();
    }

    public int totalRequiredUnits() {
        int total = items.stream().mapToInt(ItemRequirement::count).sum();
        total += fluids.stream().mapToInt(requirement -> requirement.amount() / BUCKET_AMOUNT).sum();
        return total;
    }

    public record ItemRequirement(Item item, int count) {
        public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemRequirement::item),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("count").forGetter(ItemRequirement::count)
        ).apply(instance, ItemRequirement::new));

        public ItemRequirement {
            Objects.requireNonNull(item, "item");
            if (item == Items.AIR) {
                throw new IllegalArgumentException("Item requirement cannot use minecraft:air");
            }
            if (count <= 0) {
                throw new IllegalArgumentException("Item requirement count must be positive");
            }
        }
    }

    public record FluidRequirement(Fluid fluid, int amount) {
        public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidRequirement::fluid),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(FluidRequirement::amount)
        ).apply(instance, FluidRequirement::new));

        public FluidRequirement {
            Objects.requireNonNull(fluid, "fluid");
            if (fluid == Fluids.EMPTY) {
                throw new IllegalArgumentException("Fluid requirement cannot use minecraft:empty");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("Fluid requirement amount must be positive");
            }
            amount = roundUpToBucket(amount);
        }

        private static int roundUpToBucket(int amount) {
            return ((amount + BUCKET_AMOUNT - 1) / BUCKET_AMOUNT) * BUCKET_AMOUNT;
        }
    }
}
