package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LaunchpadMenu extends AbstractContainerMenu {
    public static final int PAD_SLOT_COUNT = LaunchpadControllerBlockEntity.INVENTORY_SLOTS;
    public static final int PLAYER_SLOT_START = PAD_SLOT_COUNT;
    public static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;

    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    private LaunchpadSnapshot snapshot;

    public LaunchpadMenu(int id, Inventory playerInventory, LaunchpadControllerBlockEntity controller) {
        this(id, playerInventory, controller.getBlockPos(), LaunchpadSnapshot.capture(controller),
                ContainerLevelAccess.create(controller.getLevel(), controller.getBlockPos()), controller);
    }

    private LaunchpadMenu(int id, Inventory playerInventory, BlockPos pos, LaunchpadSnapshot snapshot,
            ContainerLevelAccess access, LaunchpadControllerBlockEntity controller) {
        super(ModMenus.LAUNCHPAD.get(), id);
        blockPos = pos.immutable();
        this.snapshot = snapshot;
        this.access = access;
        ItemStackHandler padInventory = controller != null ? controller.inventory : new ItemStackHandler(PAD_SLOT_COUNT);

        // Place launchpad slots
        for (int slot = 0; slot < PAD_SLOT_COUNT; slot++) {
            addSlot(new SlotItemHandler(padInventory, slot, 61 + slot % 6 * 18, 62 + slot / 6 * 18));
        }
        // Place player slots
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9, 7 + column * 18, 108 + row * 18));
            }
        }
    }

    public static LaunchpadMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        LaunchpadSnapshot snapshot = LaunchpadSnapshot.read(buf);
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        LaunchpadControllerBlockEntity controller = blockEntity instanceof LaunchpadControllerBlockEntity value
                ? value : null;
        ContainerLevelAccess access = controller == null ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(controller.getLevel(), pos);
        return new LaunchpadMenu(id, inventory, pos, snapshot, access, controller);
    }

    public static void writeClientSideData(RegistryFriendlyByteBuf buf, LaunchpadControllerBlockEntity controller) {
        buf.writeBlockPos(controller.getBlockPos());
        LaunchpadSnapshot.capture(controller).write(buf);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public LaunchpadSnapshot getSnapshot() {
        return snapshot;
    }

    public void applySnapshot(LaunchpadSnapshot snapshot) {
        if (snapshot.revision() >= this.snapshot.revision()) this.snapshot = snapshot;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.LAUNCHPAD_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < PAD_SLOT_COUNT) {
            if (!moveItemStackTo(source, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(source, 0, PAD_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
