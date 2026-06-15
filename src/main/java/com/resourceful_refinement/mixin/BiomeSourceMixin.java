package com.resourceful_refinement.mixin;

import com.resourceful_refinement.worldgen.choral.ChoralClustersBiomeHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin {
    @Inject(method = "possibleBiomes", at = @At("RETURN"), cancellable = true)
    private void resourceful_refinement$includeRuntimeChoralClusters(CallbackInfoReturnable<Set<Holder<Biome>>> cir) {
        Holder<Biome> choralClusters = ChoralClustersBiomeHolder.get();
        if (choralClusters != null && (Object) this instanceof TheEndBiomeSource) {
            Set<Holder<Biome>> expanded = new HashSet<>(cir.getReturnValue());
            expanded.add(choralClusters);
            cir.setReturnValue(Set.copyOf(expanded));
        }
    }
}
