package com.resourceful_refinement.content.plunger;

import com.resourceful_refinement.registry.ModEntities;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class ThrownPlunger extends ThrownTrident {

    public ThrownPlunger(EntityType<? extends ThrownPlunger> type, Level level) {
        super(type, level);
        setBaseDamage(PlungerItem.THROW_DAMAGE);
    }

    public ThrownPlunger(Level level, LivingEntity owner, ItemStack stack) {
        this(ModEntities.THROWN_PLUNGER.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        setPickupItemStack(stack.copy());
        setBaseDamage(PlungerItem.THROW_DAMAGE);
    }

    @Override
    public ItemStack getWeaponItem() {
        ItemStack stack = getPickupItemStackOrigin();
        return stack == null || stack.isEmpty() ? new ItemStack(ModItems.PLUNGER.get()) : stack;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();

        // Force the base thrown damage to be 1.0F instead of vanilla's hardcoded 8.0F
        float damage = 1.0F;

        Level level = this.level();
        DamageSource damageSource = this.damageSources().trident(this, (Entity) (owner == null ? this : owner));

        if (level instanceof ServerLevel serverLevel) {
            damage = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), hitEntity, damageSource, damage);
        }

        //this.dealtDamage = true; // Sets the protected boolean from ThrownTrident

        if (hitEntity.hurt(damageSource, damage)) {
            if (hitEntity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (hitEntity instanceof LivingEntity livingEntity) {
                if (owner instanceof LivingEntity livingOwner) {
                    if (level instanceof ServerLevel serverLevel) {
                        EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
                    }
                }
                this.doPostHurtEffects(livingEntity);
            }
        }

        // Replicate standard trident impact physics and audio
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    /**
     * Fallback item type only — calls this during construction before
     * {@link #setPickupItemStack(ItemStack)} runs. Thrown stacks (coating, durability, etc.)
     * are preserved via the pickup stack set in the throw constructor.
     */
    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.PLUNGER.get());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return PlungerSounds.HIT_GROUND;
    }

    /** Exposes stuck state for the client renderer (same test as vanilla {@code ThrownTridentRenderer}). */
    public boolean isStuckInGround() {
        return shakeTime > 4 && inGround;
    }
}
