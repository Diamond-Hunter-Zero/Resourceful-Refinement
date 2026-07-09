package com.resourceful_refinement.content.cyclotron_forge.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.content.cyclotron_forge.CyclotronControllerBlockEntity;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
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

public class CyclotronForgeRecipe extends StandardProcessingRecipe<CyclotronForgeRecipeInput> {
    private final int coilLength;
    private final int minRpm;
    private final List<Integer> luxCurve;
    private final List<SizedIngredient> sizedIngredients;
    private final List<SizedIngredient> combinedIngredients;

    public CyclotronForgeRecipe(CyclotronProcessingRecipeParams params, int coilLength, int minRpm, List<Integer> luxCurve,
            List<SizedIngredient> sizedIngredients) {
        super(ModRecipeTypes.CYCLOTRON_FORGE_TYPE_INFO, params);
        this.coilLength = Math.clamp(coilLength, CyclotronControllerBlockEntity.MIN_COIL_LENGTH,
                CyclotronControllerBlockEntity.MAX_COIL_LENGTH);
        this.minRpm = Math.max(0, minRpm);
        this.luxCurve = luxCurve == null ? List.of() : luxCurve.stream().map(value -> Math.max(0, value)).toList();

        List<SizedIngredient> combined = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            combined.add(new SizedIngredient(ingredient, 1));
        }
        this.sizedIngredients = sizedIngredients == null ? List.of() : List.copyOf(sizedIngredients);
        if (combined.size() + this.sizedIngredients.size() > getMaxInputCount()) {
            throw new IllegalArgumentException("Cyclotron Forge recipes support at most " + getMaxInputCount()
                    + " item inputs across ingredients and sized_ingredients");
        }
        combined.addAll(this.sizedIngredients);
        this.combinedIngredients = List.copyOf(combined);
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 2;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    public int getCoilLength() {
        return coilLength;
    }

    public int getMinRpm() {
        return minRpm;
    }

    public List<Integer> getLuxCurve() {
        return luxCurve;
    }

    public List<SizedIngredient> getSizedIngredients() {
        return sizedIngredients;
    }

    public List<SizedIngredient> getCombinedIngredients() {
        return combinedIngredients;
    }

    public int[] getLuxCurveArray() {
        int[] values = new int[luxCurve.size()];
        for (int i = 0; i < luxCurve.size(); i++) values[i] = luxCurve.get(i);
        return values;
    }

    public boolean requiresLux() {
        return luxCurve.stream().anyMatch(value -> value > 0);
    }

    private CyclotronProcessingRecipeParams getCyclotronParams() {
        return (CyclotronProcessingRecipeParams) getParams();
    }

    @Override
    public boolean matches(CyclotronForgeRecipeInput input, Level level) {
        return matchesItems(input.items()) && matchesFluids(input.fluids());
    }

