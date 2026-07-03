package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalBlockEntity;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalMode;
import com.resourceful_refinement.content.glare.remote.IRemoteEntanglementEndpoint;
import com.resourceful_refinement.content.glare.remote.RemoteEndpointKind;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglementMode;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglementService;
import com.resourceful_refinement.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.UUID;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder(ResourcefulRefinementMain.MOD_ID)
public final class GlareGameTests {
    private GlareGameTests() {}

    @GameTest(template = "empty")
    public static void graphCreationAndMerge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = GlareSavedData.get(level);
        TestNodes nodes = TestNodes.create(level, helper, 10);
        nodes.clear(data);

        data.registerNode(level, nodes.a);
        data.registerNode(level, nodes.b);
        data.registerNode(level, nodes.c);
        require(data.getNetworks().size() >= 3, "expected isolated nodes before linking");

        data.tryAddLink(level, nodes.a.pos, nodes.b.pos);
        data.tryAddLink(level, nodes.b.pos, nodes.c.pos);

        UUID network = requireNetwork(data, nodes.a.pos);
        require(network.equals(requireNetwork(data, nodes.b.pos)), "a and b should share a network");
        require(network.equals(requireNetwork(data, nodes.c.pos)), "b and c should share a network");
        nodes.clear(data);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void graphSplitAfterLinkRemoval(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = GlareSavedData.get(level);
        TestNodes nodes = TestNodes.create(level, helper, 30);
        nodes.clear(data);

        data.registerNode(level, nodes.a);
        data.registerNode(level, nodes.b);
        data.registerNode(level, nodes.c);
        data.tryAddLink(level, nodes.a.pos, nodes.b.pos);
        data.tryAddLink(level, nodes.b.pos, nodes.c.pos);
        data.removeLink(nodes.b.pos, nodes.c.pos);

        require(requireNetwork(data, nodes.a.pos).equals(requireNetwork(data, nodes.b.pos)), "a and b should remain connected");
        require(!requireNetwork(data, nodes.a.pos).equals(requireNetwork(data, nodes.c.pos)), "c should split into a separate network");
        nodes.clear(data);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedLinkSplitsNetworkButRemainsRemembered(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos emitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 35)));
        DimensionalNodePos relay = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 35)));
        DimensionalNodePos receiver = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 35)));

        data.registerNode(level, new TestEmitterNode(emitter, 8, 8, DyeColor.RED, true));
        data.registerNode(level, new TestNode(relay, 8));
        data.registerNode(level, new TestReceiverNode(receiver, 8, 1, GlareOperationStatus.ONLINE));
        data.tryAddLink(level, emitter, relay);
        data.tryAddLink(level, relay, receiver);

        GlareLink blocked = new GlareLink(relay, receiver);
        data.setLinkValidityForTests(blocked, GlareSavedData.LinkValidity.BLOCKED);

        require(data.getLinksFor(relay).size() == 2, "blocked link should remain remembered on the relay");
        require(data.getLinksFor(receiver).size() == 1, "blocked link should remain remembered on the receiver");
        require(!requireNetwork(data, emitter).equals(requireNetwork(data, receiver)), "blocked link should split the graph");

        GlareSavedData.NetworkRecord emitterNetwork = requireNetworkRecord(data, emitter);
        GlareSavedData.NetworkRecord receiverNetwork = requireNetworkRecord(data, receiver);
        require(emitterNetwork.luxCapacity == 8 && emitterNetwork.luxAllocated == 0, "emitter side should keep only its Lux capacity");
        require(emitterNetwork.colourCharges.getOrDefault(DyeColor.RED, 0) == 8, "emitter side should keep its colour charge");
        require(receiverNetwork.luxCapacity == 0 && receiverNetwork.luxAllocated == 1, "receiver side should lose remote emitter capacity");
        require(receiverNetwork.overloaded, "receiver side should overload after being split from capacity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unblockedLinkMergesSplitNetworks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos emitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 37)));
        DimensionalNodePos receiver = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 37)));

        data.registerNode(level, new TestEmitterNode(emitter, 8, 8, DyeColor.BLUE, true));
        data.registerNode(level, new TestReceiverNode(receiver, 8, 1, GlareOperationStatus.ONLINE));
        data.tryAddLink(level, emitter, receiver);

        GlareLink link = new GlareLink(emitter, receiver);
        data.setLinkValidityForTests(link, GlareSavedData.LinkValidity.BLOCKED);
        require(!requireNetwork(data, emitter).equals(requireNetwork(data, receiver)), "blocked link should split the graph");

        data.setLinkValidityForTests(link, GlareSavedData.LinkValidity.VALID);
        GlareSavedData.NetworkRecord merged = requireNetworkRecord(data, receiver);
        require(requireNetwork(data, emitter).equals(requireNetwork(data, receiver)), "valid link should merge the graph again");
        require(merged.luxCapacity == 8 && merged.luxAllocated == 1, "merged network should restore Lux totals");
        require(merged.colourCharges.getOrDefault(DyeColor.BLUE, 0) == 8, "merged network should restore colour charge");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void duplicateLinksAreIgnored(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = GlareSavedData.get(level);
        TestNodes nodes = TestNodes.create(level, helper, 40);
        nodes.clear(data);

        data.registerNode(level, nodes.a);
        data.registerNode(level, nodes.b);
        GlareSavedData.LinkResult first = data.tryAddLink(level, nodes.a.pos, nodes.b.pos);
        GlareSavedData.LinkResult second = data.tryAddLink(level, nodes.b.pos, nodes.a.pos);

        require(first == GlareSavedData.LinkResult.CREATED, "first link should be created");
        require(second == GlareSavedData.LinkResult.ALREADY_LINKED, "duplicate reverse link should be ignored");
        require(data.getLinksFor(nodes.a.pos).size() == 1, "duplicate should not create a second link");
        nodes.clear(data);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void maxLinkLimitEvictsOldestLink(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos a = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 6)));
        DimensionalNodePos b = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 6)));
        DimensionalNodePos c = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 6)));
        data.unregisterLoadedNode(a);
        data.unregisterLoadedNode(b);
        data.unregisterLoadedNode(c);

        data.registerNode(level, new TestNode(a, 1));
        data.registerNode(level, new TestNode(b, 8));
        data.registerNode(level, new TestNode(c, 8));
        data.tryAddLink(level, a, b);
        data.tryAddLink(level, a, c);

        require(data.getLinksFor(a).size() == 1, "single-link node should keep one link");
        require(data.getLinksFor(b).isEmpty(), "oldest link should be evicted");
        require(data.getLinksFor(c).size() == 1, "newest link should remain");
        data.unregisterLoadedNode(a);
        data.unregisterLoadedNode(b);
        data.unregisterLoadedNode(c);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void staleLoadedNodeIsCleanedUp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = GlareSavedData.get(level);
        TestNodes nodes = TestNodes.create(level, helper, 50);
        nodes.clear(data);

        data.registerNode(level, nodes.a);
        BlockPos chunkOrigin = new BlockPos(nodes.a.pos.pos().getX() & ~15, level.getMinBuildHeight(), nodes.a.pos.pos().getZ() & ~15);
        data.reconcileLoadedChunk(level, chunkOrigin);

        require(data.getNode(nodes.a.pos).isEmpty(), "loaded position without an IGlareNode BE should be pruned");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void savedDataReloadPreservesGraph(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        TestNodes nodes = TestNodes.create(level, helper, 70);

        data.registerNode(level, nodes.a);
        data.registerNode(level, nodes.b);
        data.tryAddLink(level, nodes.a.pos, nodes.b.pos);
        UUID originalNetwork = requireNetwork(data, nodes.a.pos);

        CompoundTag tag = data.save(new CompoundTag(), level.registryAccess());
        GlareSavedData loaded = GlareSavedData.loadForTests(tag);

        require(loaded.getLinksFor(nodes.a.pos).size() == 1, "link should survive save/load");
        require(originalNetwork.equals(requireNetwork(loaded, nodes.a.pos)), "network id should survive save/load rebuild");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unloadedNodesArePreservedDuringOtherChunkReconcile(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos remote = new DimensionalNodePos(level.dimension(), new BlockPos(30_000_000, 80, 30_000_000));
        TestNode remoteNode = new TestNode(remote, 8);
        TestNodes local = TestNodes.create(level, helper, 90);

        data.registerNode(level, remoteNode);
        data.registerNode(level, local.a);
        BlockPos localChunkOrigin = new BlockPos(local.a.pos.pos().getX() & ~15, level.getMinBuildHeight(), local.a.pos.pos().getZ() & ~15);
        data.reconcileLoadedChunk(level, localChunkOrigin);

        require(data.getNode(remote).isPresent(), "remote unloaded node should remain untouched");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void luxCapacityAllocationAndColourChargesAreCalculated(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos whiteEmitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 110)));
        DimensionalNodePos redEmitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 110)));
        DimensionalNodePos receiver = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 110)));

        data.registerNode(level, new TestEmitterNode(whiteEmitter, 8, 5, DyeColor.WHITE, true));
        data.registerNode(level, new TestEmitterNode(redEmitter, 8, 3, DyeColor.RED, true));
        data.registerNode(level, new TestReceiverNode(receiver, 8, 6, GlareOperationStatus.ONLINE));
        data.tryAddLink(level, whiteEmitter, receiver);
        data.tryAddLink(level, redEmitter, receiver);

        GlareSavedData.NetworkRecord network = requireNetworkRecord(data, receiver);
        require(network.luxCapacity == 8, "capacity should sum enabled emitter Lux");
        require(network.luxAllocated == 6, "allocation should sum receiver Lux");
        require(network.colourCharges.getOrDefault(DyeColor.WHITE, 0) == 5, "white colour charge should equal produced Lux");
        require(network.colourCharges.getOrDefault(DyeColor.RED, 0) == 3, "red colour charge should equal produced Lux");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void firstReceiverLinkDoesNotLatchTransientPlacementOverload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos emitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 120)));
        DimensionalNodePos receiver = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 120)));

        data.registerNode(level, new TestEmitterNode(emitter, 1, 8, DyeColor.WHITE, true));
        data.registerNode(level, new TestReceiverNode(receiver, 1, 1, GlareOperationStatus.ONLINE));

        GlareSavedData.NetworkRecord transientReceiverNetwork = requireNetworkRecord(data, receiver);
        require(transientReceiverNetwork.overloaded, "isolated receiver should initially overload with no capacity");

        data.tryAddLink(level, emitter, receiver);

        GlareSavedData.NetworkRecord linkedNetwork = requireNetworkRecord(data, receiver);
        require(linkedNetwork.luxCapacity == 8, "linked network should keep emitter capacity");
        require(linkedNetwork.luxAllocated == 1, "linked network should keep receiver allocation");
        require(!linkedNetwork.overloaded, "transient single-node overload should not latch after first valid link");
        require(data.getNode(receiver).orElseThrow().status == GlareOperationStatus.ONLINE, "receiver should return online once capacity is sufficient");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void overloadedNetworkRecoversWhenMergedWithSufficientCapacity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos weakEmitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 130)));
        DimensionalNodePos extraEmitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 130)));
        DimensionalNodePos receiver = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 130)));

        data.registerNode(level, new TestEmitterNode(weakEmitter, 8, 2, DyeColor.WHITE, true));
        data.registerNode(level, new TestEmitterNode(extraEmitter, 8, 2, DyeColor.BLUE, true));
        data.registerNode(level, new TestReceiverNode(receiver, 8, 3, GlareOperationStatus.ONLINE));
        data.tryAddLink(level, weakEmitter, receiver);

        GlareSavedData.NetworkRecord overloaded = requireNetworkRecord(data, receiver);
        require(overloaded.overloaded, "network should overload when allocation exceeds capacity");
        require(data.getNode(receiver).orElseThrow().status == GlareOperationStatus.OVERLOADED, "receiver record should be overloaded");
        require(!data.tryResetNetwork(overloaded.id), "reset should fail while still over-allocated");

        data.tryAddLink(level, extraEmitter, receiver);
        GlareSavedData.NetworkRecord recovered = requireNetworkRecord(data, receiver);
        require(recovered.luxCapacity == 4 && recovered.luxAllocated == 3, "extra emitter should fix capacity");
        require(!recovered.overloaded, "merged network should recover when combined capacity is sufficient");
        require(data.getNode(receiver).orElseThrow().status == GlareOperationStatus.ONLINE, "receiver should return online after sufficient merge");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void variableLuxCurveSamplesExpectedSegments(GameTestHelper helper) {
        int[] curve = {2, 2, 4, 8, 10, 8, 4, 2};
        require(GlareLuxCalculator.sampleVariableLux(curve, 0, 200) == 2, "start of curve should use first segment");
        require(GlareLuxCalculator.sampleVariableLux(curve, 100, 200) == 10, "middle of curve should use middle segment");
        require(GlareLuxCalculator.sampleVariableLux(curve, 199, 200) == 2, "end of curve should use last segment");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void relayWrenchCapacityAndBulkUnlinkSemantics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos center = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(2, 2, 18)));
        DimensionalNodePos first = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(4, 2, 18)));
        DimensionalNodePos second = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(6, 2, 18)));
        data.registerNode(level, new TestNode(center, 2));
        data.registerNode(level, new TestNode(first, 1));
        data.registerNode(level, new TestNode(second, 1));
        data.tryAddLink(level, center, first);

        require(!data.canAcceptLink(first), "a node at its limit must not be a valid wrench target");
        require(data.canAcceptLink(center), "a node below its limit should remain selectable");
        data.tryAddLink(level, center, second);
        require(!data.canAcceptLink(center), "the center should become invalid once its final slot is used");

        int removed = data.removeAllLinks(level, center);
        require(removed == 2, "bulk unlink should report every removed incident link");
        require(data.getLinksFor(center).isEmpty() && data.getLinksFor(first).isEmpty()
                && data.getLinksFor(second).isEmpty(), "bulk unlink must update every adjacency index");
        require(!requireNetwork(data, center).equals(requireNetwork(data, first)),
                "bulk unlink must rebuild and split the former network");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chromaticTransceiverLogicModesAndThresholds(GameTestHelper helper) {
        int[] filters = new int[DyeColor.values().length];
        java.util.Arrays.fill(filters, GlareChromaticTransceiverBlockEntity.DISABLED_FILTER);
        filters[DyeColor.RED.ordinal()] = 4;
        filters[DyeColor.BLUE.ordinal()] = 2;

        int[] bothMatch = new int[DyeColor.values().length];
        bothMatch[DyeColor.RED.ordinal()] = 4;
        bothMatch[DyeColor.BLUE.ordinal()] = 3;
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, bothMatch), "AND should pass when every enabled threshold matches");
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.OR, filters, bothMatch), "OR should pass when filters match");
        require(!GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.XOR, filters, bothMatch), "XOR should fail when two filters match");

        int[] oneMatch = bothMatch.clone();
        oneMatch[DyeColor.BLUE.ordinal()] = 1;
        require(!GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, oneMatch), "AND should fail below a threshold");
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.OR, filters, oneMatch), "OR should pass with one match");
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.XOR, filters, oneMatch), "XOR should pass with exactly one match");

        java.util.Arrays.fill(filters, GlareChromaticTransceiverBlockEntity.DISABLED_FILTER);
        require(!GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, bothMatch), "an empty filter set should not emit redstone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chromaticTransceiverComparisonTypes(GameTestHelper helper) {
        int[] filters = new int[DyeColor.values().length];
        java.util.Arrays.fill(filters, GlareChromaticTransceiverBlockEntity.DISABLED_FILTER);
        filters[DyeColor.GREEN.ordinal()] = 5;
        int[] charges = new int[DyeColor.values().length];
        charges[DyeColor.GREEN.ordinal()] = 5;
        GlareComparison[] comparisons = new GlareComparison[DyeColor.values().length];
        java.util.Arrays.fill(comparisons, GlareComparison.GREATER_THAN_OR_EQUAL);

        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, comparisons, charges), ">= should include equality");
        comparisons[DyeColor.GREEN.ordinal()] = GlareComparison.GREATER_THAN;
        require(!GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, comparisons, charges), "> should exclude equality");
        comparisons[DyeColor.GREEN.ordinal()] = GlareComparison.LESS_THAN_OR_EQUAL;
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, comparisons, charges), "<= should include equality");
        comparisons[DyeColor.GREEN.ordinal()] = GlareComparison.LESS_THAN;
        require(!GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, comparisons, charges), "< should exclude equality");
        comparisons[DyeColor.GREEN.ordinal()] = GlareComparison.EQUAL;
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, comparisons, charges), "= should match equal counts");
        comparisons[DyeColor.GREEN.ordinal()] = GlareComparison.NOT_EQUAL;
        require(!GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, comparisons, charges), "!= should reject equal counts");
        charges[DyeColor.GREEN.ordinal()] = 4;
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, comparisons, charges), "!= should accept unequal counts");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chromaticTransceiverUsesUnloadedEmitterColourState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos emitter = new DimensionalNodePos(level.dimension(), new BlockPos(30_000_000, 80, 30_000_000));
        DimensionalNodePos transceiver = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 150)));
        data.registerNode(level, new TestEmitterNode(emitter, 1, 7, DyeColor.MAGENTA, true));
        data.registerNode(level, new TestReceiverNode(transceiver, 1, 0, GlareOperationStatus.ONLINE));
        data.tryAddLink(level, emitter, transceiver);

        GlareSavedData.NetworkRecord network = requireNetworkRecord(data, transceiver);
        int[] charges = new int[DyeColor.values().length];
        for (var entry : network.colourCharges.entrySet()) charges[entry.getKey().ordinal()] = entry.getValue();
        int[] filters = new int[DyeColor.values().length];
        java.util.Arrays.fill(filters, GlareChromaticTransceiverBlockEntity.DISABLED_FILTER);
        filters[DyeColor.MAGENTA.ordinal()] = 7;

        require(charges[DyeColor.MAGENTA.ordinal()] == 7, "unloaded emitter should retain its persisted magenta charge");
        require(GlareChromaticTransceiverBlockEntity.matchesFilters(GlareLogicMode.AND, filters, charges), "transceiver gate should include unloaded emitter charge");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void telemetrySendReadDiscardCapAndCleanup(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos node = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 170)));
        data.registerNode(level, new TestNode(node, 1));
        UUID networkId = requireNetwork(data, node);
        GlareAddress sender = GlareAddress.of(Items.REDSTONE, Items.IRON_INGOT, Items.GLASS);
        GlareAddress destination = GlareAddress.of(Items.BLUE_DYE, Items.COPPER_INGOT, Items.ENDER_PEARL);

        for (int i = 0; i < 18; i++) {
            String body = i == 17 ? "x".repeat(600) : "message-" + i;
            require(data.sendTelemetry(networkId, new GlareMessage(sender, destination, body, i)), "send should find the network");
        }
        java.util.List<GlareMessage> inbox = data.readTelemetry(networkId, destination);
        require(inbox.size() == TelemetryService.MAX_MESSAGES, "normal insertion should cap an inbox at 16 messages");
        require(inbox.getFirst().body().equals("message-2"), "new messages should evict the oldest message first");
        require(inbox.getLast().body().length() == GlareMessage.MAX_BODY_LENGTH, "message bodies should be truncated to 512 characters");

        GlareMessage exact = inbox.get(4);
        require(data.discardTelemetry(networkId, destination, exact.id()).orElseThrow().equals(exact), "discard by id should remove the exact message");
        require(data.readTelemetry(networkId, destination).stream().noneMatch(message -> message.id().equals(exact.id())), "discarded message should no longer be readable");
        while (!data.readTelemetry(networkId, destination).isEmpty()) {
            data.discardTelemetryAt(networkId, destination, 0);
        }
        require(!requireNetworkRecord(data, node).telemetryInboxes.containsKey(destination), "empty inboxes should be cleaned up");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void telemetryMergeTemporarilyExceedsCapThenInsertionTrims(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos a = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 180)));
        DimensionalNodePos b = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 180)));
        data.registerNode(level, new TestNode(a, 1));
        data.registerNode(level, new TestNode(b, 1));
        GlareAddress address = GlareAddress.of(Items.RED_DYE, Items.GREEN_DYE, Items.BLUE_DYE);
        GlareAddress sender = GlareAddress.empty();
        UUID firstNetwork = requireNetwork(data, a);
        UUID secondNetwork = requireNetwork(data, b);
        for (int i = 0; i < 10; i++) {
            data.sendTelemetry(firstNetwork, new GlareMessage(sender, address, "a-" + i, i));
            data.sendTelemetry(secondNetwork, new GlareMessage(sender, address, "b-" + i, 10 + i));
        }

        data.tryAddLink(level, a, b);
        UUID mergedId = requireNetwork(data, a);
        require(mergedId.equals(requireNetwork(data, b)), "linked telemetry networks should merge");
        require(data.readTelemetry(mergedId, address).size() == 20, "network merge should temporarily preserve messages above the normal cap");

        data.sendTelemetry(mergedId, new GlareMessage(sender, address, "after-merge", 30));
        java.util.List<GlareMessage> trimmed = data.readTelemetry(mergedId, address);
        require(trimmed.size() == TelemetryService.MAX_MESSAGES, "first normal insertion after merge should restore the cap");
        require(trimmed.getLast().body().equals("after-merge"), "post-merge insertion should retain the new message");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void telemetrySplitCopiesAcrossUnloadedNodeAndSurvivesReload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos local = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 190)));
        DimensionalNodePos remote = new DimensionalNodePos(level.dimension(), new BlockPos(30_000_000, 80, 29_999_984));
        data.registerNode(level, new TestNode(local, 1));
        data.registerNode(level, new TestNode(remote, 1));
        data.tryAddLink(level, local, remote);
        GlareAddress address = GlareAddress.of(Items.COMPASS, Items.CLOCK, Items.PAPER);
        UUID joined = requireNetwork(data, local);
        GlareMessage original = new GlareMessage(GlareAddress.empty(), address, "persist me", 40);
        data.sendTelemetry(joined, original);

        GlareLink link = new GlareLink(local, remote);
        data.setLinkValidityForTests(link, GlareSavedData.LinkValidity.BLOCKED);
        UUID localNetwork = requireNetwork(data, local);
        UUID remoteNetwork = requireNetwork(data, remote);
        require(!localNetwork.equals(remoteNetwork), "split components must receive distinct network ids");
        require(data.readTelemetry(localNetwork, address).equals(java.util.List.of(original)), "local split should retain a telemetry copy");
        require(data.readTelemetry(remoteNetwork, address).equals(java.util.List.of(original)), "unloaded remote split should retain a telemetry copy");

        CompoundTag saved = data.save(new CompoundTag(), level.registryAccess());
        GlareSavedData loaded = GlareSavedData.loadForTests(saved);
        require(loaded.readTelemetry(requireNetwork(loaded, local), address).equals(java.util.List.of(original)), "local telemetry should survive saved-data reload");
        require(loaded.readTelemetry(requireNetwork(loaded, remote), address).equals(java.util.List.of(original)), "unloaded telemetry should survive saved-data reload");

        loaded.setLinkValidityForTests(link, GlareSavedData.LinkValidity.VALID);
        java.util.List<GlareMessage> remerged = loaded.readTelemetry(requireNetwork(loaded, local), address);
        require(remerged.size() == 1 && remerged.getFirst().id().equals(original.id()), "remerging split copies must not duplicate messages");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void telemetrySubscribersReceiveExactMutations(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos node = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 200)));
        data.registerNode(level, new TestNode(node, 1));
        UUID networkId = requireNetwork(data, node);
        GlareAddress address = GlareAddress.of(Items.BOOK, Items.FEATHER, Items.INK_SAC);
        AtomicInteger sends = new AtomicInteger();
        AtomicInteger discards = new AtomicInteger();
        long subscription = data.subscribeTelemetry(node, address, update -> {
            if (update.mutation() == TelemetryService.Mutation.SENT) sends.incrementAndGet();
            if (update.mutation() == TelemetryService.Mutation.DISCARDED) discards.incrementAndGet();
        });

        GlareMessage message = new GlareMessage(GlareAddress.empty(), address, "notify", 50);
        data.sendTelemetry(networkId, message);
        data.discardTelemetry(networkId, address, message.id());
        data.unsubscribeTelemetry(subscription);
        data.sendTelemetry(networkId, new GlareMessage(GlareAddress.empty(), address, "silent", 51));
        require(sends.get() == 1, "subscriber should receive one send mutation before unsubscribe");
        require(discards.get() == 1, "subscriber should receive the exact discard mutation");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void telemetryTerminalManualAndAutomaticOperations(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos senderRelative = new BlockPos(1, 2, 1);
        BlockPos receiverRelative = new BlockPos(3, 2, 1);
        helper.setBlock(senderRelative, ModBlocks.GLARE_TELEMETRY_TERMINAL.get());
        helper.setBlock(receiverRelative, ModBlocks.GLARE_TELEMETRY_TERMINAL.get());
        TelemetryTerminalBlockEntity sender = (TelemetryTerminalBlockEntity) level.getBlockEntity(helper.absolutePos(senderRelative));
        TelemetryTerminalBlockEntity receiver = (TelemetryTerminalBlockEntity) level.getBlockEntity(helper.absolutePos(receiverRelative));
        require(sender != null && receiver != null, "terminal block entities should be created");
        sender.setAddressSlot(0, new net.minecraft.world.item.ItemStack(Items.REDSTONE));
        sender.setAddressSlot(1, new net.minecraft.world.item.ItemStack(Items.IRON_INGOT));
        sender.setAddressSlot(2, new net.minecraft.world.item.ItemStack(Items.PAPER));
        receiver.setAddressSlot(0, new net.minecraft.world.item.ItemStack(Items.BLUE_DYE));
        receiver.setAddressSlot(1, new net.minecraft.world.item.ItemStack(Items.COPPER_INGOT));
        receiver.setAddressSlot(2, new net.minecraft.world.item.ItemStack(Items.BOOK));
        GlareSavedData data = GlareSavedData.get(level);
        data.tryAddLink(level, sender.getGlareNodePos(), receiver.getGlareNodePos());
        require(requireNetwork(data, sender.getGlareNodePos()).equals(requireNetwork(data, receiver.getGlareNodePos())), "terminals should share a network");
        require(requireNetworkRecord(data, sender.getGlareNodePos()).registeredTelemetryAddresses.contains(receiver.getTelemetryAddress()),
                "terminal address should register with its network even before receiving mail");

        sender.setDraft(false, receiver.getTelemetryAddress(), "manual hello");
        sender.sendManual();
        require(TelemetryService.readFromNode(level, receiver.getGlareNodePos(), receiver.getTelemetryAddress()).stream()
                .anyMatch(message -> message.body().equals("manual hello")), "manual send should deliver to a registered terminal address");

        receiver.setMode(TelemetryTerminalMode.AUTO_RECEIVE);
        receiver.addFilter("ALERT");
        receiver.setDiscardMatchingMessages(true);
        TelemetryService.sendFromNode(level, sender.getGlareNodePos(), sender.getTelemetryAddress(), receiver.getTelemetryAddress(), "system alert");
        helper.runAfterDelay(3, () -> {
            require(receiver.getLastPulseGameTime() > Long.MIN_VALUE, "matching auto-receive message should trigger a redstone pulse");
            require(TelemetryService.readFromNode(level, receiver.getGlareNodePos(), receiver.getTelemetryAddress()).stream()
                    .noneMatch(message -> message.body().equals("system alert")), "discard mode should remove the triggering message after subscribers run");
            helper.destroyBlock(senderRelative);
            helper.destroyBlock(receiverRelative);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void remoteEndpointMetadataSurvivesSavedDataReload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        GlareAddress address = GlareAddress.of(Items.GRASS_BLOCK, Items.REDSTONE, Items.ENDER_PEARL);
        DimensionalNodePos pos = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 90)));
        data.registerNode(level, new TestRemoteNode(pos, RemoteEndpointKind.TRANSPORTER, address,
                RemoteEntanglementMode.TRANSPORTER_RECEIVE_ONLY, true));

        CompoundTag saved = new CompoundTag();
        data.save(saved, level.registryAccess());
        GlareSavedData reloaded = GlareSavedData.loadForTests(saved);
        GlareSavedData.NodeRecord record = reloaded.getNode(pos).orElseThrow();

        require(record.remoteEndpointKind == RemoteEndpointKind.TRANSPORTER, "endpoint kind should persist");
        require(record.remoteAddress.equals(address), "endpoint address should persist");
        require(record.remoteMode == RemoteEntanglementMode.TRANSPORTER_RECEIVE_ONLY, "endpoint mode should persist");
        require(record.remoteAssembled, "assembly validity should persist while unloaded");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void stateOnlyRefreshPreservesTopologyAndNetworkIdentity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos emitter = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 12)));
        DimensionalNodePos receiver = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 12)));
        data.registerNode(level, new TestEmitterNode(emitter, 1, 8, DyeColor.RED, true));
        data.registerNode(level, new TestReceiverNode(receiver, 1, 1, GlareOperationStatus.ONLINE));
        data.tryAddLink(level, emitter, receiver);
        UUID originalNetwork = requireNetwork(data, emitter);

        data.updateNodeState(level, new TestEmitterNode(emitter, 1, 12, DyeColor.BLUE, true));

        GlareSavedData.NetworkRecord refreshed = requireNetworkRecord(data, receiver);
        require(originalNetwork.equals(requireNetwork(data, emitter)), "state refresh must preserve the network id");
        require(data.getLinksFor(emitter).size() == 1, "state refresh must preserve indexed topology");
        require(refreshed.luxCapacity == 12 && refreshed.luxAllocated == 1, "state refresh should update Lux locally");
        require(refreshed.colourCharges.getOrDefault(DyeColor.BLUE, 0) == 12,
                "state refresh should update colour aggregates locally");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void boundedLosValidationAdvancesRoundRobin(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        DimensionalNodePos a = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 14)));
        DimensionalNodePos b = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 14)));
        DimensionalNodePos c = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 14)));
        DimensionalNodePos d = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(7, 2, 14)));
        for (DimensionalNodePos pos : List.of(a, b, c, d)) data.registerNode(level, new TestNode(pos, 2));
        List<GlareLink> testLinks = List.of(new GlareLink(a, b), new GlareLink(b, c), new GlareLink(c, d));
        for (GlareLink link : testLinks) {
            data.tryAddLink(level, link.a(), link.b());
            data.setLinkValidityForTests(link, GlareSavedData.LinkValidity.UNKNOWN);
        }

        require(data.validateLoadedLinks(level, 1) <= 1, "one-link budget must bound the first validation tick");
        require(data.validateLoadedLinks(level, 1) <= 1, "one-link budget must bound the second validation tick");
        require(data.validateLoadedLinks(level, 1) <= 1, "one-link budget must bound the third validation tick");
        for (GlareLink link : testLinks) {
            require(data.getLinkValidity(link) == GlareSavedData.LinkValidity.VALID,
                    "round-robin validation should eventually visit every loaded link");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void remoteCandidateDiscoveryUsesNetworkAddressAndMode(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = GlareSavedData.get(level);
        GlareAddress address = GlareAddress.of(Items.GRASS_BLOCK, Items.REDSTONE, Items.ENDER_PEARL);
        DimensionalNodePos sourcePos = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 92)));
        DimensionalNodePos receiverPos = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 92)));
        DimensionalNodePos sendOnlyPos = new DimensionalNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 92)));
        data.unregisterLoadedNode(sourcePos);
        data.unregisterLoadedNode(receiverPos);
        data.unregisterLoadedNode(sendOnlyPos);

        data.registerNode(level, new TestRemoteNode(sourcePos, RemoteEndpointKind.TRANSPORTER, address,
                RemoteEntanglementMode.TRANSPORTER_AUTO, true));
        data.registerNode(level, new TestRemoteNode(receiverPos, RemoteEndpointKind.TRANSPORTER, address,
                RemoteEntanglementMode.TRANSPORTER_RECEIVE_ONLY, true));
        data.registerNode(level, new TestRemoteNode(sendOnlyPos, RemoteEndpointKind.TRANSPORTER, address,
                RemoteEntanglementMode.TRANSPORTER_SEND_ONLY, true));
        data.tryAddLink(level, sourcePos, receiverPos);
        data.tryAddLink(level, receiverPos, sendOnlyPos);

        List<GlareSavedData.NodeRecord> candidates = RemoteEntanglementService.findCandidates(level, sourcePos,
                RemoteEndpointKind.TRANSPORTER, address);
        require(candidates.size() == 1 && candidates.getFirst().pos.equals(receiverPos),
                "only an assembled receive-capable endpoint with the exact address should match");

        data.unregisterLoadedNode(sourcePos);
        data.unregisterLoadedNode(receiverPos);
        data.unregisterLoadedNode(sendOnlyPos);
        helper.succeed();
    }

    private static UUID requireNetwork(GlareSavedData data, DimensionalNodePos pos) {
        return data.getNode(pos)
                .flatMap(node -> java.util.Optional.ofNullable(node.networkId))
                .orElseThrow(() -> new AssertionError("node has no network: " + pos.toShortString()));
    }

    private static GlareSavedData.NetworkRecord requireNetworkRecord(GlareSavedData data, DimensionalNodePos pos) {
        UUID networkId = requireNetwork(data, pos);
        return data.getNetwork(networkId).orElseThrow(() -> new AssertionError("missing network record: " + networkId));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record TestNode(DimensionalNodePos pos, int maxLinks) implements IGlareNode {
        @Override
        public int getMaxGlareLinks() {
            return maxLinks;
        }

        @Override
        public DimensionalNodePos getGlareNodePos() {
            return pos;
        }
    }

    private record TestEmitterNode(DimensionalNodePos pos, int maxLinks, int producedLux, DyeColor colour, boolean enabled) implements IGlareNode, IGlareEmitter {
        @Override
        public int getMaxGlareLinks() {
            return maxLinks;
        }

        @Override
        public DimensionalNodePos getGlareNodePos() {
            return pos;
        }

        @Override
        public int getProducedLux() {
            return producedLux;
        }

        @Override
        public DyeColor getLuxColourCharge() {
            return colour;
        }

        @Override
        public boolean isGlareEmitterEnabled() {
            return enabled;
        }
    }

    private record TestReceiverNode(DimensionalNodePos pos, int maxLinks, int allocatedLux, GlareOperationStatus status) implements IGlareNode, IGlareReceiver {
        @Override
        public int getMaxGlareLinks() {
            return maxLinks;
        }

        @Override
        public DimensionalNodePos getGlareNodePos() {
            return pos;
        }

        @Override
        public int getAllocatedLux() {
            return allocatedLux;
        }

        @Override
        public GlareOperationStatus getGlareOperationStatus() {
            return status;
        }

        @Override
        public void setGlareOperationStatus(GlareOperationStatus status) {}
    }

    private record TestRemoteNode(DimensionalNodePos pos, RemoteEndpointKind kind, GlareAddress address,
                                  RemoteEntanglementMode mode, boolean assembled) implements IGlareNode, IRemoteEntanglementEndpoint {
        @Override public int getMaxGlareLinks() { return 8; }
        @Override public DimensionalNodePos getGlareNodePos() { return pos; }
        @Override public RemoteEndpointKind getRemoteEndpointKind() { return kind; }
        @Override public GlareAddress getRemoteAddress() { return address; }
        @Override public RemoteEntanglementMode getRemoteMode() { return mode; }
        @Override public boolean isRemoteEndpointAssembled() { return assembled; }
    }

    private record TestNodes(TestNode a, TestNode b, TestNode c) {
        static TestNodes create(ServerLevel level, GameTestHelper helper, int zOffset) {
            BlockPos origin = helper.absolutePos(new BlockPos(1, 2, zOffset));
            return new TestNodes(
                    new TestNode(new DimensionalNodePos(level.dimension(), origin), 8),
                    new TestNode(new DimensionalNodePos(level.dimension(), origin.offset(2, 0, 0)), 8),
                    new TestNode(new DimensionalNodePos(level.dimension(), origin.offset(4, 0, 0)), 8)
            );
        }

        void clear(GlareSavedData data) {
            data.unregisterLoadedNode(a.pos);
            data.unregisterLoadedNode(b.pos);
            data.unregisterLoadedNode(c.pos);
        }
    }
}
