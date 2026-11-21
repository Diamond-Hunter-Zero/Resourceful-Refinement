package com.resourceful_refinement.client.ct;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Wraps the baked block models of connected-texture blocks in Create's {@link CTModel}, the same thing
 * Create's registrate {@code connectedTextures(...)} helper does for casings.
 */
@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ConnectedTextureModels {

    private ConnectedTextureModels() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        wrap(models, ModBlocks.HEAVY_PLATE_SHIELDING.get(), () -> new SimpleCTBehaviour(ModSpriteShifts.PLATE_SHIELDING));
    }

    private static void wrap(Map<ModelResourceLocation, BakedModel> models, Block block,
                             Supplier<ConnectedTextureBehaviour> behaviour) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
            BakedModel original = models.get(location);
            if (original != null && !(original instanceof CTModel)) {
                models.put(location, new CTModel(original, behaviour.get()));
            }
        }
    }
}
