package com.resourceful_refinement.content.brewers_tap;

import com.mojang.serialization.Codec;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public enum FlavourType implements StringRepresentable {
    FRUIT("fruit_flavour", "Fruity", 0xF05C5C, () -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0)),
    SWEET("sweet_flavour", "Sugary", 0xF5C84C, () -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0)),
    VEG("veg_flavour", "Vegetal", 0x6FBD55, () -> new MobEffectInstance(MobEffects.JUMP, 300, 1)),
    YEAST("spice_flavour", "Spicy", 0xD6B77B, () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0)),
    CHILLED("chilled_flavour", "Chilled", 0x67CFE8, () -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0)),
    COSMIC("cosmic_flavour", "Cosmic", 0xA66BFF, () -> new MobEffectInstance(MobEffects.ABSORPTION, 400, 1));

    public static TagKey<Item> ALL_FLAVOURS_ITEM_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "all_flavour_tags"));

    public static final Codec<FlavourType> CODEC = StringRepresentable.fromEnum(FlavourType::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, FlavourType> STREAM_CODEC = StreamCodec.of(
            RegistryFriendlyByteBuf::writeEnum,
            buf -> buf.readEnum(FlavourType.class)
    );

    private final String tagName;
    private final String displayName;
    private final int color;
    private final Supplier<MobEffectInstance> effectFactory;

    FlavourType(String tagName, String displayName, int color, Supplier<MobEffectInstance> effectFactory) {
        this.tagName = tagName;
        this.displayName = displayName;
        this.color = color;
        this.effectFactory = effectFactory;
    }

    @Override
    public String getSerializedName() {
        return tagName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public MobEffectInstance createEffect() {
        return effectFactory.get();
    }

    public TagKey<Item> getItemTag() {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, tagName));
    }

    public static Optional<FlavourType> fromTagName(String tagName) {
        String normalized = tagName.contains(":") ? tagName.substring(tagName.indexOf(':') + 1) : tagName;
        normalized = normalized.toLowerCase(Locale.ROOT);
        for (FlavourType flavour : values()) {
            if (flavour.tagName.equals(normalized)) {
                return Optional.of(flavour);
            }
        }
        return Optional.empty();
    }
}
