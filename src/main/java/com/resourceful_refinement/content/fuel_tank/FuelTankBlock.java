package com.resourceful_refinement.content.fuel_tank;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlock extends Block implements EntityBlock {

    public static final MapCodec<FuelTankBlock> CODEC = simpleCodec(FuelTankBlock::new);
    public static final BooleanProperty FRONT_PORT = BooleanProperty.create("front_port");
    public static final BooleanProperty EAST_PORT = BooleanProperty.create("east_port");
    public static final BooleanProperty SOUTH_PORT = BooleanProperty.create("south_port");
    public static final BooleanProperty WEST_PORT = BooleanProperty.create("west_port");

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);
    private static final VoxelShape SUPPORT_SHAPE = Shapes.block();

    public FuelTankBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FRONT_PORT, false)
                .setValue(EAST_PORT, false)
                .setValue(SOUTH_PORT, false)
                .setValue(WEST_PORT, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FRONT_PORT, EAST_PORT, SOUTH_PORT, WEST_PORT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SUPPORT_SHAPE;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SUPPORT_SHAPE;
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            updateConnections(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            updateConnections(level, pos, state);
        }
    }

    private static void updateConnections(Level level, BlockPos pos, BlockState state) {
        BlockState updated = updateConnectionState(state, level, pos);
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    private static BlockState updateConnectionState(BlockState state, LevelAccessor level, BlockPos pos) {
        return state
                .setValue(FRONT_PORT, hasFluidInterface(level, pos, Direction.NORTH))
                .setValue(EAST_PORT, hasFluidInterface(level, pos, Direction.EAST))
                .setValue(SOUTH_PORT, hasFluidInterface(level, pos, Direction.SOUTH))
                .setValue(WEST_PORT, hasFluidInterface(level, pos, Direction.WEST));
    }

    private static boolean hasFluidInterface(LevelAccessor level, BlockPos pos, Direction direction) {
        if (!(level instanceof Level realLevel)) {
            return false;
        }

        BlockPos neighbourPos = pos.relative(direction);
        BlockState neighbourState = level.getBlockState(neighbourPos);
        FluidTransportBehaviour transport = FluidPropagator.getPipe(level, neighbourPos);
        if (transport != null && transport.canHaveFlowToward(neighbourState, direction.getOpposite())) {
            return true;
        }

        return realLevel.getCapability(Capabilities.FluidHandler.BLOCK, neighbourPos, direction.getOpposite()) != null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FuelTankBlockEntity(ModBlockEntities.FUEL_TANK_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof FuelTankBlockEntity fuelTank) {
                FuelTankBlockEntity.serverTick(lvl, pos, st, fuelTank);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || stack.getCount() != 1 || FluidUtil.getFluidHandler(stack).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FuelTankBlockEntity fuelTank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(stack).orElse(null);
        if (itemHandler == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidStack contained = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (contained.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int accepted = fuelTank.tank.fill(contained, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidStack toDrain = contained.copy();
        toDrain.setAmount(accepted);
        FluidStack drained = itemHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int filled = fuelTank.tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // If not in creative, give back an empty container
        if (!player.isCreative())
        {
            player.setItemInHand(hand, itemHandler.getContainer());
            player.getInventory().setChanged();
        }

        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return ItemInteractionResult.SUCCESS;
    }
}
