package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT,
            ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PUG_LAUNCH = register("pug_launch");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUG_FLIGHT = register("pug_flight");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUG_LAND = register("pug_land");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUG_CRASH = register("pug_crash");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
