package com.resourceful_refinement.content.plunger;

import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.List;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class PlungerItem extends TridentItem {

  /** Thrown and melee hit damage (0.5 hearts). */
  public static final float THROW_DAMAGE = 1.0F;

  public PlungerItem(Properties properties) {
    super(properties);
  }

  public static Item.Properties createProperties() {
    return new Item.Properties()
        .stacksTo(1)
        .durability(512)
        .attributes(createAttributes());
  }

  @Override
  public int getEnchantmentValue() {
    return 0;
  }

  public static ItemAttributeModifiers createAttributes() {
    return ItemAttributeModifiers.builder()
        .add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, THROW_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
        )
        .add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.9, AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
        )
        .build();
  }

  @Override
  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    consumer.accept(new IClientItemExtensions() {
      @Override
      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return PlungerItemRenderer.INSTANCE;
      }
    });
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.SPEAR;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    tooltip.add(Component.translatable("tooltip.resourceful_refinement.plunger").withColor(0x7F7F7F));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Player player = context.getPlayer();
    if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
      return InteractionResult.PASS;
    }

    Level level = context.getLevel();
    if (!PlungerFluidInteractions.hasDrainableFluid(level, context.getClickedPos(), context.getClickedFace())) {
      return InteractionResult.PASS;
    }

    if (level.isClientSide) {
      return InteractionResult.SUCCESS;
    }

    if (PlungerFluidInteractions.tryEmptyBlockTanks(level, context.getClickedPos(), context.getClickedFace())) {
      level.playSound(
          null,
          context.getClickedPos(),
          PlungerSounds.EMPTY_TANK,
          SoundSource.BLOCKS,
          0.9F,
          0.85F + level.random.nextFloat() * 0.2F
      );
      player.awardStat(Stats.ITEM_USED.get(this));
      player.swing(context.getHand(), true);
      return InteractionResult.CONSUME;
    }

    return InteractionResult.PASS;
  }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var result = super.use(level, player, hand);
        if (result.getResult().consumesAction()) {
            result.getObject().set(ModDataComponents.PLUNGER_CHARGING.get(), true);
        }
        return result;
    }

  @Override
  public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      stack.set(ModDataComponents.PLUNGER_CHARGING.get(), false);
    if (!(entity instanceof Player player)) {
      return;
    }

    int charge = getUseDuration(stack, entity) - timeLeft;
    if (charge < 10) {
      return;
    }

    if (!level.isClientSide) {
      stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));

      float power = Mth.clamp(charge / 20.0F, 0.0F, 1.0F);
      ThrownPlunger projectile = new ThrownPlunger(level, player, stack);
      projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * TridentItem.SHOOT_POWER, 1.0F);

      if (player.getAbilities().instabuild) {
        projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
      }

      level.addFreshEntity(projectile);
      level.playSound(null, projectile, PlungerSounds.THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

      if (!player.getAbilities().instabuild) {
        player.getInventory().removeItem(stack);
      }
    }

    player.awardStat(Stats.ITEM_USED.get(this));
  }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        // Fail-safe cleanup for charging data
        if (stack.getOrDefault(ModDataComponents.PLUNGER_CHARGING.get(), false)) {
            if (!(entity instanceof LivingEntity living) || !living.isUsingItem() || living.getUseItem() != stack) {
                stack.set(ModDataComponents.PLUNGER_CHARGING.get(), false);
            }
        }
    }
}
