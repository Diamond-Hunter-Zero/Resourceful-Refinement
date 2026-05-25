package com.resourceful_refinement.content.refinery;

import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

/**
 * An invisible stand-in block placed at every position inside an assembled
 * Fluid Refinery (except the Access Port itself). All interactions—right-click,
 * capability queries, and break events—are forwarded to the controller
 * (RefineryAccessPortBlockEntity) via the stored controller position.
 */
public class RefineryProxyBlock extends BaseEntityBlock {

    public static final MapCodec<RefineryProxyBlock> CODEC = simpleCodec(RefineryProxyBlock::new);
    public static final IntegerProperty HEAT_LEVEL = IntegerProperty.create("heat_level", 0, 2);

    public RefineryProxyBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HEAT_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEAT_LEVEL);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        int heat = state.getValue(HEAT_LEVEL);
        return heat == 2 ? 15 : (heat == 1 ? 10 : 0);
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
        return new RefineryProxyBlockEntity(ModBlockEntities.REFINERY_PROXY.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Interaction — delegate to controller
    // -------------------------------------------------------------------------
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RefineryProxyBlockEntity proxy)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        RefineryAccessPortBlockEntity controller = proxy.getController(level);
        if (controller == null || !controller.isAssembled()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Check for behavior interactions
        for (BlockEntityBehaviour behaviour : controller.getAllBehaviours()) {
            if (behaviour instanceof FilteringBehaviour filtering) {
                boolean hit = filtering.testHit(hitResult.getLocation());
                // com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("Proxy useItemOn: checking filtering behavior, hit={}", hit);
                if (hit) {
                    filtering.onShortInteract(player, hand, hitResult.getDirection(), hitResult);
                    return ItemInteractionResult.SUCCESS;
                }
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RefineryProxyBlockEntity proxy)) return InteractionResult.PASS;

        RefineryAccessPortBlockEntity controller = proxy.getController(level);
        if (controller == null || !controller.isAssembled()) return InteractionResult.PASS;

        // Check for behavior interactions
        for (BlockEntityBehaviour behaviour : controller.getAllBehaviours()) {
            if (behaviour instanceof FilteringBehaviour filtering) {
                boolean hit = filtering.testHit(hitResult.getLocation());
                // com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("Proxy useWithoutItem: checking filtering behavior, hit={}", hit);
                if (hit) {
                    filtering.onShortInteract(player, InteractionHand.MAIN_HAND, hitResult.getDirection(), hitResult);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // Empty hand: try to clear inventory
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            controller.tryClearInventory(player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // -------------------------------------------------------------------------
    // Block removal — triggers disassembly
    // -------------------------------------------------------------------------
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RefineryProxyBlockEntity proxy) {
                RefineryAccessPortBlockEntity controller = proxy.getController(level);
                if (controller != null && controller.isAssembled()) {
                    RefineryStructureHelper.disassemble(level, controller);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
