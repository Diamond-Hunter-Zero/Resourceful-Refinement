package com.resourceful_refinement.content.prism_cell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * The {@code prism_cell_battery} data component. Every Prism Cell item carries one, prepopulated on creation from a
 * {@link PrismCellTier} preset. It stores the cell's tier index, its maximum charge capacity, and its current
 * remaining charge, so a cell's state travels with the item stack and is shared by every block/item that
 * interchanges cells (see {@link IPrismCellBlockConsumer}).
 *
 * <p>This is the immutable, serialised form. Blocks that run a cell convert it into a mutable {@link PrismCell} while
 * installed and convert back when the cell is removed.</p>
 */
public record PrismCellBatteryComponent(int tier, int maxCharge, int charge) {

    public PrismCellBatteryComponent {
        maxCharge = Math.max(0, maxCharge);
        charge = Mth.clamp(charge, 0, maxCharge);
    }

    public static final Codec<PrismCellBatteryComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(PrismCellBatteryComponent::tier),
            Codec.INT.fieldOf("max_charge").forGetter(PrismCellBatteryComponent::maxCharge),
            Codec.INT.fieldOf("charge").forGetter(PrismCellBatteryComponent::charge)
    ).apply(instance, PrismCellBatteryComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PrismCellBatteryComponent> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.tier());
                buf.writeVarInt(data.maxCharge());
                buf.writeVarInt(data.charge());
            },
            buf -> new PrismCellBatteryComponent(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    /** Builds a full-charge component for the given grade — the preset applied to a freshly crafted cell. */
    public static PrismCellBatteryComponent forTier(PrismCellTier tier) {
        return new PrismCellBatteryComponent(tier.index(), tier.maxCharge(), tier.maxCharge());
    }

    public PrismCellTier grade() {
        return PrismCellTier.byIndex(tier);
    }

    public boolean isCreative() {
        return grade().isCreative();
    }
}
