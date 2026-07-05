package com.resourceful_refinement.content.conveyor;

import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModDataComponents;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltItem extends BlockItem {
    public static final int MAX_BELT_LENGTH = 8;

    public ConveyorBeltItem(ConveyorBeltBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack heldStack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            heldStack.remove(ModDataComponents.CONVEYOR_FIRST_SHAFT.get());
            return InteractionResult.SUCCESS;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        boolean validShaft = validateShaft(level, clickedPos);
        if (level.isClientSide) {
            return validShaft ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        if (!validShaft || player == null) {
            return InteractionResult.FAIL;
        }

        BlockPos firstShaft = heldStack.get(ModDataComponents.CONVEYOR_FIRST_SHAFT.get());
        if (firstShaft != null && (!validateShaft(level, firstShaft) || firstShaft.distManhattan(clickedPos) > MAX_BELT_LENGTH * 2)) {
            heldStack.remove(ModDataComponents.CONVEYOR_FIRST_SHAFT.get());
            firstShaft = null;
        }

        if (firstShaft == null) {
            heldStack.set(ModDataComponents.CONVEYOR_FIRST_SHAFT.get(), clickedPos);
            player.getCooldowns().addCooldown(this, 5);
            return InteractionResult.SUCCESS;
        }

        if (!firstShaft.equals(clickedPos)) {
            if (!canConnect(level, firstShaft, clickedPos)) {
                return InteractionResult.FAIL;
            }
            createBelts(level, firstShaft, clickedPos);
            if (!player.isCreative()) {
                heldStack.shrink(1);
            }
        }

        if (!heldStack.isEmpty()) {
            heldStack.remove(ModDataComponents.CONVEYOR_FIRST_SHAFT.get());
            player.getCooldowns().addCooldown(this, 5);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean canConnect(Level level, BlockPos first, BlockPos second) {
        if (!level.isLoaded(first) || !level.isLoaded(second) || first.getY() != second.getY()) {
            return false;
        }

        Direction facing = getHorizontalFacing(first, second);
        if (facing == null) {
            return false;
        }

        Direction.Axis expectedShaftAxis = facing.getClockWise().getAxis();
        if (!isShaftOnAxis(level, first, expectedShaftAxis) || !isShaftOnAxis(level, second, expectedShaftAxis)) {
            return false;
        }

        List<BlockPos> positions = getBeltChainBetween(first, second, facing);
        if (positions.isEmpty()) {
            return false;
        }
        if (positions.size() > MAX_BELT_LENGTH) {
            return false;
        }

        if (!haveCompatibleSpeeds(level, first, second)) {
            return false;
        }

        for (BlockPos pos : positions) {
            if (!canReplaceWithBelt(level, pos, expectedShaftAxis)) {
                return false;
            }
        }

        return true;
    }

    public static void createBelts(Level level, BlockPos start, BlockPos end) {
        Direction facing = getHorizontalFacing(start, end);
        if (facing == null) {
            return;
        }

        Direction.Axis shaftAxis = facing.getClockWise().getAxis();
        List<BlockPos> positions = getBeltChainBetween(start, end, facing);
        if (positions.isEmpty() || positions.size() > MAX_BELT_LENGTH) {
            return;
        }
        ConveyorBeltBlock belt = ModBlocks.CONVEYOR_BELT.get();
        BlockState beltState = belt.defaultBlockState().setValue(ConveyorBeltBlock.HORIZONTAL_FACING, facing);

        level.playSound(null, BlockPos.containing(VecHelper.getCenterOf(start.offset(end)).scale(0.5f)),
                SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5F, 1F);

        for (BlockPos pos : positions) {
            BeltPart part = pos.equals(start) ? BeltPart.START : pos.equals(end) ? BeltPart.END : BeltPart.MIDDLE;
            if (part == BeltPart.MIDDLE && isShaftOnAxis(level, pos, shaftAxis)) {
                part = BeltPart.PULLEY;
            }
            KineticBlockEntity.switchToBlockState(level, pos, beltState.setValue(ConveyorBeltBlock.PART, part));
        }

        ConveyorBeltBlock.initBelt(level, start);
    }

    public static boolean canReplaceWithBelt(Level level, BlockPos pos, Direction.Axis shaftAxis) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (ShaftBlock.isShaft(state)) {
            return state.getValue(AbstractSimpleShaftBlock.AXIS) == shaftAxis;
        }
        return state.canBeReplaced();
    }

    static boolean validateShaft(Level level, BlockPos pos) {
        return level.isLoaded(pos) && ShaftBlock.isShaft(level.getBlockState(pos));
    }

    static boolean isShaftOnAxis(Level level, BlockPos pos, Direction.Axis axis) {
        BlockState state = level.getBlockState(pos);
        return ShaftBlock.isShaft(state) && state.getValue(AbstractSimpleShaftBlock.AXIS) == axis;
    }

    private static boolean haveCompatibleSpeeds(Level level, BlockPos first, BlockPos second) {
        BlockEntity firstEntity = level.getBlockEntity(first);
        BlockEntity secondEntity = level.getBlockEntity(second);
        if (!(firstEntity instanceof KineticBlockEntity firstKinetic) || !(secondEntity instanceof KineticBlockEntity secondKinetic)) {
            return false;
        }
        float speed1 = firstKinetic.getTheoreticalSpeed();
        float speed2 = secondKinetic.getTheoreticalSpeed();
        return Math.signum(speed1) == Math.signum(speed2) || speed1 == 0 || speed2 == 0;
    }

    static Direction getHorizontalFacing(BlockPos start, BlockPos end) {
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        if (dx != 0 && dz != 0) {
            return null;
        }
        if (dx == 0 && dz == 0) {
            return null;
        }
        if (dx != 0) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    static List<BlockPos> getBeltChainBetween(BlockPos start, BlockPos end, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos cursor = start;
        int limit = 1000;
        while (limit-- > 0) {
            positions.add(cursor);
            if (cursor.equals(end)) {
                return positions;
            }
            cursor = cursor.relative(facing);
        }
        return List.of();
    }
}
