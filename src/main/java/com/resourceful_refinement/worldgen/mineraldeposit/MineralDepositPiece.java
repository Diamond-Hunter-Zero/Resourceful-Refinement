package com.resourceful_refinement.worldgen.mineraldeposit;

import com.resourceful_refinement.content.mineral_deposit.MineralDepositBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModStructurePieceTypes;
import com.resourceful_refinement.worldgen.choral.ChoralClusterNoise;
import com.resourceful_refinement.worldgen.crystalnode.CrystalNodeConfig.WeightedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Places a {@link MineralDepositStructure}: a noisy, roughly circular bowl of weighted filler blocks (deepest
 * at the centre, 1 deep at the rim) that dissolves into the terrain through a sparse outer ring, a central cluster of Mineral Deposit blocks set flush into the ground with
 * their deposit type, and optional decor on top of the filler. Follows the local terrain per column and only
 * replaces existing solid ground, so it never builds into the air or fills caves.
 *
 * <p>All decisions are position-hashed or drawn from a fixed-seed RNG in a fixed order, so every per-chunk
 * {@code postProcess} call agrees on the layout.</p>
 */
public class MineralDepositPiece extends StructurePiece {
    /** A column whose live surface strays further than this from the anchor is skipped (cliffs, overhangs). */
    private static final int MAX_SURFACE_DRIFT = 4;

    private static final Direction[] ORTHOGONAL = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private BlockPos center;
    private MineralDepositConfig config;
    private int seed;

    public MineralDepositPiece(BlockPos center, MineralDepositConfig config, RandomSource random) {
        super(ModStructurePieceTypes.MINERAL_DEPOSIT_PIECE.get(), 0, makeBox(center, config));
        this.center = center;
        this.config = config;
        this.seed = random.nextInt();
    }

