package com.resourceful_refinement.content.resonator.scan;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A single predicted world-generation feature located by a scan.
 *
 * <p>{@code x}/{@code z} are the exact horizontal block coordinates of the structure origin (deterministic from
 * the seed). {@code y} is a best-effort vertical estimate: accurate for surface geysers (sampled from the
 * world-surface heightmap), and approximate for cave/nether geysers, whose true Y is drawn from a per-chunk
 * uniform band at generation time. {@code yApproximate} flags that distinction for the UI.</p>
 */
public record ScannedPoi(int x, int y, int z, boolean yApproximate, ResonatorTarget target) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ScannedPoi> STREAM_CODEC = StreamCodec.of(
            ScannedPoi::write, ScannedPoi::read);

    private static void write(RegistryFriendlyByteBuf buf, ScannedPoi poi) {
        buf.writeVarInt(poi.x);
        buf.writeVarInt(poi.y);
        buf.writeVarInt(poi.z);
        buf.writeBoolean(poi.yApproximate);
        buf.writeVarInt(poi.target.ordinal());
    }

    private static ScannedPoi read(RegistryFriendlyByteBuf buf) {
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        int z = buf.readVarInt();
        boolean approx = buf.readBoolean();
        ResonatorTarget target = ResonatorTarget.byOrdinal(buf.readVarInt());
        if (target == null) {
            target = ResonatorTarget.SURFACE_GEYSER;
        }
        return new ScannedPoi(x, y, z, approx, target);
    }

    /** List codec for syncing a whole scan result. */
    public static final StreamCodec<RegistryFriendlyByteBuf, java.util.List<ScannedPoi>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list());
}
