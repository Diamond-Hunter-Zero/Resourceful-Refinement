package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.coating.CoatingData;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
// Import removed

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @org.spongepowered.asm.mixin.injection.Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private <T> void resourceful_refinement$interceptDamage(net.minecraft.core.component.DataComponentType<T> type, T value, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<T> cir) {
        if (type == net.minecraft.core.component.DataComponents.DAMAGE) {
            ItemStack stack = (ItemStack) (Object) this;
            
            if (stack.has(ModDataComponents.COATING_DATA.get())) {
                Integer newDamage = (Integer) value;
                Integer oldDamage = stack.get(net.minecraft.core.component.DataComponents.DAMAGE);
                int amount = newDamage - (oldDamage != null ? oldDamage : 0);

                if (amount > 0) {
                    //com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("[Coating Mixin] Intercepted DAMAGE component set! Amount: " + amount);
                    CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
                    if (data != null) {
                        int integrity = data.integrity() - amount;
                        //com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("[Coating Mixin] Redirecting to integrity: " + data.integrity() + " -> " + integrity);
                        
                        if (integrity > 0) {
                            stack.set(ModDataComponents.COATING_DATA.get(), new CoatingData(data.type(), integrity));
                            //com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("[Coating Mixin] Damage fully absorbed. Cancelling base damage.");
                            cir.setReturnValue(null); 
                        } else {
                            //com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("[Coating Mixin] Coating broken by component mutation!");
                            stack.remove(ModDataComponents.COATING_DATA.get());
                            // Value is allowed to proceed as the new DAMAGE value
                        }
                    }
                }
            }
        }
    }
}
