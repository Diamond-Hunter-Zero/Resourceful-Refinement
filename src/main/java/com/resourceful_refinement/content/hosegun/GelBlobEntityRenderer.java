package com.resourceful_refinement.content.hosegun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Renders {@link GelBlobEntity} projectiles. Vanilla {@code ThrownItemRenderer} skips empty stacks;
 * tint is applied at render time from synced fluid data rather than on the networked item stack.
 */
@OnlyIn(Dist.CLIENT)
public class GelBlobEntityRenderer extends EntityRenderer<GelBlobEntity> {

    private static final float SCALE = 0.75F;

    private final ItemRenderer itemRenderer;

    public GelBlobEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            GelBlobEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        ItemStack renderStack = buildRenderStack(entity);
        if (renderStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) - 90.0F));
        poseStack.scale(SCALE, SCALE, SCALE);

        FluidStack fluid = entity.getFluidStack();
        int tintRgb = fluid.isEmpty() ? -1 : HosegunFluidColors.getTint(fluid) & 0xFFFFFF;
        MultiBufferSource tintedSource = tintRgb == -1 ? bufferSource : new TintedMultiBufferSource(bufferSource, tintRgb);

        BakedModel model = this.itemRenderer.getItemModelShaper().getItemModel(renderStack);
        this.itemRenderer.render(
                renderStack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                tintedSource,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                model);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static ItemStack buildRenderStack(GelBlobEntity entity) {
        ItemStack synced = entity.getItem();
        if (!synced.isEmpty() && synced.is(ModItems.PAINT_BLOB.get())) {
            return synced;
        }
        return new ItemStack(ModItems.PAINT_BLOB.get());
    }

    @Override
    public ResourceLocation getTextureLocation(GelBlobEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    /** Multiplies vertex colour channels for item layer tinting without relying on synced stack components. */
    private static final class TintedMultiBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final float red;
        private final float green;
        private final float blue;

        private TintedMultiBufferSource(MultiBufferSource delegate, int tintRgb) {
            this.delegate = delegate;
            this.red = ((tintRgb >> 16) & 0xFF) / 255.0F;
            this.green = ((tintRgb >> 8) & 0xFF) / 255.0F;
            this.blue = (tintRgb & 0xFF) / 255.0F;
        }

        @Override
        public VertexConsumer getBuffer(net.minecraft.client.renderer.RenderType renderType) {
            return new TintedVertexConsumer(delegate.getBuffer(renderType), red, green, blue);
        }
    }

    private static final class TintedVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float red;
        private final float green;
        private final float blue;

        private TintedVertexConsumer(VertexConsumer delegate, float red, float green, float blue) {
            this.delegate = delegate;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return delegate.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return delegate.setColor(
                    clampChannel(r * red),
                    clampChannel(g * green),
                    clampChannel(b * blue),
                    a);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return delegate.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return delegate.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return delegate.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return delegate.setNormal(x, y, z);
        }

        private static int clampChannel(float value) {
            return Mth.clamp((int) value, 0, 255);
        }
    }
}
