package com.resourceful_refinement.content.hosegun;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Applies potion gel impacts using vanilla splash/lingering/drink behaviour.
 */
public final class PotionGelImpactHandler {

    private static final double SPLASH_RANGE = ThrownPotion.SPLASH_RANGE;
    private static final double SPLASH_RANGE_SQ = SPLASH_RANGE * SPLASH_RANGE;

    private PotionGelImpactHandler() {}

    public static void handleBlockImpact(ServerLevel level, Vec3 impactPos, FluidStack fluidStack, @Nullable Entity owner) {
        Optional<CreatePotionFluidHelper.ResolvedPotionFluid> resolved = CreatePotionFluidHelper.resolve(fluidStack);
        if (resolved.isEmpty()) {
            return;
        }

        CreatePotionFluidHelper.ResolvedPotionFluid potion = resolved.get();
        switch (potion.bottleType()) {
            case SPLASH -> applySplash(level, impactPos, potion.contents(), null, owner);
            case LINGERING -> spawnAreaEffectCloud(level, impactPos, potion.contents(), owner);
            case REGULAR -> { /* no block impact for drinkable potions */ }
        }
    }

    public static void handleEntityImpact(ServerLevel level, LivingEntity target, Vec3 impactPos, FluidStack fluidStack,
            @Nullable Entity owner) {
        Optional<CreatePotionFluidHelper.ResolvedPotionFluid> resolved = CreatePotionFluidHelper.resolve(fluidStack);
        if (resolved.isEmpty()) {
            return;
        }

        CreatePotionFluidHelper.ResolvedPotionFluid potion = resolved.get();
        switch (potion.bottleType()) {
            case REGULAR -> applyDrinkEffects(target, potion.contents());
            case SPLASH -> applySplash(level, impactPos, potion.contents(), target, owner);
            case LINGERING -> spawnAreaEffectCloud(level, impactPos, potion.contents(), owner);
        }
    }

    private static void applyDrinkEffects(LivingEntity target, PotionContents contents) {
        for (MobEffectInstance effect : contents.getAllEffects()) {
            target.addEffect(new MobEffectInstance(effect));
        }
    }

    private static void applySplash(ServerLevel level, Vec3 center, PotionContents contents, @Nullable LivingEntity directTarget,
            @Nullable Entity owner) {
        playSplashSound(level, center);

        AABB area = new AABB(
                center.x - SPLASH_RANGE, center.y - 2.0D, center.z - SPLASH_RANGE,
                center.x + SPLASH_RANGE, center.y + 2.0D, center.z + SPLASH_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions);

        for (LivingEntity living : targets) {
            double distanceSq = living.distanceToSqr(center);
            if (distanceSq >= SPLASH_RANGE_SQ) {
                continue;
            }

            double factor = directTarget != null && living == directTarget
                    ? 1.0D
                    : 1.0D - Math.sqrt(distanceSq) / SPLASH_RANGE;
            applyScaledEffects(contents, living, factor, owner instanceof LivingEntity livingOwner ? livingOwner : null);
        }
    }

    private static void applyScaledEffects(PotionContents contents, LivingEntity target, double factor, @Nullable LivingEntity owner) {
        for (MobEffectInstance effect : contents.getAllEffects()) {
            MobEffect mobEffect = effect.getEffect().value();
            if (mobEffect.isInstantenous()) {
                mobEffect.applyInstantenousEffect(null, owner, target, effect.getAmplifier(), factor);
            } else {
                int duration = (int) (factor * effect.getDuration() + 0.5D);
                if (duration > 20) {
                    target.addEffect(new MobEffectInstance(
                            effect.getEffect(),
                            duration,
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible(),
                            effect.showIcon()));
                }
            }
        }
    }

    private static void spawnAreaEffectCloud(ServerLevel level, Vec3 center, PotionContents contents, @Nullable Entity owner) {
        playSplashSound(level, center);

        AreaEffectCloud cloud = new AreaEffectCloud(level, center.x, center.y, center.z);
        if (owner instanceof LivingEntity livingOwner) {
            cloud.setOwner(livingOwner);
        }
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.setPotionContents(contents);
        level.addFreshEntity(cloud);
    }

    private static void playSplashSound(ServerLevel level, Vec3 center) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }
}
