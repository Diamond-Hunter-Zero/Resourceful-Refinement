package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.brewers_tap.BrewersTapBlockEntity;
import com.resourceful_refinement.content.tesla_coil.TeslaCoilBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import net.minecraft.core.BlockPos;
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

    /** Tesla Coil pairs flank the space above the belt rather than sitting two blocks above it, so route to them. */
    @Inject(method = "getBeltProcessingAtSegment", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$teslaCoilProcessing(int segment, CallbackInfoReturnable<BeltProcessingBehaviour> cir) {
        BlockPos space = BeltHelper.getPositionForOffset(belt, segment).above();
        BeltProcessingBehaviour tesla = TeslaCoilBlockEntity.findChargingBehaviour(belt.getLevel(), space);
        if (tesla != null) {
            cir.setReturnValue(tesla);
        }
    }
}
