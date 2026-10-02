package com.resourceful_refinement.content.resonator.scan;

import com.mojang.logging.LogUtils;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.mixin.JigsawStructureAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Seed-based prediction of <em>which specific geyser template</em> a jigsaw structure will place in a given chunk —
 * i.e. crimsite vs ochrum vs veridium, etc. — without generating the chunk ("Route B").
 *
 * <p>During generation the start template is a weighted pick from the structure's start pool, drawn from a
 * {@link WorldgenRandom} seeded by {@code setLargeFeatureSeed(seed, chunkX, chunkZ)}. We reproduce the exact draw
 * order that {@code JigsawStructure.findGenerationPoint} + {@code JigsawPlacement.addPieces} perform before the
 * template pick:</p>
 * <ol>
 *   <li>{@code startHeight.sample(rng, ctx)} — consumes the real height-provider draws (0 for constant, 1 for
 *       uniform; we call the <em>real</em> provider so rejection-sampled {@code nextInt(bound)} stays in sync);</li>
 *   <li>{@code Rotation.getRandom(rng)} — one draw;</li>
 *   <li>{@code pool.getRandomTemplate(rng)} — {@code rng.nextInt(totalWeight)}, which we mirror against a hardcoded
 *       weighted table in start-pool JSON order (equivalent to vanilla's weight-expanded list).</li>
 * </ol>
 *
 * <p><b>start_height guard.</b> The per-structure weighted tables are hardcoded and therefore coupled to the
 * datapack. Each entry also records the height-provider <em>kind</em> the model assumes. Before predicting, we read
 * the structure's live {@code startHeight} (via {@link JigsawStructureAccessor}) and verify it still matches; if a
 * datapack change swaps the type, we log once and return {@code null} (the pin still appears, just unlabelled)
 * rather than silently mislabelling. This is the canary that the hardcoded model has drifted from the data.</p>
 */
public final class GeyserVariants {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<ResourceLocation> WARNED = ConcurrentHashMap.newKeySet();

    private GeyserVariants() {}

    /** The height-provider kinds the predictor knows how to keep RNG-synced; used only for the guard check. */
    public enum StartHeightKind {
        CONSTANT {
            @Override boolean matches(HeightProvider provider) { return provider instanceof ConstantHeight; }
        },
        UNIFORM {
            @Override boolean matches(HeightProvider provider) { return provider instanceof UniformHeight; }
        };

        abstract boolean matches(HeightProvider provider);
    }

    /** One weighted start-pool element: the template (short) name and its weight, in JSON order. */
    public record WeightedTemplate(String variant, int weight) {}

    /** The hardcoded model of a geyser structure's start pool + assumed start-height kind. */
    public static final class PoolInfo {
        private final StartHeightKind heightKind;
        private final List<WeightedTemplate> templates;
        private final int totalWeight;

        private PoolInfo(StartHeightKind heightKind, WeightedTemplate... templates) {
            this.heightKind = heightKind;
            this.templates = List.of(templates);
            int sum = 0;
            for (WeightedTemplate t : templates) {
                sum += t.weight();
            }
            this.totalWeight = sum;
        }

        /** Maps a roll in {@code [0, totalWeight)} to a variant via cumulative weights in JSON order. */
        private String variantForRoll(int roll) {
            int acc = 0;
            for (WeightedTemplate t : templates) {
                acc += t.weight();
                if (roll < acc) {
                    return t.variant();
                }
            }
            // Unreachable while roll < totalWeight; guard against rounding/model error.
            return templates.isEmpty() ? null : templates.get(templates.size() - 1).variant();
        }
    }

    // Keyed by STRUCTURE id (not pool id). Element order and weights mirror each start_pool.json exactly.
    private static final Map<ResourceLocation, PoolInfo> BY_STRUCTURE = Map.of(
            rr("ore_geyser"), new PoolInfo(StartHeightKind.CONSTANT,
                    new WeightedTemplate("lava_geyser_small", 8),
                    new WeightedTemplate("crimsite_geyser", 8),
                    new WeightedTemplate("veridium_geyser", 8),
                    new WeightedTemplate("ochrum_geyser", 8),
                    new WeightedTemplate("asurine_geyser", 8),
                    new WeightedTemplate("crimsite_geyser_small", 5),
                    new WeightedTemplate("veridium_geyser_small", 5),
                    new WeightedTemplate("ochrum_geyser_small", 5),
                    new WeightedTemplate("asurine_geyser_small", 5)),
            rr("carbonox_geyser"), new PoolInfo(StartHeightKind.CONSTANT,
                    new WeightedTemplate("carbonox_geyser_small", 5)),
            rr("cave_ore_geyser"), new PoolInfo(StartHeightKind.UNIFORM,
                    new WeightedTemplate("lava_geyser_small", 3),
                    new WeightedTemplate("crimsite_geyser_small", 5),
                    new WeightedTemplate("veridium_geyser_small", 5),
                    new WeightedTemplate("ochrum_geyser_small", 5),
                    new WeightedTemplate("asurine_geyser_small", 5),
                    new WeightedTemplate("carbonox_geyser_small", 5)),
            rr("nether_scorchia_geyser"), new PoolInfo(StartHeightKind.UNIFORM,
                    new WeightedTemplate("nether_lava_geyser_small", 1),
                    new WeightedTemplate("scorchia_geyser", 2),
                    new WeightedTemplate("scorchia_geyser_small", 3)),
            rr("nether_scorchia_basalt_geyser"), new PoolInfo(StartHeightKind.UNIFORM,
                    new WeightedTemplate("scorchia_geyser_basalt_pool", 5),
                    new WeightedTemplate("scorchia_geyser_basalt_small", 4),
                    new WeightedTemplate("scorchia_geyser_basalt_tall", 5))
    );

    private static ResourceLocation rr(String path) {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, path);
    }

    /**
     * Predicts the start template name for {@code structure} at the given structure chunk, or {@code null} if the
     * structure isn't a modelled geyser or the start_height guard trips.
     *
     * @param structure     the live structure (must be a {@link JigsawStructure} for a prediction)
     * @param structureId   its registry id (keys the hardcoded table)
     * @param seed          the world seed
     * @param chunkX        structure chunk X
     * @param chunkZ        structure chunk Z
     * @param heightContext context for resolving the start-height provider (from the dimension's generator/level)
     */
    public static String predict(Object structure, ResourceLocation structureId, long seed, int chunkX, int chunkZ,
            WorldGenerationContext heightContext) {
        if (structureId == null || !(structure instanceof JigsawStructure)) {
            return null;
        }
        PoolInfo info = BY_STRUCTURE.get(structureId);
        if (info == null) {
            return null;
        }

        // start_height guard: the hardcoded table assumes a height-provider kind; bail loudly if the datapack drifted.
        HeightProvider startHeight = ((JigsawStructureAccessor) structure).resourceful_refinement$getStartHeight();
        if (!info.heightKind.matches(startHeight)) {
            if (WARNED.add(structureId)) {
                LOGGER.error("[ResourceResonator] start_height of structure '{}' is {}, but variant prediction "
                                + "assumes {}. Geyser variant labels disabled for it — update GeyserVariants to match "
                                + "the datapack.",
                        structureId, startHeight.getClass().getSimpleName(), info.heightKind);
            }
            return null;
        }

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, chunkX, chunkZ);
        startHeight.sample(random, heightContext); // consume exact height draws (0 or 1, via the real provider)
        Rotation.getRandom(random);                // rotation draw
        int roll = random.nextInt(info.totalWeight);
        return info.variantForRoll(roll);
    }
}
