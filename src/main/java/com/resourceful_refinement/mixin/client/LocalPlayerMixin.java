package com.resourceful_refinement.mixin.client;

import com.resourceful_refinement.content.hosegun.HosegunItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla applies a 0.2× movement-input multiplier while {@link LocalPlayer#isUsingItem()}.
 * The hosegun should not slow the player so SPEEDY gel and normal sprinting still work.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    ordinal = 0
            )
    )
    private boolean resourceful_refinement$skipItemUseSlowdownForHosegun(LocalPlayer instance) {
        return resourceful_refinement$treatHosegunSprayAsNotUsingItem(instance);
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    ordinal = 1
            )
    )
    private boolean resourceful_refinement$allowSprintWhileSprayingHosegun(LocalPlayer instance) {
        return resourceful_refinement$treatHosegunSprayAsNotUsingItem(instance);
    }

    private static boolean resourceful_refinement$treatHosegunSprayAsNotUsingItem(LocalPlayer instance) {
        return instance.isUsingItem() && !HosegunItem.isSpraying(instance);
    }
}
