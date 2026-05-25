/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 *  net.minecraft.CrashReport
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.ReportedException
 *  net.minecraft.SharedConstants
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup
 *  net.minecraft.core.HolderSet
 *  net.minecraft.core.Registry
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.SectionPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.protocol.game.DebugPackets
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.WorldGenRegion
 *  net.minecraft.util.RandomSource
 *  net.minecraft.util.random.WeightedRandomList
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.NoiseColumn
 *  net.minecraft.world.level.StructureManager
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.BiomeGenerationSettings
 *  net.minecraft.world.level.biome.BiomeManager
 *  net.minecraft.world.level.biome.BiomeResolver
 *  net.minecraft.world.level.biome.BiomeSource
 *  net.minecraft.world.level.biome.FeatureSorter
 *  net.minecraft.world.level.biome.FeatureSorter$StepFeatureData
 *  net.minecraft.world.level.biome.MobSpawnSettings$SpawnerData
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.ChunkGeneratorStructureState
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.chunk.StructureAccess
 *  net.minecraft.world.level.chunk.status.ChunkStatus
 *  net.minecraft.world.level.levelgen.GenerationStep$Carving
 *  net.minecraft.world.level.levelgen.GenerationStep$Decoration
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.LegacyRandomSource
 *  net.minecraft.world.level.levelgen.RandomState
 *  net.minecraft.world.level.levelgen.RandomSupport
 *  net.minecraft.world.level.levelgen.WorldgenRandom
 *  net.minecraft.world.level.levelgen.XoroshiroRandomSource
 *  net.minecraft.world.level.levelgen.blending.Blender
 *  net.minecraft.world.level.levelgen.placement.PlacedFeature
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.levelgen.structure.Structure
 *  net.minecraft.world.level.levelgen.structure.StructureCheckResult
 *  net.minecraft.world.level.levelgen.structure.StructureSet
 *  net.minecraft.world.level.levelgen.structure.StructureSet$StructureSelectionEntry
 *  net.minecraft.world.level.levelgen.structure.StructureSpawnOverride
 *  net.minecraft.world.level.levelgen.structure.StructureSpawnOverride$BoundingBoxType
 *  net.minecraft.world.level.levelgen.structure.StructureStart
 *  net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement
 *  net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
 *  net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 *  net.neoforged.neoforge.common.util.Lazy
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package net.minecraft.world.level.chunk;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus;

public abstract class ChunkGenerator {
    public static final Codec<ChunkGenerator> CODEC = BuiltInRegistries.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    protected final BiomeSource biomeSource;
    private final Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;
    private final Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter;

    public ChunkGenerator(BiomeSource biomeSource) {
        this(biomeSource, p_223234_ -> ((Biome)p_223234_.value()).getGenerationSettings());
    }

    public ChunkGenerator(BiomeSource biomeSource, Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter) {
        this.biomeSource = biomeSource;
        this.generationSettingsGetter = generationSettingsGetter;
        this.featuresPerStep = Lazy.of(() -> FeatureSorter.buildFeaturesPerStep(List.copyOf(biomeSource.possibleBiomes()), p_223216_ -> ((BiomeGenerationSettings)generationSettingsGetter.apply((Holder<Biome>)p_223216_)).features(), (boolean)true));
    }

    @ApiStatus.Internal
    public void refreshFeaturesPerStep() {
        ((Lazy)this.featuresPerStep).invalidate();
    }

    public void validate() {
        this.featuresPerStep.get();
    }

