package com.resourceful_refinement.content.refinery.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlock;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryKineticProxyBlockEntity;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.List;
import java.util.ArrayList;

import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getRotationAxisOf;

public class FluidRefineryRenderer extends SafeBlockEntityRenderer<RefineryAccessPortBlockEntity> {
    public static final ResourceLocation TEXTURE = ResourceLocation
            .fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/fluid_refinery.png");

    public static final Material FIRE_TEXTURE = new Material(
            InventoryMenu.BLOCK_ATLAS,
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/fire_0")
    );

    public static final Material SOUL_FIRE_TEXTURE = new Material(
            InventoryMenu.BLOCK_ATLAS,
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_fire_0")
    );

    private final RefineryBaseModel base;
    private final RefineryMiddleModel middle;
    private final RefineryTopModel top;
    private final RefineryBlenderModel blender;

    public FluidRefineryRenderer(BlockEntityRendererProvider.Context context) {
        this.base = new RefineryBaseModel(context.bakeLayer(RefineryLayers.BASE));
        this.middle = new RefineryMiddleModel(context.bakeLayer(RefineryLayers.MIDDLE));
        this.top = new RefineryTopModel(context.bakeLayer(RefineryLayers.TOP));
        this.blender = new RefineryBlenderModel(context.bakeLayer(RefineryLayers.BLENDER));
    }

