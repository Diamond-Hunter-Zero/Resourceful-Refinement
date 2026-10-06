package com.resourceful_refinement.content.drill_pylon;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalFissureBudBlockEntity extends BlockEntity {
    private Item sourceItem = Items.AIR;

    public CrystalFissureBudBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_FISSURE_BUD_BE.get(), pos, state);
    }

    public Item getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(Item item) {
        if (item == null) return;
        sourceItem = item;
        syncData();
    }

    /**
     * Sets the source item during worldgen. Unlike {@link #setSourceItem(Item)} this does not send a block
     * update (there are no tracking clients for an ungenerated chunk, and {@code WorldGenRegion} does not
     * support block-update packets) — the value persists via the chunk's saved block-entity NBT.
     */
    public void setSourceItemOnGenerate(Item item) {
        if (item == null || item == Items.AIR) return;
        sourceItem = item;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("SourceItem", BuiltInRegistries.ITEM.getKey(sourceItem).toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tag.contains("SourceItem")) return;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("SourceItem"));
        if (id == null) return;
        sourceItem = BuiltInRegistries.ITEM.get(id);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncData() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
