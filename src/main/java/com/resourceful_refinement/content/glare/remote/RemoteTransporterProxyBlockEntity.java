package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.content.glare.IGlareNode;
import com.resourceful_refinement.content.glare.lux.LuxSocket;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RemoteTransporterProxyBlockEntity extends SmartBlockEntity implements LuxSocket, IHaveGoggleInformation {
    private BlockPos controllerPos = BlockPos.ZERO;

    public RemoteTransporterProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REMOTE_ENTANGLEMENT_TRANSPORTER_PROXY_BE.get(), pos, state);
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos.immutable();
        setChanged();
        sendData();
    }

    public BlockPos getControllerPos() { return controllerPos; }

    public @Nullable RemoteEntanglementTransporterBlockEntity getController(Level level) {
        BlockEntity blockEntity = level.getBlockEntity(controllerPos);
        return blockEntity instanceof RemoteEntanglementTransporterBlockEntity controller ? controller : null;
    }

    /** The tank proxy's top face is the transporter's Lux Socket; every other face/proxy is inert. */
    @Override public @Nullable IGlareNode getLuxSocketNode(Direction side) {
        if (level == null) return null;
        RemoteEntanglementTransporterBlockEntity controller = getController(level);
        return controller != null && controller.isLuxSocket(this, side) ? controller : null;
    }

    /** Every proxy shows the controller's goggle readout. */
    @Override public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        RemoteEntanglementTransporterBlockEntity controller = level == null ? null : getController(level);
        return controller != null && controller.addToGoggleTooltip(tooltip, sneaking);
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Controller", NbtUtils.writeBlockPos(controllerPos));
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        controllerPos = NbtUtils.readBlockPos(tag, "Controller").orElse(BlockPos.ZERO);
    }
}