    private boolean matchesItems(List<ItemStack> items) {
        List<ItemStack> available = new ArrayList<>();
        for (ItemStack stack : items) available.add(stack.copy());

        for (SizedIngredient ingredient : combinedIngredients) {
            int remaining = ingredient.count();
            boolean found = false;
            for (int i = 0; i < available.size(); i++) {
                ItemStack stack = available.get(i);
                if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
                    int drained = Math.min(remaining, stack.getCount());
                    stack.shrink(drained);
                    remaining -= drained;
                    if (remaining <= 0) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean matchesFluids(List<FluidStack> fluids) {
        List<FluidStack> available = new ArrayList<>();
        for (FluidStack stack : fluids) available.add(stack.copy());

        for (SizedFluidIngredient ingredient : fluidIngredients) {
            boolean found = false;
            for (FluidStack stack : available) {
                if (ingredient.test(stack)) {
                    stack.shrink(ingredient.amount());
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public static class Serializer implements RecipeSerializer<CyclotronForgeRecipe> {
        private static final MapCodec<CyclotronForgeRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        CyclotronProcessingRecipeParams.CODEC.forGetter(CyclotronForgeRecipe::getCyclotronParams),
                        Codec.INT.fieldOf("coil_length").forGetter(CyclotronForgeRecipe::getCoilLength),
                        Codec.INT.optionalFieldOf("min_rpm", 0).forGetter(CyclotronForgeRecipe::getMinRpm),
                        Codec.INT.listOf().optionalFieldOf("lux_curve", List.of()).forGetter(CyclotronForgeRecipe::getLuxCurve),
                        SizedIngredient.FLAT_CODEC.listOf().optionalFieldOf("sized_ingredients", List.of()).forGetter(CyclotronForgeRecipe::getSizedIngredients)
                ).apply(inst, CyclotronForgeRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CyclotronForgeRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            CyclotronProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getCyclotronParams());
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.coilLength);
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.minRpm);
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.luxCurve.size());
                            for (int value : recipe.luxCurve) ByteBufCodecs.VAR_INT.encode(buf, value);
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.sizedIngredients.size());
                            for (SizedIngredient ingredient : recipe.sizedIngredients) {
                                SizedIngredient.STREAM_CODEC.encode(buf, ingredient);
                            }
                        },
                        buf -> {
                            CyclotronProcessingRecipeParams params = CyclotronProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            int coilLength = ByteBufCodecs.VAR_INT.decode(buf);
                            int minRpm = ByteBufCodecs.VAR_INT.decode(buf);
                            int luxCurveSize = ByteBufCodecs.VAR_INT.decode(buf);
                            List<Integer> luxCurve = new ArrayList<>(luxCurveSize);
                            for (int i = 0; i < luxCurveSize; i++) luxCurve.add(ByteBufCodecs.VAR_INT.decode(buf));
                            int sizedIngredientCount = ByteBufCodecs.VAR_INT.decode(buf);
                            List<SizedIngredient> sizedIngredients = new ArrayList<>(sizedIngredientCount);
                            for (int i = 0; i < sizedIngredientCount; i++) {
                                sizedIngredients.add(SizedIngredient.STREAM_CODEC.decode(buf));
                            }
                            return new CyclotronForgeRecipe(params, coilLength, minRpm, luxCurve, sizedIngredients);
                        }
                );

        @Override
        public MapCodec<CyclotronForgeRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CyclotronForgeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static class CyclotronProcessingRecipeParams extends ProcessingRecipeParams {
        private static final Codec<Either<SizedFluidIngredient, Ingredient>> INGREDIENT_CODEC =
                Codec.either(CreateCodecs.SIZED_FLUID_INGREDIENT, Ingredient.CODEC);
        private static final Codec<Either<FluidStack, ProcessingOutput>> RESULT_CODEC =
                Codec.either(FluidStack.CODEC, ProcessingOutput.CODEC);

        public static final MapCodec<CyclotronProcessingRecipeParams> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        INGREDIENT_CODEC.listOf().fieldOf("ingredients").forGetter(params -> params.ingredients()),
                        RESULT_CODEC.listOf().fieldOf("results").forGetter(params -> params.results()),
                        Codec.INT.optionalFieldOf("processing_time", 0).forGetter(params -> params.processingDuration())
                ).apply(inst, CyclotronProcessingRecipeParams::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CyclotronProcessingRecipeParams> STREAM_CODEC =
                StreamCodec.of(
                        (buf, params) -> {
                            CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buf, params.ingredients);
                            CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).encode(buf, params.fluidIngredients);
                            CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buf, params.results);
                            CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).encode(buf, params.fluidResults);
                            ByteBufCodecs.VAR_INT.encode(buf, params.processingDuration);
                        },
                        buf -> {
                            CyclotronProcessingRecipeParams params = new CyclotronProcessingRecipeParams();
                            params.ingredients = CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
                            params.fluidIngredients = CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).decode(buf);
                            params.results = CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buf);
                            params.fluidResults = CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).decode(buf);
                            params.processingDuration = ByteBufCodecs.VAR_INT.decode(buf);
                            return params;
                        }
                );

        protected CyclotronProcessingRecipeParams() {
        }

        private CyclotronProcessingRecipeParams(
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
