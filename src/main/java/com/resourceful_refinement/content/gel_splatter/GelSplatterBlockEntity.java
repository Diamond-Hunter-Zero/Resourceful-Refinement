package com.resourceful_refinement.content.gel_splatter;

import com.resourceful_refinement.content.gel_tracking.GelTrackingService;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.AllFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class GelSplatterBlockEntity extends BlockEntity implements GelSplatterBlockEntityAccess {

    /** Default gel fluid when none is stored (molten andesite blend for Inert, catalysed zinc for speedy, and glue for sticky). */
    public static Fluid getDefaultFluid(Block linkedBlock) {
        if (linkedBlock == ModBlocks.GEL_SPLATTER_SLIPPERY.get())
            return ModFluids.CATALYSED_ZINC.source.get();
        else if (linkedBlock == ModBlocks.GEL_SPLATTER_STICKY.get())
            return ModFluids.LIQUID_GLUE.source.get();
        else if (linkedBlock == ModBlocks.GEL_SPLATTER_MOLTEN.get())
            return ModFluids.MOLTEN_CRIMSITE.source.get();
        else if (linkedBlock == ModBlocks.GEL_SPLATTER_BOUNCY.get())
            return ModFluids.DURASTEEL_ALLOY.source.get();
        else
            return ModFluids.MOLTEN_ANDESITE_BLEND.source.get();
    }

    private Fluid fluid = getDefaultFluid(this.getBlockState().getBlock());
    private String trackingId = "";

    public GelSplatterBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.GEL_SPLATTER_BE.get(), pos, state);
    }

    public GelSplatterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Fluid getFluid() {
        return this.fluid;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public boolean hasTrackingId() {
        return trackingId != null && !trackingId.isEmpty();
    }

    /**
     * Tags this splatter for gel minigame tracking (from a bound hosegun). Server only.
     */
    public void applyTrackingId(String id) {
        if (level == null || level.isClientSide) {
            return;
        }
        String sanitised = FluidRefillStationBlockEntity.sanitiseTrackingId(id);
        if (sanitised.isEmpty()) {
            return;
        }
        if (sanitised.equals(trackingId)) {
            GelTrackingService.onSplatterAdded((ServerLevel) level, worldPosition, sanitised);
            return;
        }
        if (hasTrackingId()) {
            GelTrackingService.onSplatterRemoved(level, worldPosition);
        }
        trackingId = sanitised;
        setChanged();
        GelTrackingService.onSplatterAdded((ServerLevel) level, worldPosition, sanitised);
    }

    /**
     * Creative-only: swap this splatter's fluid (and block variant when needed) without re-adding tracking.
     */
    public void creativeRetextureWithFluid(Fluid fluid) {
        if (level == null || level.isClientSide || fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        clearTracking();
        setFluid(fluid);
    }

    /** Removes this splatter from gel minigame tracking. Server only. */
    public void clearTracking() {
        if (level == null || level.isClientSide || !hasTrackingId()) {
            return;
        }
        GelTrackingService.onSplatterRemoved(level, worldPosition);
        trackingId = "";
        setChanged();
    }

    @Override
    public void setFluid(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        this.fluid = GelPropertiesManager.resolveSourceFluid(fluid);
        syncToClients();
    }

    /**
     * Applies the correct gel splatter block variant for this fluid, bumps {@link GelSplatterBlock#FLUID_UPDATE_INDEX}
     * for client tint sync, and preserves multiface attachments.
     */
    private void syncToClients() {
        BlockState currentState = getBlockState();
        BlockState variantState = GelSplatterBlocks.withVariantForFluid(currentState, this.fluid);
        int newIndex = (variantState.getValue(GelSplatterBlock.FLUID_UPDATE_INDEX) + 1) % 8;
        BlockState newState = variantState.setValue(GelSplatterBlock.FLUID_UPDATE_INDEX, newIndex);

        setChanged();

        if (level == null || level.isClientSide) {
            return;
        }

        GelSplatterBlockEntity syncTarget = this;
        if (!newState.equals(currentState)) {
            level.setBlock(worldPosition, newState, Block.UPDATE_ALL);
            BlockEntity blockEntity = level.getBlockEntity(worldPosition);
            if (blockEntity instanceof GelSplatterBlockEntity splatter) {
                splatter.fluid = this.fluid;
                splatter.trackingId = this.trackingId;
                splatter.setChanged();
                syncTarget = splatter;
            }
        }

        dispatchBlockEntitySync(syncTarget, currentState, level.getBlockState(worldPosition));
    }

    /** Pushes BE NBT to watching clients after block state changes (setBlock can reset client-side fluid). */
    private void dispatchBlockEntitySync(GelSplatterBlockEntity splatter, BlockState oldState, BlockState newState) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(splatter);
        ChunkPos chunkPos = new ChunkPos(splatter.getBlockPos());
        for (ServerPlayer player : server.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            player.connection.send(packet);
        }

        server.sendBlockUpdated(splatter.getBlockPos(), oldState, newState, Block.UPDATE_CLIENTS);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            if (fluid == null || fluid == Fluids.EMPTY) {
                fluid = getDefaultFluid(this.getBlockState().getBlock());
                syncToClients();
            }
        }
        if (level instanceof ServerLevel server && hasTrackingId()) {
            GelTrackingService.onSplatterAdded(server, worldPosition, trackingId);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && hasTrackingId()) {
            GelTrackingService.onSplatterRemoved(level, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        if (hasTrackingId()) {
            tag.putString("TrackingId", trackingId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Fluid")) {
            ResourceLocation id = ResourceLocation.parse(tag.getString("Fluid"));
            Fluid loaded = BuiltInRegistries.FLUID.get(id);
            this.fluid = loaded != Fluids.EMPTY ? GelPropertiesManager.resolveSourceFluid(loaded) : getDefaultFluid(this.getBlockState().getBlock());
        } else {
            this.fluid = getDefaultFluid(this.getBlockState().getBlock());
        }
        trackingId = tag.contains("TrackingId") ? tag.getString("TrackingId") : "";
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        refreshClientAppearance();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        refreshClientAppearance();
    }

    private void refreshClientAppearance() {
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL_IMMEDIATE);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
