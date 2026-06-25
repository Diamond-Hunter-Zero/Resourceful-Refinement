package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.combustion_chamber.CombustionChamberFanIntegration;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EncasedFanBlock.class, remap = false)
public class EncasedFanBlockMixin {
    @Inject(method = "hasShaftTowards", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$addCombustionChamberOutputShaft(LevelReader world, BlockPos pos,
                                                                       BlockState state, Direction face,
                                                                       CallbackInfoReturnable<Boolean> cir) {

        if (face == state.getValue(EncasedFanBlock.FACING)
                && CombustionChamberFanIntegration.isFanDrivenByOutputChamber(world, pos, state)) {
            cir.setReturnValue(true);
        }
    }
}
