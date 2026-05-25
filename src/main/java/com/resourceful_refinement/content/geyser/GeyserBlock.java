package com.resourceful_refinement.content.geyser;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GeyserBlock extends Block implements EntityBlock {

    public GeyserBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return com.resourceful_refinement.registry.ModBlockEntities.GEYSER_BE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof GeyserBlockEntity geyserBE) {
                geyserBE.tick();
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isCreative()) {
            net.neoforged.neoforge.fluids.FluidStack fluidStack = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(stack).orElse(net.neoforged.neoforge.fluids.FluidStack.EMPTY);
            net.minecraft.world.level.material.Fluid fluid = fluidStack.getFluid();
            if (fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                if (level.getBlockEntity(pos) instanceof GeyserBlockEntity be) {
                    if (!level.isClientSide) {
                        be.setAssociatedFluid(fluid);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
