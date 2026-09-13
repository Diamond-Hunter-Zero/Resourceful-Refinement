package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.fluids.PouredCementPlacement;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public class OpenEndedPipeFluidHandlerMixin {

    @Shadow
    @Final
    OpenEndedPipe this$0;

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$placePouredCementFromLiquidConcrete(FluidStack resource,
                                                                           IFluidHandler.FluidAction action,
                                                                           CallbackInfoReturnable<Integer> cir) {
        if (resource.isEmpty() || !resource.getFluid().isSame(ModFluids.LIQUID_CONCRETE.source.get()))
            return;

        FluidTank tank = (FluidTank) (Object) this;
        FluidStack stored = tank.getFluid();
        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, resource)) {
            cir.setReturnValue(0);
            return;
        }

        int storedAmount = stored.getAmount();
        int amountNeeded = Math.max(0, PouredCementPlacement.LIQUID_CONCRETE_PER_SOURCE_MB - storedAmount);
        int accepted = Math.min(resource.getAmount(), amountNeeded);
        FluidStack placementStack = new FluidStack(ModFluids.LIQUID_CONCRETE.source.get(), PouredCementPlacement.LIQUID_CONCRETE_PER_SOURCE_MB);
        boolean readyToPlace = storedAmount + accepted >= PouredCementPlacement.LIQUID_CONCRETE_PER_SOURCE_MB;

        if (!readyToPlace) {
            if (action.execute()) {
                FluidStack accumulated = resource.copy();
                accumulated.setAmount(storedAmount + accepted);
                tank.setFluid(accumulated);
            }
            cir.setReturnValue(accepted);
            return;
        }

        boolean placed = PouredCementPlacement.tryPlaceAtPipeOutlet(
            this$0.getWorld(),
            this$0.getOutputPos(),
            placementStack,
            action.simulate()
        );
        if (!placed) {
            cir.setReturnValue(0);
            return;
        }

        if (action.execute())
            tank.setFluid(FluidStack.EMPTY);
        cir.setReturnValue(accepted);
    }
}
