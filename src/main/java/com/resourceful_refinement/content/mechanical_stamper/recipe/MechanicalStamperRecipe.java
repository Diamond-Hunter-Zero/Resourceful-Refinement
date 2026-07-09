package com.resourceful_refinement.content.mechanical_stamper.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.content.forge_mould.recipe.ChancedIngredient;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

public class MechanicalStamperRecipe implements Recipe<MechanicalStamperRecipeInput> {
    public static final int DEFAULT_PROCESSING_TIME = 100;

    private final Ingredient target;
    private final Optional<ChancedIngredient> stamp;
    private final Optional<SizedIngredient> fillMedium;
    private final Optional<SizedFluidIngredient> fillFluid;
    private final NonNullList<ProcessingOutput> results;
    private final int processingTime;
    private final boolean isolatedStamper;

    public MechanicalStamperRecipe(Ingredient target, Optional<ChancedIngredient> stamp,
                                   Optional<SizedIngredient> fillMedium, Optional<SizedFluidIngredient> fillFluid,
                                   List<ProcessingOutput> results, int processingTime, boolean isolatedStamper) {
        this.target = target;
        this.stamp = stamp;
        this.fillMedium = fillMedium;
        this.fillFluid = fillFluid;
        this.results = NonNullList.create();
        this.results.addAll(results);
        this.processingTime = Math.max(1, processingTime);
        this.isolatedStamper = isolatedStamper;
    }

    @Override
    public boolean matches(MechanicalStamperRecipeInput input, Level level) {
        if (!isolatedStamper && !input.paired()) {
            return false;
        }
        if (!target.test(input.target())) {
            return false;
        }
        if (stamp.isPresent() && !input.stamp().is(stamp.get().item())) {
            return false;
        }
        if (fillMedium.isPresent() && !fillMedium.get().test(input.fillMedium())) {
            return false;
        }
        return fillFluid.isEmpty() || fillFluid.get().test(input.fillFluid());
    }

    @Override
    public ItemStack assemble(MechanicalStamperRecipeInput input, HolderLookup.Provider provider) {
        return results.isEmpty() ? ItemStack.EMPTY : results.getFirst().getStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return results.isEmpty() ? ItemStack.EMPTY : results.getFirst().getStack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(target);
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.MECHANICAL_STAMPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.MECHANICAL_STAMPING_TYPE.get();
    }

    public Ingredient getTarget() {
        return target;
    }

    public Optional<ChancedIngredient> getStamp() {
        return stamp;
    }

    public Optional<SizedIngredient> getFillMedium() {
        return fillMedium;
    }

    public Optional<SizedFluidIngredient> getFillFluid() {
        return fillFluid;
    }

    public NonNullList<ProcessingOutput> getResults() {
        return results;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public boolean isIsolatedStamper() {
        return isolatedStamper;
    }

    public static class Serializer implements RecipeSerializer<MechanicalStamperRecipe> {
        private static final MapCodec<MechanicalStamperRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("target").forGetter(MechanicalStamperRecipe::getTarget),
                        Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(recipe -> List.of()),
                        ChancedIngredient.CODEC.optionalFieldOf("stamp").forGetter(MechanicalStamperRecipe::getStamp),
                        SizedIngredient.FLAT_CODEC.optionalFieldOf("fillMedium").forGetter(MechanicalStamperRecipe::getFillMedium),
                        SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fillFluid").forGetter(MechanicalStamperRecipe::getFillFluid),
                        ProcessingOutput.CODEC.listOf().fieldOf("results").forGetter(MechanicalStamperRecipe::getResults),
                        Codec.INT.optionalFieldOf("processingTime", DEFAULT_PROCESSING_TIME).forGetter(MechanicalStamperRecipe::getProcessingTime),
                        Codec.BOOL.optionalFieldOf("isolatedStamper", false).forGetter(MechanicalStamperRecipe::isIsolatedStamper)
                ).apply(inst, (target, ignoredIngredients, stamp, fillMedium, fillFluid, results, processingTime, isolatedStamper) ->
                        new MechanicalStamperRecipe(target, stamp, fillMedium, fillFluid, results, processingTime, isolatedStamper)));

        private static final StreamCodec<RegistryFriendlyByteBuf, MechanicalStamperRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getTarget());
                    writeOptional(buf, recipe.getStamp(), ChancedIngredient.STREAM_CODEC);
                    writeOptional(buf, recipe.getFillMedium(), SizedIngredient.STREAM_CODEC);
                    writeOptional(buf, recipe.getFillFluid(), SizedFluidIngredient.STREAM_CODEC);
                    CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buf, recipe.getResults());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.getProcessingTime());
                    ByteBufCodecs.BOOL.encode(buf, recipe.isIsolatedStamper());
                },
                buf -> new MechanicalStamperRecipe(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        readOptional(buf, ChancedIngredient.STREAM_CODEC),
                        readOptional(buf, SizedIngredient.STREAM_CODEC),
                        readOptional(buf, SizedFluidIngredient.STREAM_CODEC),
                        CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.BOOL.decode(buf)
                )
        );

        private static <T> void writeOptional(RegistryFriendlyByteBuf buf, Optional<T> value,
                                              StreamCodec<RegistryFriendlyByteBuf, T> codec) {
            buf.writeBoolean(value.isPresent());
            value.ifPresent(t -> codec.encode(buf, t));
        }

        private static <T> Optional<T> readOptional(RegistryFriendlyByteBuf buf,
                                                    StreamCodec<RegistryFriendlyByteBuf, T> codec) {
            return buf.readBoolean() ? Optional.of(codec.decode(buf)) : Optional.empty();
        }

        @Override
        public MapCodec<MechanicalStamperRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MechanicalStamperRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
