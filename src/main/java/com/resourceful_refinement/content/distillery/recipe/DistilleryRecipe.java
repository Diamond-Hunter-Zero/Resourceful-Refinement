package com.resourceful_refinement.content.distillery.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.codec.CreateCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

public class DistilleryRecipe extends StandardProcessingRecipe<DistilleryRecipeInput> {

    private ExtendedHeatCondition heatRequirement;
    private int heightRequirement;

    private final List<SizedIngredient> sizedIngredients;
    private final List<SizedIngredient> combinedIngredients;

    public DistilleryRecipe(DistilleryProcessingRecipeParams params, ExtendedHeatCondition heatCondition, int heightRequirement, List<SizedIngredient> sizedIngredients) {
        super(ModRecipeTypes.DISTILLERY_TYPE_INFO, params);
        this.heatRequirement = heatCondition;
        this.heightRequirement = Math.clamp(heightRequirement, DistilleryBlockEntity.MIN_STACK_HEIGHT, DistilleryBlockEntity.MAX_STACK_HEIGHT);

        // Replace Create's backing ingredients with our sized type
        List<SizedIngredient> sizedIngredientsToAdd = new ArrayList<>();
        for (Ingredient baseIngredient : ingredients) {
            sizedIngredientsToAdd.add(new SizedIngredient(baseIngredient, 1));
        }

        // Ensure passed sized ingredients list is non-null
        sizedIngredients = sizedIngredients != null ? sizedIngredients : List.of();
        for (SizedIngredient sizedIngredient : sizedIngredients) {
            sizedIngredientsToAdd.add(sizedIngredient);
        }
        this.sizedIngredients = sizedIngredients;
        this.combinedIngredients = sizedIngredientsToAdd;
    }


    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 2;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    public ExtendedHeatCondition getRequiredHeatCondition() { return heatRequirement; }

    public int getRequiredHeight() { return heightRequirement; }

    public List<SizedIngredient> getSizedIngredients() { return sizedIngredients; }
    public List<SizedIngredient> getCombinedIngredients() { return combinedIngredients; }

    private DistilleryProcessingRecipeParams getDistilleryParams() {
        return (DistilleryProcessingRecipeParams) getParams();
    }


