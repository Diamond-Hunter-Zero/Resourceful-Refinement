package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.worldgen.choral.ChoralClusterMountainStructure;
import com.resourceful_refinement.worldgen.crystalnode.CrystalNodeStructure;
import com.resourceful_refinement.worldgen.sinkhole.OverworldSinkholeStructure;
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

    public static final DeferredHolder<StructureType<?>, StructureType<ChoralClusterMountainStructure>> CHORAL_CLUSTER_MOUNTAIN =
            STRUCTURE_TYPES.register("choral_cluster_mountain", () -> () -> ChoralClusterMountainStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<CrystalNodeStructure>> CRYSTAL_NODE =
            STRUCTURE_TYPES.register("crystal_node", () -> () -> CrystalNodeStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<OverworldSinkholeStructure>> OVERWORLD_SINKHOLE =
            STRUCTURE_TYPES.register("overworld_sinkhole", () -> () -> OverworldSinkholeStructure.CODEC);
}
