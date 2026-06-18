package com.resourceful_refinement.content.glare;

import java.util.UUID;

public record GlareNetworkId(UUID value) {
    public static GlareNetworkId random() {
        return new GlareNetworkId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
