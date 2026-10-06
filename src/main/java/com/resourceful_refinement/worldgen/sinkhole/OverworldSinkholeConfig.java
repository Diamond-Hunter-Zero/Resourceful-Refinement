package com.resourceful_refinement.worldgen.sinkhole;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.worldgen.crystalnode.CrystalNodeConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Datapack-tweakable shape and contents of the Overworld Crystal Fissure sinkhole.
 *
 * @param radius              horizontal radius of the bowl (~10 → ~20 diameter)
 * @param minDepth            shallowest possible centre depth
 * @param maxDepth            deepest possible centre depth (actual is rolled per placement)
 * @param crystalBlocks       weighted mix of crystal floor blocks (terran crystal + veridium)
 * @param stoneBlock          the natural stone the crystal is interspersed with
 * @param crystalCenterChance crystal density at the bowl centre (1 = all crystal)
 * @param crystalFalloff      how fast crystal density falls toward the rim (higher = sparser sides)
 * @param growthBlock         the growth scattered through the bowl (terran crystal growth)
 * @param growthChance        per-floor-block chance to place a growth
 * @param budBlock            the centre node (crystal fissure bud)
 * @param sourceItem          the bud's stored source (terran crystal)
 */
public record OverworldSinkholeConfig(
        int radius,
        int minDepth,
        int maxDepth,
        List<CrystalNodeConfig.WeightedBlock> crystalBlocks,
        BlockState stoneBlock,
        float crystalCenterChance,
        float crystalFalloff,
        BlockState growthBlock,
        float growthChance,
        BlockState budBlock,
        Optional<Item> sourceItem
) {
    public static final MapCodec<OverworldSinkholeConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(2, 32).optionalFieldOf("radius", 10).forGetter(OverworldSinkholeConfig::radius),
            Codec.intRange(1, 32).optionalFieldOf("min_depth", 3).forGetter(OverworldSinkholeConfig::minDepth),
            Codec.intRange(1, 32).optionalFieldOf("max_depth", 8).forGetter(OverworldSinkholeConfig::maxDepth),
            CrystalNodeConfig.WeightedBlock.CODEC.listOf().fieldOf("crystal_blocks").forGetter(OverworldSinkholeConfig::crystalBlocks),
            BlockState.CODEC.optionalFieldOf("stone_block", Blocks.STONE.defaultBlockState()).forGetter(OverworldSinkholeConfig::stoneBlock),
            Codec.FLOAT.optionalFieldOf("crystal_center_chance", 0.85F).forGetter(OverworldSinkholeConfig::crystalCenterChance),
            Codec.FLOAT.optionalFieldOf("crystal_falloff", 1.6F).forGetter(OverworldSinkholeConfig::crystalFalloff),
            BlockState.CODEC.fieldOf("growth_block").forGetter(OverworldSinkholeConfig::growthBlock),
            Codec.FLOAT.optionalFieldOf("growth_chance", 0.3F).forGetter(OverworldSinkholeConfig::growthChance),
            BlockState.CODEC.fieldOf("bud_block").forGetter(OverworldSinkholeConfig::budBlock),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("source_item").forGetter(OverworldSinkholeConfig::sourceItem)
    ).apply(instance, OverworldSinkholeConfig::new));

    public static final Codec<OverworldSinkholeConfig> CODEC = MAP_CODEC.codec();
}
