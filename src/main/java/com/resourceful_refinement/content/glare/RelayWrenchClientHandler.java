package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModDataComponents;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT)
public final class RelayWrenchClientHandler {
    private RelayWrenchClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        renderWrenchTarget(player);
        renderPlacementTargets(player, player.getMainHandItem(), "main_hand");
        renderPlacementTargets(player, player.getOffhandItem(), "off_hand");
    }

    private static void renderWrenchTarget(LocalPlayer player) {
        ItemStack wrench = selectedWrench(player);
        if (wrench.isEmpty()) return;
        showTarget(player, wrench.get(ModDataComponents.RELAY_WRENCH_TARGET.get()), "relay_wrench", 0xF2C94C);
    }

    private static void renderPlacementTargets(LocalPlayer player, ItemStack stack, String hand) {
        if (!(stack.getItem() instanceof GlareNodeBlockItem)) return;
        GlareTargetsData targets = stack.getOrDefault(ModDataComponents.GLARE_TARGETS.get(), GlareTargetsData.EMPTY);
        for (int i = 0; i < targets.targets().size(); i++) {
            showTarget(player, targets.targets().get(i), "placement_" + hand + "_" + i, 0x70A8E8);
        }
    }

    private static void showTarget(LocalPlayer player, GlareNodePos target, String source, int colour) {
        if (target == null || !target.levelKey().equals(player.level().dimension())
                || !player.level().isLoaded(target.pos())) return;
        if (!(player.level().getBlockEntity(target.pos()) instanceof IGlareNode)) return;

        VoxelShape shape = player.level().getBlockState(target.pos()).getShape(player.level(), target.pos());
        List<AABB> boxes = shape.isEmpty() ? List.of(new AABB(target.pos()))
                : shape.toAabbs().stream().map(box -> box.move(target.pos())).toList();
        for (int i = 0; i < boxes.size(); i++) {
            Outliner.getInstance().showAABB(new OutlineKey(source, target, i), boxes.get(i).inflate(-1.0D / 128.0D), 2)
                    .lineWidth(1.0F / 24.0F)
                    .disableLineNormals()
                    .colored(colour);
        }
    }

    private static ItemStack selectedWrench(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof RelayWrenchItem
                && mainHand.has(ModDataComponents.RELAY_WRENCH_TARGET.get())) return mainHand;
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof RelayWrenchItem
                && offHand.has(ModDataComponents.RELAY_WRENCH_TARGET.get())) return offHand;
        return ItemStack.EMPTY;
    }

    private record OutlineKey(String source, GlareNodePos target, int boxIndex) {}
}
