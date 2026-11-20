package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ModDamageTypes {

    public static final ResourceKey<DamageType> MOLTEN_GEL_DAMAGE = key("molten_gel_damage");
    public static final ResourceKey<DamageType> RAILGUN = key("railgun");
    public static final ResourceKey<DamageType> TESLA_COIL = key("tesla_coil");

    private ModDamageTypes() {}

    public static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, path)
        );
    }

    public static DamageSource moltenGel(Level level) {
        return level.damageSources().source(MOLTEN_GEL_DAMAGE);
    }

    /** Molten gel from a projectile (e.g. hosegun blob); attributes kills to {@code owner} when present. */
    public static DamageSource moltenGel(Level level, Entity directEntity, @Nullable Entity owner) {
        return level.damageSources().source(MOLTEN_GEL_DAMAGE, directEntity, owner);
    }

    /** A railgun beam fired by {@code shooter}; attributes the kill to them. */
    public static DamageSource railgun(Level level, Entity shooter) {
        return level.damageSources().source(RAILGUN, shooter);
    }

    /** An arc between two Tesla Coils. */
    public static DamageSource teslaCoil(Level level) {
        return level.damageSources().source(TESLA_COIL);
    }
}
