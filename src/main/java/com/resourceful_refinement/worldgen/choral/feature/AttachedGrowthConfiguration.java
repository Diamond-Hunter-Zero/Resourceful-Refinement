package com.resourceful_refinement.worldgen.choral.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

/**
 * Config for {@link AttachedGrowthFeature}. All values are datapack-tweakable.
 *
 * @param growth           the block state to place (its {@code facing} is overridden per exposed face)
 * @param attachTo         blocks the growth may attach to (a block list/tag), e.g. terrain or chorus crystal
 * @param attachDirections which support directions are allowed, i.e. the direction from the growth to the
 *                         block it attaches to. {@code [down]} = only grow on top of a block (facing up);
 *                         all six (default) = grow on any exposed face (up/down/sideways)
 * @param tries            scatter attempts per feature placement
 * @param xzSpread         horizontal radius of the scatter box around the origin
 * @param ySpread          vertical radius of the scatter box around the origin
 */
public record AttachedGrowthConfiguration(
        BlockState growth,
        HolderSet<Block> attachTo,
        List<Direction> attachDirections,
        int tries,
        int xzSpread,
        int ySpread
) implements FeatureConfiguration {
    public static final Codec<AttachedGrowthConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("growth").forGetter(AttachedGrowthConfiguration::growth),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("attach_to").forGetter(AttachedGrowthConfiguration::attachTo),
            Direction.CODEC.listOf().optionalFieldOf("attach_directions", List.of(Direction.values())).forGetter(AttachedGrowthConfiguration::attachDirections),
            Codec.intRange(1, 512).optionalFieldOf("tries", 96).forGetter(AttachedGrowthConfiguration::tries),
            Codec.intRange(0, 16).optionalFieldOf("xz_spread", 7).forGetter(AttachedGrowthConfiguration::xzSpread),
            Codec.intRange(0, 64).optionalFieldOf("y_spread", 16).forGetter(AttachedGrowthConfiguration::ySpread)
    ).apply(instance, AttachedGrowthConfiguration::new));
}
