package com.resourceful_refinement.worldgen.crystalnode;

import com.resourceful_refinement.content.drill_pylon.CrystalFissureBudBlockEntity;
import com.resourceful_refinement.registry.ModStructurePieceTypes;
import com.resourceful_refinement.worldgen.choral.ChoralClusterNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Places a {@link CrystalNodeStructure}: an optional noisy circular base disc of a weighted block mix, the
 * centre node (resonance crystal / crystal fissure bud, with its source item set), a companion scatter
 * (growths on the surface, or ore buried below), and surface decor. Follows the local terrain and cuts
 * minimally. All decisions are position- or fixed-seed-deterministic so the per-chunk {@code postProcess}
 * calls agree.
 */
public class CrystalNodePiece extends StructurePiece {
    private BlockPos center;
    private CrystalNodeConfig config;
    private int seed;

    public CrystalNodePiece(BlockPos center, CrystalNodeConfig config, RandomSource random) {
        super(ModStructurePieceTypes.CRYSTAL_NODE_PIECE.get(), 0, makeBox(center, config));
        this.center = center;
        this.config = config;
        this.seed = random.nextInt();
    }

    public CrystalNodePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieceTypes.CRYSTAL_NODE_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("cx"), tag.getInt("cy"), tag.getInt("cz"));
        this.seed = tag.getInt("seed");
        this.config = CrystalNodeConfig.CODEC.parse(NbtOps.INSTANCE, tag.get("config"))
                .result().orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
        tag.putInt("seed", seed);
        CrystalNodeConfig.CODEC.encodeStart(NbtOps.INSTANCE, config).result()
                .ifPresent(encoded -> tag.put("config", encoded));
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        if (config == null) {
            return;
        }
        RandomSource rng = RandomSource.create(seed);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 1. Noisy circular base disc — replace each column's surface block with a weighted mix.
        if (config.baseRadius() > 0 && !config.baseBlocks().isEmpty()) {
            int r = config.baseRadius();
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    if (!inNoisyDisc(dx, dz, r, x, z)) {
                        continue;
                    }
                    // Leave the natural surface in a fraction of cells so the disc isn't a solid slab.
                    if (config.baseFillChance() < 1.0F
                            && (ChoralClusterNoise.randomValue(x, z, seed ^ 0xBA5EL) + 1.0D) * 0.5D >= config.baseFillChance()) {
                        continue;
                    }
                    int surfaceY = surfaceTopY(world, x, z, pos);
                    if (surfaceY == Integer.MIN_VALUE) {
                        continue;
                    }
                    setIfInside(world, box, pos.set(x, surfaceY, z), baseStateAt(x, z));
                }
            }
        }

        // 2. Centre node — re-anchor to the live surface. WORLD_SURFACE reflects structure-added terrain
        // from earlier steps (the choral mountains run at raw_generation, before this surface-structure),
        // unlike the base-noise heightmap used in findGenerationPoint.
        int nodeTop = surfaceTopY(world, center.getX(), center.getZ(), pos);
        if (nodeTop == Integer.MIN_VALUE) {
            return;
        }
        BlockPos nodePos = new BlockPos(center.getX(), nodeTop + 1, center.getZ());
        setIfInside(world, box, nodePos, config.nodeBlock());
        if (config.sourceItem().isPresent() && box.isInside(nodePos)
                && world.getBlockEntity(nodePos) instanceof CrystalFissureBudBlockEntity bud) {
            bud.setSourceItemOnGenerate(config.sourceItem().get());
        }

        // 3. Companion scatter.
        config.scatterBlock().ifPresent(scatter -> {
            if (config.scatterMode() == CrystalNodeConfig.ScatterMode.BELOW) {
                scatterBelow(world, box, rng, scatter, pos);
            } else {
                scatterOnSurface(world, box, rng, scatter, config.scatterCount(), config.scatterRadius(), pos);
            }
        });

        // 4. Surface decor on the base.
        config.decorBlock().ifPresent(decor ->
                scatterOnSurface(world, box, rng, decor, config.decorCount(),
                        Math.max(1, config.baseRadius()), pos));
    }

    private void scatterOnSurface(WorldGenLevel world, BoundingBox box, RandomSource rng, BlockState block,
                                  int count, int radius, BlockPos.MutableBlockPos pos) {
        BlockState upright = block.hasProperty(BlockStateProperties.FACING)
                ? block.setValue(BlockStateProperties.FACING, Direction.UP) : block;
        for (int i = 0; i < count; i++) {
            int x = center.getX() + rng.nextInt(radius * 2 + 1) - radius;
            int z = center.getZ() + rng.nextInt(radius * 2 + 1) - radius;
            int surfaceY = surfaceTopY(world, x, z, pos);
            if (surfaceY == Integer.MIN_VALUE) {
                continue;
            }
            pos.set(x, surfaceY + 1, z);
            if (!isReplaceable(world.getBlockState(pos))) {
                continue;
            }
            setIfInside(world, box, pos, upright);
        }
    }

    private void scatterBelow(WorldGenLevel world, BoundingBox box, RandomSource rng, BlockState block,
                              BlockPos.MutableBlockPos pos) {
        int r = config.scatterRadius();
        for (int i = 0; i < config.scatterCount(); i++) {
            int x = center.getX() + rng.nextInt(r * 2 + 1) - r;
            int z = center.getZ() + rng.nextInt(r * 2 + 1) - r;
            int depth = 1 + rng.nextInt(Math.max(1, r));
            pos.set(x, center.getY() - depth, z);
            BlockState existing = world.getBlockState(pos);
            if (!existing.isAir() && existing.getFluidState().isEmpty()) {
                setIfInside(world, box, pos, block);
            }
        }
    }

    /**
     * The top solid, sturdy-topped surface block in column (x,z), or MIN_VALUE if none. Surface anchors use
     * the live {@code WORLD_SURFACE} heightmap (so structure-added terrain like the choral mountains counts);
     * Nether-floor anchors scan locally around the floor Y found in findGenerationPoint.
     */
    private int surfaceTopY(WorldGenLevel world, int x, int z, BlockPos.MutableBlockPos pos) {
        if (config.anchor() == CrystalNodeConfig.AnchorMode.NETHER_FLOOR) {
            int cy = center.getY();
            for (int y = cy + 4; y >= cy - 8; y--) {
                if (isSurfaceTop(world, x, y, z, pos)) {
                    return y;
                }
            }
            return Integer.MIN_VALUE;
        }

        int top = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        if (top <= world.getMinBuildHeight()) {
            return Integer.MIN_VALUE;
        }
        return isSurfaceTop(world, x, top, z, pos) ? top : Integer.MIN_VALUE;
    }

    /** True if (x,y,z) is a solid, non-fluid, sturdy-topped block with a replaceable block above it. */
    private boolean isSurfaceTop(WorldGenLevel world, int x, int y, int z, BlockPos.MutableBlockPos pos) {
        BlockState state = world.getBlockState(pos.set(x, y, z));
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        if (!state.isFaceSturdy(world, pos.set(x, y, z), Direction.UP)) {
            return false;
        }
        return isReplaceable(world.getBlockState(pos.set(x, y + 1, z)));
    }

    private BlockState baseStateAt(int x, int z) {
        var list = config.baseBlocks();
        int total = 0;
        for (CrystalNodeConfig.WeightedBlock wb : list) {
            total += wb.weight();
        }
        double h = (ChoralClusterNoise.randomValue(x, z, seed ^ 0x9E3779B9L) + 1.0D) * 0.5D;
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

    private boolean inNoisyDisc(int dx, int dz, int radius, int x, int z) {
        double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
        double wobble = ChoralClusterNoise.valueNoise(x * 0.3D, z * 0.3D, seed ^ 0x1234L) * 0.28D;
        return dist <= radius * (0.85D + wobble);
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static void setIfInside(WorldGenLevel world, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            world.setBlock(pos, state, 2);
        }
    }

    private static BoundingBox makeBox(BlockPos center, CrystalNodeConfig config) {
        int r = Math.max(config.footprintRadius(), Math.max(config.baseRadius(), config.scatterRadius())) + 2;
        boolean nether = config.anchor() == CrystalNodeConfig.AnchorMode.NETHER_FLOOR;
        int below = Math.max(nether ? 14 : 16, config.scatterRadius() + 2);
        // Surface anchors re-resolve the real surface in the piece, which may sit well above the base-noise
        // height used here (e.g. the choral mountains), so the box must reach high enough to write there.
        int above = nether ? 10 : 200;
        return new BoundingBox(
                center.getX() - r, center.getY() - below, center.getZ() - r,
                center.getX() + r, center.getY() + above, center.getZ() + r);
    }
}
