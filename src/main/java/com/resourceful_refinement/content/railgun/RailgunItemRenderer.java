package com.resourceful_refinement.content.railgun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

/**
 * Draws the railgun's Java model for every display context, spinning the drum and barrel from the holder's spool
 * state ({@link RailgunClient}).
 *
 * <p>Transforms work in <b>gun space</b>: {@link #applyModelSpace} maps model pixels to blocks with the gun's feet
 * at y=0, the barrel pointing along +Z (muzzle at z=+1) and the back at z&asymp;-0.69. Each context then places gun
 * space relative to the item centre. The first-person transform also positions the beam's muzzle
 * ({@link RailgunClient#firstPersonMuzzle}), so tuning it here keeps the beam attached to the barrel.</p>
 */
public class RailgunItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final RailgunItemRenderer INSTANCE = new RailgunItemRenderer();

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "railgun"), "main");

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/item/railgun.png");

    /** Rough centre of the gun in gun space, used to centre it in GUI / ground / frame views. */
    private static final float CENTRE_Y = 0.28F;
    private static final float CENTRE_Z = 0.16F;

    private RailgunModel model;

    private RailgunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private RailgunModel model() {
        if (model == null) {
            model = new RailgunModel(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
        }
        return model;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float drumAngle = 0.0F;
        float barrelAngle = 0.0F;
        float spool = 0.0F;
        LivingEntity holder = RailgunClient.holderFor(context);
        if (holder != null) {
            RailgunClient.SpoolState spoolState = RailgunClient.updateSpool(holder);
            drumAngle = spoolState.drumAngle();
            barrelAngle = spoolState.barrelAngle();
            spool = spoolState.speed();
        }

        // The drum glows as it spools: raise its block light toward full bright.
        int glow = Math.round(15.0F * spool);
        int drumLight = LightTexture.pack(Math.max(LightTexture.block(packedLight), glow), LightTexture.sky(packedLight));

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F); // ItemRenderer hands us the item's corner; work from its centre.
        applyDisplayTransform(context, poseStack);
        applyModelSpace(poseStack);
        model().render(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, packedOverlay, drumLight, drumAngle, barrelAngle);
        poseStack.popPose();
    }

    /** Model pixels to gun space (see class docs). A 180-degree turn about X, so nothing is mirrored. */
    static void applyModelSpace(PoseStack poseStack) {
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0F, -1.5F, 0.0F);
    }

    /** Places gun space relative to the item centre for the given context. */
    static void applyDisplayTransform(ItemDisplayContext context, PoseStack poseStack) {
        switch (context) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                // Hip-held, barrel into the screen and angled up toward the crosshair. The left hand mirrors the
                // offset only; the gun itself is not mirrored.
                float side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1.0F : 1.0F;
                poseStack.translate(side * -0.175F, 0.19F, -0.07F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(-15.0F));
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                // Arm is raised forward by the CROSSBOW_HOLD arm pose; barrel continues along it.
                float scaleFactor = (context == THIRD_PERSON_RIGHT_HAND) ? 1f : -1f;
                poseStack.translate(-0.04F * scaleFactor, -0.425F, -0.225F);
                poseStack.scale(0.75F, 0.75F, 0.75F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - 12F * scaleFactor));
            }
            case GUI -> {
                // Three-quarter view, barrel to the upper right.
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(135.0F));
                poseStack.scale(0.6F, 0.6F, 0.6F);
                poseStack.translate(0.0F, -CENTRE_Y, -CENTRE_Z);
            }
            case GROUND -> {
                poseStack.scale(0.4F, 0.4F, 0.4F);
                poseStack.translate(0.0F, -CENTRE_Y, -CENTRE_Z);
            }
            case FIXED -> {
                // Item frame: side-on, barrel to the upper right.
                poseStack.mulPose(Axis.ZP.rotationDegrees(35.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.scale(0.55F, 0.55F, 0.55F);
                poseStack.translate(0.0F, -CENTRE_Y, -CENTRE_Z);
            }
            default -> {
                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.translate(0.0F, -CENTRE_Y, -CENTRE_Z);
            }
        }
    }
}
