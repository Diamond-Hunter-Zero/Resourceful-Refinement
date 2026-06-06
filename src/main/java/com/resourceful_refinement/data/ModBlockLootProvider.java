package com.resourceful_refinement.data;

import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlock;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpProxyBlock;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlock;
import com.resourceful_refinement.content.geyser.GeyserBlock;
import com.resourceful_refinement.content.refinery.RefineryKineticProxyBlock;
import com.resourceful_refinement.content.refinery.RefineryProxyBlock;
import com.resourceful_refinement.registry.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Set;
import java.util.stream.Collectors;

public class ModBlockLootProvider extends BlockLootSubProvider {

    protected ModBlockLootProvider(HolderLookup.Provider registries) {
        // Pass empty feature flags as standard setup
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Loop through all blocks registered by your mod
        for (Holder<Block> blockHolder : ModBlocks.BLOCKS.getEntries()) {
            Block block = blockHolder.value();

            // Filter out blocks that should NOT just drop themselves as basic items
            if (shouldSkipLootTable(block)) {
                continue;
            }

            // Explicitly handle blocks that are meant to drop nothing when mined.
            // This maps them to an empty builder, which fully satisfies the validation check.
            if (isDropNothingBlock(block)) {
                this.add(block, LootTable.lootTable());
                continue;
            }

            // Efficiently registers: "This block drops an itemstack of itself"
            this.dropSelf(block);
        }

        // You can still manually define complex drops for your skipped blocks right here:
        // this.add(ModBlocks.SOME_ORE.get(), block -> createOreDrop(block, ModItems.RAW_MINERAL.get()));
    }

    /**
     * Blocks handled here will NOT have any loot table generated,
     * AND they are completely ignored by the validation check.
     */
    private boolean shouldSkipLootTable(Block block) {
        // 1. Automatically skip fluid blocks since they don't drop block items
        if (block instanceof net.minecraft.world.level.block.LiquidBlock) {
            return true;
        }
        return false;
    }

    /**
     * Blocks handled here WILL generate a loot table json,
     * but the file will be empty, making them drop nothing when broken.
     */
    private boolean isDropNothingBlock(Block block) {
        return block instanceof GeyserBlock
                || block instanceof GelSplatterBlock
                || block instanceof FrackingPumpProxyBlock
                || block instanceof RefineryProxyBlock
                || block instanceof RefineryKineticProxyBlock;
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // Tells the validation system which blocks this provider is responsible for
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(Holder::value)
                .collect(Collectors.toList());
    }
}