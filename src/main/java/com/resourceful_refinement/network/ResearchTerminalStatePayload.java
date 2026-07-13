package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.client.research.ClientResearchTerminalData;
import com.resourceful_refinement.content.research_terminal.ResearchTerminalBlockEntity;
import com.resourceful_refinement.content.research_terminal.ResearchTerminalCycleKind;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ResearchTerminalStatePayload(BlockPos pos, @Nullable UUID ownerUuid, String ownerName,
        @Nullable ResourceLocation targetNodeId, int cycleTicks, int cycleDuration,
        ResearchTerminalCycleKind cycleKind, @Nullable ResourceLocation cycleResourceId)
        implements CustomPacketPayload {
    public static final Type<ResearchTerminalStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "research_terminal_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchTerminalStatePayload> STREAM_CODEC =
            StreamCodec.of(ResearchTerminalStatePayload::write, ResearchTerminalStatePayload::read);

    public ResearchTerminalStatePayload {
        ownerName = ownerName == null ? "" : ownerName;
        cycleTicks = Math.max(0, cycleTicks);
        cycleDuration = Math.max(1, cycleDuration);
        cycleKind = cycleKind == null ? ResearchTerminalCycleKind.IDLE : cycleKind;
    }

    public static ResearchTerminalStatePayload capture(ResearchTerminalBlockEntity terminal) {
        return new ResearchTerminalStatePayload(terminal.getBlockPos(), terminal.getOwnerUuid(),
                terminal.getOwnerName(), terminal.getTargetNodeId(), terminal.getCycleTicks(),
                terminal.getCycleDuration(), terminal.getCycleKind(), terminal.getCycleResourceId());
    }

    static void writeFields(RegistryFriendlyByteBuf buf, ResearchTerminalStatePayload payload) {
        buf.writeBlockPos(payload.pos);
        writeNullableUuid(buf, payload.ownerUuid);
        buf.writeUtf(payload.ownerName, 64);
        writeNullableResourceLocation(buf, payload.targetNodeId);
        buf.writeVarInt(payload.cycleTicks);
        buf.writeVarInt(payload.cycleDuration);
        buf.writeEnum(payload.cycleKind);
        writeNullableResourceLocation(buf, payload.cycleResourceId);
    }

    static ResearchTerminalStatePayload readFields(RegistryFriendlyByteBuf buf) {
        return new ResearchTerminalStatePayload(buf.readBlockPos(), readNullableUuid(buf), buf.readUtf(64),
                readNullableResourceLocation(buf), buf.readVarInt(), buf.readVarInt(),
                buf.readEnum(ResearchTerminalCycleKind.class), readNullableResourceLocation(buf));
    }

    private static void write(RegistryFriendlyByteBuf buf, ResearchTerminalStatePayload payload) {
        writeFields(buf, payload);
    }

    private static ResearchTerminalStatePayload read(RegistryFriendlyByteBuf buf) {
        return readFields(buf);
    }

    private static void writeNullableUuid(RegistryFriendlyByteBuf buf, @Nullable UUID uuid) {
        buf.writeBoolean(uuid != null);
        if (uuid != null) {
            buf.writeUUID(uuid);
        }
    }

    private static @Nullable UUID readNullableUuid(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUUID() : null;
    }

    static void writeNullableResourceLocation(RegistryFriendlyByteBuf buf, @Nullable ResourceLocation id) {
        buf.writeBoolean(id != null);
        if (id != null) {
            buf.writeResourceLocation(id);
        }
    }

    static @Nullable ResourceLocation readNullableResourceLocation(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readResourceLocation() : null;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResearchTerminalStatePayload payload) {
        ClientResearchTerminalData.apply(payload);
    }
}
