package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.api.research.ResearchNodeDefinition;
import com.resourceful_refinement.client.research.ResearchUnlockToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ResearchUnlockToastPayload(Component title, ItemStack icon) implements CustomPacketPayload {
    public static final Type<ResearchUnlockToastPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "research_unlock_toast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchUnlockToastPayload> STREAM_CODEC =
            StreamCodec.of(ResearchUnlockToastPayload::write, ResearchUnlockToastPayload::read);

    public ResearchUnlockToastPayload {
        icon = icon.copy();
    }

    public static ResearchUnlockToastPayload from(ResearchNodeDefinition node) {
        return new ResearchUnlockToastPayload(node.title(), node.icon());
    }

    private static void write(RegistryFriendlyByteBuf buf, ResearchUnlockToastPayload payload) {
        buf.writeUtf(payload.title.getString());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.icon);
    }

    private static ResearchUnlockToastPayload read(RegistryFriendlyByteBuf buf) {
        Component title = Component.literal(buf.readUtf());
        ItemStack icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        return new ResearchUnlockToastPayload(title, icon);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResearchUnlockToastPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getToasts().addToast(new ResearchUnlockToast(payload.title, payload.icon));
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }
}
