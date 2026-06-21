package com.resourceful_refinement.mixin;

import com.simibubi.create.impl.registry.SimpleRegistryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(value = SimpleRegistryImpl.class, remap = false)
public abstract class CreateSimpleRegistryMixin<K, V> {
    @Shadow
    protected Map<K, V> registrations;

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$allowIdempotentCreateRegistryRegistration(K object, V value, CallbackInfo ci) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(value, "value");

        if (registrations.containsKey(object)) {
            ci.cancel();
        }
    }
}
