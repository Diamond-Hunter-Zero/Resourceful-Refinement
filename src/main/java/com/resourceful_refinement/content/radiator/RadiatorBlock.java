package com.resourceful_refinement.content.radiator;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.AxisPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

public class RadiatorBlock extends AxisPipeBlock implements EntityBlock {

    public static final MapCodec<RadiatorBlock> CODEC = simpleCodec(RadiatorBlock::new);

    public static final IntegerProperty HEAT_STATE = IntegerProperty.create("heat_state", 0,3);

    public RadiatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(HEAT_STATE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HEAT_STATE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadiatorBlockEntity(ModBlockEntities.RADIATOR_PIPE_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof RadiatorBlockEntity radiator) {
                RadiatorBlockEntity.serverTick(lvl, pos, st, radiator);
            }
        };
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && state.getBlock() != oldState.getBlock()) {
            // Force vanilla neighbor update
            level.updateNeighborsAt(pos, this);
            // Force Create to rebuild the pipe network graph around this block
            FluidPropagator.propagateChangedPipe(level, pos, state);
            // Also schedule a tick on each neighbouring pipe so they re-evaluate
            // their connection to the newly placed radiator. Without this, they keep
            // treating the radiator as a FluidHandler endpoint until they are updated.
            for (Direction dir : Direction.values()) {
                BlockPos neighbourPos = pos.relative(dir);
                BlockState neighbourState = level.getBlockState(neighbourPos);
                FluidTransportBehaviour neighbourPipe = FluidPropagator.getPipe(level, neighbourPos);
                if (neighbourPipe != null) {
                    level.scheduleTick(neighbourPos, neighbourState.getBlock(), 1, TickPriority.HIGH);
                }
            }
        }
    }

    /**
     * Override neighborChanged so that when a pipe next to us is updated we
     * propagate correctly. The parent AxisPipeBlock implementation early-returns
     * when it sees another AxisPipeBlock as the changed neighbour, so the radiator
     * would otherwise miss pipe-to-radiator connection changes.
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block otherBlock,
                                BlockPos neighborPos, boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(level, pos);
        if (level.isClientSide) return;
        // Always schedule re-propagation when any neighbour changes so that pipes
        // re-evaluate whether to treat us as a handler endpoint or a passable radiator.
        level.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource r) {
        FluidPropagator.propagateChangedPipe(level, pos, state);
        // Also re-propagate every adjacent pipe so they pick up the correct
        // FlowSource for the radiator (OtherPipe rather than FluidHandler).
        for (Direction dir : Direction.values()) {
            BlockPos neighbourPos = pos.relative(dir);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, neighbourPos);
            if (pipe != null) {
                pipe.wipePressure();
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide() && state.getBlock() != newState.getBlock()) {
            level.updateNeighborsAt(pos, this);
            FluidPropagator.propagateChangedPipe(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
