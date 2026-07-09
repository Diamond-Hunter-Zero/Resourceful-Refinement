package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.glare.GlareSavedData;
import com.resourceful_refinement.content.glare.GlareService;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Queries persisted network membership first; callers decide when a candidate must also be loaded and live. */
public final class RemoteEntanglementService {
    private RemoteEntanglementService() {}

    public static List<GlareSavedData.NodeRecord> findCandidates(ServerLevel level, DimensionalNodePos source,
            RemoteEndpointKind kind, GlareAddress address) {
        UUID networkId = RemoteEntanglementUtil.networkId(level, source);
        if (networkId == null || !address.isComplete()) return List.of();
        GlareSavedData.NetworkRecord network = GlareService.getNetwork(level, networkId).orElse(null);
        if (network == null) return List.of();

        List<GlareSavedData.NodeRecord> result = new ArrayList<>();
        for (DimensionalNodePos candidatePos : network.nodes) {
            if (candidatePos.equals(source)) continue;
            GlareSavedData.NodeRecord candidate = GlareService.getNode(level, candidatePos).orElse(null);
            if (candidate == null || candidate.remoteEndpointKind != kind || !candidate.remoteAssembled) continue;
            if (!candidate.remoteAddress.equals(address) || !candidate.remoteMode.canReceive()) continue;
            result.add(candidate);
        }
        Collections.shuffle(result);
        return result;
    }
}
