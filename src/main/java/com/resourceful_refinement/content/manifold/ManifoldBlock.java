package com.resourceful_refinement.content.manifold;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ManifoldBlock extends Block implements EntityBlock {
    public static final MapCodec<ManifoldBlock> CODEC = simpleCodec(ManifoldBlock::new);

    public ManifoldBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DirectionalBlock.FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManifoldBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(DirectionalBlock.FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DirectionalBlock.FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(DirectionalBlock.FACING, rotation.rotate(state.getValue(DirectionalBlock.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(DirectionalBlock.FACING)));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return createStack(level, pos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ManifoldBlockEntity manifold) {
            for (ItemStack drop : drops) {
                if (drop.is(ModItems.MANIFOLD_ITEM.get())) {
                    ManifoldItem.writeAssemblyRecord(drop, manifold.assemblyRecord());
                }
            }
        }
        return drops;
    }

    private ItemStack createStack(LevelReader level, BlockPos pos) {
        ItemStack stack = ModItems.MANIFOLD_ITEM.get().getDefaultInstance();
        if (level.getBlockEntity(pos) instanceof ManifoldBlockEntity manifold) {
            ManifoldItem.writeAssemblyRecord(stack, manifold.assemblyRecord());
        }
        return stack;
    }
}
