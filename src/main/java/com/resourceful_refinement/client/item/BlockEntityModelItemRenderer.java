package com.resourceful_refinement.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.content.bucket_excavator.BucketExcavatorModel;
import com.resourceful_refinement.content.bucket_excavator.BucketExcavatorRenderer;
import com.resourceful_refinement.content.drill_pylon.DrillPylonHeadModel;
import com.resourceful_refinement.content.drill_pylon.DrillPylonHeadRendering;
import com.resourceful_refinement.content.glare.chromatic_transceiver.GlareChromaticTransceiverModel;
import com.resourceful_refinement.content.glare.chromatic_transceiver.GlareChromaticTransceiverRenderer;
import com.resourceful_refinement.content.glare.emitter.GlareEmitterDishModel;
import com.resourceful_refinement.content.glare.emitter.GlareEmitterDishRenderer;
import com.resourceful_refinement.content.glare.kinetic_receiver.GlareKineticReceiverModel;
import com.resourceful_refinement.content.glare.kinetic_receiver.GlareKineticReceiverRenderer;
import com.resourceful_refinement.content.glare.lux.GlareLuxTransceiverModel;
import com.resourceful_refinement.content.glare.lux.LuxTransceiverRenderer;
import com.resourceful_refinement.content.glare.relay.GlareRelayModel;
import com.resourceful_refinement.content.glare.relay.GlareRelayRenderer;
import com.resourceful_refinement.content.mineral_deposit.MineralDepositModel;
import com.resourceful_refinement.content.mineral_deposit.MineralDepositVisual;
import com.resourceful_refinement.content.mineral_deposit.MineralDepositVisuals;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalModel;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalRenderer;
import com.resourceful_refinement.content.resonator.ResourceResonatorModel;
import com.resourceful_refinement.content.resonator.ResourceResonatorRenderer;
import com.resourceful_refinement.registry.ModItems;
import com.resourceful_refinement.registry.ModPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Item renderer for blocks whose world visuals come entirely from a block entity renderer (empty JSON
 * block models). The item JSON parents {@code minecraft:builtin/entity} and carries the usual block
 * display transforms, so by the time {@link #renderByItem} runs the pose stack is already in block space
 * (0..1) — each branch below mirrors its block entity renderer in a static, default-state pose.
 */
public class BlockEntityModelItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final BlockEntityModelItemRenderer INSTANCE = new BlockEntityModelItemRenderer();

    /** Bucket Excavator wheel is far larger than a block; shrink it and centre the wheel in the slot. */
    private static final float EXCAVATOR_ITEM_SCALE = 0.35F;
    private static final float EXCAVATOR_WHEEL_OFFSET = 3F; // wheel hub sits 48px in front of the block

    private static final int PRISM_GREY = 0x808080;

    private BucketExcavatorModel excavator;
    private ResourceResonatorModel resonator;
    private MineralDepositModel mineralDeposit;
    private DrillPylonHeadModel drillHead;
    private GlareRelayModel relay;
    private GlareEmitterDishModel emitterDish;
    private GlareKineticReceiverModel kineticReceiver;
    private GlareChromaticTransceiverModel chromaticTransceiver;
    private GlareLuxTransceiverModel luxTransceiver;
    private TelemetryTerminalModel telemetryTerminal;

    private BlockEntityModelItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void register(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return INSTANCE;
            }
        };
        event.registerItem(extensions,
                ModItems.BUCKET_EXCAVATOR_ITEM.get(),
                ModItems.RESOURCE_RESONATOR_ITEM.get(),
                ModItems.MINERAL_DEPOSIT_ITEM.get(),
                ModItems.DRILL_PYLON_HEAD_ITEM.get(),
                ModItems.GLARE_RELAY.get(),
                ModItems.GLARE_EMITTER_DISH.get(),
                ModItems.GLARE_KINETIC_RECEIVER.get(),
                ModItems.GLARE_CHROMATIC_TRANSCEIVER.get(),
                ModItems.LUX_TRANSCEIVER.get(),
                ModItems.GLARE_TELEMETRY_TERMINAL.get());
    }

    /** Bakes lazily: the entity model set is only populated after the first resource reload. */
    private void ensureModels() {
        if (excavator != null) return;
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        excavator = new BucketExcavatorModel(models.bakeLayer(BucketExcavatorModel.LAYER_LOCATION));
        resonator = new ResourceResonatorModel(models.bakeLayer(ResourceResonatorModel.LAYER_LOCATION));
        mineralDeposit = new MineralDepositModel(models.bakeLayer(MineralDepositModel.LAYER_LOCATION));
        drillHead = new DrillPylonHeadModel(models.bakeLayer(DrillPylonHeadModel.LAYER_LOCATION));
        relay = new GlareRelayModel(models.bakeLayer(GlareRelayModel.LAYER_LOCATION));
        emitterDish = new GlareEmitterDishModel(models.bakeLayer(GlareEmitterDishModel.LAYER_LOCATION));
        kineticReceiver = new GlareKineticReceiverModel(models.bakeLayer(GlareKineticReceiverModel.LAYER_LOCATION));
        chromaticTransceiver = new GlareChromaticTransceiverModel(models.bakeLayer(GlareChromaticTransceiverModel.LAYER_LOCATION));
        telemetryTerminal = new TelemetryTerminalModel(models.bakeLayer(TelemetryTerminalModel.LAYER_LOCATION));
        luxTransceiver = new GlareLuxTransceiverModel(models.bakeLayer(GlareLuxTransceiverModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer,
                             int light, int overlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        ensureModels();
        Item item = stack.getItem();
        BlockState state = blockItem.getBlock().defaultBlockState();

        ms.pushPose();
        if (item == ModItems.BUCKET_EXCAVATOR_ITEM.get()) renderExcavator(state, ms, buffer, light, overlay);
        else if (item == ModItems.RESOURCE_RESONATOR_ITEM.get()) renderResonator(ms, buffer, light, overlay);
        else if (item == ModItems.MINERAL_DEPOSIT_ITEM.get()) renderMineralDeposit(ms, buffer, light, overlay);
        else if (item == ModItems.DRILL_PYLON_HEAD_ITEM.get())
            DrillPylonHeadRendering.render(drillHead, state, 0, ms, buffer, light, overlay);
        else if (item == ModItems.GLARE_RELAY.get()) renderRelay(state, ms, buffer, light, overlay);
        else if (item == ModItems.GLARE_EMITTER_DISH.get()) renderEmitterDish(state, ms, buffer, light, overlay);
        else if (item == ModItems.GLARE_KINETIC_RECEIVER.get()) renderKineticReceiver(state, ms, buffer, light, overlay);
        else if (item == ModItems.GLARE_CHROMATIC_TRANSCEIVER.get()) renderChromaticTransceiver(state, ms, buffer, light, overlay);
        else if (item == ModItems.LUX_TRANSCEIVER.get()) renderLuxTransceiver(state, ms, buffer, light, overlay);
        else if (item == ModItems.GLARE_TELEMETRY_TERMINAL.get()) renderTelemetryTerminal(state, ms, buffer, light, overlay);
        ms.popPose();
    }

    // --- Machines ---

    private void renderExcavator(BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Default facing is NORTH: the wheel hub sits 3 blocks north (-Z) of the block. Pull it into the slot.
        ms.translate(0.5, 0.5, 0.5);
        ms.scale(EXCAVATOR_ITEM_SCALE, EXCAVATOR_ITEM_SCALE, EXCAVATOR_ITEM_SCALE);
        ms.translate(-0.5, -0.5, -0.5 + EXCAVATOR_WHEEL_OFFSET);

        Direction facing = getFacing(state);
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        excavator.animateWheel(0);
        excavator.render(ms, buffer.getBuffer(RenderType.entityCutout(BucketExcavatorRenderer.TEXTURE)), light, overlay);
    }

    private void renderResonator(PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);
        resonator.render(ms, buffer.getBuffer(RenderType.entityCutout(ResourceResonatorRenderer.TEXTURE)), light, overlay);
    }

    private void renderMineralDeposit(PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Example pairing: stone base with its configured deposit material (basalt by default).
        Block baseBlock = Blocks.STONE;
        Block depositBlock = MineralDepositVisuals.get(Blocks.STONE).map(MineralDepositVisual::depositMaterial).orElse(Blocks.BASALT);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        dispatcher.renderSingleBlock(baseBlock.defaultBlockState(), ms, buffer, light, overlay);

        TextureAtlasSprite sprite = dispatcher.getBlockModel(depositBlock.defaultBlockState()).getParticleIcon(ModelData.EMPTY);
        VertexConsumer consumer = new SpriteCoordinateExpander(buffer.getBuffer(RenderType.cutout()), sprite);
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);
        mineralDeposit.render(ms, consumer, light, overlay);
    }

    // --- GLARE network ---

    private void renderRelay(BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);
        relay.render(ms, buffer.getBuffer(RenderType.entityCutout(GlareRelayRenderer.TEXTURE)), light, overlay);
        ms.popPose();
        renderPrism(CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state, Direction.NORTH),
                ModPartialModels.PRISM_DEFAULT_COLOUR, ms, buffer, light);
    }

    private void renderEmitterDish(BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = getFacing(state);
        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        emitterDish.animateRings(0);
        emitterDish.render(ms, buffer.getBuffer(RenderType.entityCutout(GlareEmitterDishRenderer.TEXTURE)), light, overlay);
        ms.popPose();
        renderPrism(CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state, facing),
                ModPartialModels.PRISM_DEFAULT_COLOUR, ms, buffer, light);
        renderCog(state, facing, ms, buffer, light);
    }

    private void renderKineticReceiver(BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = getFacing(state);
        ms.pushPose();
        applyReceiverTransform(ms, facing);
        kineticReceiver.render(ms, buffer.getBuffer(RenderType.entityCutout(GlareKineticReceiverRenderer.TEXTURE)), light, overlay);
        ms.popPose();
        renderPrism(CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state),
                ModPartialModels.PRISM_DEFAULT_COLOUR, ms, buffer, light);
        renderCog(state, facing, ms, buffer, light);
    }

    private void renderChromaticTransceiver(BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = getFacing(state);
        ms.pushPose();
        applyReceiverTransform(ms, facing);
        chromaticTransceiver.render(ms, buffer.getBuffer(RenderType.entityCutout(GlareChromaticTransceiverRenderer.TEXTURE)),
                light, overlay, PRISM_GREY);
        ms.popPose();
        renderPrism(CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state), PRISM_GREY, ms, buffer, light);
    }

    private void renderLuxTransceiver(BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = getFacing(state);
        ms.pushPose();
        if (facing.getAxis().isVertical()) {
            boolean isUp = facing == Direction.UP;
            ms.translate(0.5, 0.5, isUp ? -0.5 : 1.5);
            ms.scale(1, -1, -1);
            ms.mulPose(Axis.XP.rotationDegrees(isUp ? -90 : 90));
        } else {
            ms.translate(0.5, 1.5, 0.5);
            ms.scale(-1, -1, 1);
            ms.mulPose(Axis.YP.rotationDegrees(facing.getOpposite().toYRot()));
        }
        luxTransceiver.render(ms, buffer.getBuffer(RenderType.entityCutout(LuxTransceiverRenderer.TEXTURE)), light, overlay);
        ms.popPose();
        renderPrism(CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state),
                ModPartialModels.PRISM_DEFAULT_COLOUR, ms, buffer, light);
    }

    private void renderTelemetryTerminal(BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);
        ms.mulPose(Axis.YP.rotationDegrees(getFacing(state).getOpposite().toYRot()));
        telemetryTerminal.render(ms, buffer.getBuffer(RenderType.entityCutout(TelemetryTerminalRenderer.TEXTURE)),
                light, overlay, 0xffdebb);
    }

    /** Shared by the kinetic receiver and chromatic transceiver block entity renderers. */
    private static void applyReceiverTransform(PoseStack ms, Direction facing) {
        if (facing.getAxis().isVertical()) {
            boolean isUp = facing == Direction.UP;
            ms.translate(0.5, 0.5, isUp ? 1.5 : -0.5);
            ms.scale(1, -1, -1);
            ms.mulPose(Axis.XP.rotationDegrees(isUp ? 90 : -90));
        } else {
            ms.translate(0.5, 1.5, 0.5);
            ms.scale(-1, -1, 1);
            ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        }
    }

    private static Direction getFacing(BlockState state) {
        for (var property : state.getProperties()) {
            if (property instanceof DirectionProperty dir && property.getName().equals("facing")) {
                return state.getValue(dir);
            }
        }
        return Direction.NORTH;
    }

    private static void renderPrism(SuperByteBuffer prism, int colour, PoseStack ms, MultiBufferSource buffer, int light) {
        prism.light(light)
                .color(colour)
                .renderInto(ms, buffer.getBuffer(Sheets.translucentCullBlockSheet()));
    }

    private static void renderCog(BlockState state, Direction facing, PoseStack ms, MultiBufferSource buffer, int light) {
        CachedBuffers.partialFacing(ModPartialModels.GLARE_COG_SHAFT, state, facing)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
    }
}
