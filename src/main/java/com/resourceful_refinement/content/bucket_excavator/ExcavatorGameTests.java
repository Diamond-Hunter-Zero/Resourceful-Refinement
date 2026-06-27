package com.resourceful_refinement.content.bucket_excavator;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(ResourcefulRefinementMain.MOD_ID)
public final class ExcavatorGameTests {
    private ExcavatorGameTests() {}

    @GameTest(template = "empty")
    public static void testExcavatorSpatialIndexing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basePos = helper.absolutePos(new BlockPos(5, 5, 5));
        DimensionalNodePos nodePos = DimensionalNodePos.of(level, basePos);

        // Initially no region contains this
        ExcavatorRegionSavedData.RemoveExcavator(level, nodePos);
        boolean initialCheck = ExcavatorRegionSavedData.IsBlockInsideExcavationRegion(level, nodePos);
        if (initialCheck) {
            throw new AssertionError("Initially there should be no excavation region containing this block");
        }

        // Register/Update excavator facing SOUTH
        ExcavatorRegionSavedData.RegisterOrUpdateExcavator(level, nodePos, Direction.SOUTH);

        // Fetch saved data and get the record to verify region extents
        ExcavatorRegionSavedData data = ExcavatorRegionSavedData.get(level);
        ExcavatorRegionSavedData.ExcavatorRecord record = data.excavators.get(nodePos);
        if (record == null || record.excavationRegion == null) {
            throw new AssertionError("Record or its excavation region should not be null after registration");
        }

        // Check a block pos inside the excavation region
        // The region is GetFaceExtendedRegion with width=1, height=5, depth=4
        BlockPos testInsidePos = basePos.relative(Direction.SOUTH, 2); // definitely inside the 4 depth
        DimensionalNodePos testInsideNode = DimensionalNodePos.of(level, testInsidePos);
        if (!ExcavatorRegionSavedData.IsBlockInsideExcavationRegion(level, testInsideNode)) {
            throw new AssertionError("Expected testInsideNode to be inside excavation region");
        }

        // Check a block pos outside the excavation region
        BlockPos testOutsidePos = basePos.relative(Direction.NORTH, 2); // outside
        DimensionalNodePos testOutsideNode = DimensionalNodePos.of(level, testOutsidePos);
        if (ExcavatorRegionSavedData.IsBlockInsideExcavationRegion(level, testOutsideNode)) {
            throw new AssertionError("Expected testOutsideNode to be outside excavation region");
        }

        // Update facing direction to NORTH
        ExcavatorRegionSavedData.RegisterOrUpdateExcavator(level, nodePos, Direction.NORTH);

        // Now testInsideNode (SOUTH) should be outside, and testOutsideNode (NORTH) should be inside!
        if (ExcavatorRegionSavedData.IsBlockInsideExcavationRegion(level, testInsideNode)) {
            throw new AssertionError("After rotating to NORTH, testInsideNode (SOUTH) should be outside");
        }
        if (!ExcavatorRegionSavedData.IsBlockInsideExcavationRegion(level, testOutsideNode)) {
            throw new AssertionError("After rotating to NORTH, testOutsideNode (NORTH) should be inside");
        }

        // Remove excavator and verify both are outside
        ExcavatorRegionSavedData.RemoveExcavator(level, nodePos);
        if (ExcavatorRegionSavedData.IsBlockInsideExcavationRegion(level, testOutsideNode)) {
            throw new AssertionError("After removing, testOutsideNode should be outside");
        }

        helper.succeed();
    }
}
