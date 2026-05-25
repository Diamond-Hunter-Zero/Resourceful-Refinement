package com.resourceful_refinement.content.fracking_pump.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.io.Console;
import java.util.Optional;

public class FrackingPumpRecipe extends StandardProcessingRecipe<FrackingPumpRecipeInput> {

    private final ResourceLocation sourceBlockId;
    private Block cachedSourceBlock;

    /** Only non-empty when the source block is a geyser. Restricts matching to geysers storing this fluid. */
    private final Optional<ResourceLocation> sourceFluidId;
    private Fluid cachedSourceFluid;

    public FrackingPumpRecipe(ProcessingRecipeParams params, ResourceLocation sourceBlockId, Optional<ResourceLocation> sourceFluidId) {
        super(ModRecipeTypes.FRACKING_PUMP_TYPE_INFO, params);
        this.sourceBlockId = sourceBlockId;
        this.sourceFluidId = sourceFluidId;
    }

    public Block getSourceBlock() {
        if (cachedSourceBlock == null) {
            cachedSourceBlock = BuiltInRegistries.BLOCK.get(sourceBlockId);
            if (cachedSourceBlock == null)
            {
                cachedSourceBlock = Blocks.AIR;
                ResourcefulRefinementMain.LOGGER.warn("[FrackingPumpRecipe - getSourceBlock] Failed to find register block ID for " + sourceBlockId);
            }
        }
        return cachedSourceBlock;
    }

    /**
     * Returns the fluid that a geyser source block must contain for this recipe to match,
     * or {@link Fluids#EMPTY} if no fluid restriction is set.
     */
    public Fluid getSourceFluid() {
        if (cachedSourceFluid == null) {
            cachedSourceFluid = sourceFluidId
                .map(id -> BuiltInRegistries.FLUID.get(id))
                .orElse(Fluids.EMPTY);
            if (cachedSourceFluid == null) cachedSourceFluid = Fluids.EMPTY;
        }
        return cachedSourceFluid;
    }

    /** True when this recipe requires a specific fluid inside a geyser source block. */
    public boolean requiresGeyserFluid() {
        return sourceFluidId.isPresent();
    }

    @Override
    protected int getMaxInputCount() { return 0; }

    @Override
    protected int getMaxOutputCount() { return 1; }

    @Override
    protected int getMaxFluidInputCount() { return 1; }

    @Override
    protected int getMaxFluidOutputCount() { return 1; }

    @Override
    protected boolean canSpecifyDuration() { return true; }

    @Override
    public boolean matches(FrackingPumpRecipeInput input, Level level) {
        if (input.fluid().isEmpty()) return false;

        // Match source block
        if (input.sourceBlock() != getSourceBlock()) return false;

        // If a source fluid is specified, the geyser must contain that fluid
        if (requiresGeyserFluid()) {
            Fluid required = getSourceFluid();
            Fluid actual   = input.geyserFluid();
            if (actual == null || actual == Fluids.EMPTY || actual != required) return false;
        }

        // Match fluid ingredient
        if (fluidIngredients.isEmpty()) return false;
        SizedFluidIngredient ingredient = fluidIngredients.get(0);
        if (!ingredient.test(input.fluid())) return false;

        return true;
    }

    // -------------------------------------------------------------------------
    // Custom Serializer
    // -------------------------------------------------------------------------
    public static class Serializer implements RecipeSerializer<FrackingPumpRecipe> {

        private static final MapCodec<FrackingPumpRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                ProcessingRecipeParams.CODEC.forGetter(r -> r.getParams()),
                ResourceLocation.CODEC.optionalFieldOf("source_block", ResourceLocation.parse("resourceful_refinement:geyser_block"))
                    .forGetter(r -> r.sourceBlockId),
                ResourceLocation.CODEC.optionalFieldOf("source_fluid")
                    .forGetter(r -> r.sourceFluidId)
            ).apply(inst, FrackingPumpRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, FrackingPumpRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> {
                    ProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getParams());
                    buf.writeResourceLocation(recipe.sourceBlockId);
                    buf.writeBoolean(recipe.sourceFluidId.isPresent());
                    recipe.sourceFluidId.ifPresent(buf::writeResourceLocation);
                },
                buf -> {
                    ProcessingRecipeParams params = ProcessingRecipeParams.STREAM_CODEC.decode(buf);
                    ResourceLocation sourceBlockId = buf.readResourceLocation();
                    Optional<ResourceLocation> sourceFluidId = buf.readBoolean()
                        ? Optional.of(buf.readResourceLocation())
                        : Optional.empty();
                    return new FrackingPumpRecipe(params, sourceBlockId, sourceFluidId);
                }
            );

        @Override
        public MapCodec<FrackingPumpRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FrackingPumpRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
