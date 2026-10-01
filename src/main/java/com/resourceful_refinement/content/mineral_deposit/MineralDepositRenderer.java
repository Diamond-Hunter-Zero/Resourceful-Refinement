package com.resourceful_refinement.content.mineral_deposit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class MineralDepositRenderer implements BlockEntityRenderer<MineralDepositBlockEntity> {

    private final MineralDepositModel model;
    private final BlockRenderDispatcher blockRenderDispatcher;

    public MineralDepositRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MineralDepositModel(context.bakeLayer(MineralDepositModel.LAYER_LOCATION));
        this.blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(MineralDepositBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {

        // Resolve which blocks drive each visual part. Falls back to the associated block for both
        // parts when no visuals.json entry is configured, preserving the original behaviour.
        Block associated = be.getAssociatedBlock();
        MineralDepositVisual visual = MineralDepositVisuals.get(associated).orElse(null);
        Block baseBlock = visual != null ? visual.baseMineral() : associated;
        Block depositBlock = visual != null ? visual.depositMaterial() : associated;

        // --- Base mineral: solid cube ---
        BlockState baseState = baseBlock.defaultBlockState();
        this.blockRenderDispatcher.renderSingleBlock(baseState, poseStack, buffer, light, overlay);

        // --- Deposit material: sprite that skins the deposit model ---
        BakedModel depositBakedModel = blockRenderDispatcher.getBlockModelShaper().getBlockModel(depositBlock.defaultBlockState());
        TextureAtlasSprite sprite = depositBakedModel.getParticleIcon(ModelData.EMPTY);

        // Wrap it! The expander shifts 0.0-1.0 UV maps directly onto the block's current atlas coordinates.
        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.cutout());
        VertexConsumer textureRemappedConsumer = new SpriteCoordinateExpander(casingBuffer, sprite);

        // Render mineral model
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(-1, -1, 1);

        // --- Render Block Model ---
        model.render(poseStack, textureRemappedConsumer, light, overlay);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
