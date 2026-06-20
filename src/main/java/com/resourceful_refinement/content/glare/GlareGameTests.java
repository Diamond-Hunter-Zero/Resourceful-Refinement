package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.UUID;

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
        GlareNodePos emitter = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 35)));
        GlareNodePos relay = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 35)));
        GlareNodePos receiver = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 35)));

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
        GlareNodePos emitter = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 37)));
        GlareNodePos receiver = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 37)));

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
        GlareSavedData data = GlareSavedData.get(level);
        GlareNodePos a = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 60)));
        GlareNodePos b = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 60)));
        GlareNodePos c = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 60)));
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
        GlareNodePos remote = new GlareNodePos(level.dimension(), new BlockPos(30_000_000, 80, 30_000_000));
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
        GlareNodePos whiteEmitter = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 110)));
        GlareNodePos redEmitter = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 110)));
        GlareNodePos receiver = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 110)));

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
        GlareNodePos emitter = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 120)));
        GlareNodePos receiver = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 120)));

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
    public static void overloadLatchesUntilReset(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GlareSavedData data = new GlareSavedData();
        GlareNodePos weakEmitter = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 130)));
        GlareNodePos extraEmitter = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(3, 2, 130)));
        GlareNodePos receiver = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(5, 2, 130)));

        data.registerNode(level, new TestEmitterNode(weakEmitter, 8, 2, DyeColor.WHITE, true));
        data.registerNode(level, new TestEmitterNode(extraEmitter, 8, 2, DyeColor.BLUE, true));
        data.registerNode(level, new TestReceiverNode(receiver, 8, 3, GlareOperationStatus.ONLINE));
        data.tryAddLink(level, weakEmitter, receiver);

        GlareSavedData.NetworkRecord overloaded = requireNetworkRecord(data, receiver);
        require(overloaded.overloaded, "network should overload when allocation exceeds capacity");
        require(data.getNode(receiver).orElseThrow().status == GlareOperationStatus.OVERLOADED, "receiver record should be overloaded");
        require(!data.tryResetNetwork(overloaded.id), "reset should fail while still over-allocated");

        data.tryAddLink(level, extraEmitter, receiver);
        GlareSavedData.NetworkRecord latched = requireNetworkRecord(data, receiver);
        require(latched.luxCapacity == 4 && latched.luxAllocated == 3, "extra emitter should fix capacity");
        require(latched.overloaded, "network should remain latched overloaded until reset");
        require(data.tryResetNetwork(latched.id), "reset should succeed once allocation fits capacity");

        GlareSavedData.NetworkRecord reset = requireNetworkRecord(data, receiver);
        require(!reset.overloaded, "network should clear overload after successful reset");
        require(data.getNode(receiver).orElseThrow().status == GlareOperationStatus.ONLINE, "receiver should return online after reset");
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
        GlareNodePos emitter = new GlareNodePos(level.dimension(), new BlockPos(30_000_000, 80, 30_000_000));
        GlareNodePos transceiver = new GlareNodePos(level.dimension(), helper.absolutePos(new BlockPos(1, 2, 150)));
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

    private static UUID requireNetwork(GlareSavedData data, GlareNodePos pos) {
        return data.getNode(pos)
                .flatMap(node -> java.util.Optional.ofNullable(node.networkId))
                .orElseThrow(() -> new AssertionError("node has no network: " + pos.toShortString()));
    }

    private static GlareSavedData.NetworkRecord requireNetworkRecord(GlareSavedData data, GlareNodePos pos) {
        UUID networkId = requireNetwork(data, pos);
        return data.getNetwork(networkId).orElseThrow(() -> new AssertionError("missing network record: " + networkId));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record TestNode(GlareNodePos pos, int maxLinks) implements IGlareNode {
        @Override
        public int getMaxGlareLinks() {
            return maxLinks;
        }

        @Override
        public GlareNodePos getGlareNodePos() {
            return pos;
        }
    }

    private record TestEmitterNode(GlareNodePos pos, int maxLinks, int producedLux, DyeColor colour, boolean enabled) implements IGlareNode, IGlareEmitter {
        @Override
        public int getMaxGlareLinks() {
            return maxLinks;
        }

        @Override
        public GlareNodePos getGlareNodePos() {
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

    private record TestReceiverNode(GlareNodePos pos, int maxLinks, int allocatedLux, GlareOperationStatus status) implements IGlareNode, IGlareReceiver {
        @Override
        public int getMaxGlareLinks() {
            return maxLinks;
        }

        @Override
        public GlareNodePos getGlareNodePos() {
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

    private record TestNodes(TestNode a, TestNode b, TestNode c) {
        static TestNodes create(ServerLevel level, GameTestHelper helper, int zOffset) {
            BlockPos origin = helper.absolutePos(new BlockPos(1, 2, zOffset));
            return new TestNodes(
                    new TestNode(new GlareNodePos(level.dimension(), origin), 8),
                    new TestNode(new GlareNodePos(level.dimension(), origin.offset(2, 0, 0)), 8),
                    new TestNode(new GlareNodePos(level.dimension(), origin.offset(4, 0, 0)), 8)
            );
        }

        void clear(GlareSavedData data) {
            data.unregisterLoadedNode(a.pos);
            data.unregisterLoadedNode(b.pos);
            data.unregisterLoadedNode(c.pos);
        }
    }
}
