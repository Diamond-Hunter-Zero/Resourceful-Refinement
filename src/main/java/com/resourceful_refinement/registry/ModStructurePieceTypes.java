package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.worldgen.choral.ChoralClusterMountainPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructurePieceTypes {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> CHORAL_CLUSTER_MOUNTAIN_PIECE =
            STRUCTURE_PIECE_TYPES.register("choral_cluster_mountain_piece", () -> ChoralClusterMountainPiece::new);
}
