package com.resourceful_refinement.content.refinery;

import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utility class that handles validation, assembly, and disassembly
 * of the Fluid Refinery multiblock.
 * 
 * *
 * <h2>Structure (3×3, height
 * 3–{@link RefineryAccessPortBlockEntity#MAX_HEIGHT})</h2>
 *
 * <pre>
 * Bottom layer (y=0):
 *   col  -1   0   +1   (local X)
 * row -1: [BB] [CC] [BB]    ← "back" row
 * row  0: [CC] [CC] [CC]    ← middle row
 * row +1: [BB] [AP] [BB]    ← "front" row  (AP = Access Port, not replaced)
 *
 * Middle layers (y=1..h-2): repeated
 *   col  -1   0   +1
 * row -1: [GL] [GL] [GL]
 * row  0: [GL] [BL] [GL]    (BL = Blender Blade)
 * row +1: [GL] [GL] [GL]
 *
 * Top layer (y=h-1):
 *   col  -1   0   +1
 * row -1: [FT] [GL] [FT]    (FT = Fluid Tank)
 * row  0: [GL] [BL] [GL]
 * row +1: [IV] [GL] [IV]    (IV = Item Vault)
 * </pre>
 */
public class RefineryStructureHelper {

    // -------------------------------------------------------------------------
    // Public result type
    // -------------------------------------------------------------------------
    public record AssemblyResult(boolean success, int height, String reason) {
        public static AssemblyResult fail(String reason) {
            return new AssemblyResult(false, 0, reason);
        }

        public static AssemblyResult ok(int height) {
            return new AssemblyResult(true, height, "");
        }
    }

    // -------------------------------------------------------------------------
    // Main assembly entry point
    // -------------------------------------------------------------------------
    /**
     * Tries to assemble a valid 3×3 Fluid Refinery at {@code controllerPos}.
     * On success, replaces all non-controller blocks with proxy blocks and
     * notifies the controller BE.
     */
    public static AssemblyResult tryAssemble(Level level, BlockPos controllerPos, Direction facing) {
        // --- Determine valid height ---
        int height = detectHeight(level, controllerPos, facing);
        if (height < 3) {
            return AssemblyResult.fail("Structure incomplete or too short (need ≥3 layers)");
        }

        // --- Validate every layer ---
        String bottomErr = validateBottomLayer(level, controllerPos, facing);
        if (bottomErr != null)
            return AssemblyResult.fail("Bottom layer: " + bottomErr);

        for (int y = 1; y <= height - 2; y++) {
            String midErr = validateMiddleLayer(level, controllerPos, facing, y);
            if (midErr != null)
                return AssemblyResult.fail("Layer " + y + ": " + midErr);
        }

        String topErr = validateTopLayer(level, controllerPos, facing, height - 1);
        if (topErr != null)
            return AssemblyResult.fail("Top layer: " + topErr);

        // --- Replace blocks with proxies, recording originals ---
        List<BlockPos> proxyPositions = new ArrayList<>();
        List<BlockState> originalStates = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = 0; dz <= 2; dz++) {
                    if (y == 0 && dx == 0 && dz == 0)
                        continue; // skip the Access Port itself

                    boolean isCenterColumn = (dx == 0 && dz == 1);
                    BlockState proxyState = isCenterColumn ? ModBlocks.REFINERY_KINETIC_PROXY.get().defaultBlockState() : ModBlocks.REFINERY_PROXY.get().defaultBlockState();

                    BlockPos world = toWorldPos(controllerPos, facing, dx, y, dz);
                    BlockState orig = level.getBlockState(world);

                    proxyPositions.add(world);
                    originalStates.add(orig);

                    level.setBlock(world, proxyState, 3);
                    level.sendBlockUpdated(world, orig, proxyState, 3);

                    // Initialise the proxy BE with the controller's position and local offsets
                    BlockEntity be = level.getBlockEntity(world);
                    if (be instanceof RefineryProxyBlockEntity proxy) {
                        proxy.setControllerData(controllerPos, dx, y, dz);
                    } else if (be instanceof RefineryKineticProxyBlockEntity kineticProxy) {
                        kineticProxy.setControllerData(controllerPos, dx, y, dz);
                    }

                    // Invalidate capability cache so neighbors (like pipes) re-check for handlers
                    level.invalidateCapabilities(world);
                    // Now that the BE is ready, notify neighbors to check for connections
                    level.updateNeighborsAt(world, proxyState.getBlock());
                }
            }
        }

        // --- Notify controller ---
        if (level.getBlockEntity(controllerPos) instanceof RefineryAccessPortBlockEntity controller) {
            controller.onAssembled(height, proxyPositions, originalStates);
            // Set blockstate to assembled
            level.setBlock(controllerPos, level.getBlockState(controllerPos).setValue(RefineryAccessPortBlock.ASSEMBLED, true), 3);
        }

        return AssemblyResult.ok(height);
    }

    // -------------------------------------------------------------------------
    // Disassembly
    // -------------------------------------------------------------------------
    /**
     * Replaces all proxy blocks in the refinery with their original BlockStates
     * and clears the controller's assembled state.
     */
    public static void disassemble(Level level, RefineryAccessPortBlockEntity controller) {
        List<BlockPos> positions = controller.getProxyPositions();
        List<BlockState> originals = controller.getOriginalStates();

        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            BlockState current = level.getBlockState(pos);
            BlockEntity be = level.getBlockEntity(pos);

            boolean isProxy = current.is(ModBlocks.REFINERY_PROXY.get()) || current.is(ModBlocks.REFINERY_KINETIC_PROXY.get());
            boolean isProxyBE = be instanceof RefineryProxyBlockEntity || be instanceof RefineryKineticProxyBlockEntity;

            // Restore if it's currently a proxy block or has a proxy block entity (e.g. if currently being broken)
            if (isProxy || isProxyBE) {
                BlockState original = originals.get(i);
                if (be != null) {
                    level.removeBlockEntity(pos);
                }
                level.setBlock(pos, original, 3);
                level.sendBlockUpdated(pos, current, original, 3);
                level.updateNeighborsAt(pos, original.getBlock());
            }
        }

        controller.onDisassembled();
        // Also update the controller itself to ensure client-sync of the 'assembled' flag
        BlockState ctrlState = level.getBlockState(controller.getBlockPos());
        if (ctrlState.hasProperty(RefineryAccessPortBlock.ASSEMBLED)) {
            ctrlState = ctrlState.setValue(RefineryAccessPortBlock.ASSEMBLED, false);
            level.setBlock(controller.getBlockPos(), ctrlState, 3);
        }
        level.sendBlockUpdated(controller.getBlockPos(), ctrlState, ctrlState, 3);
        level.updateNeighborsAt(controller.getBlockPos(), ctrlState.getBlock());
    }

    // -------------------------------------------------------------------------
    // Height detection
    // -------------------------------------------------------------------------
    private static int detectHeight(Level level, BlockPos controllerPos, Direction facing) {
        int maxH = RefineryAccessPortBlockEntity.MAX_HEIGHT;
        String validationErrorMsg = null;
        for (int h = maxH; h >= 3; h--) {
            boolean valid = true;
            // middle layers
            for (int y = 1; y <= h - 2 && valid; y++) {
                validationErrorMsg = validateMiddleLayer(level, controllerPos, facing, y);
                valid = validationErrorMsg == null;
            }
            // top layer
            if (valid && validateTopLayer(level, controllerPos, facing, h - 1) == null) {
                return h;
            }
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Layer validators
    // -------------------------------------------------------------------------

    private static String validateBottomLayer(Level level, BlockPos ctrl, Direction facing) {
        // dz=0: Front row (BB, AP, BB)
        // dz=1: Mid row (CC, CC, CC)
        // dz=2: Back row (BB, CC, BB)
        int[][] bb = { { -1, 0 }, { 1, 0 }, { -1, 2 }, { 1, 2 } };
        int[][] cc = { { -1, 1 }, { 0, 1 }, { 1, 1 }, { 0, 2 } };

        for (int[] off : bb) {
            BlockPos world = toWorldPos(ctrl, facing, off[0], 0, off[1]);
            if (!isBlazeBurner(level, world))
                return "Expected Empty Blaze Burner at " + world.toShortString();
        }
        for (int[] off : cc) {
            BlockPos world = toWorldPos(ctrl, facing, off[0], 0, off[1]);
            if (!isCopperCasing(level, world))
                return "Expected Copper Casing at " + world.toShortString();
        }
        return null;
    }

    private static String validateMiddleLayer(Level level, BlockPos ctrl, Direction facing, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                BlockPos world = toWorldPos(ctrl, facing, dx, y, dz);
                if (dx == 0 && dz == 1) { // Center
                    if (!isBlenderBlade(level, world))
                        return "Expected Blender Blade at center (" + world.toShortString() + ")";
                } else {
                    if (!isGlass(level, world))
                        return "Expected glass at " + world.toShortString();
                }
            }
        }
        return null;
    }

    private static String validateTopLayer(Level level, BlockPos ctrl, Direction facing, int y) {
        // dz=0 (Front): IV, GL, IV
        // dz=1 (Mid): GL, BL, GL
        // dz=2 (Back): FT, GL, FT
        int[][] ft = { { -1, 2 }, { 1, 2 } };
        int[][] iv = { { -1, 0 }, { 1, 0 } };
        int[][] gl = { { 0, 0 }, { -1, 1 }, { 1, 1 }, { 0, 2 } };

        for (int[] off : ft) {
            BlockPos world = toWorldPos(ctrl, facing, off[0], y, off[1]);
            if (!isFluidTank(level, world))
                return "Expected Fluid Tank at " + world.toShortString();
        }
        for (int[] off : iv) {
            BlockPos world = toWorldPos(ctrl, facing, off[0], y, off[1]);
            if (!isItemVault(level, world))
                return "Expected Item Vault at " + world.toShortString();
        }
        for (int[] off : gl) {
            BlockPos world = toWorldPos(ctrl, facing, off[0], y, off[1]);
            if (!isGlass(level, world))
                return "Expected glass at " + world.toShortString();
        }
        BlockPos centreTop = toWorldPos(ctrl, facing, 0, y, 1);
        if (!isBlenderBlade(level, centreTop))
            return "Expected Blender Blade at top center (" + centreTop.toShortString() + ")";
        return null;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean isBlazeBurner(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return AllBlocks.BLAZE_BURNER.has(state);   // || AllBlocks.LIT_BLAZE_BURNER.has(state);
    }

    private static boolean isCopperCasing(Level level, BlockPos pos) {
        return AllBlocks.COPPER_CASING.has(level.getBlockState(pos));
    }

    private static boolean isGlass(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(net.minecraft.tags.BlockTags.IMPERMEABLE) || state.is(Blocks.GLASS)
                || state.is(Blocks.TINTED_GLASS);
    }

    private static boolean isBlenderBlade(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.BLENDER_BLADE.get());
    }

    private static boolean isFluidTank(Level level, BlockPos pos) {
        return AllBlocks.FLUID_TANK.has(level.getBlockState(pos));
    }

    private static boolean isItemVault(Level level, BlockPos pos) {
        return AllBlocks.ITEM_VAULT.has(level.getBlockState(pos));
    }

    /**
     * Converts a local (dx, dy, dz) offset relative to the Access Port into world
     * pos.
     * dz=0 is the port row, dz=1 is center row, dz=2 is back row.
     */
    public static BlockPos toWorldPos(BlockPos ctrl, Direction facing, int dx, int dy, int dz) {
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();
        return ctrl.relative(back, dz).relative(right, dx).above(dy);
    }
}
