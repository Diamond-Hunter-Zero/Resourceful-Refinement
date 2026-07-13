package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.research.ResearchCraftingGate;
import com.resourceful_refinement.network.ResearchCraftingLockPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Shadow
    @Final
    private Player owner;

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void resourceful_refinement$syncResearchLock(Container container, CallbackInfo ci) {
        if (owner instanceof ServerPlayer serverPlayer) {
            ResearchCraftingGate.syncCraftingLockState(serverPlayer, serverPlayer.containerMenu.containerId, craftSlots);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void resourceful_refinement$clearResearchLock(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    ResearchCraftingLockPayload.unlocked(serverPlayer.containerMenu.containerId));
        }
    }
}
