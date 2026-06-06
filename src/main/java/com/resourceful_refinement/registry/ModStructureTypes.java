package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.worldgen.structure.NetherSurfaceJigsawStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<NetherSurfaceJigsawStructure>> NETHER_SURFACE_JIGSAW =
            STRUCTURE_TYPES.register("nether_ground_jigsaw", () -> () -> NetherSurfaceJigsawStructure.CODEC);
}
