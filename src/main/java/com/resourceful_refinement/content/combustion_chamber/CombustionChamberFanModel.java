package com.resourceful_refinement.content.combustion_chamber;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CombustionChamberFanModel extends BakedModelWrapperWithData {
    private static final ModelProperty<Boolean> CHAMBER_OUTPUT_PROPERTY = new ModelProperty<>();
    private static final Map<Direction, BakedModel> CUSTOM_MODELS = new EnumMap<>(Direction.class);

    public CombustionChamberFanModel(BakedModel originalModel) {
        super(originalModel);
    }

    public static void loadCustomModels(Map<ModelResourceLocation, BakedModel> models) {
        CUSTOM_MODELS.clear();
        for (Direction direction : Direction.values()) {
            BakedModel model;
            switch (direction) {
                case NORTH ->
                        model = models.get(new ModelResourceLocation(ModPartialModels.COMBUSTION_FAN_NORTH.modelLocation(), "standalone"));
                case EAST ->
                        model = models.get(new ModelResourceLocation(ModPartialModels.COMBUSTION_FAN_EAST.modelLocation(), "standalone"));
                case SOUTH ->
                        model = models.get(new ModelResourceLocation(ModPartialModels.COMBUSTION_FAN_SOUTH.modelLocation(), "standalone"));
                case WEST ->
                        model = models.get(new ModelResourceLocation(ModPartialModels.COMBUSTION_FAN_WEST.modelLocation(), "standalone"));

                default -> model = null;
            }

            if (model != null)
                CUSTOM_MODELS.put(direction, model);
             //else
                //ResourcefulRefinementMain.LOGGER.warn("Failed to load custom combustion chamber fan model for facing: " + direction.getName());
        }
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world,
                                                BlockPos pos, BlockState state, ModelData blockEntityData) {
        return builder.with(CHAMBER_OUTPUT_PROPERTY,
                CombustionChamberFanIntegration.isFanDrivenByOutputChamber(world, pos, state));
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                             @NotNull ModelData data) {
        return getModel(state, data).getRenderTypes(state, rand, data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    @NotNull RandomSource rand, @NotNull ModelData data,
                                    @Nullable RenderType renderType) {
        return getModel(state, data).getQuads(state, side, rand, data, renderType);
    }

    private BakedModel getModel(@Nullable BlockState state, ModelData data) {
        if (!Boolean.TRUE.equals(data.get(CHAMBER_OUTPUT_PROPERTY))) {
            return originalModel;
        }
        if (state != null && state.hasProperty(EncasedFanBlock.FACING)) {
            Direction facing = state.getValue(EncasedFanBlock.FACING);
            BakedModel customModel = CUSTOM_MODELS.get(facing);
            if (customModel != null) {
                return customModel;
            }
        }
        return originalModel;
    }
}
