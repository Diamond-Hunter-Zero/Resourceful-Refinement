package com.resourceful_refinement.content.tesla_coil.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.content.tesla_coil.TeslaCoilBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.utilities.recipe.IRecipeSpecificity;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Tesla Coil charging: an item held on a belt or depot between a pair of Tesla Coils (facing across the one-block gap
 * above it, at least one powered) is struck {@link #getCharges()} times, then replaced by the result.
 * <pre>{@code
 * {
 *   "type": "resourceful_refinement:tesla_coil",
 *   "ingredient": { "item": "minecraft:copper_ingot", "count": 1 },
 *   "charges": 3,
 *   "result": { "id": "minecraft:lightning_rod", "count": 1, "chance": 1.0 }
 * }
 * }</pre>
 * Built on Create's {@link StandardProcessingRecipe} and {@link IAssemblyRecipe}, so the same JSON works as a step in a
 * {@code create:sequenced_assembly} recipe (use the transitional item as the ingredient and result).
 */
public class TeslaCoilRecipe extends StandardProcessingRecipe<SingleRecipeInput> implements IAssemblyRecipe, IRecipeSpecificity {

    public static final int MAX_CHARGES = 16;

    private final SizedIngredient ingredient;
    private final int charges;

    public TeslaCoilRecipe(SizedIngredient ingredient, int charges, ProcessingOutput result) {
        this(Params.of(ingredient, Math.clamp(charges, 1, MAX_CHARGES), result), ingredient, charges);
    }

    private TeslaCoilRecipe(Params params, SizedIngredient ingredient, int charges) {
        super(ModRecipeTypes.TESLA_COIL_TYPE_INFO, params);
        this.ingredient = ingredient;
        this.charges = Math.clamp(charges, 1, MAX_CHARGES);
    }

    public SizedIngredient getIngredient() {
        return ingredient;
    }

    public int getCharges() {
        return charges;
    }

    public ProcessingOutput getResult() {
        return getRollableResults().isEmpty() ? ProcessingOutput.EMPTY : getRollableResults().getFirst();
    }

    /**
     * Total ticks a held item spends charging at {@link TeslaCoilBlockEntity#CHARGE_REFERENCE_RPM}: every charge
     * strike, with a pause between consecutive strikes. Faster coil pairs finish proportionally sooner.
     */
    @Override
    public int getProcessingDuration() {
        return TeslaCoilBlockEntity.chargingDuration(charges);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    /** A larger required stack is the more specific recipe when two recipes accept the same item. */
    @Override
    public int getSpecificity() {
        return ingredient.count();
    }

    // -------------------------------------------------------------------------
    // Sequenced assembly
    // -------------------------------------------------------------------------
    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getDescriptionForAssembly() {
        return Component.translatable("recipe.resourceful_refinement.assembly.tesla_coil", charges);
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(ModBlocks.TESLA_COIL.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> TeslaCoilAssemblySubCategory::new;
    }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------
    /** Create's params, built from our single ingredient/result format. */
    public static class Params extends ProcessingRecipeParams {
        static Params of(SizedIngredient ingredient, int charges, ProcessingOutput result) {
            Params params = new Params();
            params.ingredients.add(ingredient.ingredient());
            params.results.add(result);
            params.processingDuration = TeslaCoilBlockEntity.chargingDuration(charges);
            return params;
        }
    }

    public static class Serializer implements RecipeSerializer<TeslaCoilRecipe> {
        private static final MapCodec<TeslaCoilRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        SizedIngredient.FLAT_CODEC.fieldOf("ingredient").forGetter(TeslaCoilRecipe::getIngredient),
                        ExtraCodecs.intRange(1, MAX_CHARGES).optionalFieldOf("charges", 1).forGetter(TeslaCoilRecipe::getCharges),
                        ProcessingOutput.CODEC.fieldOf("result").forGetter(TeslaCoilRecipe::getResult)
                ).apply(inst, TeslaCoilRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, TeslaCoilRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC, TeslaCoilRecipe::getIngredient,
                ByteBufCodecs.VAR_INT, TeslaCoilRecipe::getCharges,
                ProcessingOutput.STREAM_CODEC, TeslaCoilRecipe::getResult,
                TeslaCoilRecipe::new);

        @Override
        public MapCodec<TeslaCoilRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TeslaCoilRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
