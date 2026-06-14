package com.resourceful_refinement.content.milking_station;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MilkingStationSeatRenderer extends EntityRenderer<MilkingStationSeatEntity> {

    public MilkingStationSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(MilkingStationSeatEntity entity, Frustum frustum, double cameraX, double cameraY,
            double cameraZ) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(MilkingStationSeatEntity entity) {
        return null;
    }
}
