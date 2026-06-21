package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.config.ServerConfig;

public final class GlareDebug {
    private GlareDebug() {}

    public static boolean enabled() {
        return ServerConfig.GLARE_DEBUG_LOGGING.get();
    }

    public static void log(String message, Object... arguments) {
        if (enabled()) ResourcefulRefinementMain.LOGGER.info("[GLARE] " + message, arguments);
    }
}
