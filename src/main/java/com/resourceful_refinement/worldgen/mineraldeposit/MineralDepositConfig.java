package com.resourceful_refinement.worldgen.mineraldeposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.resourceful_refinement.worldgen.crystalnode.CrystalNodeConfig.WeightedBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

/**
 * Content configuration for a {@link MineralDepositStructure} placement — one per deposit variant
 * (stone, coal, andesite, cave deepslate, ...), set entirely in the structure JSON.
 *
 * <p>Fields flatten into the structure JSON via {@link #MAP_CODEC}; the whole record also serialises to
 * NBT via {@link #CODEC} so the structure piece can persist it.</p>
 *
 * @param depositType      the block stored as each generated Mineral Deposit's {@code associatedBlock}
 *                         (drives excavation recipes + {@code visuals.json})
 * @param fillerBlocks     weighted block mix for the bowl-shaped base around the deposits
 * @param depositCountMin  minimum deposits in the central cluster (centre + orthogonal neighbours)
 * @param depositCountMax  maximum deposits in the central cluster (capped at 5)
 * @param baseRadius       radius of the filler base
 * @param ringRadius       outer limit of the sparse, noisy fade-out ring around the base; its edge wanders
 *                         between {@code ringRadius - 1} and {@code ringRadius} (0 or &le; baseRadius = no ring)
 * @param maxDepth         filler depth at the centre of the bowl, tapering to 1 at the edge
 * @param decorBlocks      weighted decor placed on top of the filler (empty = none)
 * @param decorCountMin    minimum decor blocks placed (fewer only if the base has too few open cells)
 * @param decorCountMax    maximum decor blocks placed
 * @param anchor           {@code surface} (world surface) or {@code cave} (a noise-cave floor)
 * @param caveMinY         lowest cave floor Y considered (cave anchor only)
 * @param caveMaxY         highest cave floor Y considered (cave anchor only)
 * @param resonatorVariant Resource Resonator pin variant for this deposit (selects its radar icon)
 */
public record MineralDepositConfig(
        Block depositType,
        List<WeightedBlock> fillerBlocks,
        int depositCountMin,
        int depositCountMax,
        int baseRadius,
        int ringRadius,
        int maxDepth,
        List<WeightedBlock> decorBlocks,
        int decorCountMin,
        int decorCountMax,
        AnchorMode anchor,
        int caveMinY,
        int caveMaxY,
        Optional<String> resonatorVariant
) {
    public static final MapCodec<MineralDepositConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("deposit_type").forGetter(MineralDepositConfig::depositType),
            WeightedBlock.CODEC.listOf().fieldOf("filler_blocks").forGetter(MineralDepositConfig::fillerBlocks),
            Codec.intRange(1, 5).optionalFieldOf("deposit_count_min", 3).forGetter(MineralDepositConfig::depositCountMin),
            Codec.intRange(1, 5).optionalFieldOf("deposit_count_max", 4).forGetter(MineralDepositConfig::depositCountMax),
            Codec.intRange(1, 6).optionalFieldOf("base_radius", 3).forGetter(MineralDepositConfig::baseRadius),
            Codec.intRange(0, 6).optionalFieldOf("ring_radius", 5).forGetter(MineralDepositConfig::ringRadius),
            Codec.intRange(1, 6).optionalFieldOf("max_depth", 3).forGetter(MineralDepositConfig::maxDepth),
            WeightedBlock.CODEC.listOf().optionalFieldOf("decor_blocks", List.of()).forGetter(MineralDepositConfig::decorBlocks),
            Codec.intRange(0, 64).optionalFieldOf("decor_count_min", 0).forGetter(MineralDepositConfig::decorCountMin),
            Codec.intRange(0, 64).optionalFieldOf("decor_count_max", 0).forGetter(MineralDepositConfig::decorCountMax),
            AnchorMode.CODEC.optionalFieldOf("anchor", AnchorMode.SURFACE).forGetter(MineralDepositConfig::anchor),
            Codec.INT.optionalFieldOf("cave_min_y", 0).forGetter(MineralDepositConfig::caveMinY),
            Codec.INT.optionalFieldOf("cave_max_y", 56).forGetter(MineralDepositConfig::caveMaxY),
            Codec.STRING.optionalFieldOf("resonator_variant").forGetter(MineralDepositConfig::resonatorVariant)
    ).apply(instance, MineralDepositConfig::new));

    public static final Codec<MineralDepositConfig> CODEC = MAP_CODEC.codec();

    public boolean isCave() {
        return anchor == AnchorMode.CAVE;
    }

    /** How the structure finds its placement Y. */
    public enum AnchorMode implements StringRepresentable {
        SURFACE("surface"),
        CAVE("cave");

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
