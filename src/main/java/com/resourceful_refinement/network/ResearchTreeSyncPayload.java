package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.api.research.ResearchApi;
import com.resourceful_refinement.api.research.ResearchNodeDefinition;
import com.resourceful_refinement.api.research.ResearchProgress;
import com.resourceful_refinement.api.research.ResearchRequirement;
import com.resourceful_refinement.api.research.ResearchReward;
import com.resourceful_refinement.api.research.ResearchTreeDefinition;
import com.resourceful_refinement.client.research.ClientResearchTreeData;
import com.resourceful_refinement.content.research.ResearchDefinitionManager;
import com.resourceful_refinement.content.research.ResearchSavedData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ResearchTreeSyncPayload(List<TreeEntry> trees, List<NodeEntry> nodes) implements CustomPacketPayload {
    public static final Type<ResearchTreeSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "research_tree_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchTreeSyncPayload> STREAM_CODEC =
            StreamCodec.of(ResearchTreeSyncPayload::write, ResearchTreeSyncPayload::read);

    public ResearchTreeSyncPayload {
        trees = List.copyOf(trees);
        nodes = List.copyOf(nodes);
    }

    public static ResearchTreeSyncPayload capture(ServerPlayer player) {
        ResearchDefinitionManager.Snapshot snapshot = ResearchDefinitionManager.snapshot();
        ResearchSavedData savedData = ResearchSavedData.get(player.server);
        Map<ResourceLocation, TreeEntry> treeEntries = new LinkedHashMap<>();

        snapshot.trees().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> treeEntries.put(entry.getKey(), TreeEntry.from(entry.getKey(), entry.getValue())));

        for (ResourceLocation nodeId : snapshot.nodes().keySet()) {
            ResourceLocation treeId = treeIdForNode(nodeId, snapshot.trees().keySet());
            treeEntries.putIfAbsent(treeId, TreeEntry.fallback(treeId));
        }

        List<NodeEntry> nodes = snapshot.nodes().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .map(entry -> NodeEntry.from(player, savedData, snapshot.trees().keySet(), entry.getKey(),
                        entry.getValue()))
                .toList();
        return new ResearchTreeSyncPayload(List.copyOf(treeEntries.values()), nodes);
    }

    private static ResourceLocation treeIdForNode(ResourceLocation nodeId, Set<ResourceLocation> treeIds) {
        return treeIds.stream()
                .filter(treeId -> treeId.getNamespace().equals(nodeId.getNamespace()))
                .filter(treeId -> nodeId.getPath().equals(treeId.getPath())
                        || nodeId.getPath().startsWith(treeId.getPath() + "/"))
                .max(Comparator.comparingInt(treeId -> treeId.getPath().length()))
                .orElseGet(() -> fallbackTreeIdForNode(nodeId));
    }

    private static ResourceLocation fallbackTreeIdForNode(ResourceLocation nodeId) {
        String path = nodeId.getPath();
        int slash = path.indexOf('/');
        String treePath = slash >= 0 ? path.substring(0, slash) : path;
        return ResourceLocation.fromNamespaceAndPath(nodeId.getNamespace(), treePath);
    }

    private static void write(RegistryFriendlyByteBuf buf, ResearchTreeSyncPayload payload) {
        buf.writeVarInt(payload.trees.size());
        for (TreeEntry tree : payload.trees) {
            writeTree(buf, tree);
        }
        buf.writeVarInt(payload.nodes.size());
        for (NodeEntry node : payload.nodes) {
            writeNode(buf, node);
        }
    }

    private static ResearchTreeSyncPayload read(RegistryFriendlyByteBuf buf) {
        int treeCount = buf.readVarInt();
        List<TreeEntry> trees = new ArrayList<>(treeCount);
        for (int i = 0; i < treeCount; i++) {
            trees.add(readTree(buf));
        }
        int nodeCount = buf.readVarInt();
        List<NodeEntry> nodes = new ArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(readNode(buf));
        }
        return new ResearchTreeSyncPayload(trees, nodes);
    }

    private static void writeTree(RegistryFriendlyByteBuf buf, TreeEntry entry) {
        buf.writeResourceLocation(entry.id);
        buf.writeUtf(entry.name.getString());
        buf.writeUtf(entry.description.getString());
        writeStack(buf, entry.icon);
    }

    private static TreeEntry readTree(RegistryFriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component name = Component.literal(buf.readUtf());
        Component description = Component.literal(buf.readUtf());
        ItemStack icon = readStack(buf);
        return new TreeEntry(id, name, description, icon);
    }

    private static void writeNode(RegistryFriendlyByteBuf buf, NodeEntry entry) {
        buf.writeResourceLocation(entry.id);
        buf.writeResourceLocation(entry.treeId);
        buf.writeUtf(entry.title.getString());
        buf.writeUtf(entry.description.getString());
        writeStack(buf, entry.icon);
        writeResourceLocations(buf, entry.parents);
        writeStacks(buf, entry.rewards);
        writeResourceLocations(buf, entry.recipes);
        writeStacks(buf, entry.lockedItems);
        writeRequirements(buf, entry.requirements);
        buf.writeBoolean(entry.unlocked);
        buf.writeBoolean(entry.globallyUnlocked);
        buf.writeBoolean(entry.serverUnlocked);
    }

    private static NodeEntry readNode(RegistryFriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        ResourceLocation treeId = buf.readResourceLocation();
        Component title = Component.literal(buf.readUtf());
        Component description = Component.literal(buf.readUtf());
        ItemStack icon = readStack(buf);
        List<ResourceLocation> parents = readResourceLocations(buf);
        List<ItemStack> rewards = readStacks(buf);
        List<ResourceLocation> recipes = readResourceLocations(buf);
        List<ItemStack> lockedItems = readStacks(buf);
        List<RequirementEntry> requirements = readRequirements(buf);
        boolean unlocked = buf.readBoolean();
        boolean globallyUnlocked = buf.readBoolean();
        boolean serverUnlocked = buf.readBoolean();
        return new NodeEntry(id, treeId, title, description, icon, parents, rewards, recipes, lockedItems,
                requirements, unlocked, globallyUnlocked, serverUnlocked);
    }

    private static void writeStack(RegistryFriendlyByteBuf buf, ItemStack stack) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        buf.writeVarInt(stack.getCount());
    }

    private static ItemStack readStack(RegistryFriendlyByteBuf buf) {
        ResourceLocation itemId = buf.readResourceLocation();
        int count = buf.readVarInt();
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(1, count));
    }

    private static void writeResourceLocations(RegistryFriendlyByteBuf buf, List<ResourceLocation> ids) {
        buf.writeVarInt(ids.size());
        for (ResourceLocation id : ids) {
            buf.writeResourceLocation(id);
        }
    }

    private static List<ResourceLocation> readResourceLocations(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceLocation> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readResourceLocation());
        }
        return ids;
    }

    private static void writeStacks(RegistryFriendlyByteBuf buf, List<ItemStack> stacks) {
        buf.writeVarInt(stacks.size());
        for (ItemStack stack : stacks) {
            writeStack(buf, stack);
        }
    }

    private static List<ItemStack> readStacks(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(readStack(buf));
        }
        return stacks;
    }

    private static void writeRequirements(RegistryFriendlyByteBuf buf, List<RequirementEntry> requirements) {
        buf.writeVarInt(requirements.size());
        for (RequirementEntry requirement : requirements) {
            buf.writeBoolean(requirement.fluid);
            buf.writeResourceLocation(requirement.id);
            writeStack(buf, requirement.icon);
            buf.writeVarInt(requirement.current);
            buf.writeVarInt(requirement.required);
        }
    }

    private static List<RequirementEntry> readRequirements(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<RequirementEntry> requirements = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            boolean fluid = buf.readBoolean();
            ResourceLocation id = buf.readResourceLocation();
            ItemStack icon = readStack(buf);
            int current = buf.readVarInt();
            int required = buf.readVarInt();
            requirements.add(new RequirementEntry(fluid, id, icon, current, required));
        }
        return requirements;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResearchTreeSyncPayload payload) {
        ClientResearchTreeData.apply(payload);
    }

    public record TreeEntry(ResourceLocation id, Component name, Component description, ItemStack icon) {
        public TreeEntry {
            icon = icon.copy();
        }

        public static TreeEntry from(ResourceLocation id, ResearchTreeDefinition definition) {
            return new TreeEntry(id, definition.name(), definition.description(), new ItemStack(definition.icon()));
        }

        public static TreeEntry fallback(ResourceLocation id) {
            return new TreeEntry(id, Component.literal(id.toString()), Component.empty(), new ItemStack(Items.BOOK));
        }

        @Override
        public ItemStack icon() {
            return icon.copy();
        }
    }

    public record NodeEntry(ResourceLocation id, ResourceLocation treeId, Component title, Component description,
                            ItemStack icon, List<ResourceLocation> parents, List<ItemStack> rewards,
                            List<ResourceLocation> recipes, List<ItemStack> lockedItems,
                            List<RequirementEntry> requirements, boolean unlocked, boolean globallyUnlocked,
                            boolean serverUnlocked) {
        public NodeEntry {
            icon = icon.copy();
            parents = List.copyOf(parents);
            rewards = copyStacks(rewards);
            recipes = List.copyOf(recipes);
            lockedItems = copyStacks(lockedItems);
            requirements = List.copyOf(requirements);
        }

        public static NodeEntry from(ServerPlayer player, ResearchSavedData savedData, Set<ResourceLocation> treeIds,
                                     ResourceLocation id, ResearchNodeDefinition definition) {
            boolean globallyUnlocked = savedData.isGloballyUnlocked(id);
            boolean unlocked = ResearchApi.hasUnlocked(player, id);
            boolean serverUnlocked = ResearchApi.hasAnyServerUnlock(player.server, id);
            List<ItemStack> rewards = definition.rewards().stream()
                    .map(ResearchReward::toStack)
                    .toList();
            List<ItemStack> lockedItems = definition.items().stream()
                    .map(ItemStack::new)
                    .toList();
            List<RequirementEntry> requirements = requirementEntries(player, id, definition, unlocked);
            return new NodeEntry(id, treeIdForNode(id, treeIds), definition.title(), definition.description(),
                    definition.icon(), definition.parents(), rewards, definition.recipes(), lockedItems, requirements,
                    unlocked, globallyUnlocked, serverUnlocked);
        }

        @Override
        public ItemStack icon() {
            return icon.copy();
        }
    }

    public record RequirementEntry(boolean fluid, ResourceLocation id, ItemStack icon, int current, int required) {
        public RequirementEntry {
            icon = icon.copy();
            current = Math.max(0, Math.min(current, required));
            required = Math.max(0, required);
        }

        public boolean complete() {
            return required <= 0 || current >= required;
        }

        @Override
        public ItemStack icon() {
            return icon.copy();
        }
    }

    private static List<RequirementEntry> requirementEntries(ServerPlayer player, ResourceLocation nodeId,
                                                            ResearchNodeDefinition definition, boolean unlocked) {
        ResearchRequirement requirements = definition.requirements();
        ResearchProgress progress = ResearchApi.getProgress(player.server, player.getUUID(), nodeId);
        List<RequirementEntry> entries = new ArrayList<>();
        for (ResearchRequirement.ItemRequirement requirement : requirements.items()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(requirement.item());
            int required = requirement.count();
            int current = unlocked ? required : progress.itemProgress().getOrDefault(itemId, 0);
            entries.add(new RequirementEntry(false, itemId, new ItemStack(requirement.item()), current, required));
        }
        for (ResearchRequirement.FluidRequirement requirement : requirements.fluids()) {
            Fluid fluid = requirement.fluid();
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
            int required = requirement.amount();
            int current = unlocked ? required : progress.fluidProgress().getOrDefault(fluidId, 0);
            ItemStack icon = new ItemStack(fluid.getBucket());
            if (icon.isEmpty()) {
                icon = new ItemStack(Items.WATER_BUCKET);
            }
            entries.add(new RequirementEntry(true, fluidId, icon, current, required));
        }
        return entries;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> copy = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copy.add(stack.copy());
        }
        return List.copyOf(copy);
    }
}
