package com.resourceful_refinement.content.refinery;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class RefineryKineticProxyBlockEntity extends KineticBlockEntity {

    private BlockPos controllerPos = BlockPos.ZERO;
    private int dx, dy, dz;

    public RefineryKineticProxyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(java.util.List<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> behaviours) {
        // Behaviors are delegated to the controller
    }

    @Override
    public java.util.Collection<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> getAllBehaviours() {
        if (level != null && level.isClientSide) {
            RefineryAccessPortBlockEntity controller = getController(level);
            if (controller != null) {
                return controller.getAllBehaviours();
            }
        }
        return super.getAllBehaviours();
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerData(BlockPos pos, int dx, int dy, int dz) {
        this.controllerPos = pos;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        setChanged();
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDz() { return dz; }

    public @Nullable RefineryAccessPortBlockEntity getController(Level level) {
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof RefineryAccessPortBlockEntity controller) return controller;
        return null;
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("controllerPos", NbtUtils.writeBlockPos(controllerPos));
        tag.putInt("dx", dx);
        tag.putInt("dy", dy);
        tag.putInt("dz", dz);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("controllerPos")) {
            controllerPos = NbtUtils.readBlockPos(tag, "controllerPos").orElse(BlockPos.ZERO);
        }
        dx = tag.getInt("dx");
        dy = tag.getInt("dy");
        dz = tag.getInt("dz");
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        write(tag, registries, true);
        return tag;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking)
    {
        if (level == null) return false;
        RefineryAccessPortBlockEntity controller = getController(level);
        if (controller == null) return false;

        return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}
