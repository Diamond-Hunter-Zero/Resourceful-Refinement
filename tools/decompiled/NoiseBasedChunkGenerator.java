/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  net.minecraft.SharedConstants
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.QuartPos
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.WorldGenRegion
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.NaturalSpawner
 *  net.minecraft.world.level.NoiseColumn
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.StructureManager
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.BiomeGenerationSettings
 *  net.minecraft.world.level.biome.BiomeManager
 *  net.minecraft.world.level.biome.BiomeResolver
 *  net.minecraft.world.level.biome.BiomeSource
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.CarvingMask
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.chunk.ProtoChunk
 *  net.minecraft.world.level.dimension.DimensionType
 *  net.minecraft.world.level.levelgen.Aquifer
 *  net.minecraft.world.level.levelgen.Aquifer$FluidPicker
 *  net.minecraft.world.level.levelgen.Aquifer$FluidStatus
 *  net.minecraft.world.level.levelgen.Beardifier
 *  net.minecraft.world.level.levelgen.BelowZeroRetrogen
 *  net.minecraft.world.level.levelgen.DensityFunction$FunctionContext
 *  net.minecraft.world.level.levelgen.DensityFunction$SinglePointContext
 *  net.minecraft.world.level.levelgen.DensityFunctions$BeardifierMarker
 *  net.minecraft.world.level.levelgen.DensityFunctions$BeardifierOrMarker
 *  net.minecraft.world.level.levelgen.GenerationStep$Carving
 *  net.minecraft.world.level.levelgen.Heightmap
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.LegacyRandomSource
 *  net.minecraft.world.level.levelgen.NoiseChunk
 *  net.minecraft.world.level.levelgen.NoiseGeneratorSettings
 *  net.minecraft.world.level.levelgen.NoiseRouter
 *  net.minecraft.world.level.levelgen.NoiseRouterData
 *  net.minecraft.world.level.levelgen.NoiseSettings
 *  net.minecraft.world.level.levelgen.RandomState
 *  net.minecraft.world.level.levelgen.RandomSupport
 *  net.minecraft.world.level.levelgen.WorldGenerationContext
 *  net.minecraft.world.level.levelgen.WorldgenRandom
 *  net.minecraft.world.level.levelgen.blending.Blender
 *  net.minecraft.world.level.levelgen.carver.CarvingContext
 *  net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;

