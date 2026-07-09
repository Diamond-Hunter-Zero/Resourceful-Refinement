package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.function.Function;
import io.netty.buffer.Unpooled;

@GameTestHolder(ResourcefulRefinementMain.MOD_ID)
public final class PugGameTests {
    private PugGameTests() {}

    @GameTest(template = "empty")
    public static void routeQuoteUsesStartedHorizontalSteps(GameTestHelper helper) {
        PugRouteParameters parameters = PugRouteParameters.defaults();
        PugRouteQuote zero = PugRouteCalculator.quote(Level.OVERWORLD, BlockPos.ZERO, Level.OVERWORLD,
                new BlockPos(0, 200, 0), parameters);
        require(zero.horizontalDistance() == 0 && zero.fuelCostMb() == 500 && zero.travelTicks() == 400,
                "vertical separation must not affect the base route quote");

        PugRouteQuote boundary = PugRouteCalculator.quote(Level.OVERWORLD, BlockPos.ZERO, Level.OVERWORLD,
                new BlockPos(1_000, 80, 0), parameters);
        require(boundary.fuelCostMb() == 1_000, "exactly 1,000 blocks should use one fuel step");
        require(boundary.travelTicks() == 800, "1,000 blocks should use twenty travel steps");

        PugRouteQuote overBoundary = PugRouteCalculator.quote(Level.OVERWORLD, BlockPos.ZERO, Level.OVERWORLD,
                new BlockPos(1_001, 0, 0), parameters);
        require(overBoundary.fuelCostMb() == 1_500, "a started second fuel step must be charged");
        require(overBoundary.travelTicks() == 820, "a started twenty-first travel step must be charged");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crossDimensionRouteAddsFlatSurcharges(GameTestHelper helper) {
        PugRouteQuote quote = PugRouteCalculator.quote(Level.OVERWORLD, BlockPos.ZERO, Level.NETHER,
                new BlockPos(1_000, 0, 0), PugRouteParameters.defaults());
        require(quote.crossDimension(), "different dimension keys should mark a cross-dimension route");
        require(quote.fuelCostMb() == 3_000, "cross-dimension fuel surcharge should be added once");
        require(quote.travelTicks() == 3_200, "cross-dimension travel surcharge should be added once");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void defaultTankRangeMatchesIndustrialProfile(GameTestHelper helper) {
        PugRouteParameters parameters = PugRouteParameters.defaults();
        PugRouteQuote sameDimensionLimit = PugRouteCalculator.quote(Level.OVERWORLD, BlockPos.ZERO, Level.OVERWORLD,
                new BlockPos(31_000, 0, 0), parameters);
        PugRouteQuote sameDimensionOver = PugRouteCalculator.quote(Level.OVERWORLD, BlockPos.ZERO, Level.OVERWORLD,
                new BlockPos(31_001, 0, 0), parameters);
        PugRouteQuote crossDimensionLimit = PugRouteCalculator.quote(Level.OVERWORLD, BlockPos.ZERO, Level.END,
                new BlockPos(27_000, 0, 0), parameters);
        require(sameDimensionLimit.fuelCostMb() == PugRouteParameters.DEFAULT_TANK_CAPACITY_MB,
                "default same-dimension maximum should fit exactly in one tank");
        require(sameDimensionOver.fuelCostMb() > PugRouteParameters.DEFAULT_TANK_CAPACITY_MB,
                "one block into the next step should exceed the default tank");
        require(crossDimensionLimit.fuelCostMb() == PugRouteParameters.DEFAULT_TANK_CAPACITY_MB,
                "default cross-dimension maximum should fit exactly in one tank");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void flightRecordDefensivelyCopiesCargo(GameTestHelper helper) {
        GlareAddress address = GlareAddress.of(Items.REDSTONE, Items.COMPASS, Items.PAPER);
        LaunchpadEndpoint source = new LaunchpadEndpoint(Level.OVERWORLD, BlockPos.ZERO, address, LaunchpadMode.SEND);
        LaunchpadEndpoint destination = new LaunchpadEndpoint(Level.END, new BlockPos(10, 70, 20), address,
                LaunchpadMode.RECEIVE);
        ItemStack original = new ItemStack(Items.IRON_INGOT, 32);
        List<ItemStack> cargo = new ArrayList<>(List.of(original));
        PugFlightRecord record = new PugFlightRecord(UUID.randomUUID(), cargo, source, destination, 100, 20,
                PugFlightState.IN_TRANSIT);
        original.setCount(1);
        cargo.clear();
        List<ItemStack> firstRead = record.cargo();
        firstRead.getFirst().setCount(2);
        require(record.cargo().size() == 1 && record.cargo().getFirst().getCount() == 32,
                "flight cargo must be isolated from caller and accessor mutation");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadFootprintRotatesWithoutChangingShape(GameTestHelper helper) {
        BlockPos origin = new BlockPos(20, 70, 20);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            List<BlockPos> footprint = LaunchpadStructure.footprint(origin, facing);
            require(footprint.size() == 9 && new HashSet<>(footprint).size() == 9,
                    "each orientation must contain nine unique footprint blocks");
            require(LaunchpadStructure.proxyPositions(origin, facing).size() == 8,
                    "the controller must be accompanied by eight proxies");
            require(LaunchpadStructure.rearCenter(origin, facing).equals(origin.relative(facing.getOpposite(), 2)),
                    "rear-center fuel position must be two blocks behind the controller");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadAssemblyPlacesOwnedProxies(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "clear 3x3 footprint should assemble");
        require(controller.isAssembled(), "controller should record assembled state");
        require(controller.getBlockState().getValue(LaunchpadControllerBlock.ASSEMBLED),
                "assembled state should synchronize to the block state");
        for (LaunchpadStructure.ProxyPosition expected : LaunchpadStructure.proxyPositions(
                controller.getBlockPos(), Direction.NORTH)) {
            require(helper.getLevel().getBlockEntity(expected.pos()) instanceof LaunchpadProxyBlockEntity proxy
                            && proxy.belongsTo(controller.getBlockPos(), expected.lateral(), expected.depth()),
                    "every proxy must persist its exact owner and local coordinates");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedLaunchpadAssemblyIsAtomic(GameTestHelper helper) {
        BlockPos controllerRelative = controllerRelativePos();
        BlockPos obstructionRelative = controllerRelative.offset(-1, 0, 0);
        helper.setBlock(obstructionRelative, Blocks.STONE);
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(!controller.tryAssemble(), "occupied footprint should reject assembly");
        require(!controller.isAssembled(), "failed assembly must leave the controller incomplete");
        long proxies = LaunchpadStructure.proxyPositions(controller.getBlockPos(), Direction.NORTH).stream()
                .filter(proxy -> helper.getLevel().getBlockState(proxy.pos()).is(ModBlocks.LAUNCHPAD_PROXY.get()))
                .count();
        require(proxies == 0, "preflight failure must not leave partial proxies");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingProxyDestroysWholeLaunchpad(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "test launchpad should assemble");
        controller.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
        BlockPos controllerPos = controller.getBlockPos();
        BlockPos brokenProxy = LaunchpadStructure.position(controllerPos, Direction.NORTH, 1, 1);
        helper.getLevel().destroyBlock(brokenProxy, false);
        require(helper.getLevel().getBlockState(controllerPos).isAir(),
                "breaking an owned proxy must destroy the controller");
        for (LaunchpadStructure.ProxyPosition expected : LaunchpadStructure.proxyPositions(controllerPos,
                Direction.NORTH)) {
            require(helper.getLevel().getBlockState(expected.pos()).isAir(),
                    "destroying one component must remove all remaining proxies");
        }
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(controllerPos).inflate(2));
        int diamonds = drops.stream().filter(drop -> drop.getItem().is(Items.DIAMOND))
                .mapToInt(drop -> drop.getItem().getCount()).sum();
        int controllers = drops.stream().filter(drop -> drop.getItem().is(ModBlocks.LAUNCHPAD_CONTROLLER.asItem()))
                .mapToInt(drop -> drop.getItem().getCount()).sum();
        require(diamonds == 7, "teardown must drop the controller inventory exactly once");
        require(controllers == 1, "breaking any assembly component must drop one controller item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadTeardownNeverRemovesForeignProxy(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        LaunchpadStructure.ProxyPosition expected = LaunchpadStructure.proxyPositions(controller.getBlockPos(),
                Direction.NORTH).getFirst();
        helper.getLevel().setBlockAndUpdate(expected.pos(), ModBlocks.LAUNCHPAD_PROXY.get().defaultBlockState());
        LaunchpadProxyBlockEntity proxy = (LaunchpadProxyBlockEntity) helper.getLevel().getBlockEntity(expected.pos());
        require(proxy != null, "foreign proxy block entity should exist");
        proxy.setControllerData(controller.getBlockPos().offset(20, 0, 0), expected.lateral(), expected.depth());
        controller.removeAssemblyProxies();
        require(helper.getLevel().getBlockState(expected.pos()).is(ModBlocks.LAUNCHPAD_PROXY.get()),
                "teardown must not remove a proxy owned by another controller");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadProxyCapabilitiesRespectFacesAndFuelTag(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "test launchpad should assemble");
        Level level = helper.getLevel();
        BlockPos itemProxy = LaunchpadStructure.position(controller.getBlockPos(), Direction.NORTH, 1, 1);
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, itemProxy, Direction.EAST);
        require(itemHandler != null, "non-rear proxy horizontal faces should expose item automation");
        require(itemHandler.insertItem(0, new ItemStack(Items.IRON_INGOT, 32), false).isEmpty(),
                "item proxy should insert into controller inventory");
        require(controller.inventory.getStackInSlot(0).getCount() == 32,
                "proxy item capability must route to the shared twelve-slot inventory");

        BlockPos rear = LaunchpadStructure.rearCenter(controller.getBlockPos(), Direction.NORTH);
        require(level.getCapability(Capabilities.ItemHandler.BLOCK, rear, Direction.EAST) == null,
                "rear-center proxy must be reserved for fuel rather than items");
        IFluidHandler fuel = level.getCapability(Capabilities.FluidHandler.BLOCK, rear, Direction.SOUTH);
        require(fuel != null, "rear-facing side of rear-center proxy should expose fuel insertion");
        require(level.getCapability(Capabilities.FluidHandler.BLOCK, rear, Direction.NORTH) == null,
                "fuel capability must not leak onto other faces");
        require(fuel.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1_000),
                IFluidHandler.FluidAction.EXECUTE) == 0, "non-tagged fluids must be rejected");
        require(fuel.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 1_000),
                IFluidHandler.FluidAction.EXECUTE) == 1_000, "tagged carborax fuel should be accepted");
        require(fuel.drain(1_000, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                "external fuel capability must be insertion-only");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadSkyCheckCoversWholeFootprint(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble() && controller.hasClearSky(), "uncovered pad should have clear sky");
        BlockPos coveredProxy = LaunchpadStructure.position(controller.getBlockPos(), Direction.NORTH, -1, 2);
        helper.getLevel().setBlockAndUpdate(coveredProxy.above(), Blocks.GLASS.defaultBlockState());
        require(!controller.hasClearSky(), "a block above any of the nine columns must obstruct the pad");
        helper.getLevel().removeBlock(coveredProxy.above(), false);
        require(controller.hasClearSky(), "removing the obstruction should immediately restore sky access");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadTriggerStateTracksTimerAndRisingEdges(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "test launchpad should assemble");
        controller.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.SEND, GlareAddress.empty(),
                GlareAddress.empty(), LaunchCondition.TIMER, 1));
        controller.inventory.setStackInSlot(0, new ItemStack(Items.COPPER_INGOT));
        for (int tick = 0; tick < 20; tick++) controller.serverTick();
        require(controller.isTimerReady() && controller.getTimerTicksElapsed() == 20,
                "timer should begin with first cargo and stop at its configured duration");
        controller.inventory.setStackInSlot(0, ItemStack.EMPTY);
        controller.serverTick();
        require(controller.getTimerTicksElapsed() == 0, "removing all cargo should reset the timer");

