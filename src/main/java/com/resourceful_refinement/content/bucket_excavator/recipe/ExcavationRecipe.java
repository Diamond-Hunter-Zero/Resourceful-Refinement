package com.resourceful_refinement.content.bucket_excavator.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.content.distillery.recipe.DistilleryRecipeInput;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipe;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.codec.CreateCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExcavationRecipe extends StandardProcessingRecipe<ExcavationRecipeInput> {

    private final ResourceLocation minedBlockId;
    private Block cachedMinedBlock;

    private final Optional<ResourceLocation> mineralTypeId;
    private Block cachedMineralBlock;

    public ExcavationRecipe(ProcessingRecipeParams params, ResourceLocation minedBlockId, Optional<ResourceLocation> mineralTypeId) {
        super(ModRecipeTypes.EXCAVATION_TYPE_INFO, params);
        this.minedBlockId = minedBlockId;
        this.mineralTypeId = mineralTypeId;
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
        return 0;
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
    protected boolean canRequireHeat() {
        return false;
    }


    // -------------------------------------------------------------------------
    // Parameter Utilities
    // -------------------------------------------------------------------------
    public Block getMinedBlock() {
        if (cachedMinedBlock == null) {
            cachedMinedBlock = BuiltInRegistries.BLOCK.get(minedBlockId);
            if (cachedMinedBlock == null)
            {
                cachedMinedBlock = Blocks.AIR;
                ResourcefulRefinementMain.LOGGER.warn("[ExcavationRecipe - getMinedBlock] Failed to find register block ID for " + minedBlockId);
            }
        }
        return cachedMinedBlock;
    }

    public Block getMineralBlock() {
        if (cachedMineralBlock == null) {
            cachedMineralBlock = mineralTypeId
                    .map(BuiltInRegistries.BLOCK::get)
                    .orElse(Blocks.AIR);
            if (cachedMineralBlock == null)
            {
                cachedMineralBlock = Blocks.AIR;
                ResourcefulRefinementMain.LOGGER.warn("[ExcavationRecipe - getMineralBlock] Failed to find register block ID for " + mineralTypeId);
            }
        }
        return cachedMineralBlock;
    }

    public boolean requiresMineralType() {
        return mineralTypeId.isPresent();
    }


    // -------------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------------
    @Override
    public boolean matches(ExcavationRecipeInput input, Level level) {

        // Match source block
        if (input.sourceBlock() != getMinedBlock()) return false;

        // If a mineral type is specified, the recipe must have passed that block
        if (requiresMineralType()) {
            Block required = getMineralBlock();
            Block actual   = input.mineralType();
            if (actual == null || actual == Blocks.AIR || actual != required) return false;
        }

        return true;
    }


    // -------------------------------------------------------------------------
    // Custom Serializer
    // -------------------------------------------------------------------------
    public static class Serializer implements RecipeSerializer<ExcavationRecipe> {

        private static final MapCodec<ExcavationRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ProcessingRecipeParams.CODEC.forGetter(r -> r.getParams()),
                        ResourceLocation.CODEC.optionalFieldOf("mined_block", ResourceLocation.parse("resourceful_refinement:mineral_deposit"))
                                .forGetter(r -> r.minedBlockId),
                        ResourceLocation.CODEC.optionalFieldOf("mineral_deposit_type")
                                .forGetter(r -> r.mineralTypeId)
                ).apply(inst, ExcavationRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ExcavationRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            ProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getParams());
                            buf.writeResourceLocation(recipe.minedBlockId);
                            buf.writeBoolean(recipe.mineralTypeId.isPresent());
                            recipe.mineralTypeId.ifPresent(buf::writeResourceLocation);
                        },
                        buf -> {
                            ProcessingRecipeParams params = ProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            ResourceLocation minedBlockId = buf.readResourceLocation();
                            Optional<ResourceLocation> mineralTypeId = buf.readBoolean()
                                    ? Optional.of(buf.readResourceLocation())
                                    : Optional.empty();
                            return new ExcavationRecipe(params, minedBlockId, mineralTypeId);
                        }
                );

        @Override
        public MapCodec<ExcavationRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ExcavationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
