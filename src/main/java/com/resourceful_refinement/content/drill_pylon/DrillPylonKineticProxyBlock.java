package com.resourceful_refinement.content.drill_pylon;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DrillPylonKineticProxyBlock extends RotatedPillarKineticBlock implements IBE<DrillPylonKineticProxyBlockEntity> {
    public static final MapCodec<DrillPylonKineticProxyBlock> CODEC = simpleCodec(DrillPylonKineticProxyBlock::new);

    public DrillPylonKineticProxyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends RotatedPillarKineticBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof DrillPylonKineticProxyBlockEntity proxy) {
            DrillPylonHeadBlockEntity controller = proxy.getController(level);
            if (controller != null) {
                return level.getBlockState(controller.getBlockPos()).useWithoutItem(level, player, hitResult.withPosition(controller.getBlockPos()));
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof DrillPylonKineticProxyBlockEntity proxy) {
                DrillPylonHeadBlockEntity controller = proxy.getController(level);
                if (controller != null && controller.isAssembled()) {
                    controller.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Class<DrillPylonKineticProxyBlockEntity> getBlockEntityClass() {
        return DrillPylonKineticProxyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DrillPylonKineticProxyBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DRILL_PYLON_KINETIC_PROXY_BE.get();
    }
}
