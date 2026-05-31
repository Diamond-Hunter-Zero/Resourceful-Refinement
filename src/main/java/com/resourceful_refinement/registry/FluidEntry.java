package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.fluids.base.FluidGroup;
import com.resourceful_refinement.content.fluids.base.GeneralizedFlowingFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

public class FluidEntry {

    /** Shared item model for paint fluid buckets ({@code paint_bucket} underlay + {@code paint_bucket_content} tint). */
    public static final ResourceLocation PAINT_FLUID_BUCKET_MODEL = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "item/paint_fluid_bucket");

    public DeferredHolder<FluidType, FluidType> type;
    public DeferredHolder<Fluid, FlowingFluid> source;
    public DeferredHolder<Fluid, FlowingFluid> flowing;
    public DeferredBlock<LiquidBlock> block;
    public DeferredItem<BucketItem> bucket;
    public int color;
    public final FluidGroup group;

    public FluidEntry(String name, int color, FluidGroup group) {
        this.group = group;
        this.color = ((group == FluidGroup.RAW
                || group == FluidGroup.CATALYSED
                || group == FluidGroup.CARBORAX
                || group == FluidGroup.PAINT) ? 0xFA000000 : 0xFF000000) | color;
        // Register Type
        type = ModFluidTypes.registerType(name, color, group);

        // Properties Supplier to resolve circular dependencies
        Supplier<BaseFlowingFluid.Properties> properties = () -> new BaseFlowingFluid.Properties(
                this.type, this.source, this.flowing
        ).block(this.block).bucket(this.bucket);

        // Register Fluids
        source = ModFluids.FLUIDS.register(name, () -> new GeneralizedFlowingFluid.Source(properties.get(), group.dropRate));
        flowing = ModFluids.FLUIDS.register("flowing_" + name, () -> new GeneralizedFlowingFluid.Flowing(properties.get(), group.dropRate));

        // Register Block (path matches assets/blockstates/<group>/<name>.json)
        String blockId = group == FluidGroup.PAINT ? "paint/" + name : name;
        block = ModBlocks.BLOCKS.register(blockId, () -> new LiquidBlock(source.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

        // Register Bucket
        bucket = ModItems.ITEMS.register(name + "_bucket", () -> new BucketItem(source.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    }

    public FluidType getFluidType() {
        // Return your registered FluidType holder or object instance
        return this.type.get();
    }

    public boolean usesPaintBucketUnderlay() {
        return group == FluidGroup.PAINT;
    }

    /** Item model parent used by {@code models/item/<fluid>_bucket.json}. */
    /*public ResourceLocation getBucketItemModelParent() {
        return usesPaintBucketUnderlay()
                ? PAINT_FLUID_BUCKET_MODEL
                : ResourceLocation.withDefaultNamespace("item/generated");
    }*/
}
