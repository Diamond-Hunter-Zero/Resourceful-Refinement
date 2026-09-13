package com.resourceful_refinement.content.plunger;

import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryProxyBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryKineticProxyBlockEntity;
import com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlockEntity;

import java.util.IdentityHashMap;
import java.util.Map;

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

        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof RefineryAccessPortBlockEntity refinery) {
            addRefineryHandlers(refinery, handlers);
            return handlers;
        } else if (be instanceof RefineryProxyBlockEntity proxy) {
            RefineryAccessPortBlockEntity refinery = proxy.getController(level);
            if (refinery != null) {
                addRefineryHandlers(refinery, handlers);
                return handlers;
            }
        } else if (be instanceof RefineryKineticProxyBlockEntity proxy) {
            RefineryAccessPortBlockEntity refinery = proxy.getController(level);
            if (refinery != null) {
                addRefineryHandlers(refinery, handlers);
                return handlers;
            }
        } else if (be instanceof CombustionChamberBlockEntity chamber) {
            addCombustionChamberHandlers(chamber, handlers);
            return handlers;
        }
        else if (be instanceof DistilleryBlockEntity distillery) {
            addDistilleryHandlers(distillery, handlers);
            return handlers;
        }


        addHandlerIfFluid(level, pos, clickedFace, handlers);
        addHandlerIfFluid(level, pos, null, handlers);
        for (Direction direction : Direction.values()) {
            addHandlerIfFluid(level, pos, direction, handlers);
        }
        return handlers;
    }

    private static void addRefineryHandlers(RefineryAccessPortBlockEntity refinery, Map<IFluidHandler, Boolean> handlers) {
        if (!refinery.inputTankA.isEmpty()) {
            handlers.put(refinery.inputTankA, Boolean.TRUE);
        }
        if (!refinery.inputTankB.isEmpty()) {
            handlers.put(refinery.inputTankB, Boolean.TRUE);
        }
        if (!refinery.outputTank.isEmpty()) {
            handlers.put(refinery.outputTank, Boolean.TRUE);
        }
    }

    private static void addCombustionChamberHandlers(CombustionChamberBlockEntity chamber, Map<IFluidHandler, Boolean> handlers) {
        CombustionChamberBlockEntity controller = chamber.getController();
        if (controller != null) {
            for (CombustionChamberBlockEntity member : controller.getChainMembers()) {
                if (!member.inputTank.isEmpty()) {
                    handlers.put(member.inputTank, Boolean.TRUE);
                }
            }
        }
    }

    private static void addDistilleryHandlers(DistilleryBlockEntity distillery, Map<IFluidHandler, Boolean> handlers) {
        DistilleryBlockEntity controller = distillery.getController();
        if (controller != null ) {
            if (!controller.outputTank.isEmpty())
                handlers.put(controller.outputTank, Boolean.TRUE);
            if (!controller.inputTank.isEmpty())
                handlers.put(controller.inputTank, Boolean.TRUE);
        }
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
