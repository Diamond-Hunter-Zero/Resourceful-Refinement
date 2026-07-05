package com.resourceful_refinement.content.conveyor;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;

record ConveyorRotatorTarget(BlockState targetState, float requiredDegrees, boolean fullSpin) {
    private static final ConveyorRotatorTarget NO_ROTATION = new ConveyorRotatorTarget(null, 0, false);

    public static ConveyorRotatorTarget forState(BlockState state, Direction outputDirection, float speed) {
        DirectionProperty property = findDirectionProperty(state, outputDirection);
        if (property == null) {
            return NO_ROTATION;
        }

        Direction currentDirection = state.getValue(property);
        BlockState targetState = state.setValue(property, outputDirection);

        boolean fullDirectional = property.getPossibleValues().contains(Direction.UP)
                || property.getPossibleValues().contains(Direction.DOWN);

        if (currentDirection == outputDirection) {
            return NO_ROTATION;
        }
        if (fullDirectional && currentDirection.getAxis().isVertical()) {
            return new ConveyorRotatorTarget(targetState, signedDegreesForSpin(speed), true);
        }

        if (currentDirection.getAxis().isVertical()) {
            return NO_ROTATION;
        }

        int quarterTurns = quarterTurnsForSpeed(currentDirection, outputDirection, speed);
        if (quarterTurns == 0) {
            return NO_ROTATION;
        }
        float directionSign = speed < 0 ? -1 : 1;
        return new ConveyorRotatorTarget(targetState, directionSign * quarterTurns * 90.0f, false);
    }

    public boolean requiresRotation() {
        return targetState != null && requiredDegrees != 0;
    }

    private static int quarterTurnsForSpeed(Direction currentDirection, Direction outputDirection, float speed) {
        int clockwiseTurns = Math.floorMod(outputDirection.get2DDataValue() - currentDirection.get2DDataValue(), 4);
        int counterClockwiseTurns = Math.floorMod(currentDirection.get2DDataValue() - outputDirection.get2DDataValue(), 4);
        return speed < 0 ? clockwiseTurns : counterClockwiseTurns;
    }

    private static float signedDegreesForSpin(float speed) {
        return speed < 0 ? -360 : 360;
    }

    private static DirectionProperty findDirectionProperty(BlockState state, Direction outputDirection) {
        DirectionProperty fallback = null;
        for (Property<?> property : state.getProperties()) {
            if (!(property instanceof DirectionProperty directionProperty)) {
                continue;
            }
            Collection<Direction> values = directionProperty.getPossibleValues();
            if (!values.contains(outputDirection)) {
                continue;
            }
            if ("facing".equals(directionProperty.getName())) {
                return directionProperty;
            }
            if (fallback == null) {
                fallback = directionProperty;
            }
        }
        return fallback;
    }
}