        controller.updateRedstoneState(false);
        controller.updateRedstoneState(true);
        require(controller.consumePendingRedstoneLaunch(), "rising edge should queue one launch");
        controller.updateRedstoneState(true);
        require(!controller.consumePendingRedstoneLaunch(), "steady power must not queue repeated launches");
        controller.updateRedstoneState(false);
        controller.updateRedstoneState(true);
        require(controller.consumePendingRedstoneLaunch(), "a later rising edge should queue another launch");
        controller.setRoundRobinCursor(LaunchpadControllerBlockEntity.INVENTORY_SLOTS + 1);
        require(controller.getRoundRobinCursor() == 1, "round-robin cursor should wrap within twelve slots");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadMachineStateSurvivesNbtRoundTrip(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "test launchpad should assemble");
        GlareAddress local = GlareAddress.of(Items.REDSTONE, Items.COMPASS, Items.PAPER);
        GlareAddress destination = GlareAddress.of(Items.ENDER_PEARL, Items.CLOCK, Items.DIAMOND);
        controller.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.SEND, local, destination,
                LaunchCondition.TIMER, 10));
        controller.inventory.setStackInSlot(4, new ItemStack(Items.GOLD_INGOT, 23));
        controller.fuelTank.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        controller.setRoundRobinCursor(9);
        controller.updateRedstoneState(false);
        controller.updateRedstoneState(true);
        for (int tick = 0; tick < 5; tick++) controller.serverTick();

        CompoundTag saved = new CompoundTag();
        controller.write(saved, helper.getLevel().registryAccess(), false);
        LaunchpadControllerBlockEntity restored = new LaunchpadControllerBlockEntity(controller.getBlockPos(),
                controller.getBlockState());
        restored.read(saved, helper.getLevel().registryAccess(), false);

        require(restored.getConfiguration().equals(controller.getConfiguration()),
                "mode, addresses, launch condition, and timer configuration must persist");
        require(restored.inventory.getStackInSlot(4).getCount() == 23,
                "twelve-slot inventory contents must persist");
        require(restored.fuelTank.getFluidAmount() == 2_000, "fuel tank contents must persist");
        require(restored.getRoundRobinCursor() == 9, "round-robin cursor must persist");
        require(restored.hasPendingRedstoneLaunch(), "queued redstone edge must persist");
        require(restored.getTimerTicksElapsed() == 5, "active launch timer progress must persist");
        require(restored.isAssembled(), "assembly state must persist until live validation occurs");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pugSavedDataRoundTripPreservesEndpointIndex(GameTestHelper helper) {
        PugSavedData data = new PugSavedData();
        GlareAddress address = GlareAddress.of(Items.REDSTONE, Items.ENDER_PEARL, Items.COMPASS);
        LaunchpadId receiveId = new LaunchpadId(Level.OVERWORLD, new BlockPos(100, 70, 200));
        LaunchpadId sendId = new LaunchpadId(Level.END, new BlockPos(-50, 80, 20));
        data.upsert(new PugSavedData.PadRecord(receiveId, LaunchpadMode.RECEIVE, address, true));
        data.upsert(new PugSavedData.PadRecord(sendId, LaunchpadMode.SEND, address, false));
        UUID flightId = UUID.randomUUID();
        data.putFlight(new PugFlightRecord(flightId, List.of(new ItemStack(Items.COPPER_INGOT, 17)),
                new LaunchpadEndpoint(sendId.dimension(), sendId.controllerPos(), address, LaunchpadMode.SEND),
                new LaunchpadEndpoint(receiveId.dimension(), receiveId.controllerPos(), address,
                        LaunchpadMode.RECEIVE), 2_000, 500, PugFlightState.IN_TRANSIT));

        CompoundTag saved = data.save(new CompoundTag(), helper.getLevel().registryAccess());
        PugSavedData restored = PugSavedData.loadForTests(saved, helper.getLevel().registryAccess());
        require(restored.getPads().size() == 2, "all endpoint records must survive saved-data reload");
        List<PugSavedData.PadRecord> receivers = restored.findReceivers(address);
        require(receivers.size() == 1 && receivers.getFirst().id().equals(receiveId),
                "only RECEIVE endpoints should be restored into the address index");
        require(receivers.getFirst().skyClear(), "cached sky validity must survive reload");
        PugFlightRecord restoredFlight = restored.getFlight(flightId).orElseThrow();
        require(restoredFlight.elapsedTravelTicks() == 500 && restoredFlight.totalTravelTicks() == 2_000
                        && restoredFlight.cargo().getFirst().getCount() == 17,
                "active flight timing, identity, state, and cargo must survive saved-data reload");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pugAddressIndexTracksModeAndDuplicateReceivers(GameTestHelper helper) {
        PugSavedData data = new PugSavedData();
        GlareAddress address = GlareAddress.of(Items.RED_DYE, Items.GREEN_DYE, Items.BLUE_DYE);
        LaunchpadId first = new LaunchpadId(Level.OVERWORLD, new BlockPos(1, 64, 1));
        LaunchpadId second = new LaunchpadId(Level.END, new BlockPos(2, 64, 2));
        data.upsert(new PugSavedData.PadRecord(first, LaunchpadMode.RECEIVE, address, true));
        data.upsert(new PugSavedData.PadRecord(second, LaunchpadMode.RECEIVE, address, false));
        require(data.findReceivers(address).size() == 2,
                "duplicate receive addresses must retain every candidate regardless of sky state");
        data.upsert(new PugSavedData.PadRecord(second, LaunchpadMode.SEND, address, false));
        require(data.findReceivers(address).size() == 1,
                "changing an endpoint to SEND must remove it from the receive index");
        data.remove(first);
        require(data.findReceivers(address).isEmpty(), "removing the final receiver must clean up its index key");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void assembledControllerRegistersAndRemovalUnregistersEndpoint(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "test launchpad should assemble");
        GlareAddress address = GlareAddress.of(Items.IRON_INGOT, Items.CLOCK, Items.PAPER);
        controller.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.RECEIVE, address,
                GlareAddress.empty(), LaunchCondition.WHEN_FULL, 120));
        LaunchpadId id = LaunchpadId.of(helper.getLevel(), controller.getBlockPos());
        PugSavedData data = PugSavedData.get(helper.getLevel());
        PugSavedData.PadRecord record = data.getPad(id).orElseThrow();
        require(record.mode() == LaunchpadMode.RECEIVE && record.address().equals(address),
                "assembled controller state must be reflected in global saved data");
        require(PugChunkLoading.shouldRetainTicket(data, helper.getLevel(), controller.getBlockPos(),
                        new net.minecraft.world.level.ChunkPos(controller.getBlockPos()).toLong(), true),
                "assembled endpoint should own one ticking ticket for its controller chunk");
        require(!PugChunkLoading.shouldRetainTicket(data, helper.getLevel(), controller.getBlockPos(),
                        new net.minecraft.world.level.ChunkPos(controller.getBlockPos()).toLong(), false),
                "launchpad ticket must be ticking rather than a passive load ticket");

        helper.getLevel().destroyBlock(controller.getBlockPos(), false);
        require(data.getPad(id).isEmpty(), "controller removal must unregister its endpoint immediately");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void destinationResolutionRejectsDuplicatesAndObstructedSky(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "test launchpad should assemble");
        GlareAddress address = GlareAddress.of(Items.DIAMOND, Items.ENDER_PEARL, Items.COMPASS);
        controller.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.RECEIVE, address,
                GlareAddress.empty(), LaunchCondition.WHEN_FULL, 120));
        PugDestinationResult found = PugService.resolveDestination(helper.getLevel().getServer(), address);
        require(found.found() && found.endpoint().controllerPos().equals(controller.getBlockPos()),
                "one live clear receiver should resolve to its exact endpoint identity");

        PugSavedData data = PugSavedData.get(helper.getLevel());
        LaunchpadId duplicate = new LaunchpadId(Level.END, new BlockPos(1_000, 70, 1_000));
        data.upsert(new PugSavedData.PadRecord(duplicate, LaunchpadMode.RECEIVE, address, false));
        PugDestinationResult ambiguous = PugService.resolveDestination(helper.getLevel().getServer(), address);
        require(ambiguous.status() == PugDestinationResult.Status.AMBIGUOUS && ambiguous.matchingPads() == 2,
                "multiple receive records must invalidate the address before live endpoint checks");
        data.remove(duplicate);

        helper.getLevel().setBlockAndUpdate(controller.getBlockPos().above(), Blocks.GLASS.defaultBlockState());
        PugDestinationResult obstructed = PugService.resolveDestination(helper.getLevel().getServer(), address);
        require(obstructed.status() == PugDestinationResult.Status.SKY_OBSTRUCTED,
                "a unique receiver without full-footprint sky access must be rejected");
        helper.getLevel().removeBlock(controller.getBlockPos().above(), false);
        helper.getLevel().destroyBlock(controller.getBlockPos(), false);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chunkReconciliationRemovesOrphanedEndpoint(GameTestHelper helper) {
        PugSavedData data = PugSavedData.get(helper.getLevel());
        BlockPos orphanPos = helper.absolutePos(new BlockPos(2, 1, 2));
        LaunchpadId orphan = LaunchpadId.of(helper.getLevel(), orphanPos);
        data.upsert(new PugSavedData.PadRecord(orphan, LaunchpadMode.RECEIVE,
                GlareAddress.of(Items.COAL, Items.FLINT, Items.PAPER), true));
        PugService.reconcileChunk(helper.getLevel(), helper.getLevel().getChunkAt(orphanPos));
        require(data.getPad(orphan).isEmpty(),
                "loaded chunk reconciliation must remove saved endpoints whose controller no longer exists");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cargoCreatesPersistentDockedPug(GameTestHelper helper) {
        LaunchpadControllerBlockEntity controller = placeController(helper, Direction.NORTH);
        require(controller.tryAssemble(), "test launchpad should assemble");
        controller.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 8));
        controller.serverTick();
        PugEntity pug = controller.getDockedPug();
        require(pug != null && pug.getFlightState() == PugFlightState.DOCKED,
                "first cargo should create one docked PUG entity");
        require(pug.getBbWidth() >= 1.8F && pug.getBbWidth() <= 2.1F
                        && pug.getBbHeight() >= 1.8F && pug.getBbHeight() <= 2.1F,
                "PUG hitbox should remain approximately two blocks cubed");
        Vec3 expected = PugFlightService.padCenter(controller);
        require(pug.position().distanceToSqr(expected) < 0.001,
                "docked PUG should remain centered over the pad");

        CompoundTag controllerTag = new CompoundTag();
        controller.write(controllerTag, helper.getLevel().registryAccess(), false);
        LaunchpadControllerBlockEntity restored = new LaunchpadControllerBlockEntity(controller.getBlockPos(),
                controller.getBlockState());
        restored.read(controllerTag, helper.getLevel().registryAccess(), false);
        require(pug.getUUID().equals(restored.getDockedPugId()),
                "controller must persist the docked entity identity across reloads");
        pug.discard();
        helper.getLevel().destroyBlock(controller.getBlockPos(), false);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchTransfersRoundRobinCargoAndFuelIntoAscendingEntity(GameTestHelper helper) {
        LaunchpadPair pair = placeLaunchpadPair(helper,
                GlareAddress.of(Items.ENDER_PEARL, Items.COMPASS, Items.PAPER));
        pair.source().inventory.setStackInSlot(10, new ItemStack(Items.IRON_INGOT, 11));
        pair.source().inventory.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT, 12));
        pair.source().inventory.setStackInSlot(3, new ItemStack(Items.COPPER_INGOT, 13));
        pair.source().setRoundRobinCursor(10);
        pair.source().fuelTank.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        pair.source().serverTick();
        armRedstoneLaunch(pair.source());

        LaunchAttemptResult result = pair.source().attemptLaunch();
        require(result.launched() && result.fuelConsumedMb() == 1_000,
                "short same-dimension route should consume the industrial base plus one distance step");
        PugEntity pug = (PugEntity) helper.getLevel().getEntity(result.pugId());
        require(pug != null && pug.getFlightState() == PugFlightState.ASCENDING,
                "successful launch should transition the docked entity into ASCENDING");
        List<ItemStack> cargo = pug.copyCargo();
        require(cargo.size() == 3 && cargo.get(0).is(Items.IRON_INGOT)
                        && cargo.get(1).is(Items.GOLD_INGOT) && cargo.get(2).is(Items.COPPER_INGOT),
                "round-robin scan order and complete stack identity must transfer to the PUG");
        require(!pair.source().hasCargo() && pair.source().fuelTank.getFluidAmount() == 1_000,
                "launch must atomically remove selected cargo and required fuel from the source");
        require(pair.source().getRoundRobinCursor() == 4 && pair.source().getDockedPugId() == null,
                "cursor should advance past the last selected slot and release the dock claim");
        cleanupPair(helper, pair, pug);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ascentHandoffCreatesOnePersistentSimulatedFlight(GameTestHelper helper) {
        LaunchpadPair pair = placeLaunchpadPair(helper,
                GlareAddress.of(Items.DIAMOND, Items.CLOCK, Items.BOOK));
        pair.source().inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        pair.source().fuelTank.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        pair.source().serverTick();
        armRedstoneLaunch(pair.source());
        LaunchAttemptResult result = pair.source().attemptLaunch();
        PugEntity pug = (PugEntity) helper.getLevel().getEntity(result.pugId());
        require(pug != null, "launched PUG should exist before ascent handoff");
        double initialVelocity = pug.getVerticalVelocity();
        for (int tick = 0; tick < 8; tick++) pug.tick();
        require(pug.getVerticalVelocity() > initialVelocity
                        && pug.getVerticalVelocity() <= PugEntity.ASCENT_SPEED,
                "ascending PUG should accelerate gradually up to its maximum velocity");
        CompoundTag ascendingTag = new CompoundTag();
        require(pug.save(ascendingTag), "ascending PUG should serialize through vanilla entity persistence");
        Entity reloadedAscent = EntityType.loadEntityRecursive(ascendingTag, helper.getLevel(), Function.identity());
        require(reloadedAscent instanceof PugEntity restoredAscent
                        && restoredAscent.getFlightState() == PugFlightState.ASCENDING
                        && restoredAscent.copyCargo().getFirst().getCount() == 4
                        && restoredAscent.getVerticalVelocity() == pug.getVerticalVelocity()
                        && restoredAscent.getDestinationEndpoint().equals(pug.getDestinationEndpoint()),
                "mid-ascent reload must preserve state, velocity, endpoint identity, and cargo");
        pug.setPos(pug.getX(), helper.getLevel().getMaxBuildHeight() - pug.getBbHeight(), pug.getZ());
        pug.tick();

        require(pug.isRemoved(), "physical ascent entity should be discarded after world-height handoff");
        PugFlightRecord flight = PugSavedData.get(helper.getLevel()).getFlight(result.pugId()).orElseThrow();
        require(flight.state() == PugFlightState.IN_TRANSIT && flight.elapsedTravelTicks() == 0
                        && flight.cargo().getFirst().getCount() == 4,
                "handoff must preserve UUID, cargo, destination, and initial travel progress");

        PugEntity duplicate = new PugEntity(helper.getLevel());
        duplicate.setUUID(result.pugId());
        duplicate.beginAscent(flight.source(), flight.destination(), flight.totalTravelTicks(), flight.cargo());
        duplicate.tick();
        require(duplicate.isRemoved() && PugSavedData.get(helper.getLevel()).getFlights().stream()
                        .filter(record -> record.id().equals(result.pugId())).count() == 1,
                "a reloaded ascent entity must yield to an existing record instead of duplicating cargo");
        PugSavedData.get(helper.getLevel()).removeFlight(result.pugId());
        cleanupPair(helper, pair, null);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oneLaunchCarriesAtMostSixCompleteStacks(GameTestHelper helper) {
        LaunchpadPair pair = placeLaunchpadPair(helper,
                GlareAddress.of(Items.GOLD_INGOT, Items.REDSTONE, Items.MAP));
        for (int slot = 0; slot < 7; slot++) {
            pair.source().inventory.setStackInSlot(slot, new ItemStack(Items.IRON_NUGGET, slot + 1));
        }
        pair.source().fuelTank.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        pair.source().serverTick();
        armRedstoneLaunch(pair.source());
        LaunchAttemptResult result = pair.source().attemptLaunch();
        PugEntity pug = (PugEntity) helper.getLevel().getEntity(result.pugId());
        require(result.launched() && pug != null && pug.copyCargo().size() == 6,
                "PUG cargo must be capped at six occupied stacks per launch");
        for (int slot = 0; slot < 6; slot++) {
            require(pair.source().inventory.getStackInSlot(slot).isEmpty(),
                    "each selected stack should move completely rather than partially");
        }
        require(pair.source().inventory.getStackInSlot(6).getCount() == 7
                        && pair.source().getRoundRobinCursor() == 6,
                "seventh stack must remain buffered and become the next round-robin start");
        cleanupPair(helper, pair, pug);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void simulatedFlightProgressAndQueueSurviveReload(GameTestHelper helper) {
        GlareAddress address = GlareAddress.of(Items.REDSTONE, Items.COMPASS, Items.ENDER_PEARL);
        UUID id = UUID.randomUUID();
        PugFlightRecord flight = new PugFlightRecord(id, List.of(new ItemStack(Items.EMERALD, 6)),
                new LaunchpadEndpoint(Level.OVERWORLD, new BlockPos(0, 64, 0), address, LaunchpadMode.SEND),
                new LaunchpadEndpoint(Level.END, new BlockPos(10, 70, 10), address, LaunchpadMode.RECEIVE),
                3, 1, PugFlightState.IN_TRANSIT);
        PugSavedData data = PugSavedData.get(helper.getLevel());
        data.putFlight(flight);
        PugFlightService.tick(helper.getLevel().getServer());
        require(data.getFlight(id).orElseThrow().elapsedTravelTicks() == 2,
                "simulated service must advance active flights every server tick");
        PugFlightService.tick(helper.getLevel().getServer());
        require(data.getFlight(id).orElseThrow().state() == PugFlightState.QUEUED,
                "flight should enter QUEUED exactly when total travel time is reached");

        CompoundTag saved = data.save(new CompoundTag(), helper.getLevel().registryAccess());
        PugSavedData restored = PugSavedData.loadForTests(saved, helper.getLevel().registryAccess());
        PugFlightRecord queued = restored.getFlight(id).orElseThrow();
        require(queued.state() == PugFlightState.QUEUED && queued.elapsedTravelTicks() == 3
                        && queued.cargo().getFirst().getCount() == 6,
                "queued state, completed progress, and cargo must survive session reload");
        data.removeFlight(id);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void queuedFlightsClaimPadAndResumeDescendingEntity(GameTestHelper helper) {
        LaunchpadControllerBlockEntity receiver = placeControllerAt(helper, new BlockPos(1, 1, 5), Direction.SOUTH);
        require(receiver.tryAssemble(), "receiving pad should assemble");
        GlareAddress address = GlareAddress.of(Items.BLUE_DYE, Items.CLOCK, Items.PAPER);
        receiver.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.RECEIVE, address,
                GlareAddress.empty(), LaunchCondition.WHEN_FULL, 120));
        for (int slot = 0; slot < receiver.inventory.getSlots(); slot++) {
            receiver.inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        LaunchpadEndpoint source = new LaunchpadEndpoint(Level.OVERWORLD, helper.absolutePos(new BlockPos(1, 1, 0)),
                GlareAddress.empty(), LaunchpadMode.SEND);
        LaunchpadEndpoint destination = new LaunchpadEndpoint(helper.getLevel().dimension(), receiver.getBlockPos(),
                address, LaunchpadMode.RECEIVE);
        UUID firstId = UUID.randomUUID();
        PugSavedData data = PugSavedData.get(helper.getLevel());
        data.putFlight(new PugFlightRecord(firstId, List.of(new ItemStack(Items.LAPIS_LAZULI, 9)), source,
                destination, 10, 10, PugFlightState.QUEUED));
        PugFlightService.tick(helper.getLevel().getServer());

        PugEntity first = (PugEntity) helper.getLevel().getEntity(firstId);
        require(first != null && first.getFlightState() == PugFlightState.DESCENDING
                        && firstId.equals(receiver.getClaimedFlightId()) && data.getFlight(firstId).isEmpty(),
                "free receiver should atomically claim and materialize the first queued PUG");
        CompoundTag descendingTag = new CompoundTag();
        require(first.save(descendingTag), "descending PUG should serialize through vanilla entity persistence");
        Entity reloadedDescent = EntityType.loadEntityRecursive(descendingTag, helper.getLevel(), Function.identity());
        require(reloadedDescent instanceof PugEntity restoredDescent
                        && restoredDescent.getFlightState() == PugFlightState.DESCENDING
                        && restoredDescent.getUUID().equals(firstId),
                "mid-descent reload must preserve state and stable flight UUID");
        UUID secondId = UUID.randomUUID();
        data.putFlight(new PugFlightRecord(secondId, List.of(new ItemStack(Items.QUARTZ, 5)), source,
                destination, 10, 10, PugFlightState.QUEUED));
        PugFlightService.tick(helper.getLevel().getServer());
        require(data.getFlight(secondId).isPresent() && helper.getLevel().getEntity(secondId) == null,
                "later arrivals must remain queued while the receive pad is claimed");

        Vec3 landing = PugFlightService.padCenter(receiver);
        first.setPos(landing.x, landing.y + 5.0, landing.z);
        first.tick();
        require(Math.abs(first.getVerticalVelocity()) < PugEntity.DESCENT_SPEED,
                "descending PUG should decelerate as it approaches the landing pad");
        first.setPos(landing.x, landing.y + 0.1, landing.z);
        first.tick();
        require(first.isHovering(), "PUG should enter its persisted hover stage half a block above the pad");
        double minimumHoverY = first.getY();
        double maximumHoverY = first.getY();
        for (int tick = 0; tick < 30; tick++) {
            first.tick();
            minimumHoverY = Math.min(minimumHoverY, first.getY());
            maximumHoverY = Math.max(maximumHoverY, first.getY());
        }
        require(maximumHoverY - minimumHoverY > 0.15D,
                "hover stage should visibly oscillate above and below its half-block center");
        CompoundTag hoverTag = new CompoundTag();
        require(first.save(hoverTag), "hovering PUG should serialize");
        Entity reloadedHover = EntityType.loadEntityRecursive(hoverTag, helper.getLevel(), Function.identity());
        require(reloadedHover instanceof PugEntity restoredHover && restoredHover.isHovering()
                        && restoredHover.getHoverTicks() == first.getHoverTicks(),
                "mid-hover reload must preserve the hover phase and exact phase progress");
        finishLanding(first);
        require(first.getFlightState() == PugFlightState.UNLOADING,
                "hovering PUG should perform its final drop into UNLOADING");
        CompoundTag entityTag = new CompoundTag();
        require(first.save(entityTag), "physical PUG should serialize through vanilla entity persistence");
        Entity restoredEntity = EntityType.loadEntityRecursive(entityTag, helper.getLevel(), Function.identity());
        require(restoredEntity instanceof PugEntity restored
                        && restored.getFlightState() == PugFlightState.UNLOADING
                        && restored.copyCargo().getFirst().getCount() == 9
                        && restored.position().distanceToSqr(first.position()) < 0.001,
                "mid-state entity reload must preserve position, state, endpoints, UUID, and cargo");

        first.discard();
        receiver.releaseClaim(firstId);
        data.removeFlight(secondId);
        helper.getLevel().destroyBlock(receiver.getBlockPos(), false);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void whenFullRequiresSixMaximumStacksAndLaunchesAutomatically(GameTestHelper helper) {
        LaunchpadPair pair = placeLaunchpadPair(helper,
                GlareAddress.of(Items.AMETHYST_SHARD, Items.COMPASS, Items.PAPER));
        pair.source().applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.SEND, GlareAddress.empty(),
                pair.receiver().getConfiguration().localAddress(), LaunchCondition.WHEN_FULL, 120));
        pair.source().inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 63));
        pair.source().fuelTank.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        require(pair.source().attemptLaunch().failureReason() == LaunchpadFailureReason.PUG_NOT_FULL,
                "WHEN_FULL must reject fewer than six maximum-sized selected stacks");

        for (int slot = 0; slot < LaunchpadControllerBlockEntity.CARGO_SLOTS; slot++) {
            pair.source().inventory.setStackInSlot(slot, new ItemStack(Items.IRON_INGOT, 64));
        }
        pair.source().serverTick();
        PugEntity launched = findPug(helper, pair.source().getBlockPos(), PugFlightState.ASCENDING);
        require(launched != null && launched.copyCargo().size() == 6,
                "a full PUG should launch automatically without a manual trigger");
        cleanupPair(helper, pair, launched);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void redstoneAttemptWaitsForOtherPrerequisitesAndConsumesOneEdge(GameTestHelper helper) {
        LaunchpadPair pair = placeLaunchpadPair(helper,
                GlareAddress.of(Items.RED_DYE, Items.REPEATER, Items.PAPER));
        pair.source().inventory.setStackInSlot(0, new ItemStack(Items.COPPER_INGOT, 5));
        armRedstoneLaunch(pair.source());
        pair.source().serverTick();
        require(pair.source().hasPendingRedstoneLaunch(),
                "a rising edge should remain queued while fuel or another prerequisite is missing");

        pair.source().fuelTank.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        pair.source().serverTick();
        PugEntity launched = findPug(helper, pair.source().getBlockPos(), PugFlightState.ASCENDING);
        require(launched != null && !pair.source().hasPendingRedstoneLaunch(),
                "the queued edge should launch once and only be consumed after success");
        cleanupPair(helper, pair, launched);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void timerRemainsReadyUntilLaunchSucceeds(GameTestHelper helper) {
        LaunchpadPair pair = placeLaunchpadPair(helper,
                GlareAddress.of(Items.CLOCK, Items.QUARTZ, Items.BOOK));
        pair.source().applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.SEND, GlareAddress.empty(),
                pair.receiver().getConfiguration().localAddress(), LaunchCondition.TIMER, 1));
        pair.source().inventory.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT, 3));
        for (int tick = 0; tick < 20; tick++) pair.source().serverTick();
        require(pair.source().isTimerReady() && pair.source().getTimerTicksElapsed() == 20,
                "expired timer must remain ready while fuel is unavailable");

        pair.source().fuelTank.fill(new FluidStack(ModFluids.CATALYSED_CARBORAX.source.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        pair.source().serverTick();
        PugEntity launched = findPug(helper, pair.source().getBlockPos(), PugFlightState.ASCENDING);
        require(launched != null && pair.source().getTimerTicksElapsed() == 0,
                "timer should reset only after a successful launch");
        cleanupPair(helper, pair, launched);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unloadingKeepsClaimAndExposesOverflowToAutomation(GameTestHelper helper) {
        LaunchpadControllerBlockEntity receiver = placeControllerAt(helper, new BlockPos(1, 1, 5), Direction.SOUTH);
        require(receiver.tryAssemble(), "receiving pad should assemble");
        GlareAddress address = GlareAddress.of(Items.LAPIS_LAZULI, Items.HOPPER, Items.PAPER);
        receiver.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.RECEIVE, address,
                GlareAddress.empty(), LaunchCondition.WHEN_FULL, 120));
        receiver.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 60));
        for (int slot = 1; slot < receiver.inventory.getSlots(); slot++) {
            receiver.inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        LaunchpadEndpoint destination = new LaunchpadEndpoint(helper.getLevel().dimension(), receiver.getBlockPos(),
                address, LaunchpadMode.RECEIVE);
        UUID id = UUID.randomUUID();
        PugSavedData data = PugSavedData.get(helper.getLevel());
        data.putFlight(new PugFlightRecord(id, List.of(new ItemStack(Items.DIAMOND, 10)),
                new LaunchpadEndpoint(Level.OVERWORLD, helper.absolutePos(BlockPos.ZERO), GlareAddress.empty(),
                        LaunchpadMode.SEND), destination, 1, 1, PugFlightState.QUEUED));
        PugFlightService.tick(helper.getLevel().getServer());
        PugEntity pug = (PugEntity) helper.getLevel().getEntity(id);
        Vec3 landing = PugFlightService.padCenter(receiver);
        pug.setPos(landing.x, landing.y, landing.z);
        finishLanding(pug);
        pug.tick();
        require(pug.getFlightState() == PugFlightState.UNLOADING && pug.copyCargo().getFirst().getCount() == 6
                        && id.equals(receiver.getClaimedFlightId()),
                "full receiver inventory must leave overflow on a claimed unloading PUG");

        BlockPos itemProxy = LaunchpadStructure.position(receiver.getBlockPos(), Direction.SOUTH, 1, 1);
        IItemHandler automation = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                itemProxy, Direction.EAST);
        ItemStack extracted = automation.extractItem(LaunchpadControllerBlockEntity.INVENTORY_SLOTS, 64, false);
        require(extracted.is(Items.DIAMOND) && extracted.getCount() == 6,
                "proxy automation must be able to extract landed PUG overflow cargo");
        for (int tick = 0; tick < PugEntity.MINIMUM_LANDED_TICKS - 2; tick++) pug.tick();
        require(!pug.isRemoved() && id.equals(receiver.getClaimedFlightId())
                        && pug.getLandedTicks() == PugEntity.MINIMUM_LANDED_TICKS - 1,
                "empty landed PUG must continue occupying the pad for the full ten-second minimum");
        pug.tick();
        require(pug.isRemoved() && receiver.getClaimedFlightId() == null,
                "empty PUG must despawn and release the receive pad claim");
        helper.getLevel().destroyBlock(receiver.getBlockPos(), false);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void missingDestinationCrashesNearbyAndCargoIsRecoverable(GameTestHelper helper) {
        BlockPos intended = helper.absolutePos(new BlockPos(8, 1, 8));
        GlareAddress address = GlareAddress.of(Items.FLINT, Items.COMPASS, Items.MAP);
        UUID id = UUID.randomUUID();
        PugFlightRecord flight = new PugFlightRecord(id, List.of(new ItemStack(Items.EMERALD, 9)),
                new LaunchpadEndpoint(Level.OVERWORLD, helper.absolutePos(BlockPos.ZERO), GlareAddress.empty(),
                        LaunchpadMode.SEND),
                new LaunchpadEndpoint(helper.getLevel().dimension(), intended, address, LaunchpadMode.RECEIVE),
                1, 1, PugFlightState.QUEUED);
        PugSavedData data = PugSavedData.get(helper.getLevel());
        data.putFlight(flight);
        PugFlightService.tick(helper.getLevel().getServer());
        PugEntity crashed = (PugEntity) helper.getLevel().getEntity(id);
        require(crashed != null && crashed.getFlightState() == PugFlightState.CRASHED && data.getFlight(id).isEmpty(),
                "missing destination must materialize a crashed physical PUG without losing cargo");
        BlockPos crashPos = crashed.getCrashLandingPos();
        int dx = Math.abs(crashPos.getX() - intended.getX());
        int dz = Math.abs(crashPos.getZ() - intended.getZ());
        require(dx * dx + dz * dz <= 4 && (dx != 0 || dz != 0),
                "crash position must be randomized within two blocks and never equal the intended pad position");

        CompoundTag saved = new CompoundTag();
        require(crashed.save(saved), "mid-crash PUG should serialize");
        Entity restoredEntity = EntityType.loadEntityRecursive(saved, helper.getLevel(), Function.identity());
        require(restoredEntity instanceof PugEntity restored && crashPos.equals(restored.getCrashLandingPos())
                        && restored.copyCargo().getFirst().getCount() == 9,
                "crash destination and cargo must survive reload without rerolling");
        crashed.setPos(crashed.getX(), crashPos.getY() + 0.1D, crashed.getZ());
        crashed.tick();
        require(crashed.hasCrashLanded(), "crashed PUG should finish descending at its persisted offset");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        crashed.interact(player, InteractionHand.MAIN_HAND);
        require(crashed.isRemoved() && player.getInventory().countItem(Items.EMERALD) == 9,
                "interacting with a landed crash should recover cargo into the player inventory");

        UUID destroyedId = UUID.randomUUID();
        PugFlightRecord destroyedFlight = new PugFlightRecord(destroyedId,
                List.of(new ItemStack(Items.DIAMOND, 5)), flight.source(), flight.destination(), 1, 1,
                PugFlightState.QUEUED);
        PugEntity destroyed = new PugEntity(helper.getLevel());
        destroyed.setUUID(destroyedId);
        BlockPos destroyedLanding = intended.offset(1, 1, 0);
        destroyed.beginCrash(destroyedFlight, destroyedLanding, destroyedLanding.getY());
        helper.getLevel().addFreshEntity(destroyed);
        destroyed.tick();
        require(destroyed.hurt(helper.getLevel().damageSources().generic(), 1.0F) && destroyed.isRemoved(),
                "crashed PUGs should be destructible");
        int drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                        new AABB(destroyedLanding).inflate(3))
                .stream().filter(item -> item.getItem().is(Items.DIAMOND))
                .mapToInt(item -> item.getItem().getCount()).sum();
        require(drops == 5, "destroying a crashed PUG must drop its remaining cargo exactly once");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void changedDestinationIdentityCrashesDescendingPug(GameTestHelper helper) {
        LaunchpadControllerBlockEntity receiver = placeControllerAt(helper, new BlockPos(1, 1, 5), Direction.SOUTH);
        require(receiver.tryAssemble(), "receiving pad should assemble");
        GlareAddress address = GlareAddress.of(Items.ENDER_EYE, Items.CLOCK, Items.BOOK);
        receiver.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.RECEIVE, address,
                GlareAddress.empty(), LaunchCondition.WHEN_FULL, 120));
        UUID id = UUID.randomUUID();
        LaunchpadEndpoint destination = new LaunchpadEndpoint(helper.getLevel().dimension(), receiver.getBlockPos(),
                address, LaunchpadMode.RECEIVE);
        PugSavedData.get(helper.getLevel()).putFlight(new PugFlightRecord(id,
                List.of(new ItemStack(Items.QUARTZ, 4)),
                new LaunchpadEndpoint(Level.OVERWORLD, helper.absolutePos(BlockPos.ZERO), GlareAddress.empty(),
                        LaunchpadMode.SEND), destination, 1, 1, PugFlightState.QUEUED));
        PugFlightService.tick(helper.getLevel().getServer());
        PugEntity pug = (PugEntity) helper.getLevel().getEntity(id);
        receiver.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.RECEIVE,
                GlareAddress.of(Items.BLAZE_POWDER, Items.CLOCK, Items.BOOK), GlareAddress.empty(),
                LaunchCondition.WHEN_FULL, 120));
        pug.tick();
        require(pug.getFlightState() == PugFlightState.CRASHED && receiver.getClaimedFlightId() == null,
                "a destination identity change during descent must release the claim and crash the PUG");
        BlockPos crashPos = pug.getCrashLandingPos();
        int crashDx = crashPos.getX() - receiver.getBlockPos().getX();
        int crashDz = crashPos.getZ() - receiver.getBlockPos().getZ();
        require(crashDx * crashDx + crashDz * crashDz <= 4
                        && (crashPos.getX() != receiver.getBlockPos().getX()
                        || crashPos.getZ() != receiver.getBlockPos().getZ()),
                "mid-descent failure should use the same nearby randomized crash rule");
        pug.discard();
        helper.getLevel().destroyBlock(receiver.getBlockPos(), false);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void launchpadMenuSnapshotCarriesConfigurationReadinessAndInboundProgress(GameTestHelper helper) {
        LaunchpadPair pair = placeLaunchpadPair(helper,
                GlareAddress.of(Items.PRISMARINE_SHARD, Items.COMPASS, Items.BOOK));
        pair.source().inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 8));
        LaunchpadSnapshot sendSnapshot = LaunchpadSnapshot.capture(pair.source());
        require(sendSnapshot.fuelRequiredMb() == 1_000
                        && sendSnapshot.failures().contains(LaunchpadFailureReason.WAITING_FOR_REDSTONE)
                        && sendSnapshot.failures().contains(LaunchpadFailureReason.INSUFFICIENT_FUEL),
                "SEND snapshot should include route cost and ordered launch blockers");

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                helper.getLevel().registryAccess());
        sendSnapshot.write(buffer);
        LaunchpadSnapshot restored = LaunchpadSnapshot.read(buffer);
        require(restored.configuration().equals(sendSnapshot.configuration())
                        && restored.fuelRequiredMb() == sendSnapshot.fuelRequiredMb()
                        && restored.failures().equals(sendSnapshot.failures()),
                "launchpad menu snapshot must survive its network codec");

        UUID inboundId = UUID.randomUUID();
        LaunchpadEndpoint destination = new LaunchpadEndpoint(helper.getLevel().dimension(),
                pair.receiver().getBlockPos(), pair.receiver().getConfiguration().localAddress(), LaunchpadMode.RECEIVE);
        PugSavedData data = PugSavedData.get(helper.getLevel());
        data.putFlight(new PugFlightRecord(inboundId, List.of(new ItemStack(Items.COPPER_INGOT)),
                new LaunchpadEndpoint(Level.OVERWORLD, pair.source().getBlockPos(), GlareAddress.empty(),
                        LaunchpadMode.SEND), destination, 100, 40, PugFlightState.IN_TRANSIT));
        LaunchpadSnapshot receiveSnapshot = LaunchpadSnapshot.capture(pair.receiver());
        require(receiveSnapshot.inboundFlights().size() == 1
                        && receiveSnapshot.inboundFlights().getFirst().id().equals(inboundId)
                        && receiveSnapshot.inboundFlights().getFirst().progress() == 0.4F,
                "RECEIVE snapshot should expose bounded inbound progress");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        LaunchpadMenu menu = new LaunchpadMenu(1, player.getInventory(), pair.source());
        require(menu.slots.size() == 48,
                "launchpad menu should contain twelve pad slots and the complete player inventory");
        data.removeFlight(inboundId);
        cleanupPair(helper, pair, pair.source().getDockedPug());
        helper.succeed();
    }

    private static LaunchpadControllerBlockEntity placeController(GameTestHelper helper, Direction facing) {
        return placeControllerAt(helper, controllerRelativePos(), facing);
    }

    private static LaunchpadControllerBlockEntity placeControllerAt(GameTestHelper helper, BlockPos relative,
            Direction facing) {
        BlockState state = ModBlocks.LAUNCHPAD_CONTROLLER.get().defaultBlockState()
                .setValue(LaunchpadControllerBlock.FACING, facing);
        helper.setBlock(relative, state);
        LaunchpadControllerBlockEntity controller = (LaunchpadControllerBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(relative));
        if (controller == null) throw new AssertionError("launchpad controller block entity was not created");
        return controller;
    }

    private static LaunchpadPair placeLaunchpadPair(GameTestHelper helper, GlareAddress destinationAddress) {
        LaunchpadControllerBlockEntity source = placeControllerAt(helper, new BlockPos(1, 1, 0), Direction.NORTH);
        LaunchpadControllerBlockEntity receiver = placeControllerAt(helper, new BlockPos(1, 1, 5), Direction.SOUTH);
        require(source.tryAssemble() && receiver.tryAssemble(), "source and receiver launchpads should assemble");
        source.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.SEND, GlareAddress.empty(),
                destinationAddress, LaunchCondition.REDSTONE, 120));
        receiver.applyConfiguration(new LaunchpadConfiguration(LaunchpadMode.RECEIVE, destinationAddress,
                GlareAddress.empty(), LaunchCondition.WHEN_FULL, 120));
        return new LaunchpadPair(source, receiver);
    }

    private static void cleanupPair(GameTestHelper helper, LaunchpadPair pair, PugEntity pug) {
        if (pug != null) pug.discard();
        helper.getLevel().destroyBlock(pair.source().getBlockPos(), false);
        helper.getLevel().destroyBlock(pair.receiver().getBlockPos(), false);
    }

    private static void armRedstoneLaunch(LaunchpadControllerBlockEntity controller) {
        controller.updateRedstoneState(false);
        controller.updateRedstoneState(true);
    }

    private static PugEntity findPug(GameTestHelper helper, BlockPos around, PugFlightState state) {
        return helper.getLevel().getEntitiesOfClass(PugEntity.class, new AABB(around).inflate(8)).stream()
                .filter(pug -> pug.getFlightState() == state).findFirst().orElse(null);
    }

    private static void finishLanding(PugEntity pug) {
        for (int tick = 0; tick < 120 && pug.getFlightState() == PugFlightState.DESCENDING; tick++) pug.tick();
        require(pug.getFlightState() == PugFlightState.UNLOADING,
                "PUG should finish approach, hover, and final drop within the expected landing window");
    }

    private record LaunchpadPair(LaunchpadControllerBlockEntity source,
            LaunchpadControllerBlockEntity receiver) {}

    private static BlockPos controllerRelativePos() {
        return new BlockPos(1, 1, 0);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
