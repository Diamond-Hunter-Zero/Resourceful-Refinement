package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.advanced_pump.AdvancedPumpThroughputTracker;
import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.FluidNetwork;
import com.simibubi.create.foundation.ICapabilityProvider;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = FluidNetwork.class, remap = false)
public class FluidNetworkMixin {

    @Shadow
    Level world;

    @Shadow
    List<Pair<BlockFace, FlowSource>> targets;

    @Redirect(method = "tick",
        at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I"))
    private int resourceful_refinement$recordAdvancedPumpThroughput(IFluidHandler targetHandler, FluidStack resource,
                                                                    FluidAction action) {
        int filled = targetHandler.fill(resource, action);
        if (action.execute() && filled > 0) {
            BlockFace targetFace = resourceful_refinement$findTargetFace(targetHandler);
            if (targetFace != null)
                AdvancedPumpThroughputTracker.recordTransfer(world, targetFace, filled);
        }
        return filled;
    }

    @Nullable
    private BlockFace resourceful_refinement$findTargetFace(IFluidHandler targetHandler) {
        for (Pair<BlockFace, FlowSource> pair : targets) {
            @Nullable ICapabilityProvider<IFluidHandler> provider = pair.getSecond()
                .provideHandler();
            if (provider == null)
                continue;
            if (provider.getCapability() == targetHandler)
                return pair.getFirst();
        }
        return null;
    }
}
