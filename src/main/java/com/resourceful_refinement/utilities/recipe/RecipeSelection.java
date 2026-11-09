package com.resourceful_refinement.utilities.recipe;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.config.ServerConfig;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Deterministic recipe selection for Resourceful Refinement machines.
 * <p>
 * Vanilla {@code RecipeManager#getRecipeFor} returns the first match in the recipe manager's
 * iteration order. Our recipes match on "all required ingredients present", so a less specific
 * recipe (e.g. Crimsite + Silica) also matches an input meant for a more specific one
 * (Crimsite + Silica + Flux). Which one wins then depends on recipe load order, which other mods
 * can change. This helper collects every match and picks the most specific one, tie-breaking on
 * recipe ID so the result never depends on load order.
 */
public final class RecipeSelection {

    private static final Comparator<RecipeHolder<?>> PRIORITY =
            Comparator.<RecipeHolder<?>>comparingInt(h -> specificityOf(h.value())).reversed()
                    .thenComparing(h -> h.id().toString());

    private static final Map<String, String> LAST_LOGGED = new ConcurrentHashMap<>();

    private RecipeSelection() {}

    public static <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> findBestMatch(
            Level level, RecipeType<R> type, I input, String debugContext) {
        return findBestMatch(level, type, input, r -> true, debugContext);
    }

    /**
     * @param accept extra condition a matching recipe must satisfy (e.g. a machine's output filter).
     *               Applied to every candidate, not just the first, so filters can reach any recipe.
     */
    public static <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> findBestMatch(
            Level level, RecipeType<R> type, I input, Predicate<R> accept, String debugContext) {
        return findBestMatch(level, type, input, accept, r -> true, debugContext);
    }

    /**
     * Selection order:
     * <ol>
     *   <li>allowed by {@code accept} (e.g. the machine's Create filter)</li>
     *   <li>all ingredients present ({@code Recipe#matches})</li>
     *   <li>machine conditions met ({@code conditions}: heat, height...) - preferred, but not required,
     *       so a recipe whose conditions fail can still be selected and its error shown on goggles</li>
     *   <li>most distinct item + fluid ingredients</li>
     *   <li>recipe ID, for a deterministic tie-break</li>
     * </ol>
     * Callers can check {@code conditions.test(result)} to tell whether the result can actually run.
     */
    public static <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> findBestMatch(
            Level level, RecipeType<R> type, I input, Predicate<R> accept, Predicate<R> conditions, String debugContext) {
        if (level == null) return Optional.empty();

        List<RecipeHolder<R>> matches = new ArrayList<>();
        for (RecipeHolder<R> holder : level.getRecipeManager().getAllRecipesFor(type)) {
            if (holder.value().matches(input, level)) {
                matches.add(holder);
            }
        }
        if (matches.isEmpty()) return Optional.empty();

        matches.sort(PRIORITY);

        // Pass 1: accepted and conditions met. Pass 2: accepted only (conditions failing).
        Optional<RecipeHolder<R>> chosen = Optional.empty();
        for (RecipeHolder<R> holder : matches) {
            if (accept.test(holder.value()) && conditions.test(holder.value())) {
                chosen = Optional.of(holder);
                break;
            }
        }
        if (chosen.isEmpty()) {
            for (RecipeHolder<R> holder : matches) {
                if (accept.test(holder.value())) {
                    chosen = Optional.of(holder);
                    break;
                }
            }
        }

        if (isDebugEnabled()) {
            String candidates = matches.stream()
                    .map(h -> h.id() + "(" + specificityOf(h.value())
                            + (accept.test(h.value()) ? "" : ",filtered")
                            + (conditions.test(h.value()) ? "" : ",conditions unmet") + ")")
                    .toList().toString();
            String result = chosen.map(h -> h.id().toString()).orElse("none (rejected by filter)");

            // Machines re-query every tick or cycle; only log when the outcome changes for this machine
            String signature = candidates + "->" + result;
            if (!signature.equals(LAST_LOGGED.put(debugContext, signature))) {
                ResourcefulRefinementMain.LOGGER.info("[RecipeSelection] {} | input={} | candidates={} | chosen={}",
                        debugContext, input, candidates, result);
            }
        }

        return chosen;
    }

    public static int specificityOf(Recipe<?> recipe) {
        if (recipe instanceof IRecipeSpecificity s) return s.getSpecificity();
        if (recipe instanceof ProcessingRecipe<?, ?> p) {
            return p.getIngredients().size() + p.getFluidIngredients().size();
        }
        return recipe.getIngredients().size();
    }

    public static boolean isDebugEnabled() {
        return ServerConfig.SPEC.isLoaded() && ServerConfig.DEBUG_RECIPE_SELECTION.get();
    }
}
