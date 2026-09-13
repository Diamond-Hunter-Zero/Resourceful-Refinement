package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.fluids.PouredCementPlacement;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler", remap = false)
public class HosePulleyFluidHandlerMixin {

    @Shadow
    private SmartFluidTank internalTank;

    @ModifyConstant(method = "fill", constant = @Constant(intValue = 1000))
    private int resourceful_refinement$liquidConcretePlacementAmount(int original,
                                                                     FluidStack resource,
                                                                     IFluidHandler.FluidAction action) {
        return PouredCementPlacement.placementAmountFor(resource, original);
    }

    @Redirect(method = "fill",
        at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/fluids/transfer/FluidFillingBehaviour;tryDeposit(Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean resourceful_refinement$placePouredCementFromLiquidConcrete(FluidFillingBehaviour filler,
                                                                               Fluid fluid,
                                                                               BlockPos rootPos,
                                                                               boolean simulate) {
        return filler.tryDeposit(PouredCementPlacement.fluidToPlace(fluid), rootPos, simulate);
    }
}
