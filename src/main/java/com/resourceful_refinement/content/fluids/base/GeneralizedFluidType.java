package com.resourceful_refinement.content.fluids.base;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

public class GeneralizedFluidType extends FluidType {
    private final int tintColor;
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;

    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/fluids/fluid_overlay");

    public GeneralizedFluidType(Properties properties, int tintColor) {

        this(properties, tintColor,
             ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/fluids/template_fluid_still"),
             ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/fluids/template_fluid_flow"));
    }

    public GeneralizedFluidType(Properties properties, int tintColor, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties);
        this.tintColor = tintColor;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    @Override
    @SuppressWarnings("removal")
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowingTexture;
            }

            @Override
            public ResourceLocation getOverlayTexture() { return OVERLAY; }

            @Override
            public int getTintColor() {
                return tintColor;
            }

            @Override
            public org.joml.Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick, net.minecraft.client.multiplayer.ClientLevel level, int renderDistance, float darkenWorldAmount, org.joml.Vector3f fluidFogColor) {
                int color = this.getTintColor();
                float red = (color >> 16 & 255) / 255.0F;
                float green = (color >> 8 & 255) / 255.0F;
                float blue = (color & 255) / 255.0F;
                return new org.joml.Vector3f(red, green, blue);
            }


        });
    }
}
