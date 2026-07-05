package com.resourceful_refinement.content.conveyor;

import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class ConveyorBeltConnectorHandler {
    private static final DustParticleOptions VALID_PARTICLE = new DustParticleOptions(new Vector3f(.3f, .9f, .5f), 1);
    private static final DustParticleOptions INVALID_PARTICLE = new DustParticleOptions(new Vector3f(.9f, .3f, .5f), 1);

    private ConveyorBeltConnectorHandler() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (!heldItem.is(ModItems.CONVEYOR_BELT_ITEM.get()) || !heldItem.has(ModDataComponents.CONVEYOR_FIRST_SHAFT.get())) {
                continue;
            }

            BlockPos first = heldItem.get(ModDataComponents.CONVEYOR_FIRST_SHAFT.get());
            if (first == null || !ConveyorBeltItem.validateShaft(level, first)) {
                continue;
            }

            BlockState firstState = level.getBlockState(first);
            Direction.Axis shaftAxis = firstState.getValue(AbstractSimpleShaftBlock.AXIS);
            HitResult hitResult = minecraft.hitResult;
            if (!(hitResult instanceof BlockHitResult blockHitResult)) {
                sparkleFirstShaft(level, first);
                return;
            }

            BlockPos selected = blockHitResult.getBlockPos();
            if (level.getBlockState(selected).canBeReplaced()) {
                return;
            }
            if (!ShaftBlock.isShaft(level.getBlockState(selected))) {
                selected = selected.relative(blockHitResult.getDirection());
            }

            Direction.Axis beltAxis = beltAxisForShaft(shaftAxis);
            if (beltAxis == null) {
                sparkleFirstShaft(level, first);
                return;
            }

            BlockPos projected = projectSelection(first, selected, beltAxis);
            if (projected.equals(first)) {
                sparkleFirstShaft(level, first);
                return;
            }

            boolean canConnect = selected.equals(projected)
                    && ConveyorBeltItem.validateShaft(level, selected)
                    && ConveyorBeltItem.canConnect(level, first, selected);
            spawnLine(level, first, projected, beltAxis, canConnect);
            return;
        }
    }

    private static Direction.Axis beltAxisForShaft(Direction.Axis shaftAxis) {
        if (shaftAxis == Direction.Axis.X) {
            return Direction.Axis.Z;
        }
        if (shaftAxis == Direction.Axis.Z) {
            return Direction.Axis.X;
        }
        return null;
    }

    private static BlockPos projectSelection(BlockPos first, BlockPos selected, Direction.Axis beltAxis) {
        int delta = beltAxis.choose(selected.getX() - first.getX(), 0, selected.getZ() - first.getZ());
        int clampedDelta = Math.max(-ConveyorBeltItem.MAX_BELT_LENGTH + 1,
                Math.min(ConveyorBeltItem.MAX_BELT_LENGTH - 1, delta));
        if (beltAxis == Direction.Axis.X) {
            return new BlockPos(first.getX() + clampedDelta, first.getY(), first.getZ());
        }
        return new BlockPos(first.getX(), first.getY(), first.getZ() + clampedDelta);
    }

    private static void spawnLine(Level level, BlockPos first, BlockPos projected, Direction.Axis beltAxis,
                                  boolean canConnect) {
        RandomSource random = level.random;
        int delta = beltAxis.choose(projected.getX() - first.getX(), 0, projected.getZ() - first.getZ());
        int step = Integer.signum(delta);
        int length = Math.abs(delta);
        Vec3 start = Vec3.atLowerCornerOf(first);
        Vec3 direction = beltAxis == Direction.Axis.X ? new Vec3(step, 0, 0) : new Vec3(0, 0, step);
        DustParticleOptions particle = canConnect ? VALID_PARTICLE : INVALID_PARTICLE;

        for (float f = 0; f <= length; f += .0625f) {
            if (random.nextInt(10) != 0) {
                continue;
            }
            Vec3 position = start.add(direction.scale(f));
            level.addParticle(particle, position.x + .5f, position.y + .5f, position.z + .5f, 0, 0, 0);
        }
    }

    private static void sparkleFirstShaft(Level level, BlockPos first) {
        RandomSource random = level.random;
        if (random.nextInt(50) != 0) {
            return;
        }
        level.addParticle(VALID_PARTICLE,
                first.getX() + .5f + randomOffset(random, .25f),
                first.getY() + .5f + randomOffset(random, .25f),
                first.getZ() + .5f + randomOffset(random, .25f),
                0, 0, 0);
    }

    private static float randomOffset(RandomSource random, float range) {
        return (random.nextFloat() - .5f) * 2 * range;
    }
}
