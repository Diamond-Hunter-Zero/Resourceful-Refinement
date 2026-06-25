package com.resourceful_refinement.utilities;

public class GoggleUtilities {

    public static String BuildTextProgressBar(int currentProgressInt, int totalDuration)
    {
        return BuildTextProgressBar((float) currentProgressInt/totalDuration);
    }

    public static String BuildTextProgressBar(float currentProgress)
    {
        float progress = Math.min(1.0f, currentProgress);
        int filledBlocks = (int) (progress * 8);
        StringBuilder bar = new StringBuilder("Progress: ");
        for (int i = 0; i < 8; i++) {
            if (i < filledBlocks) bar.append("§c█");
            else bar.append("§0█");
        }

        return bar.toString();
    }

    public static String FormatTicksToTime(int ticks) {
        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return minutes + "m " + seconds + "s";
    }

}
