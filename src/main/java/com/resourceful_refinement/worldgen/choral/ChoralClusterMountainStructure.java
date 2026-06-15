package com.resourceful_refinement.worldgen.choral;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class ChoralClusterMountainStructure extends Structure {
    public static final MapCodec<ChoralClusterMountainStructure> CODEC = simpleCodec(ChoralClusterMountainStructure::new);

    public ChoralClusterMountainStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        RandomState randomState = context.randomState();
        RandomSource random = context.random();

        int x = chunkPos.getBlockX(randomBetween(random, 4, 12));
        int z = chunkPos.getBlockZ(randomBetween(random, 4, 12));
        int y = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        if (y <= 5) {
            return Optional.empty();
        }

        Holder<Biome> biome = chunkGenerator.getBiomeSource().getNoiseBiome(x >> 2, y >> 2, z >> 2, randomState.sampler());
        BlockPos center = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(center, builder -> generatePieces(builder, random, center, biome)));
    }

    private void generatePieces(StructurePiecesBuilder builder, RandomSource random, BlockPos center, Holder<Biome> biome) {
        float radius = randomBetween(random, ChoralClusterGenerationSettings.MIN_ISLAND_RADIUS, ChoralClusterGenerationSettings.MAX_ISLAND_RADIUS);
        float height = radius * randomBetween(random, ChoralClusterGenerationSettings.MIN_HEIGHT_MULTIPLIER, ChoralClusterGenerationSettings.MAX_HEIGHT_MULTIPLIER);
        builder.addPiece(new ChoralClusterMountainPiece(center, radius, height, random, biome));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.CHORAL_CLUSTER_MOUNTAIN.get();
    }

    private static int randomBetween(RandomSource random, int min, int max) {
        return min + random.nextInt(Math.max(1, max - min + 1));
    }

    private static float randomBetween(RandomSource random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}
