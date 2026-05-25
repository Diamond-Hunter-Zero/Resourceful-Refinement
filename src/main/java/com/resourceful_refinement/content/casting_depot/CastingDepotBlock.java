package com.resourceful_refinement.content.casting_depot;

import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlockEntity;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class CastingDepotBlock extends DepotBlock {

    // Define the facing property (North, South, East, West)
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public CastingDepotBlock(Properties properties) {
        super(properties);
        // Set the default state to North
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // When placed, face the player (opposite of the direction the player is looking)
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Add FACING while keeping the properties from DepotBlock (like WATERLOGGED)
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * This ensures your custom block uses your custom Block Entity logic.
     * If you want it to behave exactly like a standard Depot, you can
     * return a standard DepotBlockEntity instead.
     */
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CastingDepotBlockEntity(ModBlockEntities.CASTING_DEPOT_BE.get(), pos, state);
    }
}
