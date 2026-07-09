package com.resourceful_refinement.content.drill_pylon.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public class DrillPylonRecipe extends StandardProcessingRecipe<DrillPylonRecipeInput> {
    private final ResourceLocation sourceItemId;
    private final List<Integer> luxCurve;
    private Item cachedSourceItem;

    public DrillPylonRecipe(ProcessingRecipeParams params, ResourceLocation sourceItemId, List<Integer> luxCurve) {
        super(ModRecipeTypes.DRILL_PYLON_TYPE_INFO, params);
        this.sourceItemId = sourceItemId;
        this.luxCurve = luxCurve.stream().map(value -> Math.max(0, value)).toList();
    }

    public ResourceLocation getSourceItemId() {
        return sourceItemId;
    }

    public Item getSourceItem() {
        if (cachedSourceItem == null) {
            cachedSourceItem = BuiltInRegistries.ITEM.get(sourceItemId);
            if (cachedSourceItem == null) {
                cachedSourceItem = Items.AIR;
                ResourcefulRefinementMain.LOGGER.warn("[DrillPylonRecipe] Unknown source item {}", sourceItemId);
            }
        }
        return cachedSourceItem;
    }

    public int[] getLuxCurveArray() {
        int[] values = new int[luxCurve.size()];
        for (int i = 0; i < luxCurve.size(); i++) values[i] = luxCurve.get(i);
        return values;
    }

    public boolean requiresLux() {
        return luxCurve.stream().anyMatch(value -> value > 0);
    }

    @Override
    protected int getMaxInputCount() {
        return 0;
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
    public boolean matches(DrillPylonRecipeInput input, Level level) {
        return input.sourceItem() != null && input.sourceItem() == getSourceItem();
    }

    public static class Serializer implements RecipeSerializer<DrillPylonRecipe> {
        private static final MapCodec<DrillPylonRecipe> MAP_CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        ProcessingRecipeParams.CODEC.forGetter(ProcessingRecipe::getParams),
                        ResourceLocation.CODEC.fieldOf("source_item").forGetter(DrillPylonRecipe::getSourceItemId),
                        Codec.INT.listOf().optionalFieldOf("lux_curve", List.of()).forGetter(recipe -> recipe.luxCurve)
                ).apply(inst, DrillPylonRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DrillPylonRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            ProcessingRecipeParams.STREAM_CODEC.encode(buf, recipe.getParams());
                            buf.writeResourceLocation(recipe.sourceItemId);
                            buf.writeVarInt(recipe.luxCurve.size());
                            for (int value : recipe.luxCurve) buf.writeVarInt(value);
                        },
                        buf -> {
                            ProcessingRecipeParams params = ProcessingRecipeParams.STREAM_CODEC.decode(buf);
                            ResourceLocation sourceItem = buf.readResourceLocation();
                            int size = buf.readVarInt();
                            java.util.ArrayList<Integer> curve = new java.util.ArrayList<>(size);
                            for (int i = 0; i < size; i++) curve.add(buf.readVarInt());
                            return new DrillPylonRecipe(params, sourceItem, curve);
                        }
                );

        @Override
        public MapCodec<DrillPylonRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DrillPylonRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
