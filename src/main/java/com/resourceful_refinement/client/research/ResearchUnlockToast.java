package com.resourceful_refinement.client.research;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ResearchUnlockToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final int DISPLAY_TIME = 5000;
    private static final Component HEADER = Component.literal("Research Complete!");

    private final Component title;
    private final ItemStack icon;
    private boolean playedSound;

    public ResearchUnlockToast(Component title, ItemStack icon) {
        this.title = title;
        this.icon = icon.copy();
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        graphics.blitSprite(BACKGROUND_SPRITE, 0, 0, width(), height());
        List<FormattedCharSequence> titleLines = toastComponent.getMinecraft().font.split(title, 125);
        int headerColor = 0xFFFF00;
        if (titleLines.size() == 1) {
            graphics.drawString(toastComponent.getMinecraft().font, HEADER, 30, 7, headerColor | 0xFF000000, false);
            graphics.drawString(toastComponent.getMinecraft().font, titleLines.get(0), 30, 18, 0xFFFFFFFF, false);
        } else {
            if (timeSinceLastVisible < 1500L) {
                int alpha = Mth.floor(Mth.clamp((1500L - timeSinceLastVisible) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24
                        | 0x4000000;
                graphics.drawString(toastComponent.getMinecraft().font, HEADER, 30, 11, headerColor | alpha, false);
            } else {
                int alpha = Mth.floor(Mth.clamp((timeSinceLastVisible - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24
                        | 0x4000000;
                int y = height() / 2 - titleLines.size() * 9 / 2;
                for (FormattedCharSequence line : titleLines) {
                    graphics.drawString(toastComponent.getMinecraft().font, line, 30, y, 0xFFFFFF | alpha, false);
                    y += 9;
                }
            }
        }
        if (!playedSound && timeSinceLastVisible > 0L) {
            playedSound = true;
            toastComponent.getMinecraft().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
        }
        graphics.renderFakeItem(icon, 8, 8);
        return timeSinceLastVisible >= DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE
                : Visibility.SHOW;
    }
}
