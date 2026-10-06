package com.resourceful_refinement.worldgen.crystalnode;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Content configuration for a {@link CrystalNodeStructure} placement — the shared, datapack-tweakable
 * recipe for the small surface resource nodes (Resonance Crystals + End/Nether Crystal Fissures).
 *
 * <p>Fields flatten into the structure JSON via {@link #MAP_CODEC}; the whole record also serialises to
 * NBT via {@link #CODEC} so the structure piece can persist it.</p>
 *
 * @param nodeBlock       centre block (resonance crystal or crystal fissure bud)
 * @param sourceItem      if present and the node has a source-item block entity (the fissure bud), set on generation
 * @param baseBlocks      weighted block mix for a noisy circular surface disc under the node (empty = no base)
 * @param baseRadius      radius of that disc (0 = no base)
 * @param baseFillChance  per-column chance a disc cell is filled (1.0 = solid; &lt;1 leaves the natural surface showing)
 * @param scatterBlock    companion block scattered near the node (growths around, or ore below)
 * @param scatterCount    how many scatter attempts
 * @param scatterRadius   horizontal radius of the scatter
 * @param scatterMode     {@code around_surface} (on the ground, facing up) or {@code below} (buried under the node)
 * @param decorBlock      extra surface decor on the base (e.g. small amethyst buds)
 * @param decorCount      how many decor attempts
 * @param footprintRadius overall footprint used for the bounding box / local surface search
 * @param anchor          how to find the placement Y ({@code surface} or {@code nether_floor})
 */
public record CrystalNodeConfig(
        BlockState nodeBlock,
        Optional<Item> sourceItem,
        List<WeightedBlock> baseBlocks,
        int baseRadius,
        float baseFillChance,
        Optional<BlockState> scatterBlock,
        int scatterCount,
        int scatterRadius,
        ScatterMode scatterMode,
        Optional<BlockState> decorBlock,
        int decorCount,
        int footprintRadius,
        AnchorMode anchor
) {
    public static final MapCodec<CrystalNodeConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("node_block").forGetter(CrystalNodeConfig::nodeBlock),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("source_item").forGetter(CrystalNodeConfig::sourceItem),
            WeightedBlock.CODEC.listOf().optionalFieldOf("base_blocks", List.of()).forGetter(CrystalNodeConfig::baseBlocks),
            Codec.intRange(0, 16).optionalFieldOf("base_radius", 0).forGetter(CrystalNodeConfig::baseRadius),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("base_fill_chance", 1.0F).forGetter(CrystalNodeConfig::baseFillChance),
            BlockState.CODEC.optionalFieldOf("scatter_block").forGetter(CrystalNodeConfig::scatterBlock),
            Codec.intRange(0, 256).optionalFieldOf("scatter_count", 0).forGetter(CrystalNodeConfig::scatterCount),
            Codec.intRange(0, 16).optionalFieldOf("scatter_radius", 3).forGetter(CrystalNodeConfig::scatterRadius),
            ScatterMode.CODEC.optionalFieldOf("scatter_mode", ScatterMode.AROUND_SURFACE).forGetter(CrystalNodeConfig::scatterMode),
            BlockState.CODEC.optionalFieldOf("decor_block").forGetter(CrystalNodeConfig::decorBlock),
            Codec.intRange(0, 256).optionalFieldOf("decor_count", 0).forGetter(CrystalNodeConfig::decorCount),
            Codec.intRange(1, 24).optionalFieldOf("footprint_radius", 3).forGetter(CrystalNodeConfig::footprintRadius),
            AnchorMode.CODEC.optionalFieldOf("anchor", AnchorMode.SURFACE).forGetter(CrystalNodeConfig::anchor)
    ).apply(instance, CrystalNodeConfig::new));

    public static final Codec<CrystalNodeConfig> CODEC = MAP_CODEC.codec();

    /** One entry of the weighted base-block mix. */
    public record WeightedBlock(BlockState state, int weight) {
        public static final Codec<WeightedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("block").forGetter(WeightedBlock::state),
                Codec.intRange(1, 100).optionalFieldOf("weight", 1).forGetter(WeightedBlock::weight)
        ).apply(instance, WeightedBlock::new));
    }

    /** Where the companion scatter goes relative to the node. */
    public enum ScatterMode implements StringRepresentable {
        AROUND_SURFACE("around_surface"),
        BELOW("below");

        public static final Codec<ScatterMode> CODEC = StringRepresentable.fromEnum(ScatterMode::values);
        private final String name;

        ScatterMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /** How the structure finds its placement Y. */
    public enum AnchorMode implements StringRepresentable {
        SURFACE("surface"),
        NETHER_FLOOR("nether_floor");

        public static final Codec<AnchorMode> CODEC = StringRepresentable.fromEnum(AnchorMode::values);
        private final String name;

        AnchorMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
