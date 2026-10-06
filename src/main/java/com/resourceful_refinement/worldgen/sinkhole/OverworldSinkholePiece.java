package com.resourceful_refinement.worldgen.sinkhole;

import com.resourceful_refinement.content.drill_pylon.CrystalFissureBudBlockEntity;
import com.resourceful_refinement.registry.ModStructurePieceTypes;
import com.resourceful_refinement.worldgen.choral.ChoralClusterNoise;
import com.resourceful_refinement.worldgen.crystalnode.CrystalNodeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.List;

/**
 * Carves the Overworld sinkhole and fills it. Runs at {@code surface_structures}, before vegetation, so
 * the surface is clean ground (no trees to clip) and the stone/crystal floor it leaves won't host trees.
 * All decisions are position- or fixed-seed-deterministic so per-chunk {@code postProcess} calls agree.
 */
public class OverworldSinkholePiece extends StructurePiece {
    private BlockPos center;
    private OverworldSinkholeConfig config;
    private int seed;

    public OverworldSinkholePiece(BlockPos center, OverworldSinkholeConfig config, RandomSource random) {
        super(ModStructurePieceTypes.OVERWORLD_SINKHOLE_PIECE.get(), 0, makeBox(center, config));
        this.center = center;
        this.config = config;
        this.seed = random.nextInt();
    }

    public OverworldSinkholePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieceTypes.OVERWORLD_SINKHOLE_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("cx"), tag.getInt("cy"), tag.getInt("cz"));
        this.seed = tag.getInt("seed");
        this.config = OverworldSinkholeConfig.CODEC.parse(NbtOps.INSTANCE, tag.get("config")).result().orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
        tag.putInt("seed", seed);
        OverworldSinkholeConfig.CODEC.encodeStart(NbtOps.INSTANCE, config).result()
                .ifPresent(encoded -> tag.put("config", encoded));
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        if (config == null) {
            return;
        }
        RandomSource rng = RandomSource.create(seed);
        int centerDepth = config.maxDepth() > config.minDepth()
                ? config.minDepth() + rng.nextInt(config.maxDepth() - config.minDepth() + 1)
                : config.minDepth();

        int cx = center.getX();
        int cz = center.getZ();
        int radius = config.radius();
        BlockState stone = config.stoneBlock();
        BlockState growth = config.growthBlock().hasProperty(BlockStateProperties.FACING)
                ? config.growthBlock().setValue(BlockStateProperties.FACING, Direction.UP)
                : config.growthBlock();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int centerFloorY = Integer.MIN_VALUE;
        int reach = radius + 1;

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int x = cx + dx;
                int z = cz + dz;

                double edgeNoise = ChoralClusterNoise.valueNoise(x * 0.18D, z * 0.18D, seed ^ 0xA11CEL) * 0.2D;
                double effR = radius * (1.0D + edgeNoise);
                double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                if (dist > effR) {
                    continue;
                }
                double t = Math.min(1.0D, dist / Math.max(1.0D, effR));

                int depth;
                if (dx == 0 && dz == 0) {
                    depth = centerDepth;
                } else {
                    double shape = 1.0D - t * t;
                    double depthNoise = ChoralClusterNoise.valueNoise(x * 0.3D, z * 0.3D, seed ^ 0xB0BL) * 0.12D;
                    depth = Mth.floor(centerDepth * Math.max(0.0D, shape + depthNoise) + 0.5D);
                }
                if (depth <= 0) {
                    continue;
                }

                int surfaceTop = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                int floorY = surfaceTop - depth;
                if (dx == 0 && dz == 0) {
                    centerFloorY = floorY;
                }

                // Carve the bowl above the floor.
                for (int y = floorY + 1; y <= surfaceTop; y++) {
                    if (isDiggable(world.getBlockState(pos.set(x, y, z)))) {
                        setIfInside(world, box, pos, Blocks.AIR.defaultBlockState());
                    }
                }

                // Floor surface: crystal (denser toward the centre) interspersed with stone.
                double crystalChance = config.crystalCenterChance() * Math.pow(1.0D - t, config.crystalFalloff());
                double floorRoll = (ChoralClusterNoise.randomValue(x, z, seed ^ 0xC0FFEEL) + 1.0D) * 0.5D;
                setIfInside(world, box, pos.set(x, floorY, z), floorRoll < crystalChance ? crystalAt(x, z) : stone);

                // Ground the floor: if the terrain under it is air (an overhang/ledge), back-fill with stone
                // so floor blocks never float over a gap. Normal terrain stops immediately (solid below).
                for (int y = floorY - 1; y >= floorY - 4; y--) {
                    BlockState below = world.getBlockState(pos.set(x, y, z));
                    if (below.isAir() || below.canBeReplaced()) {
                        setIfInside(world, box, pos, stone);
                    } else {
                        break;
                    }
                }

                // Growths through the bowl.
                double growthRoll = (ChoralClusterNoise.randomValue(x, z, seed ^ 0xD00DL) + 1.0D) * 0.5D;
                if (growthRoll < config.growthChance()) {
                    pos.set(x, floorY + 1, z);
                    if (world.getBlockState(pos).isAir()) {
                        setIfInside(world, box, pos, growth);
                    }
                }
            }
        }

        // Centre bud, always at the bottom centre.
        if (centerFloorY != Integer.MIN_VALUE) {
            setIfInside(world, box, pos.set(cx, centerFloorY, cz), crystalAt(cx, cz));
            BlockPos budPos = new BlockPos(cx, centerFloorY + 1, cz);
            setIfInside(world, box, budPos, config.budBlock());
            if (config.sourceItem().isPresent() && box.isInside(budPos)
                    && world.getBlockEntity(budPos) instanceof CrystalFissureBudBlockEntity bud) {
                bud.setSourceItemOnGenerate(config.sourceItem().get());
            }
        }
    }

    private BlockState crystalAt(int x, int z) {
        List<CrystalNodeConfig.WeightedBlock> list = config.crystalBlocks();
        int total = 0;
        for (CrystalNodeConfig.WeightedBlock wb : list) {
            total += wb.weight();
        }
        double h = (ChoralClusterNoise.randomValue(x, z, seed ^ 0x5EEDL) + 1.0D) * 0.5D;
        int roll = Mth.clamp((int) (h * total), 0, total - 1);
        int acc = 0;
        for (CrystalNodeConfig.WeightedBlock wb : list) {
            acc += wb.weight();
            if (roll < acc) {
                return wb.state();
            }
        }
        return list.get(list.size() - 1).state();
    }

    private static boolean isDiggable(BlockState state) {
        return !state.isAir() && !state.is(Blocks.BEDROCK);
    }

    private static void setIfInside(WorldGenLevel world, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            world.setBlock(pos, state, 2);
        }
    }

    private static BoundingBox makeBox(BlockPos center, OverworldSinkholeConfig config) {
        int r = config.radius() + 2;
        return new BoundingBox(
                center.getX() - r, center.getY() - config.maxDepth() - 8, center.getZ() - r,
                center.getX() + r, center.getY() + 6, center.getZ() + r);
    }
}
