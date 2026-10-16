package com.resourceful_refinement.content.hoverpack;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Renders the Hoverpack's wings on the wearer. Reuses the vanilla {@link ElytraLayer} geometry/animation (per design:
 * "an adapted custom model, based on the vanilla elytra") and only swaps the render gate and texture. The vanilla
 * elytra layer stays on the player and renders vanilla elytras; this one renders Hoverpacks — the two don't overlap.
 *
 * <p>Note: like a vanilla elytra, a player with a custom cape/elytra skin texture will show that over the wings; a
 * player with no such skin gets the Hoverpack texture (the {@link #getElytraTexture} fallback).</p>
 */
@OnlyIn(Dist.CLIENT)
public class HoverpackWingsLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

    private static final ResourceLocation WINGS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/entity/hoverpack.png");

    public HoverpackWingsLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet) {
        super(renderer, modelSet);
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        return stack.getItem() instanceof HoverpackItem;
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        return WINGS_TEXTURE;
    }

    /** Adds a Hoverpack wings layer to each player skin renderer. Called from an {@code AddLayers} subscriber. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(EntityRenderersEvent.AddLayers event) {
        for (var skinModel : event.getSkins()) {
            EntityRenderer<? extends Player> renderer = event.getSkin(skinModel);
            if (renderer instanceof LivingEntityRenderer livingRenderer) {
                livingRenderer.addLayer(new HoverpackWingsLayer(livingRenderer, event.getEntityModels()));
            }
        }
    }
}
