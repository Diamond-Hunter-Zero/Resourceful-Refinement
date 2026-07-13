package com.resourceful_refinement.content.research_terminal;

import com.resourceful_refinement.api.research.ResearchApi;
import com.resourceful_refinement.api.research.ResearchContributionResult;
import com.resourceful_refinement.api.research.ResearchNodeDefinition;
import com.resourceful_refinement.api.research.ResearchProgress;
import com.resourceful_refinement.api.research.ResearchRequirement;
import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.network.ResearchTerminalStatePayload;
import com.resourceful_refinement.network.ResearchTreeSyncPayload;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ResearchTerminalBlockEntity extends DepotBlockEntity implements IHaveGoggleInformation {
    public static final int TANK_CAPACITY = 1000;

    private @Nullable UUID ownerUuid;
    private String ownerName = "";
    private @Nullable ResourceLocation targetNodeId;
    private ResearchTerminalCycleKind cycleKind = ResearchTerminalCycleKind.IDLE;
    private @Nullable ResourceLocation cycleResourceId;
    private ItemStack cycleItemSnapshot = ItemStack.EMPTY;
    private int cycleTicks;
    private int stateSyncTicks;

    public final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncToClient();
        }
    };

    public ResearchTerminalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    public void assignOwner(@Nullable Player player) {
        if (player == null) {
            clearOwner();
            return;
        }
        setOwner(player.getUUID(), player.getGameProfile().getName());
    }

    public void setOwner(UUID ownerUuid, String ownerName) {
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName == null ? "" : ownerName;
        resetCycle();
        syncToClient();
        sendTerminalStateToNearby();
    }

    public void clearOwner() {
        ownerUuid = null;
        ownerName = "";
        targetNodeId = null;
        resetCycle();
        syncToClient();
        sendTerminalStateToNearby();
    }

    public @Nullable UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean hasOwner() {
        return ownerUuid != null;
    }

    public @Nullable ResourceLocation getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(@Nullable ResourceLocation targetNodeId) {
        if (targetNodeId != null && targetNodeId.equals(this.targetNodeId)) {
            return;
        }
        this.targetNodeId = targetNodeId;
        resetCycle();
        syncToClient();
        sendTerminalStateToNearby();
    }

    public DepotBehaviour getDepotBehaviour() {
        return getBehaviour(DepotBehaviour.TYPE);
    }

    public IItemHandler getItemHandler() {
        DepotBehaviour behaviour = getDepotBehaviour();
        return behaviour == null ? null : behaviour.itemHandler;
    }

    public ItemStack getHeldItem() {
        DepotBehaviour behaviour = getDepotBehaviour();
        return behaviour == null ? ItemStack.EMPTY : behaviour.getHeldItemStack();
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    public ResearchTerminalCycleKind getCycleKind() {
        return cycleKind;
    }

    public @Nullable ResourceLocation getCycleResourceId() {
        return cycleResourceId;
    }

    public int getCycleTicks() {
        return cycleTicks;
    }

    public int getCycleDuration() {
        return ServerConfig.RESEARCH_TERMINAL_CYCLE_TICKS.get();
    }

    public void tick() {
        super.tick();
        if (level instanceof ServerLevel server) {
            tickResearch(server);
        }
    }

    public boolean canTargetNode(ServerLevel server, ResourceLocation nodeId) {
        if (ownerUuid == null || !ResearchApi.isEnabled()) {
            return false;
        }
        Optional<ResearchNodeDefinition> definition = ResearchApi.getNode(nodeId);
        if (definition.isEmpty() || ResearchApi.hasUnlocked(server.getServer(), ownerUuid, nodeId)) {
            return false;
        }
        return prerequisitesUnlocked(server, definition.get());
    }

    private void tickResearch(ServerLevel server) {
        if (++stateSyncTicks >= 20) {
            stateSyncTicks = 0;
            if (cycleKind != ResearchTerminalCycleKind.IDLE) {
                sendTerminalStateToNearby();
            }
        }

        if (ownerUuid == null || targetNodeId == null || !ResearchApi.isEnabled()) {
            resetCycleIfActive();
            return;
        }

        Optional<ResearchNodeDefinition> definition = ResearchApi.getNode(targetNodeId);
        if (definition.isEmpty() || ResearchApi.hasUnlocked(server.getServer(), ownerUuid, targetNodeId)
                || !prerequisitesUnlocked(server, definition.get())) {
            resetCycleIfActive();
            return;
        }

        ResearchProgress progress = ResearchApi.getProgress(server.getServer(), ownerUuid, targetNodeId);
        if (progress.isComplete()) {
            ResearchContributionResult result = ResearchApi.contribute(server.getServer(), ownerUuid, targetNodeId,
                    Map.of(), Map.of());
            if (result.changed()) {
                syncOwnerResearchTree(server);
            }
            resetCycleIfActive();
            return;
        }

        if (cycleKind != ResearchTerminalCycleKind.IDLE) {
            if (!isActiveCycleStillValid(definition.get(), progress)) {
                resetCycleIfActive();
                return;
            }
            cycleTicks++;
            if (cycleTicks >= getCycleDuration()) {
                finishCycle(server);
            }
            return;
        }

        ResourceLocation fluidCandidate = findNeededFluidCandidate(definition.get(), progress);
        if (fluidCandidate != null) {
            startCycle(ResearchTerminalCycleKind.FLUID, fluidCandidate, ItemStack.EMPTY);
            return;
        }

        ItemStack held = getHeldItem();
        ResourceLocation itemCandidate = findNeededItemCandidate(definition.get(), progress, held);
        if (itemCandidate != null) {
            startCycle(ResearchTerminalCycleKind.ITEM, itemCandidate, held.copy());
        }
    }

    private boolean prerequisitesUnlocked(ServerLevel server, ResearchNodeDefinition definition) {
        for (ResourceLocation parentId : definition.parents()) {
            if (!ResearchApi.hasUnlocked(server.getServer(), ownerUuid, parentId)) {
                return false;
            }
        }
        return true;
    }

    private boolean isActiveCycleStillValid(ResearchNodeDefinition definition, ResearchProgress progress) {
        if (cycleResourceId == null) {
            return false;
        }
        return switch (cycleKind) {
            case IDLE -> false;
            case FLUID -> {
                FluidStack fluid = tank.getFluid();
                yield !fluid.isEmpty()
                        && fluid.getAmount() >= ResearchRequirement.BUCKET_AMOUNT
                        && cycleResourceId.equals(BuiltInRegistries.FLUID.getKey(fluid.getFluid()))
                        && findNeededFluidCandidate(definition, progress) != null
                        && cycleResourceId.equals(findNeededFluidCandidate(definition, progress));
            }
            case ITEM -> {
                ItemStack held = getHeldItem();
                yield !held.isEmpty()
                        && ItemStack.matches(cycleItemSnapshot, held)
                        && cycleResourceId.equals(findNeededItemCandidate(definition, progress, held));
            }
        };
    }

    private void finishCycle(ServerLevel server) {
        if (ownerUuid == null || targetNodeId == null || cycleResourceId == null) {
            resetCycleIfActive();
            return;
        }

        ResearchContributionResult result = switch (cycleKind) {
            case FLUID -> ResearchApi.contribute(server.getServer(), ownerUuid, targetNodeId, Map.of(),
                    Map.of(cycleResourceId, ResearchRequirement.BUCKET_AMOUNT));
            case ITEM -> ResearchApi.contribute(server.getServer(), ownerUuid, targetNodeId,
                    Map.of(cycleResourceId, 1), Map.of());
            case IDLE -> null;
        };
        if (result == null) {
            resetCycleIfActive();
            return;
        }

        int accepted = switch (cycleKind) {
            case FLUID -> result.acceptedFluids().getOrDefault(cycleResourceId, 0);
            case ITEM -> result.acceptedItems().getOrDefault(cycleResourceId, 0);
            case IDLE -> 0;
        };

        if (accepted > 0) {
            if (cycleKind == ResearchTerminalCycleKind.FLUID) {
                tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            } else if (cycleKind == ResearchTerminalCycleKind.ITEM) {
                consumeHeldItems(accepted);
            }
        }
        if (result.changed()) {
            syncOwnerResearchTree(server);
        }
        resetCycleIfActive();
    }

    private void consumeHeldItems(int amount) {
        DepotBehaviour behaviour = getDepotBehaviour();
        if (behaviour == null || amount <= 0) {
            return;
        }
        ItemStack held = behaviour.getHeldItemStack().copy();
        held.shrink(amount);
        if (held.isEmpty()) {
            behaviour.removeHeldItem();
        } else {
            setHeldItem(held);
        }
        notifyUpdate();
    }

    private @Nullable ResourceLocation findNeededFluidCandidate(ResearchNodeDefinition definition,
            ResearchProgress progress) {
        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty() || fluid.getAmount() < ResearchRequirement.BUCKET_AMOUNT) {
            return null;
        }
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
        for (ResearchRequirement.FluidRequirement requirement : definition.requirements().fluids()) {
            ResourceLocation requiredId = BuiltInRegistries.FLUID.getKey(requirement.fluid());
            int current = progress.fluidProgress().getOrDefault(requiredId, 0);
            if (fluidId.equals(requiredId) && current < requirement.amount()) {
                return fluidId;
            }
        }
        return null;
    }

    private @Nullable ResourceLocation findNeededItemCandidate(ResearchNodeDefinition definition,
            ResearchProgress progress, ItemStack held) {
        if (held.isEmpty()) {
            return null;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(held.getItem());
        for (ResearchRequirement.ItemRequirement requirement : definition.requirements().items()) {
            ResourceLocation requiredId = BuiltInRegistries.ITEM.getKey(requirement.item());
            int current = progress.itemProgress().getOrDefault(requiredId, 0);
            if (itemId.equals(requiredId) && current < requirement.count()) {
                return itemId;
            }
        }
        return null;
    }

    private void startCycle(ResearchTerminalCycleKind kind, ResourceLocation resourceId, ItemStack itemSnapshot) {
        cycleKind = kind;
        cycleResourceId = resourceId;
        cycleItemSnapshot = itemSnapshot.copy();
        cycleTicks = 0;
        syncToClient();
        sendTerminalStateToNearby();
    }

    private void resetCycleIfActive() {
        if (cycleKind != ResearchTerminalCycleKind.IDLE || cycleTicks != 0 || cycleResourceId != null) {
            resetCycle();
            syncToClient();
            sendTerminalStateToNearby();
        }
    }

    private void resetCycle() {
        cycleKind = ResearchTerminalCycleKind.IDLE;
        cycleResourceId = null;
        cycleItemSnapshot = ItemStack.EMPTY;
        cycleTicks = 0;
    }

    public void sendTerminalStateToNearby() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        ResearchTerminalStatePayload payload = ResearchTerminalStatePayload.capture(this);
        for (ServerPlayer player : server.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(server.dimension())
                    && player.distanceToSqr(worldPosition.getCenter()) <= 64.0D) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    private void syncOwnerResearchTree(ServerLevel server) {
        if (ownerUuid == null) {
            return;
        }
        ServerPlayer owner = server.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner != null) {
            PacketDistributor.sendToPlayer(owner, ResearchTreeSyncPayload.capture(owner));
        }
        sendTerminalStateToNearby();
    }

    public boolean isWithinUsableDistance(Player player) {
        if (level == null) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            sendData();
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        if (!ownerName.isEmpty()) {
            tag.putString("OwnerName", ownerName);
        }
        if (targetNodeId != null) {
            tag.putString("TargetNode", targetNodeId.toString());
        }
        tag.putString("CycleKind", cycleKind.name());
        if (cycleResourceId != null) {
            tag.putString("CycleResource", cycleResourceId.toString());
        }
        if (!cycleItemSnapshot.isEmpty()) {
            tag.put("CycleItem", cycleItemSnapshot.save(registries));
        }
        tag.putInt("CycleTicks", cycleTicks);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ownerName = tag.contains("OwnerName") ? tag.getString("OwnerName") : "";
        targetNodeId = null;
        if (tag.contains("TargetNode")) {
            try {
                targetNodeId = ResourceLocation.parse(tag.getString("TargetNode"));
            } catch (IllegalArgumentException ignored) {
                targetNodeId = null;
            }
        }
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        try {
            cycleKind = tag.contains("CycleKind")
                    ? ResearchTerminalCycleKind.valueOf(tag.getString("CycleKind"))
                    : ResearchTerminalCycleKind.IDLE;
        } catch (IllegalArgumentException ignored) {
            cycleKind = ResearchTerminalCycleKind.IDLE;
        }
        cycleResourceId = null;
        if (tag.contains("CycleResource")) {
            try {
                cycleResourceId = ResourceLocation.parse(tag.getString("CycleResource"));
            } catch (IllegalArgumentException ignored) {
                cycleResourceId = null;
            }
        }
        cycleItemSnapshot = tag.contains("CycleItem")
                ? ItemStack.parseOptional(registries, tag.getCompound("CycleItem"))
                : ItemStack.EMPTY;
        cycleTicks = Math.max(0, tag.getInt("CycleTicks"));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Research Terminal"));
        tooltip.add(Component.literal(hasOwner() ? "§7Owner: §9" + ownerName : "§7Owner: unassigned"));

        if (targetNodeId != null) {
            Optional<ResearchNodeDefinition> definition = ResearchApi.getNode(targetNodeId);
            definition.ifPresent(researchNodeDefinition ->
                    tooltip.add(Component.literal((getCycleKind() == ResearchTerminalCycleKind.IDLE ? "§7Viewing: §6" : "§7Researching: §6")
                            + researchNodeDefinition.title().getString()))
            );
        }

        FluidStack fluid = tank.getFluid();
        if (!fluid.isEmpty())
            tooltip.add(Component.literal( "§8-> §7" + fluid.getHoverName().getString() + " (" + fluid.getAmount() + "mb)"));

        if (!getHeldItem().isEmpty())
            tooltip.add(Component.literal("§8-> §7" + getHeldItem().getHoverName().getString() + " x " + getHeldItem().getCount()));

        return isPlayerSneaking;
    }
}
