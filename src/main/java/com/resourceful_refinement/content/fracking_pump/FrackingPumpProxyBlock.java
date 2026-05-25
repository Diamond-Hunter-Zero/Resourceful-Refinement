package com.resourceful_refinement.content.fracking_pump;

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
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

/**
 * An invisible stand-in block placed at every position inside an
 * assembled Fracking Pump (except the Controller itself). All interactions—right-click,
 * capability queries, and break events—are forwarded to the controller
 * via the stored controller position.
 */
public class FrackingPumpProxyBlock extends BaseEntityBlock {

    public static final MapCodec<FrackingPumpProxyBlock> CODEC = simpleCodec(FrackingPumpProxyBlock::new);

    public FrackingPumpProxyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // -------------------------------------------------------------------------
    // Rendering — invisible but keeps collision / selection
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Block entity
    // -------------------------------------------------------------------------
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FrackingPumpProxyBlockEntity(ModBlockEntities.FRACKING_PUMP_PROXY_BE.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Interaction — delegate to controller
    // -------------------------------------------------------------------------

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FrackingPumpProxyBlockEntity proxy) {
            FrackingPumpOutletBlockEntity controller = proxy.getController(level);
            if (controller != null) {
                BlockState controllerState = level.getBlockState(controller.getBlockPos());
                return controllerState.useWithoutItem(level, player, hitResult.withPosition(controller.getBlockPos()));
            }
        }
        return InteractionResult.PASS;
    }

    // -------------------------------------------------------------------------
    // Block removal — triggers disassembly
    // -------------------------------------------------------------------------
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof FrackingPumpProxyBlockEntity proxy) {
                FrackingPumpOutletBlockEntity controller = proxy.getController(level);
                if (controller != null && controller.isAssembled()) {
                    controller.disassemble();
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
