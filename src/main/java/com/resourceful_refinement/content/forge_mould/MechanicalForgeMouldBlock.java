package com.resourceful_refinement.content.forge_mould;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;

public class MechanicalForgeMouldBlock extends KineticBlock implements IBE<MechanicalForgeMouldBlockEntity> {

    public static final MapCodec<MechanicalForgeMouldBlock> CODEC = simpleCodec(MechanicalForgeMouldBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MechanicalForgeMouldBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends KineticBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MechanicalForgeMouldBlockEntity(ModBlockEntities.MECHANICAL_FORGE_MOULD_BE.get(), pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        // Face the player when placed
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getClockWise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        withBlockEntityDo(level, pos, be -> {
            IItemHandler inv = be.inputInv;
            boolean extracted = false;
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stackInSlot = inv.getStackInSlot(i);
                if (!stackInSlot.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stackInSlot);
                    inv.extractItem(i, stackInSlot.getCount(), false);
                    extracted = true;
                }
            }
            if (extracted) {
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        });

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public Class<MechanicalForgeMouldBlockEntity> getBlockEntityClass() {
        return MechanicalForgeMouldBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalForgeMouldBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MECHANICAL_FORGE_MOULD_BE.get();
    }
}
