package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refinery.recipe.FluidRefineryRecipe;
import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipe;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipe;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ResourcefulRefinementMain.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidRefineryRecipe>> FLUID_REFINERY_TYPE =
            RECIPE_TYPES.register("fluid_refinery", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering fluid_refinery RecipeType");
                return RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fluid_refinery"));
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidRefineryRecipe>> FLUID_REFINERY_SERIALIZER =
            RECIPE_SERIALIZERS.register("fluid_refinery", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering fluid_refinery RecipeSerializer");
                return new StandardProcessingRecipe.Serializer<>(FluidRefineryRecipe::new);
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<MechanicalSieveRecipe>> MECHANICAL_SIEVE_TYPE =
            RECIPE_TYPES.register("mechanical_fluid_sieve", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering mechanical_fluid_sieve RecipeType");
                return RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_fluid_sieve"));
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MechanicalSieveRecipe>> MECHANICAL_SIEVE_SERIALIZER =
            RECIPE_SERIALIZERS.register("mechanical_fluid_sieve", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering mechanical_fluid_sieve RecipeSerializer");
                return new StandardProcessingRecipe.Serializer<>(MechanicalSieveRecipe::new);
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<MechanicalForgeMouldRecipe>> MECHANICAL_FORGE_MOULD_TYPE =
            RECIPE_TYPES.register("mechanical_forge_mould", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering mechanical_forge_mould RecipeType");
                return RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_forge_mould"));
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MechanicalForgeMouldRecipe>> MECHANICAL_FORGE_MOULD_SERIALIZER =
            RECIPE_SERIALIZERS.register("mechanical_forge_mould", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering mechanical_forge_mould RecipeSerializer");
                return new MechanicalForgeMouldRecipe.Serializer();
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe>> COATING_TYPE =
            RECIPE_TYPES.register("coating", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering coating RecipeType");
                return RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "coating"));
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe>> COATING_SERIALIZER =
            RECIPE_SERIALIZERS.register("coating", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering coating RecipeSerializer");
                return new com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe.Serializer();
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<FrackingPumpRecipe>> FRACKING_PUMP_TYPE =
            RECIPE_TYPES.register("fracking_pump", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering fracking_pump RecipeType");
                return RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fracking_pump"));
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FrackingPumpRecipe>> FRACKING_PUMP_SERIALIZER =
            RECIPE_SERIALIZERS.register("fracking_pump", () -> {
                ResourcefulRefinementMain.LOGGER.info("[ModRecipeTypes] Registering fracking_pump RecipeSerializer");
                return new FrackingPumpRecipe.Serializer();
            });

    public record RefineryRecipeTypeInfo(ResourceLocation id,
                                         Supplier<RecipeSerializer<?>> serializer,
                                         Supplier<RecipeType<?>> type) implements IRecipeTypeInfo {
        @Override
        public ResourceLocation getId() { return id; }
        @Override
        public <T extends RecipeSerializer<?>> T getSerializer() { return (T) serializer.get(); }

        @Override
        public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
            return (RecipeType<R>) type.get();
        }
    }

    public static final IRecipeTypeInfo FLUID_REFINERY_TYPE_INFO = new RefineryRecipeTypeInfo(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fluid_refinery"),
            FLUID_REFINERY_SERIALIZER::get,
            FLUID_REFINERY_TYPE::get
    );

    public static final IRecipeTypeInfo MECHANICAL_SIEVE_TYPE_INFO = new RefineryRecipeTypeInfo(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_fluid_sieve"),
            MECHANICAL_SIEVE_SERIALIZER::get,
            MECHANICAL_SIEVE_TYPE::get
    );

    public static final IRecipeTypeInfo MECHANICAL_FORGE_MOULD_TYPE_INFO = new RefineryRecipeTypeInfo(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_forge_mould"),
            MECHANICAL_FORGE_MOULD_SERIALIZER::get,
            MECHANICAL_FORGE_MOULD_TYPE::get
    );

    public static final IRecipeTypeInfo COATING_TYPE_INFO = new RefineryRecipeTypeInfo(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "coating"),
            COATING_SERIALIZER::get,
            COATING_TYPE::get
    );

    public static final IRecipeTypeInfo FRACKING_PUMP_TYPE_INFO = new RefineryRecipeTypeInfo(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fracking_pump"),
            FRACKING_PUMP_SERIALIZER::get,
            FRACKING_PUMP_TYPE::get
    );
}
