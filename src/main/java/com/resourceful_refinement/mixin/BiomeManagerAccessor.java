package com.resourceful_refinement.mixin;

import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link BiomeManager}'s private {@code biomeZoomSeed} so client-side block tints can be
 * seed-driven. This value is {@code BiomeManager.obfuscateSeed(worldSeed)} and is sent to the client
 * in the login packet, so it is a deterministic per-world value available on both sides.
 */
@Mixin(BiomeManager.class)
public interface BiomeManagerAccessor {
    @Accessor("biomeZoomSeed")
    long getBiomeZoomSeed();
}
