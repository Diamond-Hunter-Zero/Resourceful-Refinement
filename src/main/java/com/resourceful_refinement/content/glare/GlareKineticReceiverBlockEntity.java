package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class GlareKineticReceiverBlockEntity extends GlareNodeBlockEntity implements IGlareReceiver {

    public static final int MAX_LINK_COUNT = 1;

    private GlareOperationStatus status = GlareOperationStatus.ONLINE;

    public GlareKineticReceiverBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GLARE_KINETIC_RECEIVER_BE.get(), pos, blockState, MAX_LINK_COUNT);
    }

    @Override
    public int getAllocatedLux() {
        return 1;
    }

    @Override
    public GlareOperationStatus getGlareOperationStatus() {
        return status;
    }

    @Override
    public void setGlareOperationStatus(GlareOperationStatus status) {
        this.status = status;
        pushGlareState();
        syncToClient();
    }

    @Override
    public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) {
        if (this.status == status) {
            return;
        }
        this.status = status;
        syncToClient();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Status", status.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        try {
            status = GlareOperationStatus.valueOf(tag.getString("Status"));
        } catch (IllegalArgumentException ignored) {
            status = GlareOperationStatus.ONLINE;
        }
    }
}
