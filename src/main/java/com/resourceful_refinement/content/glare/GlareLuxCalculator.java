package com.resourceful_refinement.content.glare;

public final class GlareLuxCalculator {
    private GlareLuxCalculator() {}

    public static int sampleVariableLux(int[] curve, int progress, int duration) {
        if (curve == null || curve.length == 0) {
            return 0;
        }
        if (duration <= 0) {
            return curve[0];
        }
        int clamped = Math.max(0, Math.min(progress, duration - 1));
        int index = Math.min(curve.length - 1, (int) ((long) clamped * curve.length / duration));
        return Math.max(0, curve[index]);
    }
}
