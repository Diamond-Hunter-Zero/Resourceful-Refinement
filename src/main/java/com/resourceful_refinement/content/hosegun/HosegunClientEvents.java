package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT)
public class HosegunClientEvents {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HosegunItemRenderer.LAYER, HosegunModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        HosegunArmPoses.applyThirdPersonPlayerPose(event);
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        HosegunArmPoses.applyFirstPersonSupportArm(event);
    }
}
