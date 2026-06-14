package com.resourceful_refinement.content.milking_station.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MilkingStationRecipe extends StandardProcessingRecipe<MilkingStationRecipeInput> {

    private final ResourceLocation entityType;

    public MilkingStationRecipe(ProcessingRecipeParams params, ResourceLocation entityType) {
        super(ModRecipeTypes.MILKING_STATION_TYPE_INFO, params);
        this.entityType = entityType;
    }

    public ResourceLocation getEntityType() {
        return entityType;
    }

    @Override
    protected int getMaxInputCount() {
        return 0;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(MilkingStationRecipeInput input, Level level) {
        return input.entityType() != null && input.entityType().equals(entityType);
    }

    public static class Serializer implements RecipeSerializer<MilkingStationRecipe> {
        private static final MapCodec<MilkingStationRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ProcessingRecipeParams.CODEC.forGetter(ProcessingRecipe::getParams),
                        ResourceLocation.CODEC.fieldOf("entity").forGetter(MilkingStationRecipe::getEntityType)
                ).apply(inst, MilkingStationRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MilkingStationRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            ProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getParams());
                            buf.writeResourceLocation(recipe.entityType);
                        },
                        buf -> {
                            ProcessingRecipeParams params = ProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            ResourceLocation entityType = buf.readResourceLocation();
                            return new MilkingStationRecipe(params, entityType);
                        }
                );

        @Override
        public MapCodec<MilkingStationRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MilkingStationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
