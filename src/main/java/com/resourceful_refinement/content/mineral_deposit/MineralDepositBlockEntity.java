package com.resourceful_refinement.content.mineral_deposit;

import com.resourceful_refinement.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MineralDepositBlockEntity extends BlockEntity {

    private Block associatedBlock = Blocks.STONE;

    public MineralDepositBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Block getAssociatedBlock() {
        return associatedBlock;
    }

    public void setAssociatedBlock(Block newBlock) {
        if (newBlock == null || newBlock == Blocks.AIR || newBlock == ModBlocks.MINERAL_DEPOSIT.get()) return;
        this.associatedBlock = newBlock;
        syncData();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Block", BuiltInRegistries.BLOCK.getKey(associatedBlock).toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Block")) {
            ResourceLocation blockLoc = ResourceLocation.tryParse(tag.getString("Block"));
            if (blockLoc != null) {
                Block block = BuiltInRegistries.BLOCK.get(blockLoc);
                if (block != Blocks.AIR) {
                    associatedBlock = block;
                }
            }
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

    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
