/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderSet
 *  net.minecraft.core.QuartPos
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.RegistryFileCodec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.StructureManager
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.BiomeSource
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.levelgen.GenerationStep$Decoration
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.RandomState
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationContext
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationStub
 *  net.minecraft.world.level.levelgen.structure.Structure$StructureSettings
 *  net.minecraft.world.level.levelgen.structure.StructureSpawnOverride
 *  net.minecraft.world.level.levelgen.structure.StructureStart
 *  net.minecraft.world.level.levelgen.structure.StructureType
 *  net.minecraft.world.level.levelgen.structure.TerrainAdjustment
 *  net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer
 *  net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 *  net.neoforged.neoforge.common.world.ModifiableStructureInfo
 *  net.neoforged.neoforge.common.world.ModifiableStructureInfo$StructureInfo
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo;

public abstract class Structure {
    public static final Codec<Structure> DIRECT_CODEC = BuiltInRegistries.STRUCTURE_TYPE.byNameCodec().dispatch(Structure::type, StructureType::codec);
    public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create((ResourceKey)Registries.STRUCTURE, DIRECT_CODEC);
    private final StructureSettings settings;
    private final ModifiableStructureInfo modifiableStructureInfo;

    public static <S extends Structure> RecordCodecBuilder<S, StructureSettings> settingsCodec(RecordCodecBuilder.Instance<S> instance) {
        return StructureSettings.CODEC.forGetter(p_226595_ -> p_226595_.modifiableStructureInfo().getOriginalStructureInfo().structureSettings());
    }

    public static <S extends Structure> MapCodec<S> simpleCodec(Function<StructureSettings, S> factory) {
        return RecordCodecBuilder.mapCodec(p_226611_ -> p_226611_.group(Structure.settingsCodec(p_226611_)).apply((Applicative)p_226611_, factory));
    }

    protected Structure(StructureSettings settings) {
        this.settings = settings;
        this.modifiableStructureInfo = new ModifiableStructureInfo(new ModifiableStructureInfo.StructureInfo(settings));
    }

    public HolderSet<Biome> biomes() {
        return this.settings.biomes;
    }

    public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
        return this.settings.spawnOverrides;
    }

    public GenerationStep.Decoration step() {
        return this.settings.step;
    }

    public TerrainAdjustment terrainAdaptation() {
        return this.settings.terrainAdaptation;
    }

    public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
        return this.terrainAdaptation() != TerrainAdjustment.NONE ? boundingBox.inflatedBy(12) : boundingBox;
    }

    public StructureStart generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, long seed, ChunkPos chunkPos, int references, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome) {
        StructurePiecesBuilder structurepiecesbuilder;
        StructureStart structurestart;
        GenerationContext structure$generationcontext = new GenerationContext(registryAccess, chunkGenerator, biomeSource, randomState, structureTemplateManager, seed, chunkPos, heightAccessor, validBiome);
        Optional<GenerationStub> optional = this.findValidGenerationPoint(structure$generationcontext);
        if (optional.isPresent() && (structurestart = new StructureStart(this, chunkPos, references, (structurepiecesbuilder = optional.get().getPiecesBuilder()).build())).isValid()) {
            return structurestart;
        }
        return StructureStart.INVALID_START;
    }

    protected static Optional<GenerationStub> onTopOfChunkCenter(GenerationContext context, Heightmap.Types heightmapTypes, Consumer<StructurePiecesBuilder> generator) {
        ChunkPos chunkpos = context.chunkPos();
        int i = chunkpos.getMiddleBlockX();
        int j = chunkpos.getMiddleBlockZ();
        int k = context.chunkGenerator().getFirstOccupiedHeight(i, j, heightmapTypes, context.heightAccessor(), context.randomState());
        return Optional.of(new GenerationStub(new BlockPos(i, k, j), generator));
    }

    private static boolean isValidBiome(GenerationStub stub, GenerationContext context) {
        BlockPos blockpos = stub.position();
        return context.validBiome.test(context.chunkGenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock((int)blockpos.getX()), QuartPos.fromBlock((int)blockpos.getY()), QuartPos.fromBlock((int)blockpos.getZ()), context.randomState.sampler()));
    }

    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer pieces) {
    }

    private static int[] getCornerHeights(GenerationContext context, int minX, int maxX, int minZ, int maxZ) {
        ChunkGenerator chunkgenerator = context.chunkGenerator();
        LevelHeightAccessor levelheightaccessor = context.heightAccessor();
        RandomState randomstate = context.randomState();
        return new int[]{chunkgenerator.getFirstOccupiedHeight(minX, minZ, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(minX, minZ + maxZ, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(minX + maxX, minZ, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(minX + maxX, minZ + maxZ, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate)};
    }

    public static int getMeanFirstOccupiedHeight(GenerationContext context, int minX, int maxX, int minZ, int maxZ) {
        int[] aint = Structure.getCornerHeights(context, minX, maxX, minZ, maxZ);
        return (aint[0] + aint[1] + aint[2] + aint[3]) / 4;
    }

    protected static int getLowestY(GenerationContext context, int maxX, int maxZ) {
        ChunkPos chunkpos = context.chunkPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        return Structure.getLowestY(context, i, j, maxX, maxZ);
    }

    protected static int getLowestY(GenerationContext context, int minX, int minZ, int maxX, int maxZ) {
        int[] aint = Structure.getCornerHeights(context, minX, maxX, minZ, maxZ);
        return Math.min(Math.min(aint[0], aint[1]), Math.min(aint[2], aint[3]));
    }

    @Deprecated
    protected BlockPos getLowestYIn5by5BoxOffset7Blocks(GenerationContext context, Rotation rotation) {
        int i = 5;
        int j = 5;
        if (rotation == Rotation.CLOCKWISE_90) {
            i = -5;
        } else if (rotation == Rotation.CLOCKWISE_180) {
            i = -5;
            j = -5;
        } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            j = -5;
        }
        ChunkPos chunkpos = context.chunkPos();
        int k = chunkpos.getBlockX(7);
        int l = chunkpos.getBlockZ(7);
        return new BlockPos(k, Structure.getLowestY(context, k, l, i, j), l);
    }

    protected abstract Optional<GenerationStub> findGenerationPoint(GenerationContext var1);

    public Optional<GenerationStub> findValidGenerationPoint(GenerationContext context) {
        return this.findGenerationPoint(context).filter(p_262911_ -> Structure.isValidBiome(p_262911_, context));
    }

    public abstract StructureType<?> type();

    public ModifiableStructureInfo modifiableStructureInfo() {
        return this.modifiableStructureInfo;
    }

    public StructureSettings getModifiedStructureSettings() {
        return this.modifiableStructureInfo().get().structureSettings();
    }
}
