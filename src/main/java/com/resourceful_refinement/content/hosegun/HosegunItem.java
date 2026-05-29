package com.resourceful_refinement.content.hosegun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.gel_splatter.FluidGelTooltipHelper;
import com.resourceful_refinement.content.refill_station.FluidRefillStationInteractions;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static com.resourceful_refinement.content.gel_splatter.GelPropertiesManager.getGelAmmoCost;

public class HosegunItem extends Item {

    public static final int CAPACITY = 2000;

    public static final float GEL_BLOB_VELOCITY_FACTOR = 1.15f;
    public static final float HOSEGUN_INNACURACY = 4.5f;
    public static final int BOUND_TEXT_COLOUR = 0x9AE6B4;

    public HosegunItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** True while this entity is actively using a hosegun (spray held). */
    public static boolean isSpraying(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().getItem() instanceof HosegunItem;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return HosegunItemRenderer.INSTANCE;
            }
        });
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!FluidRefillStationInteractions.isRefillStation(level, context.getClickedPos())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            FluidRefillStationInteractions.tryToggleHosegunBindingAt(
                    level, context.getClickedPos(), player, context.getHand());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (FluidRefillStationInteractions.shouldSuppressHosegunUse(player, level)) {
            return InteractionResultHolder.fail(stack);
        }

        // Ensure player has enough fluid to fire
        HosegunFluidHandler fluidHandler = new HosegunFluidHandler(stack);
        FluidStack contained = fluidHandler.getFluid();
        
        if (!contained.isEmpty() && contained.getAmount() >= getGelAmmoCost(contained.getFluid())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // Large number for continuous firing while holding click
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (entity instanceof Player player) {
            HosegunFluidHandler fluidHandler = new HosegunFluidHandler(stack);
            FluidStack contained = fluidHandler.getFluid();
            
            if (contained.isEmpty() || contained.getAmount() < getGelAmmoCost(contained.getFluid())) {
                entity.releaseUsingItem();
                return;
            }

            if (!level.isClientSide && level instanceof ServerLevel server) {
                InteractionHand hand = player.getUsedItemHand();
                int ticksUsed = this.getUseDuration(stack, entity) - count;
                float progress = Math.min(1.0F, ticksUsed / 20.0F);
                float velocity = Mth.lerp(progress, 0.8F, 1.6F) * GEL_BLOB_VELOCITY_FACTOR;

                Vec3 sprayVelocity = HosegunParticleEffects.computeSprayVelocity(
                        player, velocity, HOSEGUN_INNACURACY, level.random);

                // Fire projectile every 2 ticks (continuous spray feel)
                if (ticksUsed % 2 == 0) {
                    Vec3 muzzle = HosegunShooting.getMuzzlePosition(player, hand);
                    GelBlobEntity projectile = new GelBlobEntity(
                            com.resourceful_refinement.registry.ModEntities.GEL_BLOB.get(),
                            muzzle.x, muzzle.y, muzzle.z,
                            level
                    );
                    projectile.setFluidStack(contained.copy());
                    HosegunTracking.getTrackingId(stack).ifPresent(projectile::setTrackingId);
                    projectile.setOwner(player);
                    projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, HOSEGUN_INNACURACY);
                    sprayVelocity = projectile.getDeltaMovement();
                    level.addFreshEntity(projectile);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.LAVA_POP, net.minecraft.sounds.SoundSource.PLAYERS,
                            0.5F, 1.2F + (level.random.nextFloat() * 0.2F));

                    fluidHandler.drain(getGelAmmoCost(contained.getFluid()), IFluidHandler.FluidAction.EXECUTE);
                }

                HosegunParticleEffects.spawnSprayStream(server, player, hand, contained, sprayVelocity, ticksUsed, level.random);
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidStack fluid = new HosegunFluidHandler(stack).getFluid();
        if (fluid.isEmpty()) {
            return super.getName(stack);
        }
        return Component.translatable("item.resourceful_refinement.hosegun").append( " [§3" + fluid.getHoverName().getString() + "§f]");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        HosegunFluidHandler fluidHandler = new HosegunFluidHandler(stack);
        FluidStack fluid = fluidHandler.getFluid();
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.hosegun.empty").withColor(0x7F7F7F));
        } else {
            FluidGelTooltipHelper.addItemFluidLines(tooltip, fluid, CAPACITY, 0x3AB3DA);
        }

        HosegunTracking.getTrackingId(stack).ifPresentOrElse(
                id -> tooltip.add(Component.translatable("tooltip.resourceful_refinement.hosegun.tracking_id", id)
                        .withColor(BOUND_TEXT_COLOUR)),
                () -> tooltip.add(Component.translatable("tooltip.resourceful_refinement.hosegun.tracking_id_unbound")
                        .withColor(0x7F7F7F))
        );
    }

    public static class HosegunFluidHandler extends FluidHandlerItemStack {
        public HosegunFluidHandler(ItemStack container) {
            super(ModDataComponents.HOSEGUN_FLUID, container, CAPACITY);
        }
    }
}
