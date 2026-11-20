package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.tesla_coil.TeslaCoilBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Depots look up their processor two blocks up ({@code pos.above(2)}). Tesla Coil pairs instead flank the space
 * directly above the depot, so check for a pair there first and fall back to Create's lookup.
 */
@Mixin(value = DepotBehaviour.class, remap = false)
public abstract class DepotBehaviourMixin {

    @Redirect(method = "tick()V", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;get(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/foundation/blockEntity/behaviour/BehaviourType;)Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;"))
    private BlockEntityBehaviour resourceful_refinement$teslaCoilProcessing(BlockGetter level, BlockPos processorPos,
                                                                            BehaviourType<?> type) {
        if (type == BeltProcessingBehaviour.TYPE) {
            BeltProcessingBehaviour tesla = TeslaCoilBlockEntity.findChargingBehaviour(level, processorPos.below());
            if (tesla != null) {
                return tesla;
            }
        }
        return BlockEntityBehaviour.get(level, processorPos, type);
    }
}
