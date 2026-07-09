package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.GlareSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record GlareLinkSyncPayload(ResourceLocation dimension, List<Link> links) implements CustomPacketPayload {
    private static final int MAX_SYNCED_LINKS = 512;

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_link_sync");
    public static final Type<GlareLinkSyncPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, GlareLinkSyncPayload> STREAM_CODEC =
            StreamCodec.of(GlareLinkSyncPayload::write, GlareLinkSyncPayload::read);

    public static GlareLinkSyncPayload of(ResourceLocation dimension, List<GlareSavedData.LinkRenderRecord> records) {
        List<Link> links = records.stream()
                .limit(MAX_SYNCED_LINKS)
                .map(record -> new Link(record.a().pos(), record.b().pos(), record.validity()))
                .toList();
        return new GlareLinkSyncPayload(dimension, links);
    }

    private static void write(RegistryFriendlyByteBuf buf, GlareLinkSyncPayload payload) {
        buf.writeResourceLocation(payload.dimension);
        buf.writeVarInt(Math.min(payload.links.size(), MAX_SYNCED_LINKS));
        for (Link link : payload.links.stream().limit(MAX_SYNCED_LINKS).toList()) {
            BlockPos.STREAM_CODEC.encode(buf, link.a);
            BlockPos.STREAM_CODEC.encode(buf, link.b);
            buf.writeEnum(link.validity);
        }
    }

    private static GlareLinkSyncPayload read(RegistryFriendlyByteBuf buf) {
        ResourceLocation dimension = buf.readResourceLocation();
        int size = Math.min(buf.readVarInt(), MAX_SYNCED_LINKS);
        List<Link> links = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BlockPos a = BlockPos.STREAM_CODEC.decode(buf);
            BlockPos b = BlockPos.STREAM_CODEC.decode(buf);
            GlareSavedData.LinkValidity validity = buf.readEnum(GlareSavedData.LinkValidity.class);
            links.add(new Link(a, b, validity));
        }
        return new GlareLinkSyncPayload(dimension, links);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Link(BlockPos a, BlockPos b, GlareSavedData.LinkValidity validity) {}
}
