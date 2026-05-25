package com.resourceful_refinement.content.forge_mould.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class MechanicalForgeMouldRecipe extends StandardProcessingRecipe<MechanicalForgeMouldRecipeInput> {

    /** Whether this recipe requires a CastingDepot at the target workspace. */
    private final boolean casting;

    public MechanicalForgeMouldRecipe(ProcessingRecipeParams params, boolean casting) {
        super(ModRecipeTypes.MECHANICAL_FORGE_MOULD_TYPE_INFO, params);
        this.casting = casting;
    }

    /** Convenience constructor for recipes without the casting flag. */
    public MechanicalForgeMouldRecipe(ProcessingRecipeParams params) {
        this(params, false);
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

        private static final MapCodec<MechanicalForgeMouldRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                ProcessingRecipeParams.CODEC.forGetter(r -> r.getParams()),
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("casting", false)
                    .forGetter(r -> r.casting)
            ).apply(inst, MechanicalForgeMouldRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MechanicalForgeMouldRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> {
                    ProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getParams());
                    buf.writeBoolean(recipe.casting);
                },
                buf -> {
                    ProcessingRecipeParams params = ProcessingRecipeParams.STREAM_CODEC.decode(buf);
                    boolean casting = buf.readBoolean();
                    return new MechanicalForgeMouldRecipe(params, casting);
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
