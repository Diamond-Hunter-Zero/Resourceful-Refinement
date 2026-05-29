package com.resourceful_refinement.content.fluids;

import com.resourceful_refinement.content.gel_splatter.FluidGelTooltipHelper;
import com.resourceful_refinement.content.hosegun.*;
import com.resourceful_refinement.content.refill_station.FluidRefillStationInteractions;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import java.util.List;
import java.util.function.Consumer;

import static com.resourceful_refinement.content.gel_splatter.GelPropertiesManager.getGelAmmoCost;

public class PlungerItem extends Item {


    public PlungerItem(Properties properties) {
        super(properties.stacksTo(1));
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

}
