/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.util.StringRepresentable
 *  net.minecraft.world.level.block.LeavesBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.Heightmap
 *  net.minecraft.world.level.levelgen.Heightmap$Usage
 */
package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public static enum Heightmap.Types implements StringRepresentable
{
    WORLD_SURFACE_WG("WORLD_SURFACE_WG", Heightmap.Usage.WORLDGEN, Heightmap.NOT_AIR),
    WORLD_SURFACE("WORLD_SURFACE", Heightmap.Usage.CLIENT, Heightmap.NOT_AIR),
    OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Heightmap.Usage.WORLDGEN, Heightmap.MATERIAL_MOTION_BLOCKING),
    OCEAN_FLOOR("OCEAN_FLOOR", Heightmap.Usage.LIVE_WORLD, Heightmap.MATERIAL_MOTION_BLOCKING),
    MOTION_BLOCKING("MOTION_BLOCKING", Heightmap.Usage.CLIENT, p_284915_ -> p_284915_.blocksMotion() || !p_284915_.getFluidState().isEmpty()),
    MOTION_BLOCKING_NO_LEAVES("MOTION_BLOCKING_NO_LEAVES", Heightmap.Usage.LIVE_WORLD, p_284914_ -> (p_284914_.blocksMotion() || !p_284914_.getFluidState().isEmpty()) && !(p_284914_.getBlock() instanceof LeavesBlock));

    public static final Codec<Heightmap.Types> CODEC;
    private final String serializationKey;
    private final Heightmap.Usage usage;
    private final Predicate<BlockState> isOpaque;

    private Heightmap.Types(String serializationKey, Heightmap.Usage usage, Predicate<BlockState> isOpaque) {
        this.serializationKey = serializationKey;
        this.usage = usage;
        this.isOpaque = isOpaque;
    }

    public String getSerializationKey() {
        return this.serializationKey;
    }

    public boolean sendToClient() {
        return this.usage == Heightmap.Usage.CLIENT;
    }

    public boolean keepAfterWorldgen() {
        return this.usage != Heightmap.Usage.WORLDGEN;
    }

    public Predicate<BlockState> isOpaque() {
        return this.isOpaque;
    }

    public String getSerializedName() {
        return this.serializationKey;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Heightmap.Types::values);
    }
}
