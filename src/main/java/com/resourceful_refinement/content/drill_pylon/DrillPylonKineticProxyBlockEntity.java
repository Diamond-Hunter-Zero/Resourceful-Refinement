package com.resourceful_refinement.content.drill_pylon;

import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrillPylonKineticProxyBlockEntity extends KineticBlockEntity {
    private BlockPos controllerPos = BlockPos.ZERO;
    private int dx;
    private int dy;
    private int dz;
    private BlockState storedState = Blocks.AIR.defaultBlockState();

    public DrillPylonKineticProxyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public DrillPylonKineticProxyBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.DRILL_PYLON_KINETIC_PROXY_BE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> behaviours) {
    }

    public void setControllerData(BlockPos controllerPos, int dx, int dy, int dz) {
        this.controllerPos = controllerPos;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        setChanged();
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDz() {
        return dz;
    }

    public BlockState getStoredState() {
        return storedState;
    }

    public void setStoredState(BlockState storedState) {
        this.storedState = storedState;
        setChanged();
    }

    public @Nullable DrillPylonHeadBlockEntity getController(Level level) {
        if (level == null || controllerPos.equals(BlockPos.ZERO)) return null;
        if (!level.isLoaded(controllerPos)) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof DrillPylonHeadBlockEntity controller ? controller : null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        DrillPylonHeadBlockEntity controller = getController(level);
        return controller != null && controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("ControllerPos", NbtUtils.writeBlockPos(controllerPos));
        tag.putInt("Dx", dx);
        tag.putInt("Dy", dy);
        tag.putInt("Dz", dz);
        tag.put("StoredState", NbtUtils.writeBlockState(storedState));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        controllerPos = NbtUtils.readBlockPos(tag, "ControllerPos").orElse(BlockPos.ZERO);
        dx = tag.getInt("Dx");
        dy = tag.getInt("Dy");
        dz = tag.getInt("Dz");
        if (tag.contains("StoredState")) {
            storedState = NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("StoredState"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        write(tag, registries, true);
        return tag;
    }
}