public class NoiseBasedChunkGenerator
extends ChunkGenerator {
    public static final MapCodec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(p_255585_ -> p_255585_.group((App)BiomeSource.CODEC.fieldOf("biome_source").forGetter(p_255584_ -> p_255584_.biomeSource), (App)NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(p_224278_ -> p_224278_.settings)).apply((Applicative)p_255585_, p_255585_.stable(NoiseBasedChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final Holder<NoiseGeneratorSettings> settings;
    private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

    public NoiseBasedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource);
        this.settings = settings;
        this.globalFluidPicker = Suppliers.memoize(() -> NoiseBasedChunkGenerator.createFluidPicker((NoiseGeneratorSettings)settings.value()));
    }

    private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus aquifer$fluidstatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = settings.seaLevel();
        Aquifer.FluidStatus aquifer$fluidstatus1 = new Aquifer.FluidStatus(i, settings.defaultFluid());
        Aquifer.FluidStatus aquifer$fluidstatus2 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
        return (p_224274_, p_224275_, p_224276_) -> p_224275_ < Math.min(-54, i) ? aquifer$fluidstatus : aquifer$fluidstatus1;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName((String)"init_biomes", () -> {
            this.doCreateBiomes(blender, randomState, structureManager, chunk);
            return chunk;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        NoiseChunk noisechunk = chunk.getOrCreateNoiseChunk(p_224340_ -> this.createNoiseChunk((ChunkAccess)p_224340_, structureManager, blender, random));
        BiomeResolver biomeresolver = BelowZeroRetrogen.getBiomeResolver((BiomeResolver)blender.getBiomeResolver((BiomeResolver)this.biomeSource), (ChunkAccess)chunk);
        chunk.fillBiomesFromNoise(biomeresolver, noisechunk.cachedClimateSampler(random.router(), ((NoiseGeneratorSettings)this.settings.value()).spawnTarget()));
    }

    private NoiseChunk createNoiseChunk(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState random) {
        return NoiseChunk.forChunk((ChunkAccess)chunk, (RandomState)random, (DensityFunctions.BeardifierOrMarker)Beardifier.forStructuresInChunk((StructureManager)structureManager, (ChunkPos)chunk.getPos()), (NoiseGeneratorSettings)((NoiseGeneratorSettings)this.settings.value()), (Aquifer.FluidPicker)this.globalFluidPicker.get(), (Blender)blender);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.settings;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> settings) {
        return this.settings.is(settings);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return this.iterateNoiseColumn(level, random, x, z, null, type.isOpaque()).orElse(level.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        MutableObject mutableobject = new MutableObject();
        this.iterateNoiseColumn(height, random, x, z, (MutableObject<NoiseColumn>)mutableobject, null);
        return (NoiseColumn)mutableobject.getValue();
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        DecimalFormat decimalformat = new DecimalFormat("0.000");
        NoiseRouter noiserouter = random.router();
        DensityFunction.SinglePointContext densityfunction$singlepointcontext = new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ());
        double d0 = noiserouter.ridges().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext);
        info.add("NoiseRouter T: " + decimalformat.format(noiserouter.temperature().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext)) + " V: " + decimalformat.format(noiserouter.vegetation().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext)) + " C: " + decimalformat.format(noiserouter.continents().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext)) + " E: " + decimalformat.format(noiserouter.erosion().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext)) + " D: " + decimalformat.format(noiserouter.depth().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext)) + " W: " + decimalformat.format(d0) + " PV: " + decimalformat.format(NoiseRouterData.peaksAndValleys((float)((float)d0))) + " AS: " + decimalformat.format(noiserouter.initialDensityWithoutJaggedness().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext)) + " N: " + decimalformat.format(noiserouter.finalDensity().compute((DensityFunction.FunctionContext)densityfunction$singlepointcontext)));
    }

    protected OptionalInt iterateNoiseColumn(LevelHeightAccessor level, RandomState random, int x, int z, @Nullable MutableObject<NoiseColumn> column, @Nullable Predicate<BlockState> stoppingState) {
        BlockState[] ablockstate;
        NoiseSettings noisesettings = ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().clampToHeightAccessor(level);
        int i = noisesettings.getCellHeight();
        int j = noisesettings.minY();
        int k = Mth.floorDiv((int)j, (int)i);
        int l = Mth.floorDiv((int)noisesettings.height(), (int)i);
        if (l <= 0) {
            return OptionalInt.empty();
        }
        if (column == null) {
            ablockstate = null;
        } else {
            ablockstate = new BlockState[noisesettings.height()];
            column.setValue((Object)new NoiseColumn(j, ablockstate));
        }
        int i1 = noisesettings.getCellWidth();
        int j1 = Math.floorDiv(x, i1);
        int k1 = Math.floorDiv(z, i1);
        int l1 = Math.floorMod(x, i1);
        int i2 = Math.floorMod(z, i1);
        int j2 = j1 * i1;
        int k2 = k1 * i1;
        double d0 = (double)l1 / (double)i1;
        double d1 = (double)i2 / (double)i1;
        NoiseChunk noisechunk = new NoiseChunk(1, random, j2, k2, noisesettings, (DensityFunctions.BeardifierOrMarker)DensityFunctions.BeardifierMarker.INSTANCE, (NoiseGeneratorSettings)this.settings.value(), this.globalFluidPicker.get(), Blender.empty());
        noisechunk.initializeForFirstCellX();
        noisechunk.advanceCellX(0);
        for (int l2 = l - 1; l2 >= 0; --l2) {
            noisechunk.selectCellYZ(l2, 0);
            for (int i3 = i - 1; i3 >= 0; --i3) {
                BlockState blockstate1;
                int j3 = (k + l2) * i + i3;
                double d2 = (double)i3 / (double)i;
                noisechunk.updateForY(j3, d2);
                noisechunk.updateForX(x, d0);
                noisechunk.updateForZ(z, d1);
                BlockState blockstate = noisechunk.getInterpolatedState();
                BlockState blockState = blockstate1 = blockstate == null ? ((NoiseGeneratorSettings)this.settings.value()).defaultBlock() : blockstate;
                if (ablockstate != null) {
                    int k3 = l2 * i + i3;
                    ablockstate[k3] = blockstate1;
                }
                if (stoppingState == null || !stoppingState.test(blockstate1)) continue;
                noisechunk.stopInterpolation();
                return OptionalInt.of(j3 + 1);
            }
        }
        noisechunk.stopInterpolation();
        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        if (!SharedConstants.debugVoidTerrain((ChunkPos)chunk.getPos())) {
            WorldGenerationContext worldgenerationcontext = new WorldGenerationContext((ChunkGenerator)this, (LevelHeightAccessor)level);
            this.buildSurface(chunk, worldgenerationcontext, random, structureManager, level.getBiomeManager(), (Registry<Biome>)level.registryAccess().registryOrThrow(Registries.BIOME), Blender.of((WorldGenRegion)level));
        }
    }

    @VisibleForTesting
    public void buildSurface(ChunkAccess chunk, WorldGenerationContext context, RandomState random, StructureManager structureManager, BiomeManager biomeManager, Registry<Biome> biomes, Blender blender) {
        NoiseChunk noisechunk = chunk.getOrCreateNoiseChunk(p_224321_ -> this.createNoiseChunk((ChunkAccess)p_224321_, structureManager, blender, random));
        NoiseGeneratorSettings noisegeneratorsettings = (NoiseGeneratorSettings)this.settings.value();
        random.surfaceSystem().buildSurface(random, biomeManager, biomes, noisegeneratorsettings.useLegacyRandomSource(), context, chunk, noisechunk, noisegeneratorsettings.surfaceRule());
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        BiomeManager biomemanager = biomeManager.withDifferentSource((p_255581_, p_255582_, p_255583_) -> this.biomeSource.getNoiseBiome(p_255581_, p_255582_, p_255583_, random.sampler()));
        WorldgenRandom worldgenrandom = new WorldgenRandom((RandomSource)new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        int i = 8;
        ChunkPos chunkpos = chunk.getPos();
        NoiseChunk noisechunk = chunk.getOrCreateNoiseChunk(p_224250_ -> this.createNoiseChunk((ChunkAccess)p_224250_, structureManager, Blender.of((WorldGenRegion)level), random));
        Aquifer aquifer = noisechunk.aquifer();
        CarvingContext carvingcontext = new CarvingContext(this, level.registryAccess(), chunk.getHeightAccessorForGeneration(), noisechunk, random, ((NoiseGeneratorSettings)this.settings.value()).surfaceRule());
        CarvingMask carvingmask = ((ProtoChunk)chunk).getOrCreateCarvingMask(step);
        for (int j = -8; j <= 8; ++j) {
            for (int k = -8; k <= 8; ++k) {
                ChunkPos chunkpos1 = new ChunkPos(chunkpos.x + j, chunkpos.z + k);
                ChunkAccess chunkaccess = level.getChunk(chunkpos1.x, chunkpos1.z);
                BiomeGenerationSettings biomegenerationsettings = chunkaccess.carverBiome(() -> this.getBiomeGenerationSettings((Holder<Biome>)this.biomeSource.getNoiseBiome(QuartPos.fromBlock((int)chunkpos1.getMinBlockX()), 0, QuartPos.fromBlock((int)chunkpos1.getMinBlockZ()), random.sampler())));
                Iterable iterable = biomegenerationsettings.getCarvers(step);
                int l = 0;
                for (Holder holder : iterable) {
                    ConfiguredWorldCarver configuredworldcarver = (ConfiguredWorldCarver)holder.value();
                    worldgenrandom.setLargeFeatureSeed(seed + (long)l, chunkpos1.x, chunkpos1.z);
                    if (configuredworldcarver.isStartChunk((RandomSource)worldgenrandom)) {
                        configuredworldcarver.carve(carvingcontext, chunk, arg_0 -> ((BiomeManager)biomemanager).getBiome(arg_0), (RandomSource)worldgenrandom, aquifer, chunkpos1, carvingmask);
                    }
                    ++l;
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        NoiseSettings noisesettings = ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().clampToHeightAccessor(chunk.getHeightAccessorForGeneration());
        int i = noisesettings.minY();
        int j = Mth.floorDiv((int)i, (int)noisesettings.getCellHeight());
        int k = Mth.floorDiv((int)noisesettings.height(), (int)noisesettings.getCellHeight());
        return k <= 0 ? CompletableFuture.completedFuture(chunk) : CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName((String)"wgen_fill_noise", () -> {
            ChunkAccess chunkaccess;
            int l = chunk.getSectionIndex(k * noisesettings.getCellHeight() - 1 + i);
            int i1 = chunk.getSectionIndex(i);
            HashSet set = Sets.newHashSet();
            for (int j1 = l; j1 >= i1; --j1) {
                LevelChunkSection levelchunksection = chunk.getSection(j1);
                levelchunksection.acquire();
                set.add(levelchunksection);
            }
            try {
                chunkaccess = this.doFill(blender, structureManager, randomState, chunk, j, k);
            }
            finally {
                for (LevelChunkSection levelchunksection1 : set) {
                    levelchunksection1.release();
                }
            }
            return chunkaccess;
        }), Util.backgroundExecutor());
    }

    private ChunkAccess doFill(Blender blender, StructureManager structureManager, RandomState random, ChunkAccess chunk, int minCellY, int cellCountY) {
        NoiseChunk noisechunk = chunk.getOrCreateNoiseChunk(p_224255_ -> this.createNoiseChunk((ChunkAccess)p_224255_, structureManager, blender, random));
        Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap1 = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkpos = chunk.getPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        Aquifer aquifer = noisechunk.aquifer();
        noisechunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int k = noisechunk.cellWidth();
        int l = noisechunk.cellHeight();
        int i1 = 16 / k;
        int j1 = 16 / k;
        for (int k1 = 0; k1 < i1; ++k1) {
            noisechunk.advanceCellX(k1);
            for (int l1 = 0; l1 < j1; ++l1) {
                int i2 = chunk.getSectionsCount() - 1;
                LevelChunkSection levelchunksection = chunk.getSection(i2);
                for (int j2 = cellCountY - 1; j2 >= 0; --j2) {
                    noisechunk.selectCellYZ(j2, l1);
                    for (int k2 = l - 1; k2 >= 0; --k2) {
                        int l2 = (minCellY + j2) * l + k2;
                        int i3 = l2 & 0xF;
                        int j3 = chunk.getSectionIndex(l2);
                        if (i2 != j3) {
                            i2 = j3;
                            levelchunksection = chunk.getSection(j3);
                        }
                        double d0 = (double)k2 / (double)l;
                        noisechunk.updateForY(l2, d0);
                        for (int k3 = 0; k3 < k; ++k3) {
                            int l3 = i + k1 * k + k3;
                            int i4 = l3 & 0xF;
                            double d1 = (double)k3 / (double)k;
                            noisechunk.updateForX(l3, d1);
                            for (int j4 = 0; j4 < k; ++j4) {
                                int k4 = j + l1 * k + j4;
                                int l4 = k4 & 0xF;
                                double d2 = (double)j4 / (double)k;
                                noisechunk.updateForZ(k4, d2);
                                BlockState blockstate = noisechunk.getInterpolatedState();
                                if (blockstate == null) {
                                    blockstate = ((NoiseGeneratorSettings)this.settings.value()).defaultBlock();
                                }
                                if ((blockstate = this.debugPreliminarySurfaceLevel(noisechunk, l3, l2, k4, blockstate)) == AIR || SharedConstants.debugVoidTerrain((ChunkPos)chunk.getPos())) continue;
                                levelchunksection.setBlockState(i4, i3, l4, blockstate, false);
                                heightmap.update(i4, l2, l4, blockstate);
                                heightmap1.update(i4, l2, l4, blockstate);
                                if (!aquifer.shouldScheduleFluidUpdate() || blockstate.getFluidState().isEmpty()) continue;
                                blockpos$mutableblockpos.set(l3, l2, k4);
                                chunk.markPosForPostprocessing((BlockPos)blockpos$mutableblockpos);
                            }
                        }
                    }
                }
            }
            noisechunk.swapSlices();
        }
        noisechunk.stopInterpolation();
        return chunk;
    }

    private BlockState debugPreliminarySurfaceLevel(NoiseChunk chunk, int x, int y, int z, BlockState state) {
        return state;
    }

    @Override
    public int getGenDepth() {
        return ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().height();
    }

    @Override
    public int getSeaLevel() {
        return ((NoiseGeneratorSettings)this.settings.value()).seaLevel();
    }

    @Override
    public int getMinY() {
        return ((NoiseGeneratorSettings)this.settings.value()).noiseSettings().minY();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        if (!((NoiseGeneratorSettings)this.settings.value()).disableMobGeneration()) {
            ChunkPos chunkpos = level.getCenter();
            Holder holder = level.getBiome(chunkpos.getWorldPosition().atY(level.getMaxBuildHeight() - 1));
            WorldgenRandom worldgenrandom = new WorldgenRandom((RandomSource)new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
            worldgenrandom.setDecorationSeed(level.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
            NaturalSpawner.spawnMobsForChunkGeneration((ServerLevelAccessor)level, (Holder)holder, (ChunkPos)chunkpos, (RandomSource)worldgenrandom);
        }
    }
}
