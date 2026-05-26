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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class GelSplatterBlockEntity extends BlockEntity {

    private Fluid fluid = Fluids.EMPTY;

    public GelSplatterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEL_SPLATTER_BE.get(), pos, state);
    }

    public Fluid getFluid() {
        return this.fluid;
    }

    public void setFluid(Fluid fluid) {
        if (fluid != null && fluid != Fluids.EMPTY) {
            this.fluid = GelPropertiesManager.resolveSourceFluid(fluid);

            BlockState currentState = this.getBlockState();
            int newIndex = (currentState.getValue(GelSplatterBlock.FLUID_UPDATE_INDEX) + 1)%8;
            BlockState newState = currentState.setValue(GelSplatterBlock.FLUID_UPDATE_INDEX, newIndex);

            this.setChanged(); // Marks the chunk dirty on the server for saving

            if (this.level != null) {
                this.level.setBlock(this.worldPosition, newState, Block.UPDATE_ALL);
                // In vanilla, this automatically queues and dispatches the
                // ClientboundBlockEntityDataPacket to all tracking players.
                this.level.sendBlockUpdated(this.worldPosition, currentState, newState, Block.UPDATE_ALL);
            }
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
        super.handleUpdateTag(tag, registries); // Applies the new NBT via loadAdditional

        if (this.level != null && this.level.isClientSide) {
            // 1. Tell NeoForge to invalidate this block's model data cache
            this.requestModelDataUpdate();

            // 2. Force the vanilla rendering engine to rebuild the chunk geometry with the new tint
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
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
