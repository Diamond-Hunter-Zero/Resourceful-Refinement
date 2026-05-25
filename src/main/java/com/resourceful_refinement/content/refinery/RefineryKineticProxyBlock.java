package com.resourceful_refinement.content.refinery;

import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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

import com.mojang.serialization.MapCodec;

public class RefineryKineticProxyBlock extends RotatedPillarKineticBlock implements com.simibubi.create.foundation.block.IBE<RefineryKineticProxyBlockEntity> {

    public static final MapCodec<RefineryKineticProxyBlock> CODEC = simpleCodec(RefineryKineticProxyBlock::new);

    public RefineryKineticProxyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return com.simibubi.create.foundation.block.IBE.super.newBlockEntity(pos, state);
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RefineryKineticProxyBlockEntity proxy)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        RefineryAccessPortBlockEntity controller = proxy.getController(level);
        if (controller == null || !controller.isAssembled()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Check for behavior interactions
        for (BlockEntityBehaviour behaviour : controller.getAllBehaviours()) {
            if (behaviour instanceof FilteringBehaviour filtering && filtering.testHit(hitResult.getLocation())) {
                filtering.onShortInteract(player, hand, hitResult.getDirection(), hitResult);
                return ItemInteractionResult.SUCCESS;
            }
        }

        // Bottom layer of the refinery allows fueling
        if (proxy.getDy() == 0) {
            int burnTime = stack.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) {
                if (controller.addFuel(stack, player)) {
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof RefineryKineticProxyBlockEntity proxy) {
            RefineryAccessPortBlockEntity controller = proxy.getController(level);
            if (controller != null && controller.isAssembled()) {
                // Check for behavior interactions
                for (BlockEntityBehaviour behaviour : controller.getAllBehaviours()) {
                    if (behaviour instanceof FilteringBehaviour filtering && filtering.testHit(hitResult.getLocation())) {
                        filtering.onShortInteract(player, InteractionHand.MAIN_HAND, hitResult.getDirection(), hitResult);
                        return InteractionResult.SUCCESS;
                    }
                }
                
                // Empty hand: try to clear inventory
                if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    controller.tryClearInventory(player);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(pos) instanceof RefineryKineticProxyBlockEntity proxy) {
                RefineryAccessPortBlockEntity controller = proxy.getController(level);
                if (controller != null) {
                    RefineryStructureHelper.disassemble(level, controller);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == Direction.Axis.Y;
    }

    @Override
    public Class<RefineryKineticProxyBlockEntity> getBlockEntityClass() {
        return RefineryKineticProxyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RefineryKineticProxyBlockEntity> getBlockEntityType() {
        return ModBlockEntities.REFINERY_KINETIC_PROXY.get();
    }
}
