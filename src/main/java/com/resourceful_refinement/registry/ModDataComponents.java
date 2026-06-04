package com.resourceful_refinement.registry;

import com.mojang.serialization.Codec;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.coating.CoatingData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CoatingData>> COATING_DATA = DATA_COMPONENTS.register("coating_data",
            () -> DataComponentType.<CoatingData>builder().persistent(CoatingData.CODEC).networkSynchronized(CoatingData.STREAM_CODEC).build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.neoforged.neoforge.fluids.SimpleFluidContent>> HOSEGUN_FLUID = DATA_COMPONENTS.register("hosegun_fluid",
            () -> DataComponentType.<net.neoforged.neoforge.fluids.SimpleFluidContent>builder().persistent(net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC).networkSynchronized(net.neoforged.neoforge.fluids.SimpleFluidContent.STREAM_CODEC).build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> HOSEGUN_TRACKING_ID = DATA_COMPONENTS.register("hosegun_tracking_id",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HOSEGUN_GLOOPY = DATA_COMPONENTS.register("hosegun_gloopy",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );
}
