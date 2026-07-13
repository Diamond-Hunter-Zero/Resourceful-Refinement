package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.research.ResearchCraftingGate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {
    @Shadow
    @Final
    private Player player;

    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$cancelLockedResearchCraftingTake(Player player, ItemStack stack,
                                                                        CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            ResearchCraftingGate.syncCraftingLockState(serverPlayer, serverPlayer.containerMenu.containerId, craftSlots)
                    .ifPresent(result -> {
                serverPlayer.displayClientMessage(result.message(), true);
                ci.cancel();
            });
        }
    }
}
