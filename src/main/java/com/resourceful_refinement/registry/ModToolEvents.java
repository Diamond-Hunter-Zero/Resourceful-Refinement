package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.coating.CoatingData;
import com.resourceful_refinement.content.coating.CoatingType;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
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
        
        if (!stack.has(ModDataComponents.COATING_DATA.get())) return;

        CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
        if (data == null) return;

        Level level = (Level) event.getLevel();
        // Conduction: Small chance to spawn lightning nearby
        if (data.type() == CoatingType.CONDUCTION)
            SpawnConductionLightning(player, level, event.getPos().getBottomCenter());
        else if (data.type() == CoatingType.GLOOPY)
            SpawnGloopSplatter(player, level, player.position(), 1, 0.16f);

    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        // Get the entity being attacked and the player attacking
        var target = event.getTarget();
        var player = event.getEntity();
        ItemStack stack = player.getMainHandItem();

        if (player.level().isClientSide()) return;

        if (!stack.has(ModDataComponents.COATING_DATA.get())) return;

        CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
        if (data == null) return;

        // Run coating behaviours
        if (data.type() == CoatingType.CONDUCTION)
            SpawnConductionLightning(player, player.level(), target.position());

        else if (data.type() == CoatingType.GLOOPY)
            SpawnGloopSplatter(player, player.level(), target.position(), 3, 0.07f);
    }

    private static void SpawnConductionLightning(Player player, Level level, Vec3 eventPos)
    {
        if (player.getRandom().nextFloat() < 0.04f) { // 4% chance
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                double ranPosX = player.getRandom().nextInt(3) + 1;
                if (player.getRandom().nextBoolean())
                    ranPosX *= -1;

                double ranPosZ = player.getRandom().nextInt(3) + 1;
                if (player.getRandom().nextBoolean())
                    ranPosZ *= -1;

                lightning.moveTo(eventPos.add(ranPosX, 0, ranPosZ));
                level.addFreshEntity(lightning);
            }
        }
    }

    private static void SpawnGloopSplatter(Player player, Level level, Vec3 eventPos, int radius, float successChance)
    {
        if (player.getRandom().nextFloat() < successChance) {
            BlockPos centerPos = BlockPos.containing(eventPos);

            // Try to place the core block directly underneath/at the target position
            tryPlaceGel(level, centerPos, 0, 0);

            // Generate a 'splatter' pattern around the target
            int splatterAttempts = 6; // Number of outer droplets to attempt
            for (int i = 0; i < splatterAttempts; i++) {
                // Random offset within a radius (-radius to radius)
                int offsetX = player.getRandom().nextInt(radius * 2) - radius;
                int offsetZ = player.getRandom().nextInt(radius * 2) - radius;

                // Skip the center
                if (offsetX == 0 && offsetZ == 0) continue;

                // Give each outer droplet a 60% chance to succeed
                if (player.getRandom().nextFloat() < 0.60f) {
                    tryPlaceGel(level, centerPos, offsetX, offsetZ);
                }
            }
        }
    }

    private static void tryPlaceGel(Level level, BlockPos centerPos, int offsetX, int offsetZ) {
        BlockPos columnPos = centerPos.offset(offsetX, 0, offsetZ);

        // Scan a small vertical window (+2 to -3 blocks) to adapt perfectly to slopes/uneven ground
        for (int yOffset = 2; yOffset >= -3; yOffset--) {
            BlockPos checkPos = columnPos.above(yOffset);
            BlockState currentFloorState = level.getBlockState(checkPos);
            BlockState blockBelowState = level.getBlockState(checkPos.below());

            // The target position must be empty (Air) or replaceable (like tall grass, vines, etc.)
            boolean isReplaceable = currentFloorState.isAir() || currentFloorState.canBeReplaced();

            // The block directly beneath it must have a sturdy solid upper surface (to support the gel)
            boolean hasSolidSurfaceBelow = blockBelowState.isFaceSturdy(level, checkPos.below(), Direction.UP);

            if (isReplaceable && hasSolidSurfaceBelow) {
                BlockState gelState = ModBlocks.GEL_SPLATTER_STICKY.get().defaultBlockState();

                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = checkPos.relative(direction);
                    BlockState neighbourState = level.getBlockState(neighborPos);
                    if (MultifaceBlock.canAttachTo(level, direction, neighborPos, neighbourState)) {
                        gelState = gelState.setValue(MultifaceBlock.getFaceProperty(direction), true);
                    }
                }

                level.setBlockAndUpdate(checkPos, gelState);
                break;
            }
        }
    }
}
