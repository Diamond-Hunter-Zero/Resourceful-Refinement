package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.coating.CoatingData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT)
public class ModClientGameEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.has(ModDataComponents.COATING_DATA.get())) {
            CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
            if (data != null) {
                String name = data.type().getSerializedName();
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                event.getToolTip().add(Component.literal("Coating: " + name + " (" + data.integrity() + "/" + data.type().getMaxDurability() + ")").withColor(data.type().getColor()));
            }
        }
    }
}
