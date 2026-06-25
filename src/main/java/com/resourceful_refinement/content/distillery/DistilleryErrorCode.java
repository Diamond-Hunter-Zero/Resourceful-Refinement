package com.resourceful_refinement.content.distillery;

import org.jetbrains.annotations.NotNull;

public enum DistilleryErrorCode {

    NO_RECIPE("No Recipe"),
    NO_FILTER_MATCH("No recipes match filter"),
    INPUT_FLUID("Insufficient fluid"),
    INPUT_ITEM("Insufficient items"),
    HEAT("Distillery must be "),
    STACK_HEIGHT("Distillery must be "),
    OUTPUT_SPACE("Output tank full"),
    NO_ERROR("");

    private final String errorText;

    private DistilleryErrorCode(String errorText) {
        this.errorText = errorText;
    }

    public @NotNull String getErrorText() {
        return this.errorText;
    }

}
