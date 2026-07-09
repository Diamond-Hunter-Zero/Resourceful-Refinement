package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,
            ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> TELEPORT_SICKNESS = EFFECTS.register(
            "teleportation_sickness", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x6C4AA3) {});

    private ModEffects() {}
}
