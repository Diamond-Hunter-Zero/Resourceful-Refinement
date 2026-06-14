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
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MilkingStationBlock extends KineticBlock implements IBE<MilkingStationBlockEntity> {

    public static final MapCodec<MilkingStationBlock> CODEC = simpleCodec(MilkingStationBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

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
        if (!stack.is(Items.LEAD)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof MilkingStationBlockEntity station)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (station.hasCapturedEntity()) {
            if (station.releaseCapturedEntity(player, true)) {
                level.playSound(null, pos, SoundEvents.LEASH_KNOT_BREAK, SoundSource.BLOCKS, 0.8f, 1.0f);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }

        Optional<Mob> mobToCapture = findNearestLeashedMob(level, pos, player)
                .or(() -> findNearestMobPassengerInLeashedBoat(level, pos, player));
        if (mobToCapture.isEmpty()) {
            player.displayClientMessage(Component.literal("No leashed mob nearby."), true);
            return ItemInteractionResult.CONSUME;
        }

        Mob mob = mobToCapture.get();
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

    private Optional<Mob> findNearestMobPassengerInLeashedBoat(Level level, BlockPos pos, Player player) {
        AABB searchBox = new AABB(pos).inflate(8.0);
        List<Boat> boats = level.getEntitiesOfClass(Boat.class, searchBox, boat -> isLeashedToPlayer(boat, player))
                .stream()
                .sorted(Comparator.comparingDouble(boat -> boat.distanceToSqr(player)))
                .toList();

        for (Boat boat : boats) {
            Optional<Mob> passenger = boat.getPassengers().stream()
                    .filter(Mob.class::isInstance)
                    .map(Mob.class::cast)
                    .filter(this::canCaptureBoatPassenger)
                    .findFirst();

            if (passenger.isPresent()) {
                if (boat instanceof Leashable leashable) {
                    leashable.dropLeash(false, false);
                }
                return passenger;
            }
        }

        return Optional.empty();
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
