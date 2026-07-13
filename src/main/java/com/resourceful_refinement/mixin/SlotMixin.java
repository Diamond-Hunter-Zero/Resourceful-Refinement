package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.research.ResearchCraftingGate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$blockLockedResearchCrafting(Player player,
                                                                   CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ResultSlotAccessor resultSlot && player instanceof ServerPlayer serverPlayer) {
            ResearchCraftingGate.syncCraftingLockState(serverPlayer, serverPlayer.containerMenu.containerId,
                            resultSlot.resourceful_refinement$getCraftSlots())
                    .ifPresent(result -> {
                        serverPlayer.displayClientMessage(result.message(), true);
                        cir.setReturnValue(false);
                    });
        }
    }
}
