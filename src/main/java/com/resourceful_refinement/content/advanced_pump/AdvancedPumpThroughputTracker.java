package com.resourceful_refinement.content.advanced_pump;

import net.createmod.catnip.math.BlockFace;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AdvancedPumpThroughputTracker {

    private static final Map<AdvancedPumpBlockEntity, Set<BlockFace>> PUMP_PATHS = new HashMap<>();

    private AdvancedPumpThroughputTracker() {
    }

    public static void addPath(AdvancedPumpBlockEntity pump, Set<BlockFace> faces) {
        if (faces.isEmpty())
            return;

        Set<BlockFace> combinedFaces = new HashSet<>(PUMP_PATHS.getOrDefault(pump, Collections.emptySet()));
        combinedFaces.addAll(faces);
        PUMP_PATHS.put(pump, Collections.unmodifiableSet(combinedFaces));
    }

    public static void remove(AdvancedPumpBlockEntity pump) {
        PUMP_PATHS.remove(pump);
    }

    public static void recordTransfer(Level level, BlockFace targetFace, int amount) {
        if (amount <= 0)
            return;

        for (Iterator<Map.Entry<AdvancedPumpBlockEntity, Set<BlockFace>>> iterator = PUMP_PATHS.entrySet()
            .iterator(); iterator.hasNext();) {
            Map.Entry<AdvancedPumpBlockEntity, Set<BlockFace>> entry = iterator.next();
            AdvancedPumpBlockEntity pump = entry.getKey();
            if (pump.isRemoved() || pump.getLevel() != level) {
                iterator.remove();
                continue;
            }

            if (entry.getValue().contains(targetFace))
                pump.recordMeasuredThroughput(amount);
        }
    }
}
