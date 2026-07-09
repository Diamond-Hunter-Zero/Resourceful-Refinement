package com.resourceful_refinement.content.glare.common;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;

/** Three-position counterpart to Create's {@link ValueBoxTransform.Dual}. */
public abstract class Trio extends ValueBoxTransform {
    protected final int index;

    protected Trio(int index) {
        if (index < 0 || index > 2) throw new IllegalArgumentException("Trio slot index must be 0-2");
        this.index = index;
    }

    public int index() { return index; }

    public static <T extends Trio> List<T> makeSlots(Function<Integer, T> factory) {
        return List.of(factory.apply(0), factory.apply(1), factory.apply(2));
    }

    @Override public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
        Vec3 offset = getLocalOffset(level, pos, state);
        return offset != null && localHit.distanceTo(offset) < scale / 4.5F;
    }
}
