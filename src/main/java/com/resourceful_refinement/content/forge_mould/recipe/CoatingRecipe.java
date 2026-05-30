package com.resourceful_refinement.content.forge_mould.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.content.coating.CoatingData;
import com.resourceful_refinement.content.coating.CoatingType;
import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class CoatingRecipe implements Recipe<MechanicalForgeMouldRecipeInput> {

    private final SizedFluidIngredient fluidIngredient;
    private final Ingredient itemIngredient;
    private final CoatingType coatingType;
    private final int processingDuration;

    public CoatingRecipe(SizedFluidIngredient fluidIngredient, Ingredient itemIngredient, CoatingType coatingType, int processingDuration) {
        this.fluidIngredient = fluidIngredient;
        this.itemIngredient = itemIngredient;
        this.coatingType = coatingType;
        this.processingDuration = processingDuration;
    }

    @Override
    public boolean matches(MechanicalForgeMouldRecipeInput input, Level level) {
        if (!input.hasCastingDepot()) return false;
        if (!fluidIngredient.test(input.fluid())) return false;
        if (!itemIngredient.test(input.item())) return false;

        ItemStack tool = input.depotItem();
        if (tool.isEmpty()) return false;

        // Check if tool is valid (DiggerItem, SwordItem, TridentItem)
        if (!(tool.getItem() instanceof DiggerItem) && !(tool.getItem() instanceof SwordItem) && !(tool.getItem() instanceof TridentItem)) return false;

        // Coating compatibility: reject mismatched types, or same type already at full integrity
        CoatingData data = tool.get(ModDataComponents.COATING_DATA.get());
        if (data != null) {
            if (data.type() != coatingType) return false;
            if (data.integrity() >= coatingType.getMaxDurability()) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(MechanicalForgeMouldRecipeInput input, HolderLookup.Provider provider) {
        ItemStack result = input.depotItem().copy();
        result.setCount(1);
        result.set(ModDataComponents.COATING_DATA.get(), new CoatingData(coatingType, coatingType.getMaxDurability()));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.COATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.COATING_TYPE.get();
    }

    public int getProcessingDuration() {
        return processingDuration;
    }

    public CoatingType getCoatingType() {
        return coatingType;
    }

    public SizedFluidIngredient getFluidIngredient() {
        return fluidIngredient;
    }

    public Ingredient getItemIngredient() {
        return itemIngredient;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        if (!itemIngredient.isEmpty()) {
            ingredients.add(itemIngredient);
        }
        return ingredients;
    }

    public static class Serializer implements RecipeSerializer<CoatingRecipe> {
        private static final MapCodec<CoatingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid").forGetter(CoatingRecipe::getFluidIngredient),
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CoatingRecipe::getItemIngredient),
                CoatingType.CODEC.fieldOf("coating").forGetter(CoatingRecipe::getCoatingType),
                Codec.INT.optionalFieldOf("duration", 100).forGetter(CoatingRecipe::getProcessingDuration)
        ).apply(inst, CoatingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CoatingRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    SizedFluidIngredient.STREAM_CODEC.encode(buf, recipe.getFluidIngredient());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getItemIngredient());
                    buf.writeEnum(recipe.getCoatingType());
                    buf.writeInt(recipe.getProcessingDuration());
                },
                buf -> new CoatingRecipe(
                        SizedFluidIngredient.STREAM_CODEC.decode(buf),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        buf.readEnum(CoatingType.class),
                        buf.readInt()
                )
        );

        @Override
        public MapCodec<CoatingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CoatingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
