package com.resourceful_refinement.content.hosegun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Vector3f;

/**
 * Two-handed hosegun pose: active arm forward, off hand gripping the side tank.
 */
public final class HosegunArmPoses {

    private HosegunArmPoses() {}

    public static boolean isHoldingHosegun(Player player) {
        return !getHeldHosegun(player).isEmpty();
    }

    public static ItemStack getHeldHosegun(Player player) {
        if (player.getMainHandItem().is(ModItems.HOSEGUN.get())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(ModItems.HOSEGUN.get())) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    public static void applyThirdPersonPlayerPose(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!isHoldingHosegun(player)) {
            return;
        }

        HumanoidModel<?> model = (HumanoidModel<?>) event.getRenderer().getModel();
        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        boolean hosegunInMain = player.getMainHandItem().is(ModItems.HOSEGUN.get());

        float bodyX = model.body.xRot;

        if (rightHanded) {
            if (hosegunInMain) {
                model.rightArm.xRot = (float) Math.toRadians(-90.35F) + bodyX;
                model.rightArm.yRot = (float) Math.toRadians(-0.15F);
                model.rightArm.zRot = 0.0F;
                model.leftArm.xRot = -1.05F + bodyX;
                model.leftArm.yRot = 0.45F;
                model.leftArm.zRot = -0.2F;
            } else {
                model.leftArm.xRot = -1.35F + bodyX;
                model.leftArm.yRot = 0.15F;
                model.leftArm.zRot = 0.0F;
                model.rightArm.xRot = -1.05F + bodyX;
                model.rightArm.yRot = -0.45F;
                model.rightArm.zRot = 0.2F;
            }
        } else {
            if (hosegunInMain) {
                model.leftArm.xRot = -1.35F + bodyX;
                model.leftArm.yRot = 0.15F;
                model.leftArm.zRot = 0.0F;
                model.rightArm.xRot = -1.05F + bodyX;
                model.rightArm.yRot = -0.45F;
                model.rightArm.zRot = 0.2F;
            } else {
                model.rightArm.xRot = -1.35F + bodyX;
                model.rightArm.yRot = -0.15F;
                model.rightArm.zRot = 0.0F;
                model.leftArm.xRot = -1.05F + bodyX;
                model.leftArm.yRot = 0.45F;
                model.leftArm.zRot = -0.2F;
            }
        }
    }

    /** Off-hand reaches inward toward the hosegun body in first person. */
    public static void applyFirstPersonSupportArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        if (!player.getMainHandItem().is(ModItems.HOSEGUN.get())) {
            return;
        }

        HumanoidArm supportArm = player.getMainArm().getOpposite();
        if (event.getArm() != supportArm) {
            return;
        }

        var poseStack = event.getPoseStack();
        poseStack.translate(0.12F, 0.05F, -0.35F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(supportArm == HumanoidArm.RIGHT ? -18.0F : 18.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-8.0F));
    }
}
