/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.WorldGenerationContext
 *  net.minecraft.world.level.levelgen.heightproviders.HeightProvider
 *  net.minecraft.world.level.levelgen.structure.Structure
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationContext
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationStub
 *  net.minecraft.world.level.levelgen.structure.Structure$StructureSettings
 *  net.minecraft.world.level.levelgen.structure.StructureType
 *  net.minecraft.world.level.levelgen.structure.pools.DimensionPadding
 *  net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement
 *  net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
 *  net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding
 *  net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup
 *  net.minecraft.world.level.levelgen.structure.structures.JigsawStructure$1
 *  net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public final class JigsawStructure
extends Structure {
    public static final DimensionPadding DEFAULT_DIMENSION_PADDING = DimensionPadding.ZERO;
    public static final LiquidSettings DEFAULT_LIQUID_SETTINGS = LiquidSettings.APPLY_WATERLOGGING;
    public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
    public static final int MIN_DEPTH = 0;
    public static final int MAX_DEPTH = 20;
    public static final MapCodec<JigsawStructure> CODEC = RecordCodecBuilder.mapCodec(p_227640_ -> p_227640_.group((App)JigsawStructure.settingsCodec((RecordCodecBuilder.Instance)p_227640_), (App)StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(p_227656_ -> p_227656_.startPool), (App)ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(p_227654_ -> p_227654_.startJigsawName), (App)Codec.intRange((int)0, (int)20).fieldOf("size").forGetter(p_227652_ -> p_227652_.maxDepth), (App)HeightProvider.CODEC.fieldOf("start_height").forGetter(p_227649_ -> p_227649_.startHeight), (App)Codec.BOOL.fieldOf("use_expansion_hack").forGetter(p_227646_ -> p_227646_.useExpansionHack), (App)Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(p_227644_ -> p_227644_.projectStartToHeightmap), (App)Codec.intRange((int)1, (int)128).fieldOf("max_distance_from_center").forGetter(p_227642_ -> p_227642_.maxDistanceFromCenter), (App)Codec.list((Codec)PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(p_307187_ -> p_307187_.poolAliases), (App)DimensionPadding.CODEC.optionalFieldOf("dimension_padding", (Object)DEFAULT_DIMENSION_PADDING).forGetter(p_348455_ -> p_348455_.dimensionPadding), (App)LiquidSettings.CODEC.optionalFieldOf("liquid_settings", (Object)DEFAULT_LIQUID_SETTINGS).forGetter(p_352036_ -> p_352036_.liquidSettings)).apply((Applicative)p_227640_, JigsawStructure::new)).validate(JigsawStructure::verifyRange);
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    private static DataResult<JigsawStructure> verifyRange(JigsawStructure structure) {
        int i = switch (1.$SwitchMap$net$minecraft$world$level$levelgen$structure$TerrainAdjustment[structure.terrainAdaptation().ordinal()]) {
            default -> throw new MatchException(null, null);
            case 1 -> 0;
            case 2, 3, 4, 5 -> 12;
        };
        return structure.maxDistanceFromCenter + i > 128 ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128") : DataResult.success((Object)((Object)structure));
    }

    public JigsawStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, List<PoolAliasBinding> poolAliases, DimensionPadding dimensionPadding, LiquidSettings liquidSettings) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliases = poolAliases;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    public JigsawStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, HeightProvider startHeight, boolean useExpansionHack, Heightmap.Types projectStartToHeightmap) {
        this(settings, startPool, Optional.empty(), maxDepth, startHeight, useExpansionHack, Optional.of(projectStartToHeightmap), 80, List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    public JigsawStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, HeightProvider startHeight, boolean useExpansionHack) {
        this(settings, startPool, Optional.empty(), maxDepth, startHeight, useExpansionHack, Optional.empty(), 80, List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunkpos = context.chunkPos();
        int i = this.startHeight.sample((RandomSource)context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), i, chunkpos.getMinBlockZ());
        return JigsawPlacement.addPieces((Structure.GenerationContext)context, this.startPool, this.startJigsawName, (int)this.maxDepth, (BlockPos)blockpos, (boolean)this.useExpansionHack, this.projectStartToHeightmap, (int)this.maxDistanceFromCenter, (PoolAliasLookup)PoolAliasLookup.create(this.poolAliases, (BlockPos)blockpos, (long)context.seed()), (DimensionPadding)this.dimensionPadding, (LiquidSettings)this.liquidSettings);
    }

    public StructureType<?> type() {
        return StructureType.JIGSAW;
    }
}
