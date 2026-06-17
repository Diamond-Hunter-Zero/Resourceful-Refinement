package com.resourceful_refinement.content.combustion_chamber;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.foundation.model.BakedModelHelper;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombustionChamberFanModel extends BakedModelWrapperWithData {
    private static final ModelProperty<Boolean> CHAMBER_OUTPUT_PROPERTY = new ModelProperty<>();
    private static final ResourceLocation CHAMBER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/combustion_chamber");

    private BakedModel chamberCasingModel;

    public CombustionChamberFanModel(BakedModel originalModel) {
        super(originalModel);
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
        return getModel(data).getRenderTypes(state, rand, data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    @NotNull RandomSource rand, @NotNull ModelData data,
                                    @Nullable RenderType renderType) {
        return getModel(data).getQuads(state, side, rand, data, renderType);
    }

    private BakedModel getModel(ModelData data) {
        if (!Boolean.TRUE.equals(data.get(CHAMBER_OUTPUT_PROPERTY))) {
            return originalModel;
        }
        if (chamberCasingModel == null) {
            chamberCasingModel = BakedModelHelper.generateModel(originalModel, this::swapCasingSprite);
        }
        return chamberCasingModel;
    }

    private TextureAtlasSprite swapCasingSprite(TextureAtlasSprite sprite) {
        ResourceLocation spriteName = sprite.contents().name();
        if (!"create".equals(spriteName.getNamespace()) || !spriteName.getPath().startsWith("block/fan_")) {
            return null;
        }
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(CHAMBER_TEXTURE);
    }
}
