package com.resourceful_refinement.content.manifold;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Owns the compact item representation used for Manifold break, pick, and placement. */
public class ManifoldItem extends BlockItem {
    public ManifoldItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static void writeAssemblyRecord(ItemStack stack, ManifoldAssemblyRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.put("AssemblyRecord", record.save());
        BlockItem.setBlockEntityData(stack, ModBlockEntities.MANIFOLD_BE.get(), tag);
    }
}
