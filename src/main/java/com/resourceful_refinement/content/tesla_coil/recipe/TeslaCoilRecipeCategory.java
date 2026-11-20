package com.resourceful_refinement.content.tesla_coil.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.tesla_coil.TeslaCoilBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

/**
 * JEI view of Tesla Coil charging: input on the left, a coil pair facing across the space above a depot in the middle
 * (the bolt sags onto the item and flickers in time with the in-world strikes), and the result on the right. No backdrop texture; slots and the bolt
 * are drawn directly.
 */
public class TeslaCoilRecipeCategory implements IRecipeCategory<TeslaCoilRecipe> {

    public static final RecipeType<TeslaCoilRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "tesla_coil", TeslaCoilRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEIGHT = 66;
    private static final int INPUT_X = 20;
    private static final int OUTPUT_X = 124;
    private static final int SLOT_Y = 22;
    private static final int MACHINE_X = WIDTH / 2;
    private static final int COIL_Y = 32;
    private static final int COIL_OFFSET = 20;
    /** Distance from a coil's centre to its head tip on screen. */
    private static final int HEAD_REACH = 9;
    /** Lowest point of the charging bolt, just above the depot's item. */
    private static final int BOLT_SAG_Y = 40;

    private static final int BOLT_GLOW = 0x704C6CFF;
    private static final int BOLT_CORE = 0xFFE8F4FF;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic outputSlot;
    private final ItemStack coilStack;
    private final ItemStack depotStack;

    public TeslaCoilRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.coilStack = new ItemStack(ModBlocks.TESLA_COIL.get());
        this.depotStack = AllBlocks.DEPOT.asStack();
        this.icon = guiHelper.createDrawableItemStack(coilStack);
        this.slot = guiHelper.getSlotDrawable();
        this.outputSlot = guiHelper.getOutputSlot();
    }

    @Override
    public RecipeType<TeslaCoilRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("jei.resourceful_refinement.tesla_coil"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TeslaCoilRecipe recipe, IFocusGroup focuses) {
        int count = recipe.getIngredient().count();
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
                .addItemStacks(Arrays.stream(recipe.getIngredient().ingredient().getItems())
                        .map(stack -> stack.copyWithCount(count))
                        .toList());

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
                .addItemStack(recipe.getResult().getStack());
    }

    @Override
    public void draw(TeslaCoilRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        slot.draw(guiGraphics, INPUT_X - 1, SLOT_Y - 1);
        outputSlot.draw(guiGraphics, OUTPUT_X - 5, SLOT_Y - 5);

        // Depot, with the coil pair facing each other across the space above it.
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(MACHINE_X, 50, 0);
        pose.scale(1.5F, 1.5F, 1.5F);
        guiGraphics.renderFakeItem(depotStack, -8, -8);
        pose.popPose();

        renderCoil(guiGraphics, MACHINE_X - COIL_OFFSET, 90.0F);
        renderCoil(guiGraphics, MACHINE_X + COIL_OFFSET, -90.0F);

        drawBolt(guiGraphics, recipe);

        String charges = recipe.getCharges() == 1 ? "1 charge" : recipe.getCharges() + " charges";
        guiGraphics.drawString(font, charges, 4, 4, 0xFF808080, false);
        String time = String.format("%.1fs @ %d RPM", recipe.getProcessingDuration() / 20f,
                (int) TeslaCoilBlockEntity.CHARGE_REFERENCE_RPM);
        guiGraphics.drawString(font, time, 4, HEIGHT - 12, 0xFF808080, false);

        float chance = recipe.getResult().getChance();
        if (chance < 1.0F) {
            String chanceText = Math.round(chance * 100) + "%";
            guiGraphics.drawString(font, chanceText, OUTPUT_X + 8 - font.width(chanceText) / 2, SLOT_Y + 22, 0xFFD0A040, false);
        }
    }

    /** One coil at {@code (x, COIL_Y)}, rotated about the screen axis so its head points at the other coil. */
    private void renderCoil(GuiGraphics guiGraphics, int x, float rotation) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, COIL_Y, 100);
        pose.mulPose(Axis.ZP.rotationDegrees(rotation));
        pose.scale(1.5F, 1.5F, 1.5F);
        guiGraphics.renderFakeItem(coilStack, -8, -8);
        pose.popPose();
    }

    /**
     * Jagged bolt from one coil head to the other, sagging down to the item on the depot. On during each strike and
     * off during the pause, timed as at {@link TeslaCoilBlockEntity#CHARGE_REFERENCE_RPM}.
     */
    private static void drawBolt(GuiGraphics guiGraphics, TeslaCoilRecipe recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        long ticks = minecraft.level != null ? minecraft.level.getGameTime() : System.currentTimeMillis() / 50L;
        int cycle = TeslaCoilBlockEntity.CHARGE_STRIKE_TICKS + TeslaCoilBlockEntity.CHARGE_PAUSE_TICKS;
        long elapsed = ticks % ((long) recipe.getProcessingDuration() + TeslaCoilBlockEntity.CHARGE_PAUSE_TICKS);
        if (elapsed % cycle >= TeslaCoilBlockEntity.CHARGE_STRIKE_TICKS) {
            return;
        }

        // Re-strike the shape every couple of ticks. The sag is a parabola through both heads and the item.
        RandomSource random = RandomSource.create(ticks / 2L * 31L + recipe.getCharges());
        int left = MACHINE_X - COIL_OFFSET + HEAD_REACH;
        int right = MACHINE_X + COIL_OFFSET - HEAD_REACH;
        int previousY = COIL_Y;
        for (int x = left; x <= right; x += 2) {
            float t = (x - left) / (float) (right - left) * 2.0F - 1.0F;
            int y = Math.round(COIL_Y + (BOLT_SAG_Y - COIL_Y) * (1.0F - t * t));
            if (x > left && x < right - 1) {
                y += random.nextInt(3) - 1;
            }
            int top = Math.min(previousY, y);
            int bottom = Math.max(previousY, y) + 1;
            guiGraphics.fill(x - 1, top - 1, x + 2, bottom + 1, 300, BOLT_GLOW);
            guiGraphics.fill(x, top, x + 1, bottom, 300, BOLT_CORE);
            guiGraphics.fill(x + 1, y, x + 2, y + 1, 300, BOLT_CORE);
            previousY = y;
        }
    }
}
