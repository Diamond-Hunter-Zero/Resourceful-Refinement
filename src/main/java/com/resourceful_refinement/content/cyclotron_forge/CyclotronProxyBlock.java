package com.resourceful_refinement.content.cyclotron_forge;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.gui.GlarePowerTerminalOpener;
import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CyclotronProxyBlock extends BaseEntityBlock {
    public static final MapCodec<CyclotronProxyBlock> CODEC = simpleCodec(CyclotronProxyBlock::new);

    public CyclotronProxyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
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
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CyclotronProxyBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CyclotronProxyBlockEntity proxy) {
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
            if (level.getBlockEntity(pos) instanceof CyclotronProxyBlockEntity proxy) {
                CyclotronControllerBlockEntity controller = proxy.getController(level);
                if (controller != null && controller.isAssembled()) {
                    controller.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
