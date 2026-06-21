package com.resourceful_refinement.content.brewers_tap.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.brewers_tap.FlavourType;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

public class BrewersTapRecipe extends StandardProcessingRecipe<BrewersTapRecipeInput> {

    private final List<String> flavourTags;

    public BrewersTapRecipe(ProcessingRecipeParams params, List<String> flavourTags) {
        super(ModRecipeTypes.BREWERS_TAP_TYPE_INFO, params);
        this.flavourTags = List.copyOf(flavourTags);
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
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 0;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(BrewersTapRecipeInput input, Level level) {
        if (input.workItem().isEmpty() || input.fluid().isEmpty()) {
            return false;
        }

        if (ingredients.isEmpty() || fluidIngredients.isEmpty()) {
            return false;
        }

        Ingredient itemIngredient = ingredients.get(0);
        if (!itemIngredient.test(input.workItem())) {
            return false;
        }

        SizedFluidIngredient fluidIngredient = fluidIngredients.get(0);
        return fluidIngredient.test(input.fluid());
    }

    public List<String> getFlavourTags() {
        return flavourTags;
    }

    public Optional<FlavourType> getMatchingFlavour(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        for (String flavourTag : flavourTags) {
            Optional<FlavourType> flavour = FlavourType.fromTagName(flavourTag);
            if (flavour.isEmpty()) {
                ResourcefulRefinementMain.LOGGER.warn("Unknown Brewer's Tap flavour tag '{}'", flavourTag);
                continue;
            }

            ResourceLocation tagId = parseFlavourTag(flavourTag);
            if (tagId == null) {
                ResourcefulRefinementMain.LOGGER.warn("Invalid Brewer's Tap flavour tag '{}'", flavourTag);
                continue;
            }

            if (stack.is(TagKey.create(Registries.ITEM, tagId))) {
                return flavour;
            }
        }

        return Optional.empty();
    }

    private static ResourceLocation parseFlavourTag(String flavourTag) {
        if (flavourTag.contains(":")) {
            return ResourceLocation.tryParse(flavourTag);
        }
        return ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, flavourTag);
    }

    public static class Serializer implements RecipeSerializer<BrewersTapRecipe> {
        private static final MapCodec<BrewersTapRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ProcessingRecipeParams.CODEC.forGetter(ProcessingRecipe::getParams),
                        Codec.STRING.listOf().optionalFieldOf("flavour_tags", List.of()).forGetter(BrewersTapRecipe::getFlavourTags)
                ).apply(inst, BrewersTapRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BrewersTapRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            ProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getParams());
                            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, recipe.flavourTags);
                        },
                        buf -> {
                            ProcessingRecipeParams params = ProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            List<String> flavourTags = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
                            return new BrewersTapRecipe(params, flavourTags);
                        }
                );

        @Override
        public MapCodec<BrewersTapRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BrewersTapRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
