package com.resourceful_refinement.content.plushie;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.AllItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;

public class PlushieRenderer implements BlockEntityRenderer<PlushieBlockEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fox_plushie"), "main");
    private  PlushieModel model;

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/plushie/fox_plushie.png"),
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/plushie/fox_engineer_plushie.png"),
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/plushie/fox_derp_plushie.png"),
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/plushie/fox_arctic_plushie.png")
    };

    public PlushieRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlushieModel(context.bakeLayer(PlushieRenderer.LAYER_LOCATION));
    }

    @Override
    public void render(PlushieBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ms.pushPose();
        
        // Apply rotation based on the BlockState property
        BlockState state = be.getBlockState();
        float rotationAngle = 0;
        if (state.hasProperty(PlushieBlock.ROTATION)) {
            rotationAngle = RotationSegment.convertToDegrees(state.getValue(PlushieBlock.ROTATION));
        }

        // Flip the model (Blockbench exports are often upside down for BEs)
        ms.translate(0.5, 1.125, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(-rotationAngle));
        ms.scale(0.75f, -0.75f, -0.75f);

        // Get the texture based on the variant stored in the BlockEntity
        int variantId = be.getVariant();
        ResourceLocation texture = TEXTURES[variantId % TEXTURES.length];
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(texture));

        this.model.renderToBuffer(ms, vc, light, overlay, 0xFFFFFFFF);

        if (variantId == 1)
        {
            renderHeldWrench(be, ms, buffer, light, overlay);
        }

        ms.popPose();
    }

    private void renderHeldWrench(PlushieBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ms.pushPose();

        // Adjust the item to sit in the "hand"
        // These values usually require some trial and error based on your model's scale
        ms.translate(0.21, 1.05, -0.3);
        ms.mulPose(Axis.YP.rotationDegrees(180f));
        ms.mulPose(Axis.XP.rotationDegrees(180f));
        ms.scale(0.66f, 0.66f, 0.66f);

        // Render the actual Wrench item
        ItemStack wrench = new ItemStack(AllItems.WRENCH.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                wrench,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                light,
                overlay,
                ms,
                buffer,
                be.getLevel(),
                0
        );

        ms.popPose();
    }
}
