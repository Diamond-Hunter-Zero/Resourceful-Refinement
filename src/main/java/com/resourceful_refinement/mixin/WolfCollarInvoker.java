package com.resourceful_refinement.mixin;

import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Wolf.class)
public interface WolfCollarInvoker {

    @Invoker("setCollarColor")
    void resourceful_refinement$setCollarColor(DyeColor color);
}
