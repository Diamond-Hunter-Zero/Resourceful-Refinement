package com.resourceful_refinement.content.railgun;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** Client hooks for the railgun: custom model renderer, two-handed aiming pose, first-person motion. */
public final class RailgunClientExtensions implements IClientItemExtensions {

    public static final RailgunClientExtensions INSTANCE = new RailgunClientExtensions();

    private RailgunClientExtensions() {}

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return RailgunItemRenderer.INSTANCE;
    }

    /** Both arms raised and aimed along the view, like a loaded crossbow, whether idle, charging or firing. */
    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        return HumanoidModel.ArmPose.CROSSBOW_HOLD;
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack,
                                           float partialTick, float equipProgress, float swingProgress) {
        RailgunClient.applyFirstPersonTransform(poseStack, player, arm, partialTick, equipProgress);
        return true;
    }
}
