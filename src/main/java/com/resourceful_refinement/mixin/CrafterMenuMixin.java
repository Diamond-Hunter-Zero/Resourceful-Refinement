package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.research.ResearchCraftingGate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterMenu.class)
public abstract class CrafterMenuMixin {
    @Shadow
    @Final
    private Player player;

    @Shadow
    @Final
    private CraftingContainer container;

    @Inject(method = "refreshRecipeResult", at = @At("TAIL"))
    private void resourceful_refinement$syncResearchLock(CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ResearchCraftingGate.syncServerCraftingLockState(serverPlayer, serverPlayer.containerMenu.containerId,
                    container);
        }
    }
}
