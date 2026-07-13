package com.resourceful_refinement.mixin.client;

import com.resourceful_refinement.client.research.ClientResearchLockState;
import com.resourceful_refinement.content.gui.LockedRecipeIndicator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Inject(method = "render", at = @At("TAIL"))
    private void resourceful_refinement$renderResearchLockIndicator(GuiGraphics graphics, int mouseX, int mouseY,
                                                                    float partialTick, CallbackInfo ci) {
        if (!isSupportedCraftingMenu(menu)
                || !ClientResearchLockState.isCraftingResultLocked(menu.containerId)
                || menu.slots.isEmpty()) {
            return;
        }

        Slot resultSlot = menu instanceof CrafterMenu && menu.slots.size() > 7 ? menu.getSlot(7) : menu.getSlot(0);
        LockedRecipeIndicator.renderBelowSlot(graphics, Minecraft.getInstance().font,
                leftPos + resultSlot.x, topPos + resultSlot.y, Minecraft.getInstance().getWindow().getGuiScaledWidth());
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void resourceful_refinement$clearResearchLockIndicator(CallbackInfo ci) {
        ClientResearchLockState.clear(menu.containerId);
    }

    private static boolean isSupportedCraftingMenu(AbstractContainerMenu menu) {
        return menu instanceof CraftingMenu || menu instanceof InventoryMenu || menu instanceof CrafterMenu;
    }
}
