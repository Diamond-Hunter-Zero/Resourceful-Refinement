package com.resourceful_refinement.content.casting_depot;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class CastingDepotBlockEntity extends DepotBlockEntity {

    public CastingDepotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public DepotBehaviour GetDepotBehaviour()
    {
        return getBehaviour(DepotBehaviour.TYPE);
    }

    public IItemHandler getItemHandler() {
        DepotBehaviour behavior = GetDepotBehaviour();
        return behavior != null ? behavior.itemHandler : null;
    }

    // You can override methods here if you need to change the item's hover height
    // or how it reacts when an item is placed, but for a simple visual change,
    // the base implementation is usually sufficient.
}
