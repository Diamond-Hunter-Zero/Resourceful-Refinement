package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.client.research.ClientResearchLockState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ResearchCraftingLockPayload(int containerId, boolean locked, Optional<ResourceLocation> recipeId,
                                          Optional<ResourceLocation> lockingNode) implements CustomPacketPayload {
    public static final Type<ResearchCraftingLockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "research_crafting_lock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchCraftingLockPayload> STREAM_CODEC =
            StreamCodec.of(ResearchCraftingLockPayload::write, ResearchCraftingLockPayload::read);

    public static ResearchCraftingLockPayload locked(int containerId, ResourceLocation recipeId,
                                                     Optional<ResourceLocation> lockingNode) {
        return new ResearchCraftingLockPayload(containerId, true, Optional.of(recipeId), lockingNode);
    }

    public static ResearchCraftingLockPayload unlocked(int containerId) {
        return new ResearchCraftingLockPayload(containerId, false, Optional.empty(), Optional.empty());
    }

    private static void write(RegistryFriendlyByteBuf buf, ResearchCraftingLockPayload payload) {
        buf.writeVarInt(payload.containerId);
        buf.writeBoolean(payload.locked);
        if (payload.locked) {
            buf.writeResourceLocation(payload.recipeId.orElseThrow());
            buf.writeOptional(payload.lockingNode, (target, lockingNode) -> target.writeResourceLocation(lockingNode));
        }
    }

    private static ResearchCraftingLockPayload read(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        boolean locked = buf.readBoolean();
        if (!locked) {
            return unlocked(containerId);
        }
        ResourceLocation recipeId = buf.readResourceLocation();
        Optional<ResourceLocation> lockingNode = buf.readOptional(target -> target.readResourceLocation());
        return locked(containerId, recipeId, lockingNode);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResearchCraftingLockPayload payload) {
        ClientResearchLockState.handle(payload);
    }
}