    protected abstract MapCodec<? extends ChunkGenerator> codec();

    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed) {
        return ChunkGeneratorStructureState.createForNormal((RandomState)randomState, (long)seed, (BiomeSource)this.biomeSource, structureSetLookup);
    }

    public Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return BuiltInRegistries.CHUNK_GENERATOR.getResourceKey(this.codec());
    }

    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName((String)"init_biomes", () -> {
            chunk.fillBiomesFromNoise((BiomeResolver)this.biomeSource, randomState.sampler());
            return chunk;
        }), Util.backgroundExecutor());
    }

    public abstract void applyCarvers(WorldGenRegion var1, long var2, RandomState var4, BiomeManager var5, StructureManager var6, ChunkAccess var7, GenerationStep.Carving var8);

    @Nullable
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel level, HolderSet<Structure> structure, BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        ChunkGeneratorStructureState chunkgeneratorstructurestate = level.getChunkSource().getGeneratorState();
        Object2ObjectArrayMap map = new Object2ObjectArrayMap();
        for (Holder holder : structure) {
            for (StructurePlacement structureplacement : chunkgeneratorstructurestate.getPlacementsForStructure(holder)) {
                map.computeIfAbsent(structureplacement, p_223127_ -> new ObjectArraySet()).add(holder);
            }
        }
        if (map.isEmpty()) {
            return null;
        }
        Pair<BlockPos, Holder<Structure>> pair2 = null;
        double d2 = Double.MAX_VALUE;
        StructureManager structuremanager = level.structureManager();
        ArrayList list = new ArrayList(map.size());
        for (Map.Entry entry : map.entrySet()) {
            StructurePlacement structureplacement1 = (StructurePlacement)entry.getKey();
            if (structureplacement1 instanceof ConcentricRingsStructurePlacement) {
                BlockPos blockpos;
                double d0;
                ConcentricRingsStructurePlacement concentricringsstructureplacement = (ConcentricRingsStructurePlacement)structureplacement1;
                Pair<BlockPos, Holder<Structure>> pair = this.getNearestGeneratedStructure((Set)entry.getValue(), level, structuremanager, pos, skipKnownStructures, concentricringsstructureplacement);
                if (pair == null || !((d0 = pos.distSqr((Vec3i)(blockpos = (BlockPos)pair.getFirst()))) < d2)) continue;
                d2 = d0;
                pair2 = pair;
                continue;
            }
            if (!(structureplacement1 instanceof RandomSpreadStructurePlacement)) continue;
            list.add(entry);
        }
        if (!list.isEmpty()) {
            int i = SectionPos.blockToSectionCoord((int)pos.getX());
            int j = SectionPos.blockToSectionCoord((int)pos.getZ());
            for (int k = 0; k <= searchRadius; ++k) {
                boolean flag = false;
                for (Map.Entry entry1 : list) {
                    RandomSpreadStructurePlacement randomspreadstructureplacement = (RandomSpreadStructurePlacement)entry1.getKey();
                    Pair<BlockPos, Holder<Structure>> pair1 = ChunkGenerator.getNearestGeneratedStructure((Set)entry1.getValue(), (LevelReader)level, structuremanager, i, j, k, skipKnownStructures, chunkgeneratorstructurestate.getLevelSeed(), randomspreadstructureplacement);
                    if (pair1 == null) continue;
                    flag = true;
                    double d1 = pos.distSqr((Vec3i)pair1.getFirst());
                    if (!(d1 < d2)) continue;
                    d2 = d1;
                    pair2 = pair1;
                }
                if (!flag) continue;
                return pair2;
            }
        }
        return pair2;
    }

    @Nullable
    private Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> structureHoldersSet, ServerLevel level, StructureManager structureManager, BlockPos pos, boolean skipKnownStructures, ConcentricRingsStructurePlacement placement) {
        List list = level.getChunkSource().getGeneratorState().getRingPositionsFor(placement);
        if (list == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        }
        Pair<BlockPos, Holder<Structure>> pair = null;
        double d0 = Double.MAX_VALUE;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (ChunkPos chunkpos : list) {
            Pair<BlockPos, Holder<Structure>> pair1;
            blockpos$mutableblockpos.set(SectionPos.sectionToBlockCoord((int)chunkpos.x, (int)8), 32, SectionPos.sectionToBlockCoord((int)chunkpos.z, (int)8));
            double d1 = blockpos$mutableblockpos.distSqr((Vec3i)pos);
            boolean flag = pair == null || d1 < d0;
            if (!flag || (pair1 = ChunkGenerator.getStructureGeneratingAt(structureHoldersSet, (LevelReader)level, structureManager, skipKnownStructures, (StructurePlacement)placement, chunkpos)) == null) continue;
            pair = pair1;
            d0 = d1;
        }
        return pair;
    }

    @Nullable
    private static Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> structureHoldersSet, LevelReader level, StructureManager structureManager, int x, int y, int z, boolean skipKnownStructures, long seed, RandomSpreadStructurePlacement spreadPlacement) {
        int i = spreadPlacement.spacing();
        for (int j = -z; j <= z; ++j) {
            boolean flag = j == -z || j == z;
            for (int k = -z; k <= z; ++k) {
                int i1;
                int l;
                ChunkPos chunkpos;
                Pair<BlockPos, Holder<Structure>> pair;
                boolean flag1;
                boolean bl = flag1 = k == -z || k == z;
                if (!flag && !flag1 || (pair = ChunkGenerator.getStructureGeneratingAt(structureHoldersSet, level, structureManager, skipKnownStructures, (StructurePlacement)spreadPlacement, chunkpos = spreadPlacement.getPotentialStructureChunk(seed, l = x + i * j, i1 = y + i * k))) == null) continue;
                return pair;
            }
        }
        return null;
    }

    @Nullable
    private static Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(Set<Holder<Structure>> structureHoldersSet, LevelReader level, StructureManager structureManager, boolean skipKnownStructures, StructurePlacement placement, ChunkPos chunkPos) {
        for (Holder<Structure> holder : structureHoldersSet) {
            StructureCheckResult structurecheckresult = structureManager.checkStructurePresence(chunkPos, (Structure)holder.value(), placement, skipKnownStructures);
            if (structurecheckresult == StructureCheckResult.START_NOT_PRESENT) continue;
            if (!skipKnownStructures && structurecheckresult == StructureCheckResult.START_PRESENT) {
                return Pair.of((Object)placement.getLocatePos(chunkPos), holder);
            }
            ChunkAccess chunkaccess = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart structurestart = structureManager.getStartForStructure(SectionPos.bottomOf((ChunkAccess)chunkaccess), (Structure)holder.value(), (StructureAccess)chunkaccess);
            if (structurestart == null || !structurestart.isValid() || skipKnownStructures && !ChunkGenerator.tryAddReference(structureManager, structurestart)) continue;
            return Pair.of((Object)placement.getLocatePos(structurestart.getChunkPos()), holder);
        }
        return null;
    }

    private static boolean tryAddReference(StructureManager structureManager, StructureStart structureStart) {
        if (structureStart.canBeReferenced()) {
            structureManager.addReference(structureStart);
            return true;
        }
        return false;
    }

    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        ChunkPos chunkpos = chunk.getPos();
        if (!SharedConstants.debugVoidTerrain((ChunkPos)chunkpos)) {
            SectionPos sectionpos = SectionPos.of((ChunkPos)chunkpos, (int)level.getMinSection());
            BlockPos blockpos = sectionpos.origin();
            Registry registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Map<Integer, List<Structure>> map = registry.stream().collect(Collectors.groupingBy(p_223103_ -> p_223103_.step().ordinal()));
            List<FeatureSorter.StepFeatureData> list = this.featuresPerStep.get();
            WorldgenRandom worldgenrandom = new WorldgenRandom((RandomSource)new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
            long i = worldgenrandom.setDecorationSeed(level.getSeed(), blockpos.getX(), blockpos.getZ());
            ObjectArraySet set = new ObjectArraySet();
            ChunkPos.rangeClosed((ChunkPos)sectionpos.chunk(), (int)1).forEach(arg_0 -> ChunkGenerator.lambda$applyBiomeDecoration$6(level, (Set)set, arg_0));
            set.retainAll(this.biomeSource.possibleBiomes());
            int j = list.size();
            try {
                Registry registry1 = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
                int i1 = Math.max(GenerationStep.Decoration.values().length, j);
                for (int k = 0; k < i1; ++k) {
                    int l = 0;
                    if (structureManager.shouldGenerateStructures()) {
                        for (Structure structure : map.getOrDefault(k, Collections.emptyList())) {
                            worldgenrandom.setFeatureSeed(i, l, k);
                            Supplier<String> supplier = () -> registry.getResourceKey((Object)structure).map(Object::toString).orElseGet(structure::toString);
                            try {
                                level.setCurrentlyGenerating(supplier);
                                structureManager.startsForStructure(sectionpos, structure).forEach(p_223086_ -> p_223086_.placeInChunk(level, structureManager, this, (RandomSource)worldgenrandom, ChunkGenerator.getWritableArea(chunk), chunkpos));
                            }
                            catch (Exception exception) {
                                CrashReport crashreport1 = CrashReport.forThrowable((Throwable)exception, (String)"Feature placement");
                                crashreport1.addCategory("Feature").setDetail("Description", supplier::get);
                                throw new ReportedException(crashreport1);
                            }
                            ++l;
                        }
                    }
                    if (k >= j) continue;
                    IntArraySet intset = new IntArraySet();
                    for (Holder holder : set) {
                        List list1 = this.generationSettingsGetter.apply((Holder<Biome>)holder).features();
                        if (k >= list1.size()) continue;
                        HolderSet holderset = (HolderSet)list1.get(k);
                        FeatureSorter.StepFeatureData featuresorter$stepfeaturedata1 = list.get(k);
                        holderset.stream().map(Holder::value).forEach(arg_0 -> ChunkGenerator.lambda$applyBiomeDecoration$9((IntSet)intset, featuresorter$stepfeaturedata1, arg_0));
                    }
                    int j1 = intset.size();
                    int[] aint = intset.toIntArray();
                    Arrays.sort(aint);
                    FeatureSorter.StepFeatureData featuresorter$stepfeaturedata = list.get(k);
                    for (int k1 = 0; k1 < j1; ++k1) {
                        int l1 = aint[k1];
                        PlacedFeature placedfeature = (PlacedFeature)featuresorter$stepfeaturedata.features().get(l1);
                        Supplier<String> supplier1 = () -> registry1.getResourceKey((Object)placedfeature).map(Object::toString).orElseGet(() -> ((PlacedFeature)placedfeature).toString());
                        worldgenrandom.setFeatureSeed(i, l1, k);
                        try {
                            level.setCurrentlyGenerating(supplier1);
                            placedfeature.placeWithBiomeCheck(level, this, (RandomSource)worldgenrandom, blockpos);
                            continue;
                        }
                        catch (Exception exception1) {
                            CrashReport crashreport2 = CrashReport.forThrowable((Throwable)exception1, (String)"Feature placement");
                            crashreport2.addCategory("Feature").setDetail("Description", supplier1::get);
                            throw new ReportedException(crashreport2);
                        }
                    }
                }
                level.setCurrentlyGenerating(null);
            }
            catch (Exception exception2) {
                CrashReport crashreport = CrashReport.forThrowable((Throwable)exception2, (String)"Biome decoration");
                crashreport.addCategory("Generation").setDetail("CenterX", (Object)chunkpos.x).setDetail("CenterZ", (Object)chunkpos.z).setDetail("Decoration Seed", (Object)i);
                throw new ReportedException(crashreport);
            }
        }
    }

    private static BoundingBox getWritableArea(ChunkAccess chunk) {
        ChunkPos chunkpos = chunk.getPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        LevelHeightAccessor levelheightaccessor = chunk.getHeightAccessorForGeneration();
        int k = levelheightaccessor.getMinBuildHeight() + 1;
        int l = levelheightaccessor.getMaxBuildHeight() - 1;
        return new BoundingBox(i, k, j, i + 15, l, j + 15);
    }

    public abstract void buildSurface(WorldGenRegion var1, StructureManager var2, RandomState var3, ChunkAccess var4);

    public abstract void spawnOriginalMobs(WorldGenRegion var1);

    public int getSpawnHeight(LevelHeightAccessor level) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.biomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> biome, StructureManager structureManager, MobCategory category, BlockPos pos) {
        Map map = structureManager.getAllStructuresAt(pos);
        for (Map.Entry entry : map.entrySet()) {
            Structure structure = (Structure)entry.getKey();
            StructureSpawnOverride structurespawnoverride = (StructureSpawnOverride)structure.spawnOverrides().get(category);
            if (structurespawnoverride == null) continue;
            MutableBoolean mutableboolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = structurespawnoverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE ? p_223065_ -> structureManager.structureHasPieceAt(pos, p_223065_) : p_223130_ -> p_223130_.getBoundingBox().isInside((Vec3i)pos);
            structureManager.fillStartsForStructure(structure, (LongSet)entry.getValue(), p_223220_ -> {
                if (mutableboolean.isFalse() && predicate.test((StructureStart)p_223220_)) {
                    mutableboolean.setTrue();
                }
            });
            if (!mutableboolean.isTrue()) continue;
            return structurespawnoverride.spawns();
        }
        return ((Biome)biome.value()).getMobSettings().getMobs(category);
    }

    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager structureTemplateManager) {
        ChunkPos chunkpos = chunk.getPos();
        SectionPos sectionpos = SectionPos.bottomOf((ChunkAccess)chunk);
        RandomState randomstate = structureState.randomState();
        structureState.possibleStructureSets().forEach(p_255564_ -> {
            StructurePlacement structureplacement = ((StructureSet)p_255564_.value()).placement();
            List list = ((StructureSet)p_255564_.value()).structures();
            for (StructureSet.StructureSelectionEntry structureset$structureselectionentry : list) {
                StructureStart structurestart = structureManager.getStartForStructure(sectionpos, (Structure)structureset$structureselectionentry.structure().value(), (StructureAccess)chunk);
                if (structurestart == null || !structurestart.isValid()) continue;
                return;
            }
            if (structureplacement.isStructureChunk(structureState, chunkpos.x, chunkpos.z)) {
                if (list.size() == 1) {
                    this.tryGenerateStructure((StructureSet.StructureSelectionEntry)list.get(0), structureManager, registryAccess, randomstate, structureTemplateManager, structureState.getLevelSeed(), chunk, chunkpos, sectionpos);
                } else {
                    ArrayList arraylist = new ArrayList(list.size());
                    arraylist.addAll(list);
                    WorldgenRandom worldgenrandom = new WorldgenRandom((RandomSource)new LegacyRandomSource(0L));
                    worldgenrandom.setLargeFeatureSeed(structureState.getLevelSeed(), chunkpos.x, chunkpos.z);
                    int i = 0;
                    for (StructureSet.StructureSelectionEntry structureset$structureselectionentry1 : arraylist) {
                        i += structureset$structureselectionentry1.weight();
                    }
                    while (!arraylist.isEmpty()) {
                        StructureSet.StructureSelectionEntry structureset$structureselectionentry2;
                        int j = worldgenrandom.nextInt(i);
                        int k = 0;
                        Iterator iterator = arraylist.iterator();
                        while (iterator.hasNext() && (j -= (structureset$structureselectionentry2 = (StructureSet.StructureSelectionEntry)iterator.next()).weight()) >= 0) {
                            ++k;
                        }
                        StructureSet.StructureSelectionEntry structureset$structureselectionentry3 = (StructureSet.StructureSelectionEntry)arraylist.get(k);
                        if (this.tryGenerateStructure(structureset$structureselectionentry3, structureManager, registryAccess, randomstate, structureTemplateManager, structureState.getLevelSeed(), chunk, chunkpos, sectionpos)) {
                            return;
                        }
                        arraylist.remove(k);
                        i -= structureset$structureselectionentry3.weight();
                    }
                }
            }
        });
    }

    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState random, StructureTemplateManager structureTemplateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos) {
        Structure structure = (Structure)structureSelectionEntry.structure().value();
        int i = ChunkGenerator.fetchReferences(structureManager, chunk, sectionPos, structure);
        HolderSet holderset = structure.biomes();
        Predicate<Holder> predicate = arg_0 -> ((HolderSet)holderset).contains(arg_0);
        StructureStart structurestart = structure.generate(registryAccess, this, this.biomeSource, random, structureTemplateManager, seed, chunkPos, i, (LevelHeightAccessor)chunk, predicate);
        if (structurestart.isValid()) {
            structureManager.setStartForStructure(sectionPos, structure, structurestart, (StructureAccess)chunk);
            return true;
        }
        return false;
    }

    private static int fetchReferences(StructureManager structureManager, ChunkAccess chunk, SectionPos sectionPos, Structure structure) {
        StructureStart structurestart = structureManager.getStartForStructure(sectionPos, structure, (StructureAccess)chunk);
        return structurestart != null ? structurestart.getReferences() : 0;
    }

    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
        int i = 8;
        ChunkPos chunkpos = chunk.getPos();
        int j = chunkpos.x;
        int k = chunkpos.z;
        int l = chunkpos.getMinBlockX();
        int i1 = chunkpos.getMinBlockZ();
        SectionPos sectionpos = SectionPos.bottomOf((ChunkAccess)chunk);
        for (int j1 = j - 8; j1 <= j + 8; ++j1) {
            for (int k1 = k - 8; k1 <= k + 8; ++k1) {
                long l1 = ChunkPos.asLong((int)j1, (int)k1);
                for (StructureStart structurestart : level.getChunk(j1, k1).getAllStarts().values()) {
                    try {
                        if (!structurestart.isValid() || !structurestart.getBoundingBox().intersects(l, i1, l + 15, i1 + 15)) continue;
                        structureManager.addReferenceForStructure(sectionpos, structurestart.getStructure(), l1, (StructureAccess)chunk);
                        DebugPackets.sendStructurePacket((WorldGenLevel)level, (StructureStart)structurestart);
                    }
                    catch (Exception exception) {
                        CrashReport crashreport = CrashReport.forThrowable((Throwable)exception, (String)"Generating structure reference");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Structure");
                        Optional optional = level.registryAccess().registry(Registries.STRUCTURE);
                        crashreportcategory.setDetail("Id", () -> optional.map(p_258977_ -> p_258977_.getKey((Object)structurestart.getStructure()).toString()).orElse("UNKNOWN"));
                        crashreportcategory.setDetail("Name", () -> BuiltInRegistries.STRUCTURE_TYPE.getKey((Object)structurestart.getStructure().type()).toString());
                        crashreportcategory.setDetail("Class", () -> structurestart.getStructure().getClass().getCanonicalName());
                        throw new ReportedException(crashreport);
                    }
                }
            }
        }
    }

    public abstract CompletableFuture<ChunkAccess> fillFromNoise(Blender var1, RandomState var2, StructureManager var3, ChunkAccess var4);

    public abstract int getSeaLevel();

    public abstract int getMinY();

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4, RandomState var5);

    public abstract NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3, RandomState var4);

    public int getFirstFreeHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return this.getBaseHeight(x, z, type, level, random);
    }

    public int getFirstOccupiedHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor level, RandomState random) {
        return this.getBaseHeight(x, z, types, level, random) - 1;
    }

    public abstract void addDebugScreenInfo(List<String> var1, RandomState var2, BlockPos var3);

    @Deprecated
    public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> biome) {
        return this.generationSettingsGetter.apply(biome);
    }

    private static /* synthetic */ void lambda$applyBiomeDecoration$9(IntSet intset, FeatureSorter.StepFeatureData featuresorter$stepfeaturedata1, PlacedFeature p_223174_) {
        intset.add(featuresorter$stepfeaturedata1.indexMapping().applyAsInt(p_223174_));
    }

    private static /* synthetic */ void lambda$applyBiomeDecoration$6(WorldGenLevel level, Set set, ChunkPos p_223093_) {
        ChunkAccess chunkaccess = level.getChunk(p_223093_.x, p_223093_.z);
        for (LevelChunkSection levelchunksection : chunkaccess.getSections()) {
            levelchunksection.getBiomes().getAll(set::add);
        }
    }
}
