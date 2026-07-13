package com.resourceful_refinement.client.research;

import com.resourceful_refinement.network.ResearchTerminalStatePayload;
import net.minecraft.core.BlockPos;

import java.util.Optional;

public final class ClientResearchTerminalData {
    private static ResearchTerminalStatePayload current;

    private ClientResearchTerminalData() {}

    public static void open(ResearchTerminalStatePayload payload) {
        current = payload;
    }

    public static void apply(ResearchTerminalStatePayload payload) {
        if (current != null && current.pos().equals(payload.pos())) {
            current = payload;
        }
    }

    public static Optional<ResearchTerminalStatePayload> current() {
        return Optional.ofNullable(current);
    }

    public static boolean isCurrent(BlockPos pos) {
        return current != null && current.pos().equals(pos);
    }

    public static void clear() {
        current = null;
    }
}