    public MineralDepositPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieceTypes.MINERAL_DEPOSIT_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("cx"), tag.getInt("cy"), tag.getInt("cz"));
        this.seed = tag.getInt("seed");
        this.config = MineralDepositConfig.CODEC.parse(NbtOps.INSTANCE, tag.get("config"))
                .result().orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("cx", center.getX());
        tag.putInt("cy", center.getY());
        tag.putInt("cz", center.getZ());
        tag.putInt("seed", seed);
        MineralDepositConfig.CODEC.encodeStart(NbtOps.INSTANCE, config).result()
                .ifPresent(encoded -> tag.put("config", encoded));
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        if (config == null || config.fillerBlocks().isEmpty()) {
            return;
        }
        RandomSource rng = RandomSource.create(seed);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        List<int[]> deposits = pickDepositColumns(rng);
        int r = footprintRadius(config);

        // 1. Filler bowl + fade-out ring — replace each column's top solid blocks with the weighted mix.
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int depth = isDeposit(deposits, dx, dz) ? config.maxDepth() : columnDepth(dx, dz);
                if (depth <= 0) {
                    continue;
                }
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int top = surfaceTopY(world, x, z, pos);
                if (top == Integer.MIN_VALUE) {
                    continue;
                }
                for (int k = 0; k < depth; k++) {
                    BlockState filler = fillerAt(x, top - k, z);
                    pos.set(x, top - k, z);
                    if (!isReplaceableGround(world, pos)) {
                        break;
                    }
                    // Never leave a falling block (sand) hanging over a cave.
                    if (filler.getBlock() instanceof FallingBlock && !isSolid(world.getBlockState(pos.below()))) {
                        break;
                    }
                    setIfInside(world, box, pos, filler);
                }
            }
        }

        // 2. Central deposit cluster, flush with the (now filler) surface.
        BlockState depositState = ModBlocks.MINERAL_DEPOSIT.get().defaultBlockState();
        for (int[] column : deposits) {
            int x = center.getX() + column[0];
            int z = center.getZ() + column[1];
            int top = surfaceTopY(world, x, z, pos);
            if (top == Integer.MIN_VALUE) {
                continue;
            }
            BlockPos depositPos = new BlockPos(x, top, z);
            if (!box.isInside(depositPos)) {
                continue;
            }
            world.setBlock(depositPos, depositState, 2);
            if (world.getBlockEntity(depositPos) instanceof MineralDepositBlockEntity be) {
                be.setAssociatedBlockOnGenerate(config.depositType());
            }
        }

        // 3. Decor on top of the filler (never on the deposits themselves).
        placeDecor(world, box, rng, deposits, pos);
    }

    private void placeDecor(WorldGenLevel world, BoundingBox box, RandomSource rng, List<int[]> deposits,
                            BlockPos.MutableBlockPos pos) {
        List<WeightedBlock> decor = config.decorBlocks();
        if (decor.isEmpty() || config.decorCountMax() <= 0) {
            return;
        }
        int total = 0;
        for (WeightedBlock wb : decor) {
            total += wb.weight();
        }
        int spread = Math.max(0, config.decorCountMax() - config.decorCountMin());
        int count = config.decorCountMin() + rng.nextInt(spread + 1);

        // Candidate cells: filled filler columns that aren't deposits. Both tests are deterministic, so the list
        // (and every RNG draw below) is identical in each postProcess call.
        List<int[]> candidates = new ArrayList<>();
        int r = footprintRadius(config);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (!isDeposit(deposits, dx, dz) && columnDepth(dx, dz) > 0) {
                    candidates.add(new int[]{dx, dz});
                }
            }
        }
        shuffle(candidates, rng);
        int[] rolls = new int[candidates.size()];
        Direction[] facings = new Direction[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            rolls[i] = rng.nextInt(total);
            facings[i] = Direction.Plane.HORIZONTAL.getRandomDirection(rng);
        }

        // Walk the shuffled cells until the target count is placed, skipping any whose surface can't take decor.
        int placed = 0;
        for (int i = 0; i < candidates.size() && placed < count; i++) {
            int x = center.getX() + candidates.get(i)[0];
            int z = center.getZ() + candidates.get(i)[1];
            int top = surfaceTopY(world, x, z, pos);
            if (top == Integer.MIN_VALUE) {
                continue;
            }
            pos.set(x, top + 1, z);
            if (!world.getBlockState(pos).isAir()) {
                continue;
            }
            BlockState state = orient(pickWeighted(decor, rolls[i]), facings[i]);
            if (state.canSurvive(world, pos)) {
                setIfInside(world, box, pos, state);
                placed++;
            }
        }
    }

    /** Decor faces up when it has a full FACING (crystal growths) and a random way when horizontal (buttons). */
    private static BlockState orient(BlockState state, Direction horizontal) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, horizontal);
        }
        return state;
    }

    /** The centre column plus a shuffled subset of its orthogonal neighbours, as {dx, dz} offsets. */
    private List<int[]> pickDepositColumns(RandomSource rng) {
        int min = Math.min(config.depositCountMin(), config.depositCountMax());
        int max = Math.max(config.depositCountMin(), config.depositCountMax());
        int count = min + rng.nextInt(max - min + 1);

        List<Direction> neighbours = new ArrayList<>(List.of(ORTHOGONAL));
        shuffle(neighbours, rng);

        List<int[]> columns = new ArrayList<>();
        columns.add(new int[]{0, 0});
        for (int i = 0; i < count - 1 && i < neighbours.size(); i++) {
            Direction d = neighbours.get(i);
            columns.add(new int[]{d.getStepX(), d.getStepZ()});
        }
        return columns;
    }

    /** Fisher-Yates shuffle driven by the piece's fixed-seed RNG. */
    private static <T> void shuffle(List<T> list, RandomSource rng) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private static boolean isDeposit(List<int[]> deposits, int dx, int dz) {
        for (int[] column : deposits) {
            if (column[0] == dx && column[1] == dz) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filler depth for a column (0 = untouched).
     * <ul>
     *     <li><b>Base</b> (within ~{@code baseRadius}): a shallow bowl, {@code maxDepth} at the centre tapering to 1
     *     at the rim, with a noisy, slightly ragged edge and a little depth jitter.</li>
     *     <li><b>Ring</b> (out to {@code ringRadius - 1}..{@code ringRadius}): single-depth cells whose coverage
     *     falls from ~80% at the base edge to 0 at the ring's noisy outer edge, mildly clumped by low-frequency
     *     noise, so the base dissolves into the natural ground instead of ending on a hard line.</li>
     * </ul>
     */
    private int columnDepth(int dx, int dz) {
        int x = center.getX() + dx;
        int z = center.getZ() + dz;
        double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
        double wobble = ChoralClusterNoise.valueNoise(x * 0.35D, z * 0.35D, seed ^ 0x1234L) * 0.3D;
        double edge = config.baseRadius() * (0.9D + wobble);

        if (dist <= edge) {
            double t = dist / edge;
            // Slightly ragged rim (the ring below does most of the fading).
            if (t > 0.7D) {
                double keep = 1.0D - (t - 0.7D) / 0.3D * 0.35D;
                if (unit(x, z, 0xBA5EL) > keep) {
                    return 0;
                }
            }
            int depth = 1 + (int) Math.round((config.maxDepth() - 1) * (1.0D - t * t));
            if (depth > 1 && unit(x, z, 0xDE97L) < 0.3D) {
                depth--;
            }
            return depth;
        }

        if (config.ringRadius() <= config.baseRadius()) {
            return 0;
        }
        double outerWobble = (ChoralClusterNoise.valueNoise(x * 0.3D, z * 0.3D, seed ^ 0x0E7EL) + 1.0D) * 0.5D;
        double outer = Math.max(edge + 0.5D, config.ringRadius() - 1 + outerWobble);
        if (dist > outer) {
            return 0;
        }
        double t = (dist - edge) / (outer - edge);
        double density = 0.8D * Math.pow(1.0D - t, 0.7D);
        double clump = (ChoralClusterNoise.valueNoise(x * 0.6D, z * 0.6D, seed ^ 0xC1A3L) + 1.0D) * 0.5D;
        return unit(x, z, 0x51DEL) * 0.6D + clump * 0.4D < density ? 1 : 0;
    }

    /**
     * The top solid, sturdy-topped block of column (x,z) with dry air above, within {@link #MAX_SURFACE_DRIFT}
     * of the anchor, or MIN_VALUE if none. Surface anchors read the live {@code WORLD_SURFACE} heightmap; cave
     * anchors scan locally around the cave floor found in findGenerationPoint.
     */
    private int surfaceTopY(WorldGenLevel world, int x, int z, BlockPos.MutableBlockPos pos) {
        int anchorTop = center.getY() - 1;
        if (config.isCave()) {
            for (int y = anchorTop + MAX_SURFACE_DRIFT; y >= anchorTop - MAX_SURFACE_DRIFT; y--) {
                if (isSurfaceTop(world, x, y, z, pos)) {
                    return y;
                }
            }
            return Integer.MIN_VALUE;
        }

        int top = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        if (Math.abs(top - anchorTop) > MAX_SURFACE_DRIFT || !isSurfaceTop(world, x, top, z, pos)) {
            return Integer.MIN_VALUE;
        }
        return top;
    }

    private static boolean isSurfaceTop(WorldGenLevel world, int x, int y, int z, BlockPos.MutableBlockPos pos) {
        BlockState state = world.getBlockState(pos.set(x, y, z));
        if (!isSolid(state) || !state.isFaceSturdy(world, pos, Direction.UP)) {
            return false;
        }
        BlockState above = world.getBlockState(pos.set(x, y + 1, z));
        return (above.isAir() || above.canBeReplaced()) && above.getFluidState().isEmpty();
    }

    /** Solid natural ground we may overwrite: no fluids, block entities or unbreakable blocks (bedrock). */
    private static boolean isReplaceableGround(WorldGenLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return isSolid(state) && !state.hasBlockEntity() && state.getDestroySpeed(world, pos) >= 0.0F;
    }

    private static boolean isSolid(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    private BlockState fillerAt(int x, int y, int z) {
        List<WeightedBlock> list = config.fillerBlocks();
        int total = 0;
        for (WeightedBlock wb : list) {
            total += wb.weight();
        }
        // Mix y into the hash so stacked filler layers don't repeat the same block.
        int roll = Math.min((int) (unit(x, z + y * 31, 0x9E3779B9L) * total), total - 1);
        return pickWeighted(list, roll);
    }

    private static BlockState pickWeighted(List<WeightedBlock> list, int roll) {
        int acc = 0;
        for (WeightedBlock wb : list) {
            acc += wb.weight();
            if (roll < acc) {
                return wb.state();
            }
        }
        return list.get(list.size() - 1).state();
    }

    /** Position-hashed value in [0, 1). */
    private double unit(int x, int z, long salt) {
        double v = (ChoralClusterNoise.randomValue(x, z, seed ^ salt) + 1.0D) * 0.5D;
        return Math.min(v, 0.999999D);
    }

    private static void setIfInside(WorldGenLevel world, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            world.setBlock(pos, state, 2);
        }
    }

    /** Horizontal reach of the filler: the ring when there is one, else the base. */
    private static int footprintRadius(MineralDepositConfig config) {
        return Math.max(config.baseRadius(), config.ringRadius());
    }

    private static BoundingBox makeBox(BlockPos center, MineralDepositConfig config) {
        int r = footprintRadius(config) + 1;
        int below = config.maxDepth() + MAX_SURFACE_DRIFT + 2;
        int above = MAX_SURFACE_DRIFT + 2;
        return new BoundingBox(
                center.getX() - r, center.getY() - below, center.getZ() - r,
                center.getX() + r, center.getY() + above, center.getZ() + r);
    }
}
