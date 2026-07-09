package com.resourceful_refinement.content.drill_pylon.recipe;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DrillPylonRecipeCategory implements IRecipeCategory<DrillPylonRecipe> {
    public static final RecipeType<DrillPylonRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "drill_pylon", DrillPylonRecipe.class);

    private final IDrawableStatic background;
    private final IDrawable icon;

    public DrillPylonRecipeCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(150, 54);
        icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.DRILL_PYLON_HEAD.get()));
    }

    @Override
    public RecipeType<DrillPylonRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.resourceful_refinement.drill_pylon");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DrillPylonRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 18).addItemStack(new ItemStack(recipe.getSourceItem()));
        int x = 86;
        for (var output : recipe.getRollableResults()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, 18).addItemStack(output.getStack());
            x += 20;
            if (x > 126) break;
        }
    }
}
