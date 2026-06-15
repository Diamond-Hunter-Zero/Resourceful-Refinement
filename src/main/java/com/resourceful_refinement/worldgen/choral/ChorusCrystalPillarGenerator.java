package com.resourceful_refinement.worldgen.choral;

import com.resourceful_refinement.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class ChorusCrystalPillarGenerator {
    private final ChorusCrystalPillarPreset preset;
    private final BlockState crystal;

    public ChorusCrystalPillarGenerator(ChorusCrystalPillarPreset preset) {
        this.preset = preset;
        this.crystal = ModBlocks.CHORUS_CRYSTAL.get().defaultBlockState();
    }

    public void generateForChunk(ChunkAccess chunk, RandomSource random) {
        Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);
        int count = randomBetween(random, preset.minCount(), preset.maxCount());
        for (int i = 0; i < count; i++) {
            int localX = random.nextInt(16);
            int localZ = random.nextInt(16);
            int y = heightmap.getFirstAvailable(localX, localZ);
            if (y < 48 || y > chunk.getMaxBuildHeight() - 8) {
                continue;
            }

            BlockPos origin = new BlockPos(localX, y, localZ);
            if (!canAnchor(chunk, origin.below())) {
                continue;
            }

            float roll = random.nextFloat() * preset.totalWeight();
            if (roll < preset.verticalWeight()) {
                placePillar(chunk, random, origin, new Vec3(0.0D, 1.0D, 0.0D), false);
            } else if (roll < preset.verticalWeight() + preset.slantedWeight()) {
                placePillar(chunk, random, origin, slantedDirection(random, false), false);
            } else {
                boolean downward = random.nextBoolean();
                placePillar(chunk, random, origin, slantedDirection(random, downward), true);
            }
        }
    }

    private void placePillar(ChunkAccess chunk, RandomSource random, BlockPos origin, Vec3 direction, boolean pointed) {
        float radius = randomBetween(random, preset.minRadius(), preset.maxRadius());
        float length = randomBetween(random, preset.minLength(), preset.maxLength());
        if (random.nextFloat() < preset.monumentalChance() * 0.12F) {
            radius *= 1.45F;
            length *= 1.55F;
        }

        Vec3 normal = direction.normalize();
        int steps = Math.max(1, Mth.ceil(length));
        for (int step = -preset.rootDepth(); step <= steps; step++) {
            double progress = Mth.clamp(step / (double) steps, 0.0D, 1.0D);
            double taper = pointed ? Math.max(0.15D, 1.0D - Math.pow(progress, 1.25D) * preset.taperStrength()) : 1.0D;
            double sliceRadius = Math.max(0.45D, radius * taper);
            Vec3 center = Vec3.atLowerCornerOf(origin).add(0.5D, 0.0D, 0.5D).add(normal.scale(step));
            placeHexSlice(chunk, center, sliceRadius);
        }
    }

    private void placeHexSlice(ChunkAccess chunk, Vec3 center, double radius) {
        int minX = Mth.floor(center.x - radius);
        int maxX = Mth.ceil(center.x + radius);
        int minY = Mth.floor(center.y - radius);
        int maxY = Mth.ceil(center.y + radius);
        int minZ = Mth.floor(center.z - radius);
        int maxZ = Mth.ceil(center.z + radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            if (x < 0 || x > 15) {
                continue;
            }
            for (int z = minZ; z <= maxZ; z++) {
                if (z < 0 || z > 15) {
                    continue;
                }
                double dx = Math.abs((x + 0.5D) - center.x);
                double dz = Math.abs((z + 0.5D) - center.z);
                if (dx + dz * 0.62D > radius * 1.18D || Math.max(dx, dz) > radius) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    if (y < chunk.getMinBuildHeight() || y >= chunk.getMaxBuildHeight()) {
                        continue;
                    }
                    if (Math.abs((y + 0.5D) - center.y) > Math.max(0.75D, radius * 0.55D)) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (canReplace(chunk.getBlockState(pos))) {
                        chunk.setBlockState(pos, crystal, false);
                    }
                }
            }
        }
    }

    private Vec3 slantedDirection(RandomSource random, boolean downward) {
        double yaw = random.nextDouble() * Math.PI * 2.0D;
        double angle = Math.toRadians(randomBetween(random, preset.minSlantDegrees(), preset.maxSlantDegrees()));
        double horizontal = Math.sin(angle);
        double vertical = Math.cos(angle) * (downward ? -1.0D : 1.0D);
        return new Vec3(Math.cos(yaw) * horizontal, vertical, Math.sin(yaw) * horizontal);
    }

    private boolean canAnchor(ChunkAccess chunk, BlockPos pos) {
        BlockState state = chunk.getBlockState(pos);
        return state.is(Blocks.END_STONE) || state.is(ModBlocks.CHORAL_END_STONE.get()) || state.is(ModBlocks.CHORUS_CRYSTAL.get());
    }

    private boolean canReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.END_STONE)
                || state.is(Blocks.CAVE_AIR)
                || state.is(ModBlocks.CHORAL_END_STONE.get())
                || state.is(ModBlocks.CHORUS_CRYSTAL.get());
    }

    private int randomBetween(RandomSource random, int min, int max) {
        return min + random.nextInt(Math.max(1, max - min + 1));
    }

    private float randomBetween(RandomSource random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}
