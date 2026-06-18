package com.resourceful_refinement.content.glare;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class GlareLineOfSight {
    private GlareLineOfSight() {}

    public static boolean canValidate(MinecraftServer server, GlareNodePos a, GlareNodePos b) {
        if (!a.levelKey().equals(b.levelKey())) {
            return false;
        }
        ServerLevel level = server.getLevel(a.levelKey());
        return level != null && canValidate(level, a, b);
    }

    public static boolean hasLineOfSight(MinecraftServer server, GlareNodePos a, GlareNodePos b) {
        ServerLevel level = server.getLevel(a.levelKey());
        if (level == null || !canValidate(level, a, b)) {
            return true;
        }

        Vec3 from = Vec3.atCenterOf(a.pos());
        Vec3 to = Vec3.atCenterOf(b.pos());
        Vec3 delta = to.subtract(from);
        int samples = Math.max(1, (int) Math.ceil(delta.length() * 4.0D));
        BlockPos lastChecked = null;
        for (int i = 1; i < samples; i++) {
            Vec3 sample = from.add(delta.scale(i / (double) samples));
            BlockPos samplePos = BlockPos.containing(sample);
            if (samplePos.equals(lastChecked) || samplePos.equals(a.pos()) || samplePos.equals(b.pos())) {
                continue;
            }
            lastChecked = samplePos;
            if (!level.isLoaded(samplePos)) {
                return true;
            }
            BlockState state = level.getBlockState(samplePos);
            FluidState fluid = state.getFluidState();
            if (!state.isAir() && fluid.isEmpty() && !state.canBeReplaced() && !state.getCollisionShape(level, samplePos).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean canValidate(ServerLevel level, GlareNodePos a, GlareNodePos b) {
        if (!level.isLoaded(a.pos()) || !level.isLoaded(b.pos())) {
            return false;
        }

        int startChunkX = a.pos().getX() >> 4;
        int startChunkZ = a.pos().getZ() >> 4;
        int endChunkX = b.pos().getX() >> 4;
        int endChunkZ = b.pos().getZ() >> 4;
        int steps = Math.max(Math.abs(endChunkX - startChunkX), Math.abs(endChunkZ - startChunkZ));
        if (steps > 512) {
            return false;
        }
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0D : (double) i / (double) steps;
            int chunkX = (int) Math.floor(startChunkX + (endChunkX - startChunkX) * t);
            int chunkZ = (int) Math.floor(startChunkZ + (endChunkZ - startChunkZ) * t);
            if (!level.hasChunk(chunkX, chunkZ)) {
                return false;
            }
        }
        return true;
    }
}
