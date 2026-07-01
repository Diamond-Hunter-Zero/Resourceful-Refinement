package com.resourceful_refinement.content.cyclotron_forge;

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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CyclotronProxyBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, LuxSocket {
    private BlockPos controllerPos = BlockPos.ZERO;
    private int dx;
    private int dy;
    private int dz;
    private BlockState storedState = Blocks.AIR.defaultBlockState();
    private CyclotronProxyRole role = CyclotronProxyRole.STRUCTURE;

    public CyclotronProxyBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CYCLOTRON_PROXY_BE.get(), pos, state);
    }

    public CyclotronProxyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public void setControllerData(BlockPos controllerPos, int dx, int dy, int dz, CyclotronProxyRole role) {
        this.controllerPos = controllerPos;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.role = role;
        setChanged();
    }

    public BlockPos getControllerPos() {
        return controllerPos;
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

    public CyclotronProxyRole getRole() {
        return role;
    }

    public BlockState getStoredState() {
        return storedState;
    }

    public void setStoredState(BlockState storedState) {
        this.storedState = storedState;
        setChanged();
    }

    public @Nullable CyclotronControllerBlockEntity getController(Level level) {
        if (level == null || controllerPos.equals(BlockPos.ZERO)) return null;
        if (!level.isLoaded(controllerPos)) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof CyclotronControllerBlockEntity controller ? controller : null;
    }

    @Override
    public @Nullable IGlareNode getLuxSocketNode(Direction side) {
        CyclotronControllerBlockEntity controller = getController(level);
        return controller != null && controller.isLuxSocketSide(this, side) ? controller : null;
    }

    @Override
    public boolean isLuxSocketEnabled(Direction side) {
        return getLuxSocketNode(side) != null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CyclotronControllerBlockEntity controller = getController(level);
        return controller != null && controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("ControllerPos", NbtUtils.writeBlockPos(controllerPos));
        tag.putInt("Dx", dx);
        tag.putInt("Dy", dy);
        tag.putInt("Dz", dz);
        tag.putString("Role", role.name());
        tag.put("StoredState", NbtUtils.writeBlockState(storedState));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        controllerPos = NbtUtils.readBlockPos(tag, "ControllerPos").orElse(BlockPos.ZERO);
        dx = tag.getInt("Dx");
        dy = tag.getInt("Dy");
        dz = tag.getInt("Dz");
        try {
            role = CyclotronProxyRole.valueOf(tag.getString("Role"));
        } catch (IllegalArgumentException ignored) {
            role = CyclotronProxyRole.STRUCTURE;
        }
        if (tag.contains("StoredState")) {
            storedState = NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                    tag.getCompound("StoredState"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        write(tag, registries, true);
        return tag;
    }
}