    // -------------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------------
    @Override
    public boolean matches(DistilleryRecipeInput input, Level level) {
        if (input.items().size() < combinedIngredients.size()) return false;
        if (input.fluids().size() < fluidIngredients.size()) return false;

        // Check Combined Item Ingredients
        for (SizedIngredient ingredient : combinedIngredients) {
            boolean found = false;
            for (ItemStack stack : input.items()) {
                if (ingredient.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        // Check Fluid Ingredients
        for (SizedFluidIngredient ingredient : fluidIngredients) {
            boolean found = false;
            for (FluidStack stack : input.fluids()) {
                if (ingredient.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    public boolean matchesFilter(FilteringBehaviour filter) {
        if (filter == null || filter.getFilter().isEmpty()) return true;

        // Check fluid outputs
        for (FluidStack fluid : getFluidResults()) {
            if (filter.test(fluid)) return true;
        }

        // Check item outputs
        for (ProcessingOutput output : results) {
            if (filter.test(output.getStack())) return true;
        }

        return false;
    }


    // -------------------------------------------------------------------------
    // Custom Serializer
    // -------------------------------------------------------------------------
    public static class Serializer implements RecipeSerializer<DistilleryRecipe> {

        private static final MapCodec<DistilleryRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        DistilleryProcessingRecipeParams.CODEC.forGetter(DistilleryRecipe::getDistilleryParams),
                        ExtendedHeatCondition.CODEC.optionalFieldOf("heat_requirement", ExtendedHeatCondition.NONE).forGetter(DistilleryRecipe::getRequiredHeatCondition),
                        Codec.INT.fieldOf("distillery_height").forGetter(DistilleryRecipe::getRequiredHeight),
                        SizedIngredient.FLAT_CODEC.listOf().optionalFieldOf("sized_ingredients", List.of()).forGetter(DistilleryRecipe::getSizedIngredients)
                ).apply(inst, DistilleryRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DistilleryRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            DistilleryProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getDistilleryParams());
                            ExtendedHeatCondition.STREAM_CODEC.encode(buf, recipe.getRequiredHeatCondition());
                            buf.writeInt(recipe.getRequiredHeight());

                            buf.writeInt(recipe.getSizedIngredients().size());
                            for (SizedIngredient ingredient : recipe.getSizedIngredients()) {
                                SizedIngredient.STREAM_CODEC.encode(buf, ingredient);
                            }
                        },
                        buf -> {
                            DistilleryProcessingRecipeParams params = DistilleryProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            ExtendedHeatCondition heatRequirementId = ExtendedHeatCondition.STREAM_CODEC.decode(buf);
                            int heightRequirement = buf.readInt();

                            int count = buf.readInt();
                            List<SizedIngredient> sizedIngredients = new ArrayList<>(count);
                            for (int i = 0; i < count; i++) {
                                sizedIngredients.add(SizedIngredient.STREAM_CODEC.decode(buf));
                            }

                            return new DistilleryRecipe(params, heatRequirementId, heightRequirement, sizedIngredients);
                        }
                );

        @Override
        public MapCodec<DistilleryRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DistilleryRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    /**
     * Create's standard params codec includes its own {@code heat_requirement}
     * field. Distillery recipes replace that field with {@link ExtendedHeatCondition},
     * so their params codec serializes only the remaining processing properties.
     */
    public static class DistilleryProcessingRecipeParams extends ProcessingRecipeParams {

        private static final Codec<Either<SizedFluidIngredient, Ingredient>> INGREDIENT_CODEC =
                Codec.either(CreateCodecs.SIZED_FLUID_INGREDIENT, Ingredient.CODEC);
        private static final Codec<Either<FluidStack, ProcessingOutput>> RESULT_CODEC =
                Codec.either(FluidStack.CODEC, ProcessingOutput.CODEC);

        public static final MapCodec<DistilleryProcessingRecipeParams> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        INGREDIENT_CODEC.listOf().fieldOf("ingredients").forGetter(params -> params.ingredients()),
                        RESULT_CODEC.listOf().fieldOf("results").forGetter(params -> params.results()),
                        Codec.INT.optionalFieldOf("processing_time", 0).forGetter(params -> params.processingDuration())
                ).apply(inst, DistilleryProcessingRecipeParams::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DistilleryProcessingRecipeParams> STREAM_CODEC =
                StreamCodec.of(
                        (buf, params) -> {
                            CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buf, params.ingredients);
                            CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).encode(buf, params.fluidIngredients);
                            CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buf, params.results);
                            CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).encode(buf, params.fluidResults);
                            ByteBufCodecs.VAR_INT.encode(buf, params.processingDuration);
                        },
                        buf -> {
                            DistilleryProcessingRecipeParams params = new DistilleryProcessingRecipeParams();
                            params.ingredients = CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
                            params.fluidIngredients = CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).decode(buf);
                            params.results = CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buf);
                            params.fluidResults = CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).decode(buf);
                            params.processingDuration = ByteBufCodecs.VAR_INT.decode(buf);
                            return params;
                        }
                );

        protected DistilleryProcessingRecipeParams() {
        }

        private DistilleryProcessingRecipeParams(
                List<Either<SizedFluidIngredient, Ingredient>> ingredients,
                List<Either<FluidStack, ProcessingOutput>> results,
                int processingDuration
        ) {
            ingredients.forEach(ingredient -> ingredient.ifLeft(fluidIngredients::add).ifRight(this.ingredients::add));
            results.forEach(result -> result.ifLeft(fluidResults::add).ifRight(this.results::add));
            this.processingDuration = processingDuration;
        }
    }
}
