package com.resourceful_refinement.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.List;
import java.util.Optional;

/**
 * Jigsaw structures in the Nether cannot use {@code WORLD_SURFACE_WG} — that heightmap resolves to
 * the column ceiling, not the walkable floor. This type scans the noise column for the highest
 * solid block with open space above it (the cave floor).
 */
public class NetherSurfaceJigsawStructure extends Structure {
    public static final MapCodec<NetherSurfaceJigsawStructure> CODEC = RecordCodecBuilder.<NetherSurfaceJigsawStructure>mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
                    Codec.intRange(0, 20).fieldOf("size").forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeightOffset),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", LiquidSettings.IGNORE_WATERLOGGING).forGetter(s -> s.liquidSettings)
            ).apply(instance, NetherSurfaceJigsawStructure::new)
    ).validate(structure -> {
        int terrainPadding = switch (structure.terrainAdaptation()) {
            case NONE -> 0;
            default -> 12;
        };
        return structure.maxDistanceFromCenter + terrainPadding > 128
                ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128")
                : DataResult.success(structure);
    });

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeightOffset;
    private final int maxDistanceFromCenter;
    private final LiquidSettings liquidSettings;

    public NetherSurfaceJigsawStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            HeightProvider startHeightOffset,
            int maxDistanceFromCenter,
            LiquidSettings liquidSettings
    ) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeightOffset = startHeightOffset;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.liquidSettings = liquidSettings;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int floorY = findNetherFloorY(context, centerX, centerZ);
        int offsetY = this.startHeightOffset.sample(
                context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor())
        );
        BlockPos startPos = new BlockPos(chunkPos.getMinBlockX(), floorY + offsetY, chunkPos.getMinBlockZ());

        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.maxDepth,
                startPos,
                false,
                Optional.empty(),
                this.maxDistanceFromCenter,
                PoolAliasLookup.create(List.<PoolAliasBinding>of(), startPos, context.seed()),
                JigsawStructure.DEFAULT_DIMENSION_PADDING,
                this.liquidSettings
        );
    }

    /**
     * Finds the highest solid block in the column that has at least two air/fluid blocks above it —
     * the walkable cave floor rather than the nether ceiling.
     */
    static int findNetherFloorY(GenerationContext context, int x, int z) {
        ChunkGenerator generator = context.chunkGenerator();
        NoiseColumn column = generator.getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        LevelHeightAccessor level = context.heightAccessor();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 3;

        int floorY = -1;
        for (int y = minY; y < maxY; y++) {
            if (isNetherGround(column.getBlock(y))
                    && isOpen(column.getBlock(y + 1))
                    && isOpen(column.getBlock(y + 2))) {
                floorY = y;
            }
        }

        return floorY >= 0 ? floorY + 1 : 64;
    }

    private static boolean isNetherGround(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && state.blocksMotion();
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.NETHER_SURFACE_JIGSAW.get();
    }
}
