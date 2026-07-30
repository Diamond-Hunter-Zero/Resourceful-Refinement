package com.resourceful_refinement.content.sports_ball;

import com.resourceful_refinement.content.plushie.PlushieItemRenderer;
import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModEntities;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Carried form of the {@link SportsBallEntity}; right-click throws it back out into the world. */
public class SportsBallItem extends Item {

    /** Speed the ball leaves the hand at, along the player's look vector. */
    private static final double THROW_SPEED = 0.9D;
    /** How far in front of the eyes the ball spawns, to keep it clear of the thrower. */
    private static final double SPAWN_OFFSET = 0.5D;
    private static final int USE_COOLDOWN_TICKS = 4;

    public SportsBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return SportsBallItemRenderer.INSTANCE;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();

            SportsBallEntity ball = new SportsBallEntity(ModEntities.SPORTS_BALL.get(), level);
            ball.setPos(
                    player.getX() + look.x * SPAWN_OFFSET,
                    player.getEyeY() - SportsBallEntity.RADIUS,
                    player.getZ() + look.z * SPAWN_OFFSET
            );
            ball.setDeltaMovement(look.scale(THROW_SPEED));
            ball.hasImpulse = true;

            int type = 0;
            if (stack.has(ModDataComponents.BALL_TYPE.get()))
                type = stack.get(ModDataComponents.BALL_TYPE.get());
            ball.setBallType(type);

            level.addFreshEntity(ball);
            level.playSound(null, ball, SoundEvents.WOOL_HIT, SoundSource.PLAYERS, 0.7F,
                    1.1F + level.getRandom().nextFloat() * 0.2F);
        }

        player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.shrink(1);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
