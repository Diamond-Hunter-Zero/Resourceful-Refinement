package com.resourceful_refinement.content.refinery.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.distillery.recipe.DistilleryRecipe;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.codec.CreateCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

public class FluidRefineryRecipe extends StandardProcessingRecipe<FluidRefineryRecipeInput> {

    private final ExtendedHeatCondition heatRequirement;
    private final List<SizedIngredient> sizedIngredients;
    private final List<SizedIngredient> combinedIngredients;

    public FluidRefineryRecipe(ProcessingRecipeParams params, ExtendedHeatCondition heatRequirement, List<SizedIngredient> sizedIngredients) {
        super(ModRecipeTypes.FLUID_REFINERY_TYPE_INFO, params);
        this.heatRequirement = heatRequirement != null ? heatRequirement : ExtendedHeatCondition.NONE;

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
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    public ExtendedHeatCondition getRequiredHeatCondition() { return heatRequirement; }
    public List<SizedIngredient> getSizedIngredients() { return sizedIngredients; }
    public List<SizedIngredient> getCombinedIngredients() { return combinedIngredients; }

    private FluidRefineryProcessingRecipeParams getFluidRefineryParams() {
        return (FluidRefineryProcessingRecipeParams) getParams();
    }


    // -------------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------------
    @Override
    public boolean matches(FluidRefineryRecipeInput input, Level level) {
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
            for (net.neoforged.neoforge.fluids.FluidStack stack : input.fluids()) {
                if (ingredient.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    public boolean matchesFilter(com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour filter) {
        if (filter == null || filter.getFilter().isEmpty()) return true;

        // Check fluid outputs
        for (net.neoforged.neoforge.fluids.FluidStack fluid : getFluidResults()) {
            if (filter.test(fluid)) return true;
        }

        // Check item outputs
        for (com.simibubi.create.content.processing.recipe.ProcessingOutput output : results) {
            if (filter.test(output.getStack())) return true;
        }

        return false;
    }


    // -------------------------------------------------------------------------
    // Custom Serializer
    // -------------------------------------------------------------------------
    public static class Serializer implements RecipeSerializer<FluidRefineryRecipe> {

        private static final MapCodec<FluidRefineryRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        FluidRefineryProcessingRecipeParams.CODEC.forGetter(FluidRefineryRecipe::getFluidRefineryParams),
                        ExtendedHeatCondition.CODEC.optionalFieldOf("heat_requirement", ExtendedHeatCondition.NONE).forGetter(FluidRefineryRecipe::getRequiredHeatCondition),
                        SizedIngredient.FLAT_CODEC.listOf().optionalFieldOf("sized_ingredients", List.of()).forGetter(FluidRefineryRecipe::getSizedIngredients)
                ).apply(inst, FluidRefineryRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, FluidRefineryRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            FluidRefineryProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getFluidRefineryParams());
                            ExtendedHeatCondition.STREAM_CODEC.encode(buf, recipe.getRequiredHeatCondition());

                            buf.writeInt(recipe.getSizedIngredients().size());
                            for (SizedIngredient ingredient : recipe.getSizedIngredients()) {
                                SizedIngredient.STREAM_CODEC.encode(buf, ingredient);
                            }
                        },
                        buf -> {
                            FluidRefineryProcessingRecipeParams params = FluidRefineryProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            ExtendedHeatCondition heatRequirement = ExtendedHeatCondition.STREAM_CODEC.decode(buf);

                            int count = buf.readInt();
                            List<SizedIngredient> sizedIngredients = new ArrayList<>(count);
                            for (int i = 0; i < count; i++) {
                                sizedIngredients.add(SizedIngredient.STREAM_CODEC.decode(buf));
                            }

                            return new FluidRefineryRecipe(params, heatRequirement, sizedIngredients);
                        }
                );

        @Override
        public MapCodec<FluidRefineryRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidRefineryRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static class FluidRefineryProcessingRecipeParams extends ProcessingRecipeParams {

        private static final Codec<Either<SizedFluidIngredient, Ingredient>> INGREDIENT_CODEC =
                Codec.either(CreateCodecs.SIZED_FLUID_INGREDIENT, Ingredient.CODEC);
        private static final Codec<Either<FluidStack, ProcessingOutput>> RESULT_CODEC =
                Codec.either(FluidStack.CODEC, ProcessingOutput.CODEC);

        public static final MapCodec<FluidRefineryProcessingRecipeParams> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        INGREDIENT_CODEC.listOf().fieldOf("ingredients").forGetter(params -> params.ingredients()),
                        RESULT_CODEC.listOf().fieldOf("results").forGetter(params -> params.results()),
                        Codec.INT.optionalFieldOf("processing_time", 0).forGetter(params -> params.processingDuration())
                ).apply(inst, FluidRefineryProcessingRecipeParams::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidRefineryProcessingRecipeParams> STREAM_CODEC =
                StreamCodec.of(
                        (buf, params) -> {
                            CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buf, params.ingredients);
                            CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).encode(buf, params.fluidIngredients);
                            CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buf, params.results);
                            CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).encode(buf, params.fluidResults);
                            ByteBufCodecs.VAR_INT.encode(buf, params.processingDuration);
                        },
                        buf -> {
                            FluidRefineryProcessingRecipeParams params = new FluidRefineryProcessingRecipeParams();
                            params.ingredients = CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
                            params.fluidIngredients = CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).decode(buf);
                            params.results = CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buf);
                            params.fluidResults = CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).decode(buf);
                            params.processingDuration = ByteBufCodecs.VAR_INT.decode(buf);
                            return params;
                        }
                );

        protected FluidRefineryProcessingRecipeParams() {
        }

        private FluidRefineryProcessingRecipeParams(
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
