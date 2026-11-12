package com.resourceful_refinement.content.railgun;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.prism_cell.IPrismCellItemConsumer;
import com.resourceful_refinement.content.prism_cell.PrismCellBatteryComponent;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Prism-Cell-powered railgun. Holding use charges it for {@link #CHARGE_TICKS}; on completing the charge it fires a
 * piercing lightning beam along the reticle that lasts {@link #FIRE_TICKS}, tracks the reticle, and damages every
 * living entity on it each {@link #DAMAGE_INTERVAL_TICKS} (see {@link RailgunFiring}). Each shot costs
 * {@link ServerConfig#RAILGUN_CHARGE_COST} from the installed cell. Releasing early cancels the charge.
 *
 * <p>Holding use continuously re-charges straight after each shot, so the gun keeps spooled between shots.</p>
 */
public class RailgunItem extends Item implements IPrismCellItemConsumer {

    /** Charge time before firing (0.75s). The use duration, so vanilla completes the use on both sides. */
    public static final int CHARGE_TICKS = 15;
    /** How long the beam persists (1s). */
    public static final int FIRE_TICKS = 20;
    /** Interval between damage pulses while the beam persists (0.25s). */
    public static final int DAMAGE_INTERVAL_TICKS = 5;

    private static final int BAR_COLOR = 0x3AB3DA;

    public RailgunItem(Properties properties) {
        super(properties);
    }

    public static Item.Properties createProperties() {
        return new Item.Properties().stacksTo(1);
    }

    /** True while {@code entity} is charging a railgun. */
    public static boolean isCharging(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().getItem() instanceof RailgunItem;
    }

    // --- Charging / firing ----------------------------------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasChargeForShot(stack)) {
            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6F, 1.6F);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.6F, 1.6F);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHARGE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // NONE so the player renderer falls through to our CROSSBOW_HOLD arm pose (RailgunClientExtensions).
        return UseAnim.NONE;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            RailgunFiring.fire(player, stack, player.getUsedItemHand());
        }
        return stack;
    }

    /** Draining the cell rewrites the stack's components; keep charging/holding through that. */
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return !newStack.isEmpty() && newStack.getItem() == this;
    }

    /** Don't replay the equip animation when only the cell's charge changed. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public boolean hasChargeForShot(ItemStack stack) {
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        return battery != null && (battery.isCreative() || battery.charge() >= ServerConfig.RAILGUN_CHARGE_COST.get());
    }

    // --- Charge bar -----------------------------------------------------------------------------------------

    @Override
    public boolean isBarVisible(ItemStack stack) {
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        return battery != null && !battery.isCreative() && battery.maxCharge() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null || battery.maxCharge() <= 0) {
            return 0;
        }
        return Mth.clamp(Math.round(13.0F * battery.charge() / battery.maxCharge()), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    // --- Prism Cell exchange (bundle-style) -----------------------------------------------------------------

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action,
                                            Player player, SlotAccess access) {
        return handleOtherStackedOnMe(self, other, action, player);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player) {
        return handleStackedOnOther(self, slot, action, player);
    }

    // --- Tooltip --------------------------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null) {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.railgun.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        if (battery.isCreative()) {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.installed_creative")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.railgun.shots_infinite")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.installed",
                battery.charge(), battery.maxCharge()).withStyle(ChatFormatting.AQUA));
        int cost = ServerConfig.SPEC.isLoaded() ? ServerConfig.RAILGUN_CHARGE_COST.get() : 0;
        if (cost > 0) {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.railgun.shots", battery.charge() / cost)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
