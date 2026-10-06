package com.resourceful_refinement.worldgen.choral.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Scatters a directional "cluster" block (e.g. {@code chorus_crystal_growth}) onto exposed faces of a
 * configured block set, orienting its {@code facing} away from the surface it attaches to — like
 * amethyst clusters in a geode, but as a standalone placed feature. With a block set of
 * terrain + chorus crystal this yields grass-like up-facing growth on flat ground and occasional
 * side/under growth on crystal formations and cliffs in one pass.
 */
public class AttachedGrowthFeature extends Feature<AttachedGrowthConfiguration> {
    public AttachedGrowthFeature(Codec<AttachedGrowthConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<AttachedGrowthConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        AttachedGrowthConfiguration config = context.config();

        boolean placedAny = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int xz = config.xzSpread();
        int y = config.ySpread();

        for (int i = 0; i < config.tries(); i++) {
            pos.set(
                    origin.getX() + random.nextInt(xz * 2 + 1) - xz,
                    origin.getY() + random.nextInt(y * 2 + 1) - y,
                    origin.getZ() + random.nextInt(xz * 2 + 1) - xz
            );
            if (!level.isEmptyBlock(pos)) {
                continue;
            }

            Direction toSupport = pickSupportDirection(level, pos, config, random);
            if (toSupport == null) {
                continue;
            }

            BlockState state = config.growth();
            if (state.hasProperty(BlockStateProperties.FACING)) {
                // The growth points away from the surface it attaches to.
                state = state.setValue(BlockStateProperties.FACING, toSupport.getOpposite());
            }
            if (state.canSurvive(level, pos)) {
                level.setBlock(pos, state, 2);
                placedAny = true;
            }
        }
        return placedAny;
    }

    private static Direction pickSupportDirection(WorldGenLevel level, BlockPos pos,
                                                  AttachedGrowthConfiguration config, RandomSource random) {
        List<Direction> valid = new ArrayList<>(6);
        for (Direction direction : config.attachDirections()) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            if (config.attachTo().contains(neighbour.getBlock().builtInRegistryHolder())) {
                valid.add(direction);
            }
        }
        if (valid.isEmpty()) {
            return null;
        }
        return valid.get(random.nextInt(valid.size()));
    }
}
