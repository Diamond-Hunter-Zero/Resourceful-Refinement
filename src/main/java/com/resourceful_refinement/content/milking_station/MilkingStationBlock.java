package com.resourceful_refinement.content.milking_station;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

public class MilkingStationBlock extends KineticBlock implements IBE<MilkingStationBlockEntity> {

    public static final MapCodec<MilkingStationBlock> CODEC = simpleCodec(MilkingStationBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape BOUNDING_AABB = Shapes.or(Block.box(0, 0, 0, 16, 13, 16), Block.box(2, 13, 2, 14, 16, 14));
    private static final VoxelShape SUPPORT_SHAPE = Shapes.block();


    public MilkingStationBlock(Properties properties) {
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // -------------------------------------------------------------------------
    // Block Shapes
    // -------------------------------------------------------------------------
    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return BOUNDING_AABB; }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {  return BOUNDING_AABB; }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SUPPORT_SHAPE;
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }



    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MilkingStationBlockEntity(ModBlockEntities.MILKING_STATION_BE.get(), pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof MilkingStationBlockEntity station) {
            station.releaseCapturedEntity(null, false);
            station.clearSeatedPlayer();
            discardSeat(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof MilkingStationBlockEntity station) || !station.canSeatPlayer()
                || player.isPassenger()) {
            return InteractionResult.PASS;
        }

        MilkingStationSeatEntity seat = new MilkingStationSeatEntity(level, pos);
        if (!level.addFreshEntity(seat)) {
            return InteractionResult.CONSUME;
        }

        if (!player.startRiding(seat, true)) {
            seat.discard();
            return InteractionResult.CONSUME;
        }

        Direction facing = state.getValue(FACING);
        player.setYRot(facing.toYRot() + 180f);
        player.setYHeadRot(facing.toYRot() + 180f);
        station.setSeatedPlayer(player);
        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5f, 1.2f);
        return InteractionResult.CONSUME;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MilkingStationBlockEntity station)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean holdingLead = stack.is(Items.LEAD);

        // Releasing with a lead re-leashes the mob to the player; with both hands empty it is set loose.
        if (station.hasCapturedEntity()) {
            boolean emptyHanded = player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
            if (!holdingLead && !emptyHanded) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (station.releaseCapturedEntity(player, holdingLead)) {
                if (holdingLead) {
                    stack.consume(1, player);
                }
                level.playSound(null, pos, SoundEvents.LEASH_KNOT_BREAK, SoundSource.BLOCKS, 0.8f, 1.0f);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }

        if (station.hasSeatedPlayer()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Capturing only needs a mob leashed to the player; in survival the lead is no longer in hand.
        Optional<Mob> leashedMob = findNearestLeashedMob(level, pos, player);
        Optional<Boat> leashedBoat = leashedMob.isPresent()
                ? Optional.empty()
                : findNearestLeashedBoatWithMob(level, pos, player);
        if (leashedMob.isEmpty() && leashedBoat.isEmpty()) {
            if (holdingLead && !level.isClientSide) {
                player.displayClientMessage(Component.literal("No leashed mob nearby."), true);
                return ItemInteractionResult.CONSUME;
            }
            return holdingLead ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        // Unhooking the leash drops the lead item, matching vanilla unleashing.
        boolean dropLead = !player.hasInfiniteMaterials();
        Mob mob;
        if (leashedMob.isPresent()) {
            mob = leashedMob.get();
            ((Leashable) mob).dropLeash(true, dropLead);
        } else {
            Boat boat = leashedBoat.get();
            mob = getCapturableBoatPassenger(boat).get();
            ((Leashable) boat).dropLeash(true, dropLead);
        }

        mob.stopRiding();
        if (station.captureEntity(mob)) {
            level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 0.8f, 1.0f);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.CONSUME;
    }

    private Optional<Mob> findNearestLeashedMob(Level level, BlockPos pos, Player player) {
        AABB searchBox = new AABB(pos).inflate(8.0);
        return level.getEntitiesOfClass(Mob.class, searchBox, mob -> canCapture(mob, player))
                .stream()
                .min(Comparator.comparingDouble(mob -> mob.distanceToSqr(player)));
    }

    private Optional<Boat> findNearestLeashedBoatWithMob(Level level, BlockPos pos, Player player) {
        AABB searchBox = new AABB(pos).inflate(8.0);
        return level.getEntitiesOfClass(Boat.class, searchBox,
                        boat -> isLeashedToPlayer(boat, player) && getCapturableBoatPassenger(boat).isPresent())
                .stream()
                .min(Comparator.comparingDouble(boat -> boat.distanceToSqr(player)));
    }

    private Optional<Mob> getCapturableBoatPassenger(Boat boat) {
        return boat.getPassengers().stream()
                .filter(Mob.class::isInstance)
                .map(Mob.class::cast)
                .filter(this::canCaptureBoatPassenger)
                .findFirst();
    }

    private boolean canCapture(Mob mob, Player player) {
        if (!mob.isAlive() || mob.isPassenger() || mob.isVehicle()) {
            return false;
        }

        if (!(mob instanceof Leashable leashable) || !leashable.isLeashed()) {
            return false;
        }

        Entity leashHolder = leashable.getLeashHolder();
        return leashHolder == player;
    }

    private boolean isLeashedToPlayer(Entity entity, Player player) {
        if (!(entity instanceof Leashable leashable) || !leashable.isLeashed()) {
            return false;
        }
        return leashable.getLeashHolder() == player;
    }

    private boolean canCaptureBoatPassenger(Mob mob) {
        return mob.isAlive() && !mob.isVehicle();
    }

    private void discardSeat(Level level, BlockPos pos) {
        AABB searchBox = new AABB(pos).inflate(1.0);
        for (MilkingStationSeatEntity seat : level.getEntitiesOfClass(MilkingStationSeatEntity.class, searchBox,
                seat -> seat.blockPosition().equals(pos))) {
            seat.discard();
        }
    }

    @Override
    public Class<MilkingStationBlockEntity> getBlockEntityClass() {
        return MilkingStationBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MilkingStationBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MILKING_STATION_BE.get();
    }
}
