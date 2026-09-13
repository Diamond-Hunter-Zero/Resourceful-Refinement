package com.resourceful_refinement.content.radiator;

import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public record RadiatorVirtualHeatingRecord(TagKey<Fluid> fluidTag, ExtendedHeatCondition resultingHeat, Double consumptionRate)
{
}
