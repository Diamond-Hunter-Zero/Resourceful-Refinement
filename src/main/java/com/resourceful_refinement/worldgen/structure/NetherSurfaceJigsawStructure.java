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

import java.util.List;
import java.util.Optional;

/**
 * Jigsaw structures in the Nether cannot use {@code WORLD_SURFACE_WG} — that heightmap resolves to
 * the column ceiling, not the walkable floor. This type scans the noise column for the highest
 * solid block with open space above it (the cave floor).
 */
public class NetherSurfaceJigsawStructure extends Structure {
    public static final MapCodec<NetherSurfaceJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
                    Codec.intRange(0, 7).fieldOf("size").forGetter(s -> s.maxDepth),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter)
            ).apply(instance, NetherSurfaceJigsawStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public NetherSurfaceJigsawStructure(Structure.StructureSettings settings,
                                       Holder<StructureTemplatePool> startPool,
                                       Optional<ResourceLocation> startJigsawName,
                                       int maxDepth,
                                       Optional<Heightmap.Types> projectStartToHeightmap,
                                       int maxDistanceFromCenter) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        BlockPos chunkMiddle = context.chunkPos().getMiddleBlockPosition(0);
        int x = chunkMiddle.getX();
        int z = chunkMiddle.getZ();

        // Sample the block column from generation noise before chunks are fully instantiated
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());

        // Scan downwards from below the nether ceiling (Y=115) to find the first real floor pocket
        int targetY = 32; // Fallback to lava sea level if nothing is found
        for (int y = 115; y > 31; y--) {
            BlockState stateCurrent = column.getBlock(y);
            BlockState stateBelow = column.getBlock(y - 1);

            // If the current block is air/liquid and the block below is solid ground, anchor here
            if ((stateCurrent.isAir() || stateCurrent.getFluidState().isEmpty())
                    && !stateBelow.isAir() && stateBelow.getFluidState().isEmpty()) {
                targetY = y;
                break;
            }
        }

        BlockPos targetPos = new BlockPos(x, targetY, z);
        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.maxDepth,
                targetPos,
                false, // useExpansionHack
                this.projectStartToHeightmap,
                this.maxDistanceFromCenter,
                PoolAliasLookup.EMPTY,                  // 1.21.1 additions: Empty registry aliases lookup
                DimensionPadding.ZERO,                 // 1.21.1 additions: No extra dimension border padding restrictions
                LiquidSettings.IGNORE_WATERLOGGING     // 1.21.1 additions: Standard liquid handling behavior
        );
    }

    @Override
    public StructureType<?> type() {
        // Replace with your actual deferred/deferred structure registry holder reference
        return ModStructureTypes.NETHER_SURFACE_JIGSAW.get();
    }
}
