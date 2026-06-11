package com.resourceful_refinement.content.forge_mould.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record ChancedIngredient(Item item, float consumptionChance) {
    public static final Codec<ChancedIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ChancedIngredient::item),
            Codec.FLOAT.optionalFieldOf("consumption_chance", 1.0f).forGetter(ChancedIngredient::consumptionChance)
    ).apply(inst, ChancedIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChancedIngredient> STREAM_CODEC = StreamCodec.of(
            (buf, chanced) -> {
                ByteBufCodecs.registry(Registries.ITEM).encode(buf, chanced.item());
                buf.writeFloat(chanced.consumptionChance());
            },
            buf -> {
                Item item = ByteBufCodecs.registry(Registries.ITEM).decode(buf);
                float chance = buf.readFloat();
                return new ChancedIngredient(item, chance);
            }
    );
}
