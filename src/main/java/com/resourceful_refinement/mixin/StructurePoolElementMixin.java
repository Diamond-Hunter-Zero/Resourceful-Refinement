package com.resourceful_refinement.mixin;

import com.resourceful_refinement.worldgen.GeyserOffsetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePoolElement.class)
public abstract class StructurePoolElementMixin {

    @Inject(method = "getGroundLevelDelta", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$customGroundLevelDelta(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof SinglePoolElement singlePoolElement) {
            ResourceLocation activePool = GeyserOffsetManager.getActiveStartPool();
            if (activePool != null) {
                try {
                    var templateEither = ((SinglePoolElementAccessor) singlePoolElement).resourceful_refinement$getTemplate();
                    if (templateEither != null) {
                        var optLoc = templateEither.left();
                        if (optLoc.isPresent()) {
                            ResourceLocation templateLoc = optLoc.get();
                            Integer customDelta = GeyserOffsetManager.getOffset(activePool, templateLoc);
                            if (customDelta != null) {
                                cir.setReturnValue(customDelta);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Fail-safe in case of any issues with accessor or types
                }
            }
        }
    }
}
