package com.resourceful_refinement.content.cyclotron_forge;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.drill_pylon.DrillPylonHeadBlockEntity;
import com.resourceful_refinement.content.gui.GlarePowerTerminalOpener;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CyclotronControllerBlock extends HorizontalDirectionalBlock implements IBE<CyclotronControllerBlockEntity> {
    public static final MapCodec<CyclotronControllerBlock> CODEC = simpleCodec(CyclotronControllerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public CyclotronControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ASSEMBLED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CyclotronControllerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof CyclotronControllerBlockEntity controller)) {
            return InteractionResult.PASS;
        }
        if (controller.isAssembled()) return GlarePowerTerminalOpener.open(level, pos, player);
        if (level.isClientSide) return InteractionResult.SUCCESS;

        CyclotronControllerBlockEntity.AssemblyResult result = controller.tryAssemble();
        if (result.success()) {
            player.displayClientMessage(Component.literal("[Cyclotron Forge] Structure assembled."), false);
        } else if (!result.reason().isEmpty()) {
            player.displayClientMessage(Component.literal("[Cyclotron Forge] Assembly failed: " + result.reason()), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof CyclotronControllerBlockEntity controller) {
                controller.disassemble(false);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<CyclotronControllerBlockEntity> getBlockEntityClass() {
        return CyclotronControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CyclotronControllerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CYCLOTRON_CONTROLLER_BE.get();
    }
}
