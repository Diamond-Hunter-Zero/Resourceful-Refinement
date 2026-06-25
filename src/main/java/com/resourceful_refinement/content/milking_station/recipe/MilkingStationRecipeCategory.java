package com.resourceful_refinement.content.milking_station.recipe;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MilkingStationRecipeCategory implements IRecipeCategory<MilkingStationRecipe> {

    public static final RecipeType<MilkingStationRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "milking_station", MilkingStationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_milking_station_backdrop.png");
    private final Map<ResourceLocation, LivingEntity> entityPreviews = new HashMap<>();

    public MilkingStationRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MILKING_STATION.get()));
    }

    @Override
    public RecipeType<MilkingStationRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Mechanical Milking"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MilkingStationRecipe recipe, IFocusGroup focuses) {

        int width = this.background.getWidth();
        int centreYPos = (this.background.getHeight() / 2);

        if (!recipe.getRollableResults().isEmpty())
            builder.addSlot(RecipeIngredientRole.OUTPUT, width - 26, centreYPos-9).addItemStack(recipe.getRollableResults().getFirst().getStack());

        if (!recipe.getFluidResults().isEmpty())
            builder.addSlot(RecipeIngredientRole.OUTPUT, width - 51, centreYPos-9).addFluidStack(recipe.getFluidResults().getFirst().getFluid());
    }

    @Override
    public void draw(MilkingStationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int width = this.background.getWidth();
        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // 1. Render Processing Time (e.g., "200 ticks")
        String timeText = (int)(recipe.getProcessingDuration()/20f) + "s";
        int timeWidth = font.width(timeText);
        // Draw text at x=100, y=40 (adjust based on your background)
        guiGraphics.drawString(font, timeText, width - 46, centreYPos + 25, 0xFFF5F5F5, false);

        // Render fluid stack amounts
        if (!recipe.getFluidResults().isEmpty())
        {
            int outputAmount = recipe.getFluidResults().getFirst().getAmount();
            String outputAmountText = outputAmount + "mB";
            int outputAmountWidth = font.width(outputAmountText);
            guiGraphics.drawString(font, outputAmountText, width - 43 - outputAmountWidth/2f, centreYPos + 11, 0xFF808080, false);
        }

        // Render block model
        ItemStack forgeBlock = new ItemStack(ModBlocks.MILKING_STATION.get());
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(-centreXPos, -centreYPos-3, 0);
        pose.scale(2.0f, 2.0f, 2.0f);
        guiGraphics.renderFakeItem(forgeBlock, centreXPos-7, centreYPos-8);
        pose.popPose();

        renderRecipeEntity(recipe, guiGraphics);
    }

    private void renderRecipeEntity(MilkingStationRecipe recipe, GuiGraphics guiGraphics) {
        Optional<LivingEntity> entity = getOrCreatePreviewEntity(recipe.getEntityType());
        if (entity.isEmpty()) {
            return;
        }

        if (recipe.getEntityType().toString().equals("minecraft:player"))
        {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.scale(2.0f, 2.0f, 2.0f);
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            guiGraphics.drawString(font, "?",  16, 16, 0xFFF5F5F5, false);
            pose.popPose();
            return;
        }

        LivingEntity livingEntity = entity.get();
        float yaw = 25f;
        livingEntity.yRotO = yaw;
        livingEntity.setYRot(yaw);
        livingEntity.yHeadRotO = yaw;
        livingEntity.yHeadRot = yaw;
        livingEntity.yBodyRotO = yaw;
        livingEntity.yBodyRot = yaw;
        livingEntity.setXRot(0);
        livingEntity.xRotO = 0;
        livingEntity.walkAnimation.setSpeed(0);
        livingEntity.walkAnimation.update(0, 1);

        var mobHeight = livingEntity.getBbHeight();
        int scale = (int)Math.clamp((1.45/mobHeight)*24, 0.1, 25f);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(34, 50, 100);
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(160));
        pose.mulPose(Axis.YP.rotationDegrees(135 + yaw));

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> dispatcher.render(livingEntity, 0, 0, 0, 0, 1.0f,
                pose, guiGraphics.bufferSource(), LightTexture.FULL_SKY));
        guiGraphics.flush();
        dispatcher.setRenderShadow(true);
        pose.popPose();
    }

    private Optional<LivingEntity> getOrCreatePreviewEntity(ResourceLocation entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || entityId == null) {
            return Optional.empty();
        }

        LivingEntity cached = entityPreviews.get(entityId);
        if (cached != null && cached.level() == minecraft.level) {
            return Optional.of(cached);
        }

        if (entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER)) && minecraft.player != null) {
            entityPreviews.put(entityId, minecraft.player);
            return Optional.of(minecraft.player);
        }

        Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
        if (entityType.isEmpty()) {
            entityPreviews.remove(entityId);
            return Optional.empty();
        }

        Entity entity = entityType.get().create(minecraft.level);
        if (!(entity instanceof LivingEntity livingEntity)) {
            entityPreviews.remove(entityId);
            return Optional.empty();
        }

        if (livingEntity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        entityPreviews.put(entityId, livingEntity);
        return Optional.of(livingEntity);
    }
}
