package com.resourceful_refinement.content.plunger;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Distinct from vanilla trident audio; uses placeholder events until custom {@code .ogg} files are added.
 */
public final class PlungerSounds {

    private PlungerSounds() {}

    public static final SoundEvent THROW = SoundEvents.SLIME_JUMP;
    public static final SoundEvent HIT_GROUND = SoundEvents.WET_SPONGE_BREAK;
    public static final SoundEvent HIT_ENTITY = SoundEvents.SLIME_ATTACK;
    public static final SoundEvent EMPTY_TANK = SoundEvents.BOTTLE_EMPTY;
}
