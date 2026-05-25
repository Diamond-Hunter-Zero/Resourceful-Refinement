package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.coating.CoatingData;
import com.resourceful_refinement.content.coating.CoatingType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public class ModToolEvents {

    private static final ResourceLocation COATING_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "coating_damage");

    @SubscribeEvent
    public static void onAttribute(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.has(ModDataComponents.COATING_DATA.get())) {
            CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
            if (data == null) return;

            // Obsidianite: +2 Armor
            if (data.type() == CoatingType.OBSIDIANITE) {
                event.addModifier(Attributes.ARMOR, new AttributeModifier(COATING_DAMAGE_MODIFIER, 2.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.ANY);
            }
            
            // Durasteel: +0.25 Knockback Resistance
            if (data.type() == CoatingType.DURASTEEL) {
                event.addModifier(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(COATING_DAMAGE_MODIFIER, 0.25, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.ANY);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || player instanceof FakePlayer) return;

        if (player.level().isClientSide()) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        handleTickEffect(player, mainHand);
        handleTickEffect(player, offHand);
    }

    private static void handleTickEffect(Player player, ItemStack stack) {
        if (!stack.has(ModDataComponents.COATING_DATA.get())) return;
        CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
        if (data == null) return;

        // Quicksilver: Haste II
        if (data.type() == CoatingType.QUICKSILVER) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 10, 1, true, false));
        }

        // Uplift: Slow Fall
        if (data.type() == CoatingType.UPLIFT) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 0, true, false));
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        
        if (stack.has(ModDataComponents.COATING_DATA.get())) {
            CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
            if (data == null) return;

            // Conduction: Small chance to spawn lightning nearby
            if (data.type() == CoatingType.CONDUCTION) {
                if (player.getRandom().nextFloat() < 0.04f) { // 4% chance
                    net.minecraft.world.level.Level level = (net.minecraft.world.level.Level) event.getLevel();
                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                    if (lightning != null) {
                        double ranPosX = player.getRandom().nextInt(3) + 1;
                        if (player.getRandom().nextBoolean())
                            ranPosX *= -1;

                        double ranPosZ = player.getRandom().nextInt(3) + 1;
                        if (player.getRandom().nextBoolean())
                            ranPosZ *= -1;

                        lightning.moveTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(event.getPos()).add(ranPosX, 0, ranPosZ));
                        level.addFreshEntity(lightning);
                    }
                }
            }
        }
    }
}
