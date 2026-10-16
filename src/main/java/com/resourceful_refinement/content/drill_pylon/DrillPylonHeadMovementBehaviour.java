package com.resourceful_refinement.content.drill_pylon;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.kinetics.drill.DrillMovementBehaviour;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Contraption behaviour for an unassembled Drill Pylon Head: a Create drill that clears a 3x3 face.
 * Breaking progress (and the contraption stall) is driven by the centre block exactly like the Create
 * Drill; when it breaks, the surrounding 8 blocks in the face plane break with it.
 */
public class DrillPylonHeadMovementBehaviour extends DrillMovementBehaviour {

    private static final String CENTRE_KEY = "DrillPylonFaceCentre";

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        // Remember the face centre, and if it's empty drive the break from another block in the face
        context.data.put(CENTRE_KEY, NbtUtils.writeBlockPos(pos));
        BlockPos target = pos;
        if (!context.world.isClientSide && !canBreak(context.world, pos, context.world.getBlockState(pos))) {
            Direction.Axis axis = getWorldFacingAxis(context);
            for (int a = -1; a <= 1 && target == pos; a++) {
                for (int b = -1; b <= 1; b++) {
                    BlockPos candidate = DrillPylonHeadBlockEntity.offsetInPlane(pos, axis, a, b);
                    if (canBreak(context.world, candidate, context.world.getBlockState(candidate))) {
                        target = candidate;
                        break;
                    }
                }
            }
        }
        super.visitNewPosition(context, target);
    }

    @Override
    protected void destroyBlock(MovementContext context, BlockPos pos) {
        super.destroyBlock(context, pos);

        BlockPos centre = NbtUtils.readBlockPos(context.data, CENTRE_KEY).orElse(pos);
        Direction.Axis axis = getWorldFacingAxis(context);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos target = DrillPylonHeadBlockEntity.offsetInPlane(centre, axis, a, b);
                if (target.equals(pos)) continue;
                if (canBreak(context.world, target, context.world.getBlockState(target))) {
                    super.destroyBlock(context, target);
                }
            }
        }
    }

    // Facing is stored in contraption-local space; rotate it into the world to find the face plane
    private static Direction.Axis getWorldFacingAxis(MovementContext context) {
        Direction localFacing = context.state.getValue(DrillPylonHeadBlock.FACING);
        Vec3 worldNormal = context.rotation.apply(Vec3.atLowerCornerOf(localFacing.getNormal()));
        return Direction.getNearest(worldNormal.x, worldNormal.y, worldNormal.z).getAxis();
    }

    // Drawn here with the head model rather than through the block entity renderer, so the bit is driven
    // by the contraption's motion instead of the stale kinetic speed stored on the moved block entity
    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        DrillPylonHeadRendering.renderInContraption(context, renderWorld, matrices, buffer);
    }

    @Override
    public @Nullable ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return null;
    }
}
