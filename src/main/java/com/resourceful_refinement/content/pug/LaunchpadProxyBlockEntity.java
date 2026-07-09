package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LaunchpadProxyBlockEntity extends BlockEntity {
    private BlockPos controllerPos = BlockPos.ZERO;
    private int lateral;
    private int depth;

    public LaunchpadProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCHPAD_PROXY_BE.get(), pos, state);
    }

    public void setControllerData(BlockPos controllerPos, int lateral, int depth) {
        this.controllerPos = controllerPos.immutable();
        this.lateral = lateral;
        this.depth = depth;
        setChanged();
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public int getLateral() {
        return lateral;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isRearCenter() {
        return lateral == 0 && depth == LaunchpadStructure.DEPTH - 1;
    }

    public boolean belongsTo(BlockPos controllerPos) {
        return this.controllerPos.equals(controllerPos);
    }

    public boolean belongsTo(BlockPos controllerPos, int lateral, int depth) {
        return belongsTo(controllerPos) && this.lateral == lateral && this.depth == depth;
    }

    public @Nullable LaunchpadControllerBlockEntity getController(Level level) {
        if (controllerPos.equals(BlockPos.ZERO)) return null;
        BlockEntity blockEntity = level.getBlockEntity(controllerPos);
        return blockEntity instanceof LaunchpadControllerBlockEntity controller ? controller : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Controller", NbtUtils.writeBlockPos(controllerPos));
        tag.putInt("Lateral", lateral);
        tag.putInt("Depth", depth);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controllerPos = NbtUtils.readBlockPos(tag, "Controller").orElse(BlockPos.ZERO);
        lateral = tag.getInt("Lateral");
        depth = tag.getInt("Depth");
    }
}
