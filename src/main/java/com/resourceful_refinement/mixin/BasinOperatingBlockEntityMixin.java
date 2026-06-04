package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.hosegun.HosegunGloopyRecipes;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = BasinOperatingBlockEntity.class, remap = false)
public abstract class BasinOperatingBlockEntityMixin {

    @Shadow
    protected Recipe<?> currentRecipe;

    @Shadow
    protected abstract Optional<BasinBlockEntity> getBasin();

    @Inject(method = "applyBasinRecipe", at = @At("HEAD"), remap = false)
    private void resourceful_refinement$captureGloopyHosegun(CallbackInfo ci) {
        if (currentRecipe == null) {
            return;
        }

        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isEmpty() || basin.get().getLevel() == null) {
            return;
        }

        if (!HosegunGloopyRecipes.isUngloopRecipe(currentRecipe, basin.get().getLevel())) {
            return;
        }

        HosegunGloopyRecipes.captureGloopyInput(basin.get());
    }

    @Inject(method = "applyBasinRecipe", at = @At("TAIL"), remap = false)
    private void resourceful_refinement$ungloopHosegunAfterMixing(CallbackInfo ci) {
        if (currentRecipe == null) {
            return;
        }

        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isEmpty() || basin.get().getLevel() == null) {
            return;
        }

        if (!HosegunGloopyRecipes.isUngloopRecipe(currentRecipe, basin.get().getLevel())) {
            return;
        }

        HosegunGloopyRecipes.finishUngloop(basin.get());
    }
}
