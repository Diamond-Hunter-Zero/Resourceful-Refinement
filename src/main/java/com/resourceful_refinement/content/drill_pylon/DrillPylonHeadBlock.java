package com.resourceful_refinement.content.drill_pylon;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DrillPylonHeadBlock extends KineticBlock implements IBE<DrillPylonHeadBlockEntity> {
    public static final MapCodec<DrillPylonHeadBlock> CODEC = simpleCodec(DrillPylonHeadBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public DrillPylonHeadBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends KineticBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof DrillPylonHeadBlockEntity head)) return InteractionResult.PASS;
        if (head.isAssembled()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        DrillPylonHeadBlockEntity.AssemblyResult result = head.tryAssemble(player);
        if (result.success()) {
            player.displayClientMessage(Component.literal("[Drill Pylon] Structure assembled."), false);
        } else if (!result.reason().isEmpty()) {
            player.displayClientMessage(Component.literal("[Drill Pylon] Assembly failed: " + result.reason()), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            withBlockEntityDo(level, pos, DrillPylonHeadBlockEntity::disassemble);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Class<DrillPylonHeadBlockEntity> getBlockEntityClass() {
        return DrillPylonHeadBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DrillPylonHeadBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DRILL_PYLON_HEAD_BE.get();
    }
}
