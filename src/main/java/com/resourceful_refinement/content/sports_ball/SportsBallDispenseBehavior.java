package com.resourceful_refinement.content.sports_ball;

import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.Level;

/**
 * When a dispenser fires a Sports Ball, spawn a live {@link SportsBallEntity} in the dispense
 * direction rather than the usual {@code ItemEntity} — so the ball leaves the dispenser already
 * rolling. Extending {@link DefaultDispenseItemBehavior} keeps the vanilla click + smoke animation
 * for free by only overriding {@link #execute}.
 */
public class SportsBallDispenseBehavior extends DefaultDispenseItemBehavior {

    /** Launch speed along the dispenser's facing. Modest — the ball is meant to roll, not fly. */
    private static final double LAUNCH_SPEED = 0.4D;

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        Position spawn = DispenserBlock.getDispensePosition(source);
        Level level = source.level();

        // Consume one ball from the stack; hand its ball-type through to the spawned entity so
        // dispensing a coloured ball produces the same coloured ball in the world.
        ItemStack one = stack.split(1);
        int type = one.getOrDefault(ModDataComponents.BALL_TYPE.get(), 0);

        SportsBallEntity ball = new SportsBallEntity(ModEntities.SPORTS_BALL.get(), level);
        // Sit the ball a little below the dispense position so its feet clear the block face when
        // firing horizontally — same 1/8-ish drop vanilla applies to dispensed items.
        double dy = facing.getAxis() == Direction.Axis.Y ? -0.125D : -0.15625D;
        ball.setPos(spawn.x(), spawn.y() + dy - SportsBallEntity.RADIUS, spawn.z());
        ball.setDeltaMovement(
                facing.getStepX() * LAUNCH_SPEED,
                facing.getStepY() * LAUNCH_SPEED,
                facing.getStepZ() * LAUNCH_SPEED
        );
        ball.hasImpulse = true;
        ball.setBallType(type);

        level.addFreshEntity(ball);
        return stack;
    }
}
