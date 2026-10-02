package com.resourceful_refinement.content.resonator.scan;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Seed-based prediction of where {@code minecraft:random_spread} world-generation structures (geysers, and later
 * mineral deposits / crystal fissures) will generate, without loading or generating any chunks.
 *
 * <p>The placement of a {@code random_spread} structure set is fully deterministic from the world seed via
 * {@link RandomSpreadStructurePlacement#getPotentialStructureChunk(long, int, int)} — the same math vanilla uses
 * during generation. For each candidate chunk we then apply vanilla's own biome filter by sampling the live
 * {@link BiomeSource} (also seed-based, no chunk generation) and testing membership in the structure's biome set.
 * This makes the scan both reliable (it mirrors the actual placement decision) and cheap (it iterates the sparse
 * structure regions overlapping the radius, not every chunk).</p>
 *
 * <p>Horizontal (X/Z) coordinates are exact. Y is a best-effort estimate — exact for surface geysers (world-surface
 * heightmap), approximate for cave/nether geysers whose Y is a per-chunk uniform draw made at generation time.</p>
 */
public final class ResourceScanService {

    /** Hard cap on returned pins, protecting the sync payload and radar from pathological radii. */
    public static final int MAX_RESULTS = 4096;

    private ResourceScanService() {}

    /**
     * Predicts all matching feature locations within {@code chunkRadius} chunks of {@code center} in the given level.
     *
     * @param level       the dimension to scan (its seed, biome source and generator drive the prediction)
     * @param center      the scan origin (the Resonator's position)
     * @param chunkRadius circular radius in chunks
     * @param filter      which feature categories to include
     * @return predicted points of interest, horizontally exact, capped at {@link #MAX_RESULTS}
     */
    public static List<ScannedPoi> scan(ServerLevel level, net.minecraft.core.BlockPos center, int chunkRadius,
            Set<ResonatorTarget> filter) {
        List<ScannedPoi> results = new ArrayList<>();
        if (chunkRadius <= 0 || filter.isEmpty()) {
            return results;
        }

        ServerChunkCache chunkCache = level.getChunkSource();
        ChunkGenerator generator = chunkCache.getGenerator();
        ChunkGeneratorStructureState structureState = chunkCache.getGeneratorState();
        RandomState randomState = chunkCache.randomState();
        Climate.Sampler sampler = randomState.sampler();
        BiomeSource biomeSource = generator.getBiomeSource();
        Set<Holder<Biome>> possibleBiomes = biomeSource.possibleBiomes();
        long seed = structureState.getLevelSeed();
        WorldGenerationContext heightContext = new WorldGenerationContext(generator, level);

        var setRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);

        int centerChunkX = SectionPos.blockToSectionCoord(center.getX());
        int centerChunkZ = SectionPos.blockToSectionCoord(center.getZ());
        long radiusSq = (long) chunkRadius * chunkRadius;
        boolean nether = level.dimension() == Level.NETHER;

        for (ResonatorTarget target : filter) {
            for (ResourceLocation setId : target.structureSets()) {
                StructureSet set = setRegistry.get(setId);
                if (set == null) {
                    continue;
                }
                StructurePlacement placement = set.placement();
                if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
                    // Only random_spread is seed-predictable with this fast path; all shipped geyser sets use it.
                    continue;
                }

                // Restrict to structures whose biomes can occur in this dimension (vanilla's own gate). This also
                // excludes nether sets while scanning the overworld and vice versa.
                List<Holder<Structure>> dimensionStructures = new ArrayList<>();
                for (StructureSet.StructureSelectionEntry entry : set.structures()) {
                    Holder<Structure> structureHolder = entry.structure();
                    if (structureHolder.value().biomes().stream().anyMatch(possibleBiomes::contains)) {
                        dimensionStructures.add(structureHolder);
                    }
                }
                if (dimensionStructures.isEmpty()) {
                    continue;
                }

                int spacing = spread.spacing();
                int minRegionX = Math.floorDiv(centerChunkX - chunkRadius, spacing);
                int maxRegionX = Math.floorDiv(centerChunkX + chunkRadius, spacing);
                int minRegionZ = Math.floorDiv(centerChunkZ - chunkRadius, spacing);
                int maxRegionZ = Math.floorDiv(centerChunkZ + chunkRadius, spacing);

                for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
                    for (int regionZ = minRegionZ; regionZ <= maxRegionZ; regionZ++) {
                        // Any chunk coordinate inside the region resolves to the same potential structure chunk.
                        ChunkPos candidate = spread.getPotentialStructureChunk(seed, regionX * spacing, regionZ * spacing);

                        long dx = candidate.x - centerChunkX;
                        long dz = candidate.z - centerChunkZ;
                        if (dx * dx + dz * dz > radiusSq) {
                            continue;
                        }

                        int blockX = candidate.getMiddleBlockX();
                        int blockZ = candidate.getMiddleBlockZ();

                        int surfaceY = generator.getBaseHeight(blockX, blockZ, Heightmap.Types.WORLD_SURFACE_WG,
                                level, randomState);
                        int sampleY = nether ? 64 : surfaceY;

                        Holder<Biome> biome = biomeSource.getNoiseBiome(
                                QuartPos.fromBlock(blockX), QuartPos.fromBlock(sampleY), QuartPos.fromBlock(blockZ),
                                sampler);

                        Holder<Structure> matched = null;
                        for (Holder<Structure> structureHolder : dimensionStructures) {
                            if (structureHolder.value().biomes().contains(biome)) {
                                matched = structureHolder;
                                break;
                            }
                        }
                        if (matched == null) {
                            continue;
                        }

                        ResourceLocation structureId = matched.unwrapKey().map(ResourceKey::location).orElse(null);
                        String variant = GeyserVariants.predict(matched.value(), structureId, seed,
                                candidate.x, candidate.z, heightContext);

                        boolean yApproximate = target != ResonatorTarget.SURFACE_GEYSER;
                        int reportedY = target == ResonatorTarget.SURFACE_GEYSER ? surfaceY : sampleY;
                        results.add(new ScannedPoi(blockX, reportedY, blockZ, yApproximate, target, variant));

                        if (results.size() >= MAX_RESULTS) {
                            return results;
                        }
                    }
                }
            }
        }

        return results;
    }
}
