package com.resourceful_refinement.content.combustion_chamber;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CombustionChamberFanIntegration {
    public static boolean isFanDrivenByOutputChamber(LevelReader level, BlockPos fanPos, BlockState fanState) {
        if (!fanState.hasProperty(EncasedFanBlock.FACING)) {
            return false;
        }

        Direction fanFacing = fanState.getValue(EncasedFanBlock.FACING);
        BlockPos chamberPos = fanPos.relative(fanFacing.getOpposite());
        return level.getBlockEntity(chamberPos) instanceof CombustionChamberBlockEntity chamber
                && chamber.getBlockState().hasProperty(CombustionChamberBlock.FACING)
                && chamber.isOutputEngine()
                && chamber.getBlockState().getValue(CombustionChamberBlock.FACING) == fanFacing;
    }

    public static boolean isFanDrivenByOutputChamber(BlockGetter level, BlockPos fanPos, BlockState fanState) {
        if (!fanState.hasProperty(EncasedFanBlock.FACING)) {
            return false;
        }

        Direction fanFacing = fanState.getValue(EncasedFanBlock.FACING);
        BlockPos chamberPos = fanPos.relative(fanFacing.getOpposite());
        return level.getBlockEntity(chamberPos) instanceof CombustionChamberBlockEntity chamber
                && chamber.getBlockState().hasProperty(CombustionChamberBlock.FACING)
                && chamber.isOutputEngine()
                && chamber.getBlockState().getValue(CombustionChamberBlock.FACING) == fanFacing;
    }

    public static boolean hasIntakeFan(Level level, CombustionChamberBlockEntity chamber) {
        BlockState chamberState = chamber.getBlockState();
        if (!chamberState.hasProperty(CombustionChamberBlock.FACING)) {
            return false;
        }

        Direction facing = chamberState.getValue(CombustionChamberBlock.FACING);
        BlockPos fanPos = chamber.getBlockPos().relative(facing);
        BlockState fanState = level.getBlockState(fanPos);
        return fanState.getBlock() instanceof EncasedFanBlock
                && fanState.hasProperty(EncasedFanBlock.FACING)
                && fanState.getValue(EncasedFanBlock.FACING) == facing;
    }

    public static void refreshFanInFront(Level level, BlockPos chamberPos, BlockState chamberState) {
        if (level.isClientSide || !chamberState.hasProperty(CombustionChamberBlock.FACING)) {
            return;
        }

        Direction facing = chamberState.getValue(CombustionChamberBlock.FACING);
        BlockPos fanPos = chamberPos.relative(facing);
        BlockState fanState = level.getBlockState(fanPos);
        if (!(fanState.getBlock() instanceof EncasedFanBlock) || fanState.getValue(EncasedFanBlock.FACING) != facing) {
            return;
        }

        if (level.getBlockEntity(fanPos) instanceof KineticBlockEntity fan) {
            fan.detachKinetics();
            fan.clearKineticInformation();
            fan.updateSpeed = true;
            fan.requestModelDataUpdate();
        }

        level.sendBlockUpdated(fanPos, fanState, fanState, Block.UPDATE_CLIENTS);
    }
}
