package com.resourceful_refinement.content.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Empties every fluid tank exposed by a block's {@link IFluidHandler} capabilities (all faces).
 */
public final class PlungerFluidInteractions {

    private PlungerFluidInteractions() {}

    public static boolean hasDrainableFluid(Level level, BlockPos pos, @Nullable Direction clickedFace) {
        return !collectHandlers(level, pos, clickedFace).isEmpty();
    }

    /** Drains all tanks on every distinct fluid handler reachable at {@code pos}. */
    public static boolean tryEmptyBlockTanks(Level level, BlockPos pos, @Nullable Direction clickedFace) {
        Map<IFluidHandler, Boolean> handlers = collectHandlers(level, pos, clickedFace);
        if (handlers.isEmpty()) {
            return false;
        }

        boolean drainedAny = false;
        for (IFluidHandler handler : handlers.keySet()) {
            drainedAny |= drainAllTanks(handler);
        }
        return drainedAny;
    }

    private static Map<IFluidHandler, Boolean> collectHandlers(Level level, BlockPos pos, @Nullable Direction clickedFace) {
        Map<IFluidHandler, Boolean> handlers = new IdentityHashMap<>();
        addHandlerIfFluid(level, pos, clickedFace, handlers);
        addHandlerIfFluid(level, pos, null, handlers);
        for (Direction direction : Direction.values()) {
            addHandlerIfFluid(level, pos, direction, handlers);
        }
        return handlers;
    }

    private static void addHandlerIfFluid(
            Level level,
            BlockPos pos,
            @Nullable Direction side,
            Map<IFluidHandler, Boolean> handlers
    ) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
        if (handler == null || handlers.containsKey(handler)) {
            return;
        }
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (!handler.getFluidInTank(tank).isEmpty()) {
                handlers.put(handler, Boolean.TRUE);
                return;
            }
        }
    }

    private static boolean drainAllTanks(IFluidHandler handler) {
        boolean drainedAny = false;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack fluid = handler.getFluidInTank(tank);
            while (!fluid.isEmpty()) {
                FluidStack drained = handler.drain(
                        new FluidStack(fluid.getFluid(), fluid.getAmount()),
                        IFluidHandler.FluidAction.EXECUTE
                );
                if (drained.isEmpty()) {
                    break;
                }
                drainedAny = true;
                fluid = handler.getFluidInTank(tank);
            }
        }
        return drainedAny;
    }
}
