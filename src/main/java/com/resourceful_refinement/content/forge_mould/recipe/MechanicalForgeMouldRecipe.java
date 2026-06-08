package com.resourceful_refinement.content.forge_mould.recipe;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MechanicalForgeMouldRecipe extends StandardProcessingRecipe<MechanicalForgeMouldRecipeInput> {

    /** Whether this recipe requires a CastingDepot at the target workspace. */
    private final boolean casting;
    /** Holds the parsed items paired with their individual consumption chances. */
    private final List<ChancedIngredient> chancedIngredients;

    public MechanicalForgeMouldRecipe(ProcessingRecipeParams params, boolean casting, List<ChancedIngredient> chancedIngredients) {
        super(ModRecipeTypes.MECHANICAL_FORGE_MOULD_TYPE_INFO, params);
        this.casting = casting;
        this.chancedIngredients = chancedIngredients;
    }

    @Override
    protected int getMaxInputCount() { return 64; }

    @Override
    protected int getMaxOutputCount() { return 1; }

    @Override
    protected int getMaxFluidInputCount() { return 1; }

    @Override
    protected int getMaxFluidOutputCount() { return 0; }

    @Override
    protected boolean canSpecifyDuration() { return true; }

    @Override
    public boolean matches(MechanicalForgeMouldRecipeInput input, Level level) {
        if (input.fluid().isEmpty()) return false;

        // Match item ingredients (Optional if list is empty)
        if (!ingredients.isEmpty()) {
            if (input.item().isEmpty()) return false;
            
            // With only 1 input slot, all ingredients must match the same item
            for (net.minecraft.world.item.crafting.Ingredient ingredient : ingredients) {
                if (!ingredient.test(input.item())) return false;
            }
            
            // Check if stack size is sufficient for the number of ingredients
            if (input.item().getCount() < ingredients.size()) return false;
        }

        // Match fluid ingredient
        if (fluidIngredients.isEmpty()) return false;
        SizedFluidIngredient ingredient = fluidIngredients.get(0);
        if (!ingredient.test(input.fluid())) return false;

        // If this is a casting recipe, a CastingDepot must be below
        if (this.casting && !input.hasCastingDepot()) return false;

        return true;
    }

    public boolean isCasting() {
        return casting;
    }

    public float getConsumptionChance(Item item) {
        for (ChancedIngredient chanced : this.chancedIngredients) {
            if (chanced.item() == item) {
                return chanced.consumptionChance();
            }
        }
        return 1.0f;
    }

    public boolean matchesFilter(FilteringBehaviour filter) {
        if (filter == null || filter.getFilter().isEmpty()) return true;
        for (ProcessingOutput output : results) {
            if (filter.test(output.getStack())) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Custom Serializer — wraps ProcessingRecipeParams codec + extra "casting" field
    // -------------------------------------------------------------------------
    public static class Serializer implements RecipeSerializer<MechanicalForgeMouldRecipe> {

        // Intercepts the "ingredients" JSON array to pull out ChancedIngredient structures
        private static final MapCodec<List<ChancedIngredient>> CHANCED_INGREDIENTS_MAP_CODEC = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString("ingredients"));
            }

            @Override
            public <T> DataResult<List<ChancedIngredient>> decode(DynamicOps<T> ops, MapLike<T> input) {
                T ingredientsNode = input.get("ingredients");
                if (ingredientsNode == null) {
                    return DataResult.success(List.of());
                }
                return ops.getStream(ingredientsNode).map(stream -> {
                    List<ChancedIngredient> list = new ArrayList<>();
                    stream.forEach(element -> {
                        // Attempt parsing via ChancedIngredient. Items succeed; Fluids safely fail and are skipped
                        ChancedIngredient.CODEC.parse(ops, element).result().ifPresent(list::add);
                    });
                    return list;
                });
            }

            @Override
            public <T> RecordBuilder<T> encode(List<ChancedIngredient> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                // Return prefix untouched so we don't duplicate/overwrite Create's own ingredient array writer
                return prefix;
            }
        };

        private static final MapCodec<MechanicalForgeMouldRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ProcessingRecipeParams.CODEC.forGetter(r -> r.getParams()),
                        Codec.BOOL.optionalFieldOf("casting", false).forGetter(r -> r.casting),
                        CHANCED_INGREDIENTS_MAP_CODEC.forGetter(r -> r.chancedIngredients)
                ).apply(inst, MechanicalForgeMouldRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MechanicalForgeMouldRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            ProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getParams());
                            buf.writeBoolean(recipe.casting);
                            ChancedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.chancedIngredients);
                        },
                        buf -> {
                            ProcessingRecipeParams params = ProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            boolean casting = buf.readBoolean();
                            List<ChancedIngredient> chancedIngredients = ChancedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                            return new MechanicalForgeMouldRecipe(params, casting, chancedIngredients);
                        }
                );

        @Override
        public MapCodec<MechanicalForgeMouldRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MechanicalForgeMouldRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
