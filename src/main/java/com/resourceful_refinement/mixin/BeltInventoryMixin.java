package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.brewers_tap.BrewersTapBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltInventory.class, remap = false)
public abstract class BeltInventoryMixin {

    @Shadow
    @Final
    private BeltBlockEntity belt;

    @Inject(method = "handleBeltProcessingAndCheckIfRemoved", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$handleBrewersTapProcessing(TransportedItemStack transported, float nextOffset, boolean blocking, CallbackInfoReturnable<Boolean> cir) {
        Boolean result = BrewersTapBlockEntity.handleBeltProcessing(belt, transported, nextOffset, blocking);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
