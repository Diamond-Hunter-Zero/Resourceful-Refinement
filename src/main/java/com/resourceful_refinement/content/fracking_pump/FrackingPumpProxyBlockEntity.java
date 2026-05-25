package com.resourceful_refinement.content.fracking_pump;

import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Minimal block entity for FrackingPumpProxyBlock.
 * Stores only the world-space position of its controller
 * and provides a helper to retrieve it.
 */
public class FrackingPumpProxyBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private BlockPos controllerPos = BlockPos.ZERO;
    private int dx, dy, dz;
    private BlockState storedState = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();

    public FrackingPumpProxyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Behaviors are delegated to the controller
    }

    @Override
    public Collection<BlockEntityBehaviour> getAllBehaviours() {
        if (level != null && level.isClientSide) {
            FrackingPumpOutletBlockEntity controller = getController(level);
            if (controller != null) {
                return controller.getAllBehaviours();
            }
        }
        return super.getAllBehaviours();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (level == null) return false;
        FrackingPumpOutletBlockEntity controller = getController(level);
        if (controller != null) {
            return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Controller lookup
    // -------------------------------------------------------------------------
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerData(BlockPos pos, int dx, int dy, int dz) {
        this.controllerPos = pos;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        // com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("Proxy at {} set controller to {}", getBlockPos(), pos);
        setChanged();
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDz() { return dz; }

    public BlockState getStoredState() { return storedState; }
    public void setStoredState(BlockState state) { 
        this.storedState = state; 
        setChanged();
    }

    /**
     * Returns the controller BE, or null if the world is unavailable or the
     * block at the stored position is no longer an Access Port.
     */
    public @Nullable FrackingPumpOutletBlockEntity getController(Level level) {
        if (controllerPos.equals(BlockPos.ZERO)) {
            // com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.warn("Proxy at {} has ZERO controller pos!", getBlockPos());
            return null;
        }
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof FrackingPumpOutletBlockEntity controller) return controller;
        return null;
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("controllerPos", NbtUtils.writeBlockPos(controllerPos));
        tag.putInt("dx", dx);
        tag.putInt("dy", dy);
        tag.putInt("dz", dz);
        tag.put("StoredState", NbtUtils.writeBlockState(storedState));
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
        if (tag.contains("StoredState")) {
            storedState = NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("StoredState"));
        }
        // if (!clientPacket) com.resourceful_refinement.ResourcefulRefinementMain.LOGGER.info("Proxy at {} loaded controller pos {}", getBlockPos(), controllerPos);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        write(tag, registries, true);
        return tag;
    }
}
