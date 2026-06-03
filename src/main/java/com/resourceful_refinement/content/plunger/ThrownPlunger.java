package com.resourceful_refinement.content.plunger;

import com.resourceful_refinement.registry.ModEntities;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

    /**
     * Fallback item type only — {@link AbstractArrow} calls this during construction before
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
