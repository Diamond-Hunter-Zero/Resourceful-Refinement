package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.content.glare.GlareNodeBlockItem;
import com.resourceful_refinement.content.glare.RelayWrenchItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TelemetryAddressBehaviour extends FilteringBehaviour {
    private static final BehaviourType<TelemetryAddressBehaviour> FIRST = new BehaviourType<>();
    private static final BehaviourType<TelemetryAddressBehaviour> SECOND = new BehaviourType<>();
    private static final BehaviourType<TelemetryAddressBehaviour> THIRD = new BehaviourType<>();
    private final int slot;

    public TelemetryAddressBehaviour(SmartBlockEntity blockEntity, ValueBoxTransform transform, int slot) {
        super(blockEntity, transform);
        this.slot = slot;
        this.customLabel = Component.literal("Frequency #" + (slot+1));
    }

    @Override public BehaviourType<?> getType() {
        return switch (slot) { case 0 -> FIRST; case 1 -> SECOND; default -> THIRD; };
    }

    @Override public int netId() {
        return slot;
    }

    @Override public boolean setFilter(ItemStack stack) {
        ItemStack single = stack.copy();
        if (!single.isEmpty()) single.setCount(1);
        return super.setFilter(single);
    }

    @Override public boolean canShortInteract(ItemStack stack) {
        return !(stack.getItem() instanceof GlareNodeBlockItem)
                && !(stack.getItem() instanceof RelayWrenchItem)
                && super.canShortInteract(stack);
    }

    @Override public boolean bypassesInput(ItemStack stack) {
        return !canShortInteract(stack);
    }

    @Override public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        CompoundTag slotTag = new CompoundTag();
        super.write(slotTag, registries, clientPacket);
        nbt.put("TelemetryAddressSlot" + slot, slotTag);
    }

    @Override public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(nbt.getCompound("TelemetryAddressSlot" + slot), registries, clientPacket);
    }
}
