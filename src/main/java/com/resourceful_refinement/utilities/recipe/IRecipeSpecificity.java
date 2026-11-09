package com.resourceful_refinement.utilities.recipe;

/**
 * Implemented by recipes whose specificity can't be inferred from their ingredient lists alone
 * (sized ingredients, casting requirements, optional source conditions...).
 * Higher values are preferred when several recipes match the same input.
 */
public interface IRecipeSpecificity {
    int getSpecificity();
}
