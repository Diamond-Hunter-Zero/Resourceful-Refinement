package com.resourceful_refinement.worldgen.mineraldeposit;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * One configurable structure type for every Mineral Deposit variant. Behaviour is driven by
 * {@link MineralDepositConfig} in the structure JSON, so each variant is just data + its own
 * {@code random_spread} set.
 *
 * <p>Placement sits at the chunk's middle block, so the [[Resource Resonator]] scan (which reports the chunk
 * middle) is exact in X/Z, and the whole footprint stays inside that one chunk. The placement Y comes from
 * {@link #findAnchorY}, which reads only seed-derived generator state — the Resonator scan calls the same
 * method, so it rejects exactly the sites generation rejects (underwater, no cave floor, ...).</p>
 */
public class MineralDepositStructure extends Structure {
    public static final MapCodec<MineralDepositStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    MineralDepositConfig.MAP_CODEC.forGetter(s -> s.config)
            ).apply(instance, MineralDepositStructure::new));

    /** A cave floor must sit at least this far below the surface to count as underground. */
    private static final int MIN_CAVE_COVER = 8;

    private final MineralDepositConfig config;

    public MineralDepositStructure(StructureSettings settings, MineralDepositConfig config) {
        super(settings);
        this.config = config;
    }

    public MineralDepositConfig config() {
        return config;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        OptionalInt y = findAnchorY(context.chunkGenerator(), context.heightAccessor(), context.randomState(),
                context.seed(), chunkPos.x, chunkPos.z);
        if (y.isEmpty()) {
            return Optional.empty();
        }

        BlockPos center = new BlockPos(chunkPos.getMiddleBlockX(), y.getAsInt(), chunkPos.getMiddleBlockZ());
        RandomSource random = context.random();
        return Optional.of(new GenerationStub(center, builder ->
                builder.addPiece(new MineralDepositPiece(center, config, random))));
    }

    /**
     * The placement Y (the air block directly above the ground the deposit sits in) for the structure in the
     * given chunk, or empty if this chunk has no valid site. Deterministic from the world seed and generator —
     * no chunks are generated — so the Resonator scan can call it to predict the exact site.
     */
    public OptionalInt findAnchorY(ChunkGenerator generator, LevelHeightAccessor heightAccessor,
                                   RandomState randomState, long seed, int chunkX, int chunkZ) {
        int x = SectionPos.sectionToBlockCoord(chunkX, 8);
        int z = SectionPos.sectionToBlockCoord(chunkZ, 8);
        int surfaceY = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        if (surfaceY <= heightAccessor.getMinBuildHeight() + 5) {
            return OptionalInt.empty();
        }

        if (!config.isCave()) {
            // Reject underwater surface sites (fluid above the ocean floor). No effect in the End (no water).
            int oceanFloor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heightAccessor, randomState);
            return surfaceY > oceanFloor ? OptionalInt.empty() : OptionalInt.of(surfaceY);
        }

        int minY = Math.max(config.caveMinY(), heightAccessor.getMinBuildHeight() + 1);
        int maxY = Math.min(config.caveMaxY(), surfaceY - MIN_CAVE_COVER);
        if (maxY < minY) {
            return OptionalInt.empty();
        }

        // Start the floor search from a seeded height in the band so deposits spread through it rather than
        // always taking the highest cave. Salted per deposit type so overlapping sets don't pick the same floor.
        long salt = BuiltInRegistries.BLOCK.getKey(config.depositType()).hashCode();
        RandomSource rng = RandomSource.create(Mth.getSeed(chunkX, (int) salt, chunkZ) ^ seed);
        int startY = minY + rng.nextInt(maxY - minY + 1);

        NoiseColumn column = generator.getBaseColumn(x, z, heightAccessor, randomState);
        for (int y = startY; y >= minY; y--) {
            if (isCaveFloor(column, y)) {
                return OptionalInt.of(y);
            }
        }
        for (int y = startY + 1; y <= maxY; y++) {
            if (isCaveFloor(column, y)) {
                return OptionalInt.of(y);
            }
        }
        return OptionalInt.empty();
    }

    /** Two blocks of dry air standing on a solid, non-fluid block. */
    private static boolean isCaveFloor(NoiseColumn column, int y) {
        BlockState below = column.getBlock(y - 1);
        return column.getBlock(y).isAir() && column.getBlock(y + 1).isAir()
                && !below.isAir() && below.getFluidState().isEmpty();
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.MINERAL_DEPOSIT.get();
    }
}
