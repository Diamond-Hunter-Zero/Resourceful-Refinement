package com.resourceful_refinement.worldgen.crystalnode;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.registry.ModStructureTypes;
import com.resourceful_refinement.worldgen.structure.NetherFloorFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * One configurable structure type for the small surface resource nodes — Resonance Crystals (End/Nether/
 * Overworld) and the End &amp; Nether Crystal Fissures. All behaviour is driven by {@link CrystalNodeConfig}
 * set in the structure JSON, so each variant is just data + its own {@code random_spread} set.
 *
 * <p>Placement sits at the chunk's middle block (so the [[Resource Resonator]] scan, which reports the chunk
 * middle, is exact in X/Z). Anchoring is surface ({@code WORLD_SURFACE_WG}) or Nether cave floor
 * ({@link NetherFloorFinder}). The actual block work is in {@link CrystalNodePiece}.</p>
 */
public class CrystalNodeStructure extends Structure {
    public static final MapCodec<CrystalNodeStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    CrystalNodeConfig.MAP_CODEC.forGetter(s -> s.config)
            ).apply(instance, CrystalNodeStructure::new));

    private final CrystalNodeConfig config;

    public CrystalNodeStructure(StructureSettings settings, CrystalNodeConfig config) {
        super(settings);
        this.config = config;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();

        int y;
        if (config.anchor() == CrystalNodeConfig.AnchorMode.NETHER_FLOOR) {
            OptionalInt floor = NetherFloorFinder.findFloorY(context, x, z);
            if (floor.isEmpty()) {
                return Optional.empty();
            }
            y = floor.getAsInt();
        } else {
            ChunkGenerator generator = context.chunkGenerator();
            LevelHeightAccessor heightAccessor = context.heightAccessor();
            RandomState randomState = context.randomState();
            y = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
            if (y <= heightAccessor.getMinBuildHeight() + 5) {
                return Optional.empty();
            }
            // Reject underwater surface sites (fluid above the ocean floor). No effect in the End (no water).
            int oceanFloor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heightAccessor, randomState);
            if (y > oceanFloor) {
                return Optional.empty();
            }
        }

        BlockPos center = new BlockPos(x, y, z);
        RandomSource random = context.random();
        return Optional.of(new GenerationStub(center, builder ->
                builder.addPiece(new CrystalNodePiece(center, config, random))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.CRYSTAL_NODE.get();
    }
}
