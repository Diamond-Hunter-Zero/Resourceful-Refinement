package com.resourceful_refinement.content.gel_splatter;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class GelSplatterBlockEntity extends BlockEntity implements GelSplatterBlockEntityAccess {

    private Fluid fluid = Fluids.EMPTY;

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
        BlockState currentState = this.getBlockState();
        BlockState variantState = GelSplatterBlocks.withVariantForFluid(currentState, this.fluid);
        int newIndex = (variantState.getValue(GelSplatterBlock.FLUID_UPDATE_INDEX) + 1) % 8;
        BlockState newState = variantState.setValue(GelSplatterBlock.FLUID_UPDATE_INDEX, newIndex);

        this.setChanged();
        if (this.level != null) {
            this.level.setBlock(this.worldPosition, newState, Block.UPDATE_ALL);
            this.level.sendBlockUpdated(this.worldPosition, currentState, newState, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Fluid")) {
            ResourceLocation id = ResourceLocation.parse(tag.getString("Fluid"));
            Fluid loaded = BuiltInRegistries.FLUID.get(id);
            this.fluid = loaded != Fluids.EMPTY ? GelPropertiesManager.resolveSourceFluid(loaded) : Fluids.EMPTY;
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
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
