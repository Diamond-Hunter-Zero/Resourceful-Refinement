/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.BiomeSource
 *  net.minecraft.world.level.levelgen.LegacyRandomSource
 *  net.minecraft.world.level.levelgen.RandomState
 *  net.minecraft.world.level.levelgen.WorldgenRandom
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 */
package net.minecraft.world.level.levelgen.structure;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public record Structure.GenerationContext(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, WorldgenRandom random, long seed, ChunkPos chunkPos, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome) {
    public Structure.GenerationContext(RegistryAccess p_226632_, ChunkGenerator p_226633_, BiomeSource p_226634_, RandomState p_226635_, StructureTemplateManager p_226636_, long p_226637_, ChunkPos p_226638_, LevelHeightAccessor p_226639_, Predicate<Holder<Biome>> p_226640_) {
        this(p_226632_, p_226633_, p_226634_, p_226635_, p_226636_, Structure.GenerationContext.makeRandom(p_226637_, p_226638_), p_226637_, p_226638_, p_226639_, p_226640_);
    }

    private static WorldgenRandom makeRandom(long seed, ChunkPos chunkPos) {
        WorldgenRandom worldgenrandom = new WorldgenRandom((RandomSource)new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureSeed(seed, chunkPos.x, chunkPos.z);
        return worldgenrandom;
    }
}
