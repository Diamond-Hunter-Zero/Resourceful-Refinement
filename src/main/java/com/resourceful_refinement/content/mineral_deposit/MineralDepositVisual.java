package com.resourceful_refinement.content.mineral_deposit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * A single visual mapping for a Mineral Deposit, loaded from
 * {@code assets/resourceful_refinement/mineral_deposit/visuals.json}.
 *
 * <ul>
 *     <li>{@link #baseMineral()}     &mdash; the block rendered as the solid base cube.</li>
 *     <li>{@link #depositMaterial()} &mdash; the block whose texture skins the deposit model.</li>
 * </ul>
 *
 * Purely client-side visualisation data; it has no gameplay meaning.
 */
public record MineralDepositVisual(Block baseMineral, Block depositMaterial) {

    /**
     * Codec for the <em>value</em> object of a dictionary entry. The dictionary key (the expected
     * {@code associatedBlock} id) is handled by the loader, not this codec.
     *
     * <p>{@code byNameCodec()} resolves ids straight to {@link Block} singletons and reports a
     * failure for unknown ids, which the loader turns into a skipped entry + warning.
     */
    public static final Codec<MineralDepositVisual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("base_mineral").forGetter(MineralDepositVisual::baseMineral),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("deposit_material").forGetter(MineralDepositVisual::depositMaterial)
    ).apply(instance, MineralDepositVisual::new));
}
