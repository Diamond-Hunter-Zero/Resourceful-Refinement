package com.resourceful_refinement.content.conveyor;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.manifold.ManifoldAssemblyAction;
import com.resourceful_refinement.content.manifold.ManifoldBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@GameTestHolder(ResourcefulRefinementMain.MOD_ID)
public final class ConveyorGameTests {
    private static final BlockPos ORIGIN = new BlockPos(2, 1, 2);
    private static final BlockPos SOURCE = ORIGIN.above();
    private static final BlockPos DESTINATION = SOURCE.east();
    private static final BlockPos WEST_DESTINATION = SOURCE.west();

    private ConveyorGameTests() {
    }

    @GameTest(template = "empty")
    public static void conveyorBeltMovesBlockAbove(GameTestHelper helper) {
        helper.setBlock(ORIGIN, ModBlocks.CONVEYOR_BELT.get().defaultBlockState()
                .setValue(ConveyorBeltBlock.HORIZONTAL_FACING, Direction.EAST)
                .setValue(ConveyorBeltBlock.PART, BeltPart.START));
        helper.setBlock(SOURCE, Blocks.DIRT.defaultBlockState());

        ConveyorBeltBlockEntity belt = (ConveyorBeltBlockEntity) helper.getBlockEntity(ORIGIN);
        belt.setController(helper.absolutePos(ORIGIN));
        belt.beltLength = 1;
        belt.index = 0;
        belt.tickBlockMovement(128);

        helper.succeedIf(() -> {
            helper.assertBlockPresent(Blocks.AIR, SOURCE);
            helper.assertBlockPresent(Blocks.DIRT, WEST_DESTINATION);
        });
    }

    @GameTest(template = "empty")
    public static void conveyorBeltAppliesNoStress(GameTestHelper helper) {
        helper.setBlock(ORIGIN, ModBlocks.CONVEYOR_BELT.get().defaultBlockState()
                .setValue(ConveyorBeltBlock.HORIZONTAL_FACING, Direction.EAST)
                .setValue(ConveyorBeltBlock.PART, BeltPart.START));

        ConveyorBeltBlockEntity belt = (ConveyorBeltBlockEntity) helper.getBlockEntity(ORIGIN);
        belt.setController(helper.absolutePos(ORIGIN));

        helper.succeedIf(() -> {
            if (belt.calculateStressApplied() != 0) {
                throw new GameTestAssertException("Conveyor belt applied stress: " + belt.calculateStressApplied());
            }
        });
    }

    @GameTest(template = "empty")
    public static void overlongConveyorBeltCreatePlacesNothing(GameTestHelper helper) {
        BlockPos start = ORIGIN;
        BlockPos end = ORIGIN.east(8);

        ConveyorBeltItem.createBelts(helper.getLevel(), helper.absolutePos(start), helper.absolutePos(end));

        helper.succeedIf(() -> {
            for (int i = 0; i <= 8; i++) {
                helper.assertBlockPresent(Blocks.AIR, start.east(i));
            }
        });
    }

    @GameTest(template = "empty")
    public static void conveyorBeltBreakRemovesWholeTrain(GameTestHelper helper) {
        BlockState baseState = ModBlocks.CONVEYOR_BELT.get().defaultBlockState()
                .setValue(ConveyorBeltBlock.HORIZONTAL_FACING, Direction.EAST);
        helper.setBlock(ORIGIN, baseState.setValue(ConveyorBeltBlock.PART, BeltPart.START));
        helper.setBlock(ORIGIN.east(), baseState.setValue(ConveyorBeltBlock.PART, BeltPart.MIDDLE));
        helper.setBlock(ORIGIN.east(2), baseState.setValue(ConveyorBeltBlock.PART, BeltPart.END));

        helper.getLevel().destroyBlock(helper.absolutePos(ORIGIN.east()), true);

        helper.succeedIf(() -> {
            helper.assertBlockPresent(Blocks.AIR, ORIGIN);
            helper.assertBlockPresent(Blocks.AIR, ORIGIN.east());
            helper.assertBlockPresent(Blocks.AIR, ORIGIN.east(2));
        });
    }

    @GameTest(template = "empty")
    public static void conveyorRotatorFacesAndMovesDirectionalBlock(GameTestHelper helper) {
        helper.setBlock(ORIGIN, ModBlocks.CONVEYOR_ROTATOR.get().defaultBlockState()
                .setValue(ConveyorRotatorBlock.HORIZONTAL_FACING, Direction.EAST));
        helper.setBlock(SOURCE, Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));

        ConveyorRotatorBlockEntity rotator = (ConveyorRotatorBlockEntity) helper.getBlockEntity(ORIGIN);
        rotator.setOutputDirection(Direction.EAST);

        for (int i = 0; i < 24; i++) {
            rotator.tickRotatorWork(128);
        }

