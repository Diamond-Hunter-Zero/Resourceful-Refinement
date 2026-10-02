package com.resourceful_refinement.worldgen.choral;

import com.resourceful_refinement.registry.ModBiomes;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class ChoralClusterMountainPiece extends StructurePiece {
    private BlockPos center;
    private float radius;
    private float height;
    private float radiusSquared;
    private int terrainSeed;
    private ResourceLocation biomeId;

    public ChoralClusterMountainPiece(BlockPos center, float radius, float height, RandomSource random, Holder<Biome> biome) {
        super(ModStructurePieceTypes.CHORAL_CLUSTER_MOUNTAIN_PIECE.get(), random.nextInt(), makeBox(center, radius, height));
        this.center = center;
        this.radius = radius;
        this.height = height;
        this.radiusSquared = radius * radius;
        this.terrainSeed = random.nextInt();
        this.biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(ModBiomes.CHORAL_CLUSTERS.location());
    }

    public ChoralClusterMountainPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieceTypes.CHORAL_CLUSTER_MOUNTAIN_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("center_x"), tag.getInt("center_y"), tag.getInt("center_z"));
        this.radius = tag.getFloat("radius");
        this.height = tag.getFloat("height");
        this.radiusSquared = radius * radius;
        this.terrainSeed = tag.getInt("terrain_seed");
        this.biomeId = ResourceLocation.parse(tag.getString("biome"));
        this.boundingBox = makeBox(center, radius, height);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("center_x", center.getX());
        tag.putInt("center_y", center.getY());
        tag.putInt("center_z", center.getZ());
        tag.putFloat("radius", radius);
        tag.putFloat("height", height);
        tag.putInt("terrain_seed", terrainSeed);
        tag.putString("biome", biomeId.toString());
    }

    @Override
    public void postProcess(
            WorldGenLevel world,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BoundingBox box,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        ChunkAccess chunk = world.getChunk(chunkPos.x, chunkPos.z);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);
        Heightmap worldgenSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockState terrainState = ModBlocks.CHORAL_END_STONE.get().defaultBlockState();
        BlockPos.MutableBlockPos local = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos worldPos = new BlockPos.MutableBlockPos();

        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            int x = chunkMinX + localX;
            int dx = x - center.getX();
            int dx2 = dx * dx;
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = chunkMinZ + localZ;
                int dz = z - center.getZ();
                float distanceSquared = dx2 + dz * dz;
                if (distanceSquared >= radiusSquared) {
                    continue;
                }

                int baseY = Math.max(surface.getFirstAvailable(localX, localZ), worldgenSurface.getFirstAvailable(localX, localZ));
                if (baseY < 10) {
                    continue;
                }

                float radialFalloff = 1.0F - (float) Math.pow(distanceSquared / radiusSquared, ChoralClusterGenerationSettings.VALLEY_SHARPNESS);
                float edgeClamp = getBiomeEdgeClamp(world, x, z);
                if (edgeClamp <= 0.0F) {
                    continue;
                }

                double primary = ChoralClusterNoise.valueNoise(x * ChoralClusterGenerationSettings.TERRAIN_PRIMARY_NOISE_SCALE, z * ChoralClusterGenerationSettings.TERRAIN_PRIMARY_NOISE_SCALE, terrainSeed);
                double secondary = ChoralClusterNoise.valueNoise(x * ChoralClusterGenerationSettings.TERRAIN_SECONDARY_NOISE_SCALE, z * ChoralClusterGenerationSettings.TERRAIN_SECONDARY_NOISE_SCALE, terrainSeed ^ 0x4f3a21);
                float terrainHeight = radialFalloff * height * edgeClamp;
                terrainHeight *= (float) (0.78D + primary * 0.28D + secondary * 0.12D);

                int maxY = Mth.floor(center.getY() + Math.max(0.0F, terrainHeight));
                int minY = Math.max(baseY - 1, center.getY() - 10);
                for (int y = minY; y < maxY && y < chunk.getMaxBuildHeight(); y++) {
                    local.set(localX, y, localZ);
                    if (canReplaceTerrain(chunk.getBlockState(local))) {
                        chunk.setBlockState(local, terrainState, false);
                    }
                }
            }
        }

        RandomSource pillarRandom = RandomSource.create(((long) terrainSeed << 32) ^ chunkPos.toLong());
        new ChorusCrystalPillarGenerator(ChoralClusterGenerationSettings.ACTIVE_PRESET).generateForChunk(chunk, pillarRandom);
    }

    private float getBiomeEdgeClamp(WorldGenLevel world, int x, int z) {
        float total = 0.0F;
        float max = 0.0F;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int radius = ChoralClusterGenerationSettings.EDGE_SAMPLE_RADIUS;
        for (int ox = -radius; ox <= radius; ox += 4) {
            for (int oz = -radius; oz <= radius; oz += 4) {
                float weight = 1.0F - Mth.sqrt(ox * ox + oz * oz) / radius;
                if (weight <= 0.0F) {
                    continue;
                }
                pos.set(x + ox, center.getY(), z + oz);
                if (world.getBiome(pos).is(ModBiomes.CHORAL_CLUSTERS)) {
                    total += weight;
                }
                max += weight;
            }
        }
        return max <= 0.0F ? 0.0F : Mth.clamp(total / max, 0.0F, 1.0F);
    }

    private boolean canReplaceTerrain(BlockState state) {
        return state.isAir()
                || state.is(Blocks.END_STONE)
                || state.is(Blocks.CAVE_AIR)
                || state.is(ModBlocks.CHORAL_END_STONE.get());
    }

    private static BoundingBox makeBox(BlockPos center, float radius, float height) {
        int horizontal = Mth.ceil(radius) + 4;
        int below = Math.max(16, Mth.ceil(radius * 0.35F));
        int above = Mth.ceil(Math.max(radius, height)) + 80;
        return new BoundingBox(
                center.getX() - horizontal,
                center.getY() - below,
                center.getZ() - horizontal,
                center.getX() + horizontal,
                center.getY() + above,
                center.getZ() + horizontal
        );
    }
}
