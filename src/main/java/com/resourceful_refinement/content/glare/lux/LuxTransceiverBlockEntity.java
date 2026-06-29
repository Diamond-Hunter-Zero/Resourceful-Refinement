package com.resourceful_refinement.content.glare.lux;

import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.glare.GlareNodeBlockEntity;
import com.resourceful_refinement.content.glare.GlareOperationStatus;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.content.glare.IGlareNode;
import com.resourceful_refinement.content.glare.IGlareReceiver;
import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LuxTransceiverBlockEntity extends GlareNodeBlockEntity implements IGlareReceiver {
    public static final int MAX_LINK_COUNT = 1;

    private GlareOperationStatus status = GlareOperationStatus.ONLINE;
    @Nullable
    private DimensionalNodePos socketNodePos;

    public LuxTransceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUX_TRANSCEIVER_BE.get(), pos, state, MAX_LINK_COUNT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshSocketLink();
    }

    public void refreshSocketLink() {
        if (!(level instanceof ServerLevel server) || !getBlockState().hasProperty(LuxTransceiverBlock.FACING)) {
            return;
        }

        DimensionalNodePos ownPos = DimensionalNodePos.of(server, worldPosition);
        IGlareNode socketNode = findSocketNode(server);
        DimensionalNodePos targetPos = socketNode == null ? null : socketNode.getGlareNodePos();

        if (socketNodePos != null && !socketNodePos.equals(targetPos)) {
            GlareService.removeLink(server, ownPos, socketNodePos);
            socketNodePos = null;
        }

        if (socketNode == null || targetPos == null || targetPos.equals(ownPos)) {
            return;
        }

        GlareService.onNodeLoaded(server, socketNode);
        GlareService.trySocketLink(server, ownPos, targetPos);
        socketNodePos = targetPos;
    }

    public void clearSocketLink() {
        if (socketNodePos == null || !(level instanceof ServerLevel server)) {
            return;
        }
        GlareService.removeLink(server, DimensionalNodePos.of(server, worldPosition), socketNodePos);
        socketNodePos = null;
    }

    @Nullable
    private IGlareNode findSocketNode(ServerLevel server) {
        Direction facing = getBlockState().getValue(LuxTransceiverBlock.FACING);
        Direction back = facing.getOpposite();
        BlockPos socketPos = worldPosition.relative(back);
        if (!server.isLoaded(socketPos)) {
            return null;
        }
        BlockEntity blockEntity = server.getBlockEntity(socketPos);
        if (!(blockEntity instanceof LuxSocket socket) || !socket.isLuxSocketEnabled(facing)) {
            return null;
        }
        return socket.getLuxSocketNode(facing);
    }

    @Override
    public int getAllocatedLux() {
        return 0;
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
        if (socketNodePos != null) {
            DimensionalNodePos.writePos(tag, "SocketNode", socketNodePos);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        try {
            status = GlareOperationStatus.valueOf(tag.getString("Status"));
        } catch (IllegalArgumentException ignored) {
            status = GlareOperationStatus.ONLINE;
        }
        socketNodePos = DimensionalNodePos.readPos(tag, "SocketNode").orElse(null);
    }
}
