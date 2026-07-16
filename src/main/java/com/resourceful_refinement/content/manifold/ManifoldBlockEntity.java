package com.resourceful_refinement.content.manifold;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManifoldBlockEntity extends BlockEntity {
    private static final String NBT_ASSEMBLY_RECORD = "AssemblyRecord";

    private ManifoldAssemblyRecord assemblyRecord = ManifoldAssemblyRecord.EMPTY;
    private long assemblyLockedUntilGameTime;

    public ManifoldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANIFOLD_BE.get(), pos, state);
    }

    public ManifoldAssemblyRecord assemblyRecord() {
        return assemblyRecord;
    }

    public void holdForAssemblyTicks(int ticks) {
        if (level != null) {
            assemblyLockedUntilGameTime = Math.max(assemblyLockedUntilGameTime, level.getGameTime() + ticks);
        }
    }

    public boolean isAssemblyLocked() {
        return level != null && level.getGameTime() < assemblyLockedUntilGameTime;
    }

    /** Applies an action once and synchronizes the changed record to nearby clients. */
    public boolean applyAssemblyAction(ManifoldAssemblyAction action) {
        ManifoldAssemblyRecord next = action.apply(assemblyRecord);
        if (next.equals(assemblyRecord)) {
            return false;
        }
        assemblyRecord = next;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public void setAssemblyRecord(ManifoldAssemblyRecord record) {
        assemblyRecord = record;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(NBT_ASSEMBLY_RECORD, assemblyRecord.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        assemblyRecord = tag.contains(NBT_ASSEMBLY_RECORD)
                ? ManifoldAssemblyRecord.load(tag.getCompound(NBT_ASSEMBLY_RECORD))
                : ManifoldAssemblyRecord.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
