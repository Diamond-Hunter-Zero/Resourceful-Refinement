package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.mixin.CatCollarInvoker;
import com.resourceful_refinement.mixin.WolfCollarInvoker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;

/**
 * Applies PAINT gel dye to dyeable entities (sheep wool, tamed wolf/cat collars).
 */
public final class PaintGelCollarHelper {

    private PaintGelCollarHelper() {}

    public static boolean tryDyeEntity(ServerLevel level, LivingEntity entity, DyeColor color) {
        if (color == null) {
            return false;
        }

        boolean dyed = switch (entity) {
            case Sheep sheep when sheep.getColor() != color -> {
                sheep.setColor(color);
                yield true;
            }
            case Wolf wolf when wolf.isTame() && wolf.getCollarColor() != color -> {
                ((WolfCollarInvoker) wolf).resourceful_refinement$setCollarColor(color);
                yield true;
            }
            case Cat cat when cat.isTame() && cat.getCollarColor() != color -> {
                ((CatCollarInvoker) cat).resourceful_refinement$setCollarColor(color);
                yield true;
            }
            default -> false;
        };

        if (dyed) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.DYE_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        return dyed;
    }
}
