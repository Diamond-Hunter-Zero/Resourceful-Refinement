package com.resourceful_refinement.worldgen.sinkhole;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.registry.ModStructureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * The Overworld Crystal Fissure: a shallow eroded sinkhole bowl carved into plains/hills/forest terrain,
 * its floor and sides seeded with Terran Crystal and Veridium (denser at the centre), scattered with
 * Terran Crystal Growths, and a single Crystal Fissure Bud (source {@code terran_crystal}) at the bottom
 * centre. Separate from the shared {@code crystal_node} structure because it carves terrain.
 */
public class OverworldSinkholeStructure extends Structure {
    public static final MapCodec<OverworldSinkholeStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    OverworldSinkholeConfig.MAP_CODEC.forGetter(s -> s.config)
            ).apply(instance, OverworldSinkholeStructure::new));

    private final OverworldSinkholeConfig config;

    public OverworldSinkholeStructure(StructureSettings settings, OverworldSinkholeConfig config) {
        super(settings);
        this.config = config;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int y = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        if (y <= context.heightAccessor().getMinBuildHeight() + config.maxDepth() + 2) {
            return Optional.empty();
        }
        // Reject underwater sites (lakes/rivers/oceans): fluid above the ocean floor would flood the bowl.
        int oceanFloor = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                context.heightAccessor(), context.randomState());
        if (y > oceanFloor) {
            return Optional.empty();
        }

        BlockPos center = new BlockPos(x, y, z);
        RandomSource random = context.random();
        return Optional.of(new GenerationStub(center, builder ->
                builder.addPiece(new OverworldSinkholePiece(center, config, random))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.OVERWORLD_SINKHOLE.get();
    }
}
