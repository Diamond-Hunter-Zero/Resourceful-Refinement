package com.resourceful_refinement.mixin;

import com.resourceful_refinement.registry.ModBiomes;
import com.resourceful_refinement.worldgen.choral.ChoralClustersBiomeHolder;
import com.resourceful_refinement.worldgen.choral.ChoralClusterNoise;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(TheEndBiomeSource.class)
public abstract class ChoralClustersEndBiomeSourceMixin {
    @Shadow
    @Final
    private Holder<Biome> highlands;

    @Shadow
    @Final
    private Holder<Biome> midlands;

    @Unique
    private static Holder<Biome> resourceful_refinement$choralClusters;

    @Inject(method = "create", at = @At("HEAD"))
    private static void resourceful_refinement$captureChoralClusters(HolderGetter<Biome> biomeGetter, CallbackInfoReturnable<TheEndBiomeSource> cir) {
        if (resourceful_refinement$isVanillaRegistryValidation()) {
            resourceful_refinement$choralClusters = null;
            return;
        }
        resourceful_refinement$choralClusters = biomeGetter.get(ModBiomes.CHORAL_CLUSTERS)
                .map(holder -> (Holder<Biome>) holder)
                .orElse(null);
    }

    @Unique
    private static boolean resourceful_refinement$isVanillaRegistryValidation() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if ("net.minecraft.data.registries.VanillaRegistries".equals(element.getClassName())
                    || "net.minecraft.server.Bootstrap".equals(element.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "collectPossibleBiomes", at = @At("RETURN"), cancellable = true)
    private void resourceful_refinement$addChoralClustersToPossibleBiomes(CallbackInfoReturnable<Stream<Holder<Biome>>> cir) {
        Holder<Biome> choralClusters = resourceful_refinement$getChoralClusters();
        if (choralClusters != null) {
            cir.setReturnValue(Stream.concat(cir.getReturnValue(), Stream.of(choralClusters)).distinct());
        }
    }

    @Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
    private void resourceful_refinement$replaceOuterEndBiome(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        Holder<Biome> original = cir.getReturnValue();
        Holder<Biome> choralClusters = resourceful_refinement$getChoralClusters();
        if (choralClusters == null) {
            return;
        }
        if ((original == highlands || original == midlands || original.is(Biomes.END_HIGHLANDS) || original.is(Biomes.END_MIDLANDS))
                && ChoralClusterNoise.shouldPlaceChoralBiome(x, z)) {
            cir.setReturnValue(choralClusters);
        }
    }

    @Unique
    private static Holder<Biome> resourceful_refinement$getChoralClusters() {
        Holder<Biome> resolved = ChoralClustersBiomeHolder.get();
        return resolved != null ? resolved : resourceful_refinement$choralClusters;
    }
}
