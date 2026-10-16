package com.resourceful_refinement.content.hoverpack;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.prism_cell.IPrismCellItemConsumer;
import com.resourceful_refinement.content.prism_cell.PrismCellBatteryComponent;
import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Hoverpack: a Prism-Cell-powered alternative to the vanilla elytra. It equips in the chest slot (providing armor),
 * glides like an elytra for free, and — while a charged Prism Cell is installed — launches the wearer with the Launch
 * key, draining charge per launch (see {@link HoverpackLaunchHandler}).
 *
 * <p>Gliding is wired through NeoForge's {@link #canElytraFly}/{@link #elytraFlightTick} hooks rather than the vanilla
 * hardcoded elytra check, so this custom item flies. The installed cell is stored on the stack via
 * {@link IPrismCellItemConsumer} (bundle-style inventory exchange).</p>
 */
public class HoverpackItem extends Item implements Equipable, IPrismCellItemConsumer {

    /** Attribute-modifier id for the armor this grants while worn. */
    private static final ResourceLocation ARMOR_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "hoverpack_armor");

    /** Armor points provided while worn. */
    public static final double ARMOR_VALUE = 6.0;

    public HoverpackItem(Properties properties) {
        super(properties);
    }

    public static Item.Properties createProperties() {
        return new Item.Properties()
                .stacksTo(1)
                .durability(512)
                .attributes(createAttributes());
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(ARMOR_MODIFIER_ID, ARMOR_VALUE, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.CHEST
                )
                .build();
    }

    /** Whether this hoverpack still has durability to glide (mirrors {@code ElytraItem#isFlyEnabled}). */
    public static boolean isFlyEnabled(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    // --- Equipping (chest slot) -----------------------------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_ELYTRA;
    }

    // --- Gliding (NeoForge elytra hooks) --------------------------------------------------------------------

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return isFlyEnabled(stack);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        // Glide is free: unlike a vanilla elytra, no durability is consumed while flying (durability is only lost to
        // armor damage-on-hit). The glide game event is still emitted for parity (sculk sensors, etc.).
        if (!entity.level().isClientSide && (flightTicks + 1) % 10 == 0) {
            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
        return isFlyEnabled(stack);
    }

    // --- Repair ---------------------------------------------------------------------------------------------

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ModItems.DURASTEEL_SHEET.get());
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
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.hoverpack.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        if (battery.isCreative()) {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.installed_creative")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.installed",
                    battery.charge(), battery.maxCharge()).withStyle(ChatFormatting.AQUA));
        }
    }
}