        helper.succeedIf(() -> {
            helper.assertBlockPresent(Blocks.AIR, SOURCE);
            BlockState movedState = helper.getBlockState(DESTINATION);
            helper.assertBlockPresent(Blocks.OAK_STAIRS, DESTINATION);
            if (movedState.getValue(HorizontalDirectionalBlock.FACING) != Direction.EAST) {
                throw new GameTestAssertException("Rotated block did not face east after moving");
            }
        });
    }

    @GameTest(template = "empty")
    public static void conveyorRotatorUsesProxyDuringRotation(GameTestHelper helper) {
        helper.setBlock(ORIGIN, ModBlocks.CONVEYOR_ROTATOR.get().defaultBlockState()
                .setValue(ConveyorRotatorBlock.HORIZONTAL_FACING, Direction.EAST));
        helper.setBlock(SOURCE, Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));

        ConveyorRotatorBlockEntity rotator = (ConveyorRotatorBlockEntity) helper.getBlockEntity(ORIGIN);
        rotator.setOutputDirection(Direction.EAST);
        rotator.tickRotatorWork(16);

        helper.succeedIf(() -> {
            helper.assertBlockPresent(ModBlocks.CONVEYOR_ROTATOR_PROXY.get(), SOURCE);
            if (!rotator.hasRotationSession()) {
                throw new GameTestAssertException("Rotator did not keep an active rotation session");
            }
        });
    }

    @GameTest(template = "empty")
    public static void conveyorRotatorContinuesIfProxyRemoved(GameTestHelper helper) {
        helper.setBlock(ORIGIN, ModBlocks.CONVEYOR_ROTATOR.get().defaultBlockState()
                .setValue(ConveyorRotatorBlock.HORIZONTAL_FACING, Direction.EAST));
        helper.setBlock(SOURCE, Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));

        ConveyorRotatorBlockEntity rotator = (ConveyorRotatorBlockEntity) helper.getBlockEntity(ORIGIN);
        rotator.setOutputDirection(Direction.EAST);
        rotator.tickRotatorWork(128);
        helper.getLevel().destroyBlock(helper.absolutePos(SOURCE), false);

        for (int i = 0; i < 24; i++) {
            rotator.tickRotatorWork(128);
        }

        helper.succeedIf(() -> {
            helper.assertBlockPresent(Blocks.AIR, SOURCE);
            BlockState movedState = helper.getBlockState(DESTINATION);
            helper.assertBlockPresent(Blocks.OAK_STAIRS, DESTINATION);
            if (movedState.getValue(HorizontalDirectionalBlock.FACING) != Direction.EAST) {
                throw new GameTestAssertException("Rotator did not finish after proxy removal");
            }
        });
    }

    @GameTest(template = "empty")
    public static void conveyorRotatorUsesSignedRotationDistance(GameTestHelper helper) {
        BlockState northStairs = Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

        ConveyorRotatorTarget positiveTarget = ConveyorRotatorTarget.forState(northStairs, Direction.EAST, 128);
        ConveyorRotatorTarget negativeTarget = ConveyorRotatorTarget.forState(northStairs, Direction.EAST, -128);

        helper.succeedIf(() -> {
            assertDegrees(positiveTarget.requiredDegrees(), 270, "positive speed should rotate counter-clockwise");
            assertDegrees(negativeTarget.requiredDegrees(), -90, "negative speed should rotate clockwise");
        });
    }

    @GameTest(template = "empty")
    public static void conveyorRotatorFullyDirectionalUsesInitialOrientation(GameTestHelper helper) {
        BlockState northObserver = Blocks.OBSERVER.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.NORTH);
        BlockState upObserver = Blocks.OBSERVER.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.UP);

        ConveyorRotatorTarget horizontalTarget = ConveyorRotatorTarget.forState(northObserver, Direction.EAST, 128);
        ConveyorRotatorTarget verticalTarget = ConveyorRotatorTarget.forState(upObserver, Direction.EAST, 128);

        helper.succeedIf(() -> {
            assertDegrees(horizontalTarget.requiredDegrees(), 270,
                    "horizontal fully-directional blocks should not force a full spin");
            assertDegrees(verticalTarget.requiredDegrees(), 360,
                    "vertical fully-directional starts should retain full-spin visual handling");
        });
    }

    @GameTest(template = "empty")
    public static void conveyorMoverRejectsImmovableBlocks(GameTestHelper helper) {
        helper.setBlock(SOURCE, Blocks.OBSIDIAN.defaultBlockState());

        ConveyorMovementResult.FailureReason failureReason = ConveyorBlockMover.getSourceMovementFailure(
                helper.getLevel(), helper.absolutePos(SOURCE), Direction.EAST);

        helper.succeedIf(() -> {
            if (failureReason != ConveyorMovementResult.FailureReason.PISTON_RULE
                    && failureReason != ConveyorMovementResult.FailureReason.PUSH_REACTION_BLOCK) {
                throw new GameTestAssertException("Expected immovable block failure, got " + failureReason);
            }
        });
    }

    @GameTest(template = "empty")
    public static void conveyorMoverCarriesManifoldAssemblyRecord(GameTestHelper helper) {
        helper.setBlock(SOURCE, ModBlocks.MANIFOLD.get().defaultBlockState());
        ManifoldBlockEntity manifold = (ManifoldBlockEntity) helper.getBlockEntity(SOURCE);
        manifold.applyAssemblyAction(new ManifoldAssemblyAction.Etching());
        manifold.applyAssemblyAction(new ManifoldAssemblyAction.Fill(
                ResourceLocation.fromNamespaceAndPath("minecraft", "water")));
        String expectedHash = manifold.assemblyRecord().identityHash();

        ConveyorMovementResult result = ConveyorBlockMover.tryMoveBlock(helper.getLevel(), helper.absolutePos(SOURCE),
                Direction.EAST);

        helper.succeedIf(() -> {
            if (!result.moved()) {
                throw new GameTestAssertException("Expected manifold to move, got " + result.failureReason());
            }
            helper.assertBlockPresent(Blocks.AIR, SOURCE);
            helper.assertBlockPresent(ModBlocks.MANIFOLD.get(), DESTINATION);
            ManifoldBlockEntity movedManifold = (ManifoldBlockEntity) helper.getBlockEntity(DESTINATION);
            String actualHash = movedManifold.assemblyRecord().identityHash();
            if (!expectedHash.equals(actualHash)) {
                throw new GameTestAssertException("Manifold assembly record changed while moving");
            }
        });
    }

    private static void assertDegrees(float actual, float expected, String message) {
        if (Math.abs(actual - expected) > 0.01f) {
            throw new GameTestAssertException(message + ": expected " + expected + ", got " + actual);
        }
    }
}
