package com.resourceful_refinement.mixin;

import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private {@code startHeight} of {@link JigsawStructure} so the Resource Resonator can validate (the
 * "start_height guard") that a geyser structure still uses the height-provider type its variant prediction assumes.
 * {@code JigsawStructure} is final, so callers cross-cast through {@code Object}.
 */
@Mixin(JigsawStructure.class)
public interface JigsawStructureAccessor {
    @Accessor("startHeight")
    HeightProvider resourceful_refinement$getStartHeight();
}
