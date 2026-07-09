package com.resourceful_refinement.content.cyclotron_forge;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.gui.GlarePowerTerminalOpener;
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

public class CyclotronKineticProxyBlock extends RotatedPillarKineticBlock implements IBE<CyclotronKineticProxyBlockEntity> {
    public static final MapCodec<CyclotronKineticProxyBlock> CODEC = simpleCodec(CyclotronKineticProxyBlock::new);

    public CyclotronKineticProxyBlock(Properties properties) {
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
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CyclotronKineticProxyBlockEntity proxy) {
            CyclotronControllerBlockEntity controller = proxy.getController(level);
            if (controller != null) {
                return GlarePowerTerminalOpener.open(level, controller.getBlockPos(), pos, player);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof CyclotronKineticProxyBlockEntity proxy) {
                CyclotronControllerBlockEntity controller = proxy.getController(level);
                if (controller != null && controller.isAssembled()) {
                    controller.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<CyclotronKineticProxyBlockEntity> getBlockEntityClass() {
        return CyclotronKineticProxyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CyclotronKineticProxyBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CYCLOTRON_KINETIC_PROXY_BE.get();
    }
}