    @Override
    protected void renderSafe(RefineryAccessPortBlockEntity be, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (!be.isAssembled() && be.renderFalseRefinery <= 0)
            return;

        Direction facing = be.getBlockState().getValue(RefineryAccessPortBlock.FACING);
        int height = be.getStructureHeight();
        if (be.renderFalseRefinery > 0)
            height = be.renderFalseRefinery;

        // Fix blackened model by getting light from the air block in front of the
        // access port
        int light = net.minecraft.client.renderer.LevelRenderer.getLightColor(be.getLevel(),
                be.getBlockPos().relative(facing));

        // Fetch the kinetic proxy to get smooth network-synced angles, even if the refinery is underpowered
        float angle = 0;
        float speed = 0;
        RefineryKineticProxyBlockEntity kineticProxy = null;
        if (be.isAssembled() && be.getLevel() != null) {
            for (BlockPos proxyPos : be.getProxyPositions()) {
                net.minecraft.world.level.block.entity.BlockEntity proxyBe = be.getLevel().getBlockEntity(proxyPos);
                if (proxyBe instanceof RefineryKineticProxyBlockEntity kp) {
                    kineticProxy = kp;
                    break;
                }
            }
        }

        if (kineticProxy != null) {
            angle = -getAngleForBe(kineticProxy, kineticProxy.getBlockPos(), Direction.Axis.Y);
            speed = kineticProxy.getSpeed();
        } else {
            angle = (float) Math.toRadians(be.getRotationAngle());
        }
        //angle -= (float) Math.toRadians(-4.2f * Math.abs(speed));

        poseStack.pushPose();
        // 1. Move to the center of the Port block
        poseStack.translate(0.5, 0, 0.5);

        // 2. Rotate based on facing and apply 180 correction as requested
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180));

        // 3. Move to the center of the 3x3 base (1 block back from port row)
        poseStack.translate(0, 0, 1.0);

        // --- Render Fluids ---
        renderFluids(be, poseStack, buffer, light);

        // 4. Align with Blockbench / EntityModel coordinate system
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0, -1.5, 0);

        // --- Render Base ---
        base.render(poseStack, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), light, combinedOverlay);

        // --- Render Middle Layers & Blenders ---
        for (int i = 1; i < height - 1; i++) {
            poseStack.pushPose();
            // All models now use offset 24, so we just translate by 1 block (16 units) per layer
            poseStack.translate(0, -i, 0);
            middle.render(poseStack, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), light, combinedOverlay);

            if (be.renderFalseRefinery <= 0)
            {
                renderAnimatedBlender((float) Math.toDegrees(angle), be, poseStack, buffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                        partialTicks,
                        light, combinedOverlay);
            }

            poseStack.popPose();
        }

        // --- Render Top Cap ---
        poseStack.pushPose();
        poseStack.translate(0, -(height - 1), 0);
        top.render(poseStack, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), light, combinedOverlay);
        poseStack.popPose();

        // --- Shaft Rendering ---
        if (be.renderFalseRefinery <= 0)
        {
            SuperByteBuffer shaft = CachedBuffers.partial(ModPartialModels.SHAFT_VERTICAL, be.getBlockState());

            poseStack.pushPose();
            poseStack.translate(-0.5, -(height - 1.5), -0.5);
            shaft.rotateCentered(angle, Direction.Axis.Y)
                    .light(light)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
            poseStack.popPose();
        }

        // --- Fire Rendering ---
        if (be.getHeatLevel() > 0)
            renderBlazeFires(buffer.getBuffer(RenderType.cutout()), poseStack, light, be.getHeatLevel());

        // ---Send to Rendering ---
        poseStack.popPose();

        FilteringRenderer.renderOnBlockEntity(be, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
    }

    private void renderAnimatedBlender(float angle, RefineryAccessPortBlockEntity be, PoseStack poseStack, VertexConsumer buffer,
            float partialTicks, int light, int overlay) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        blender.render(poseStack, buffer, light, overlay);

        poseStack.popPose();
    }

    private void renderFluids(RefineryAccessPortBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
        int height = be.getStructureHeight();
        float totalHeight = height - 1.0625f;
        float tankHeightLimit = totalHeight / 3f;
        float startY = 0.9375f;

        float currentY = startY;

        // Render Output Tank (Bottom)
        currentY += renderFluidStack(be.outputTank.getFluid(), be.outputTank.getCapacity(), ms, buffer, light, currentY,
                tankHeightLimit);

        // Render Input A (Middle)
        currentY += renderFluidStack(be.inputTankA.getFluid(), be.inputTankA.getCapacity(), ms, buffer, light,
                currentY,
                tankHeightLimit);

        // Render Input B (Top)
        currentY += renderFluidStack(be.inputTankB.getFluid(), be.inputTankB.getCapacity(), ms, buffer, light,
                currentY,
                tankHeightLimit);
    }

    /**
     * Renders a fluid stack and returns the actual height it occupied.
     * 
     * Tweakable Parameters:
     * - uScale / vScale: Controls texture tiling.
     * Since fluids are on the block atlas, values > 1.0 / box_width may cause
     * bleeding.
     * Default (1.0) stretches the texture once across the face.
     */
    private float renderFluidStack(FluidStack stack, int capacity, PoseStack ms, MultiBufferSource buffer, int light,
            float yStart, float maxHeight) {
        if (stack.isEmpty() || stack.getAmount() <= 0)
            return 0;

        float h = (float) stack.getAmount() / capacity * maxHeight;
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(stack.getFluid());
        int color = props.getTintColor(stack);
        ResourceLocation stillTexture = props.getStillTexture(stack);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        // Texture Tiling Settings
        float uScale = 1.0f;
        float vScale = 0.85f;

        // Make sure it's translucent (at least 50% opacity (0x80 is 50%))
        if ((color >> 24 & 0xFF) == 0xFF) {
            color = (color & 0x00FFFFFF) | (0xC8 << 24); // 0xC8 is 80% opacity
        }

        renderBox(ms, buffer.getBuffer(RenderType.cutout()), -1.35f, yStart, -1.35f, 1.35f,
                yStart + h,
                1.35f,
                color, light, sprite, uScale, vScale);

        return h;
    }

    private void renderBox(PoseStack ms, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2,
            float z2, int color, int light, TextureAtlasSprite sprite, float uScale, float vScale) {
        PoseStack.Pose pose = ms.last();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Calculate UV widths based on scale
        float uw = (u1 - u0) * uScale;
        float vw = (v1 - v0) * vScale;

        // Bottom
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0 + uw, v0, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u0 + uw, v0 + vw, 0, -1, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v0 + vw, 0, -1, 0, light);

        // Top
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, 0, 1, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v0 + vw, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u0 + uw, v0 + vw, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0 + uw, v0, 0, 1, 0, light);

        // North
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0 + vw, 0, 0, -1, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, 0, 0, -1, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0 + uw, v0, 0, 0, -1, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0 + uw, v0 + vw, 0, 0, -1, light);

        // South
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v0 + vw, 0, 0, 1, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u0 + uw, v0 + vw, 0, 0, 1, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u0 + uw, v0, 0, 0, 1, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v0, 0, 0, 1, light);

        // West
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0 + vw, -1, 0, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0 + uw, v0 + vw, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0 + uw, v0, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, -1, 0, 0, light);

        // East
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0, v0 + vw, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0, v0, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u0 + uw, v0, 1, 0, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u0 + uw, v0 + vw, 1, 0, 0, light);
    }

    private void renderBlazeFires(VertexConsumer vc, PoseStack ms, int light, int heatLevel)
    {
        ms.pushPose();

        TextureAtlasSprite sprite;
        if (heatLevel == 2)
            sprite = SOUL_FIRE_TEXTURE.sprite();
        else
            sprite = FIRE_TEXTURE.sprite();

        // 1. Move the coordinate system origin from the block corner to the block center, and offset from centre
        ms.translate(0, 1.45, 1);
        ms.scale(0.976f,0.976f,0.976f);
        ms.mulPose(Axis.ZP.rotationDegrees(180));

        // 2. Rotate the entire world space to match the controller's horizontal orientation
        // Minecraft's toYRot() paired with a negative angle perfectly aligns local +Z with world facing

        // --- FRONT-LEFT CORNER ---
        // Front Face (Facing +Z)
        renderFireQuad(vc, ms, light, sprite, new Vec3(1.1, 0.0, 0.5), 0.0f, new Vec3(0, 0, -1));
        // Left Face (Facing +X)
        renderFireQuad(vc, ms, light, sprite, new Vec3(1.5, 0.0, 0.1), 90.0f, new Vec3(-1, 0, 0));

        // --- FRONT-RIGHT CORNER ---
        // Front Face (Facing +Z)
        renderFireQuad(vc, ms, light, sprite, new Vec3(-1.1, 0.0, 0.5), 0.0f, new Vec3(0, 0, -1));
        // Right Face (Facing -X)
        renderFireQuad(vc, ms, light, sprite, new Vec3(-1.5, 0.0, 0.1), 270.0f, new Vec3(1, 0, 0));

        // --- BACK-LEFT CORNER ---
        // Back Face (Facing -Z)
        renderFireQuad(vc, ms, light, sprite, new Vec3(1.1, 0.0, -2.5), 180.0f, new Vec3(0, 0, 1));
        // Left Face (Facing +X)
        renderFireQuad(vc, ms, light, sprite, new Vec3(1.5, 0.0, -2.1), 90.0f, new Vec3(-1, 0, 0));

        // --- BACK-RIGHT CORNER ---
        // Back Face (Facing -Z)
        renderFireQuad(vc, ms, light, sprite, new Vec3(-1.1, 0.0, -2.5), 180.0f, new Vec3(0, 0, 1));
        // Right Face (Facing -X)
        renderFireQuad(vc, ms, light, sprite, new Vec3(-1.5, 0.0, -2.1), 270.0f, new Vec3(0, 0, 1));

        ms.popPose();
    }

    private void renderFireQuad(VertexConsumer vc, PoseStack ms, int light, TextureAtlasSprite sprite, Vec3 posOffset, float rotY, Vec3 norm)
    {
        ms.pushPose();

        ms.translate(posOffset.x, posOffset.y, posOffset.z);
        ms.mulPose(Axis.YP.rotationDegrees(rotY));

        Matrix4f pose = ms.last().pose();

        // Draw a flat quad (Facing the viewer is handled by your desired rotations)
        // Adjust coordinates as necessary to fit your block's dimension
        vc.addVertex(pose, -0.3f, 0f, 0f)
                .setColor(255, 255, 255, 255)
                .setUv(sprite.getU0(), sprite.getV1())
                .setLight(light)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(ms.last(), (float) norm.x, (float)norm.y, (float)norm.z);

        vc.addVertex(pose, 0.3f, 0f, 0f)
                .setColor(255, 255, 255, 255)
                .setUv(sprite.getU1(), sprite.getV1())
                .setLight(light)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(ms.last(), (float) norm.x, (float)norm.y, (float)norm.z);

        vc.addVertex(pose, 0.3f, 1f, 0f)
                .setColor(255, 255, 255, 255)
                .setUv(sprite.getU1(), sprite.getV0())
                .setLight(light)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(ms.last(), (float) norm.x, (float)norm.y, (float)norm.z);

        vc.addVertex(pose, -0.3f, 1f, 0f)
                .setColor(255, 255, 255, 255)
                .setUv(sprite.getU0(), sprite.getV0())
                .setLight(light)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(ms.last(), (float) norm.x, (float)norm.y, (float)norm.z);

        ms.popPose();
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int r, int g,
            int b, int a, float u, float v, float nx, float ny, float nz, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(RefineryAccessPortBlockEntity be) {
        if (!be.isAssembled())
            return super.getRenderBoundingBox(be);

        BlockState state = be.getBlockState();
        if (!state.hasProperty(RefineryAccessPortBlock.FACING))
            return super.getRenderBoundingBox(be);

        Direction facing = state.getValue(RefineryAccessPortBlock.FACING);
        BlockPos worldPos = be.getBlockPos();
        int height = be.getStructureHeight();

        BlockPos p1 = worldPos.relative(facing.getOpposite(), 2).relative(facing.getClockWise(), 1);
        BlockPos p2 = worldPos.relative(facing.getCounterClockWise(), 1).above(height - 1);

        return new AABB(worldPos).minmax(new AABB(p1)).minmax(new AABB(p2)).inflate(1.0);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(RefineryAccessPortBlockEntity be) {
        return be.isAssembled();
    }
}
