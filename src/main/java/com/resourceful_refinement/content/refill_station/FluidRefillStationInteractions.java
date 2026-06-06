package com.resourceful_refinement.content.refill_station;

import com.resourceful_refinement.content.hosegun.HosegunItem;
import com.resourceful_refinement.content.hosegun.HosegunTracking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

/**
 * Drains the refill station tank into a held fluid-container item. Never transfers fluid into the station.
 */
public final class FluidRefillStationInteractions {

    private FluidRefillStationInteractions() {}

    public static boolean canRefillFromItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() == 1 && FluidUtil.getFluidHandler(stack).isPresent();
    }

    public static boolean isRefillStation(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FluidRefillStationBlockEntity;
    }

    /**
     * When true, {@link HosegunItem#use} must not start the bow-style spray (binding uses sneak-use on the block).
     */
    public static boolean shouldSuppressHosegunUse(Player player, Level level) {
        if (!player.isShiftKeyDown()) {
            return false;
        }
        HitResult hit = player.pick(player.blockInteractionRange(), 0f, false);
        if (hit instanceof BlockHitResult blockHit) {
            return isRefillStation(level, blockHit.getBlockPos());
        }
        return false;
    }

    /**
     * Sneak-use with a hosegun on a refill station at {@code pos}.
     *
     * @return {@code true} if handled on the server (including the no-tracking-id message)
     */
    public static boolean tryToggleHosegunBindingAt(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof FluidRefillStationBlockEntity station)) {
            return false;
        }
        return tryToggleHosegunBinding(level, station, player, hand);
    }

    /**
     * Sneak-use with a hosegun on a labelled station: bind to the station's Tracking ID, or unbind if already matched.
     *
     * @return {@code true} if this interaction was handled (including failure feedback when the station has no ID)
     */
    public static boolean tryToggleHosegunBinding(Level level, FluidRefillStationBlockEntity station, Player player,
            InteractionHand hand) {
        if (level.isClientSide) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof HosegunItem) || !player.isShiftKeyDown()) {
            return false;
        }

        if (!station.hasTrackingId()) {
            player.displayClientMessage(
                    Component.translatable("message.resourceful_refinement.refill_station.no_tracking_id"), true);
            playBindingSound(level, station);
            return true;
        }

        String stationId = station.getTrackingId();
        if (HosegunTracking.isBoundTo(stack, stationId)) {
            HosegunTracking.clearTrackingId(stack);
            player.displayClientMessage(
                    Component.translatable("message.resourceful_refinement.refill_station.hosegun_unbound", stationId),
                    true);
        } else {
            HosegunTracking.setTrackingId(stack, stationId);
            player.displayClientMessage(
                    Component.translatable("message.resourceful_refinement.refill_station.hosegun_bound", stationId),
                    true);
        }

        playBindingSound(level, station);
        player.getInventory().setChanged();
        return true;
    }

    /**
     * @return {@code true} if fluid was moved from the station into the held item
     */
    public static boolean tryFillHeldItem(Level level, BlockEntity be, Player player, InteractionHand hand) {
        if (level.isClientSide || !(be instanceof FluidRefillStationBlockEntity station)) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.getCount() != 1) {
            return false;
        }

        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(stack).orElse(null);
        if (itemHandler == null || station.tank.isEmpty()) {
            return false;
        }

        FluidStack stationFluid = station.tank.getFluid();
        FluidStack itemFluid = getStoredFluid(itemHandler);

        if (!itemFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(itemFluid, stationFluid)) {
            return false;
        }

        int stationAmount = station.tank.getFluidAmount();
        FluidStack offer = stationFluid.copy();
        offer.setAmount(stationAmount);

        int accepted = itemHandler.fill(offer, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return false;
        }

        int toTransfer = resolveTransferAmount(stack, itemHandler, stationFluid, stationAmount, accepted, itemFluid);
        if (toTransfer <= 0) {
            return false;
        }

        FluidStack executeStack = stationFluid.copy();
        executeStack.setAmount(toTransfer);
        int filled = itemHandler.fill(executeStack, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            return false;
        }

        station.tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);

        ItemStack result = itemHandler.getContainer();
        player.setItemInHand(hand, result);
        playFillSound(level, be);
        player.getInventory().setChanged();
        return true;
    }

    /**
     * Dynamic containers (hosegun) take whatever fits. Discrete containers require enough station fluid for the
     * full intended fill in one interaction (e.g. a full bucket amount when empty).
     */
    private static int resolveTransferAmount(ItemStack stack, IFluidHandlerItem itemHandler, FluidStack stationFluid,
            int stationAmount, int simulatedAccepted, FluidStack itemFluid) {
        if (isDynamicContainer(stack)) {
            return simulatedAccepted;
        }

        if (itemFluid.isEmpty()) {
            int unitSize = simulateMaxFill(itemHandler, stationFluid);
            if (unitSize <= 0 || stationAmount < unitSize) {
                return 0;
            }
            return unitSize;
        }

        if (stationAmount < simulatedAccepted) {
            return 0;
        }
        return simulatedAccepted;
    }

    private static boolean isDynamicContainer(ItemStack stack) {
        return stack.getItem() instanceof HosegunItem;
    }

    private static int simulateMaxFill(IFluidHandlerItem handler, FluidStack reference) {
        return handler.fill(new FluidStack(reference.getFluid(), Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE);
    }

    @Nullable
    private static FluidStack getStoredFluid(IFluidHandlerItem handler) {
        FluidStack found = FluidStack.EMPTY;
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack inTank = handler.getFluidInTank(i);
            if (!inTank.isEmpty()) {
                if (found.isEmpty()) {
                    found = inTank;
                } else if (!FluidStack.isSameFluidSameComponents(found, inTank)) {
                    return inTank;
                }
            }
        }
        return found;
    }

    private static void playFillSound(Level level, BlockEntity be) {
        level.playSound(null, be.getBlockPos(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void playBindingSound(Level level, FluidRefillStationBlockEntity station) {
        level.playSound(null, station.getBlockPos(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.55F,
                1.25F);
    }
}
