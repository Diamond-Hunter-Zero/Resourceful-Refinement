package com.resourceful_refinement.client.research;

import com.mojang.blaze3d.platform.InputConstants;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import com.resourceful_refinement.content.gui.GuiTextUtilities;
import com.resourceful_refinement.content.gui.ScaledGuiCanvas;
import com.resourceful_refinement.content.gui.SqrHoverButton;
import com.resourceful_refinement.network.RequestResearchTreeDataPayload;
import com.resourceful_refinement.network.ResearchTerminalActionPayload;
import com.resourceful_refinement.network.ResearchTerminalStatePayload;
import com.resourceful_refinement.network.ResearchTreeSyncPayload;
import com.resourceful_refinement.content.research_terminal.ResearchTerminalCycleKind;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ResearchTreeScreen extends Screen {
    private static final int LEFT_PANEL_WIDTH = 192;
    private static final int DETAILS_HEIGHT = 102;
    private static final int PANEL = 0xF0060A13;
    private static final int PANEL_ALT = 0xF00B1020;
    private static final int BORDER = 0xFF4D5868;
    private static final int TEXT = 0xFFE8ECF0;
    private static final int MUTED = 0xFF9BA5B1;
    private static final int ACCENT = 0xFFFFC857;
    private static final int LOCKED = 0xFFFF8B38;
    private static final int LOCKED_FOR_PLAYER = 0xFFE5C65F;
    private static final int UNLOCKED = 0xFF79D889;
    private static final double MIN_ZOOM = 0.25;
    private static final double MAX_ZOOM = 2.0;
    private static final int GUI_WINDOW_WIDTH_REFERENCE = 1080;
    private static final int VIEW_PADDING = 140;
    private static final int CLOSE_BUTTON_SIZE = 18;
    private static final int DETAILS_SCROLL_STEP = 18;
    private static final int REQUIREMENTS_PANEL_WIDTH = 144;
    private static final int REQUIREMENTS_PANEL_TOP = 76;
    private static final int REQUIREMENT_ROW_HEIGHT = 28;
    private static final int PROGRESS = 0xFF4BE7D5;
    private static final int COMPLETE_MUTED = 0xFF66707C;
    private static final int TERMINAL_TAB_WIDTH = 208;
    private static final int TERMINAL_TAB_HEIGHT = 30;
    private static final int TERMINAL_BUTTON_WIDTH = 154;
    private static final int TERMINAL_BUTTON_HEIGHT = 20;

    private final boolean terminalMode;
    private ResourceLocation selectedNodeId;
    private ResourceLocation hoveredNodeId;
    private ResourceLocation lastTreeId;
    private int treeScroll;
    private int detailScroll;
    private double cameraX = 80.0;
    private double cameraY = 40.0;
    private double zoom = 1.0;
    private boolean draggingTree;
    private boolean focusedTerminalTarget;
    private boolean pendingTerminalTargetCamera;
    private long lastRequestMillis;
    private ItemStack hoveredDetailStack = ItemStack.EMPTY;
    private ScaledGuiCanvas canvas;

    public ResearchTreeScreen() {
        this(false);
    }

    public ResearchTreeScreen(boolean terminalMode) {
        super(Component.translatable("gui.resourceful_refinement.research_tree"));
        this.terminalMode = terminalMode;
    }

    @Override
    protected void init() {
        updateCanvas();
        addRenderableWidget(new SqrHoverButton(
                width - CLOSE_BUTTON_SIZE - 6,
                6,
                CommonSqrButtonTextures.HOVER_CLOSE,
                this::onClose));
        if (!terminalMode) {
            ClientResearchTerminalData.clear();
        }
        requestSync();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateCanvas();
        int contentMouseX = canvas.toCanvasX(mouseX);
        int contentMouseY = canvas.toCanvasY(mouseY);
        hoveredDetailStack = ItemStack.EMPTY;
        graphics.fill(0, 0, width, height, 0xFF050914);
        canvas.push(graphics);
        renderTreeList(graphics, contentMouseX, contentMouseY);

        int viewX = LEFT_PANEL_WIDTH;
        int viewY = 0;
        int viewW = canvas.width() - LEFT_PANEL_WIDTH;
        int viewH = canvas.height() - DETAILS_HEIGHT;
        graphics.fill(viewX, viewY, canvas.width(), viewH, 0xFF08101D);
        renderGrid(graphics, viewX, viewY, viewW, viewH);

        ResourceLocation treeId = ClientResearchTreeData.selectedTreeId();
        if (treeId == null) {
            maybeRequestAgain();
            graphics.drawCenteredString(font, Component.literal("Loading research trees..."), viewX + viewW / 2,
                    viewY + viewH / 2 - 5, MUTED);
            canvas.pop(graphics);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        if (!treeId.equals(lastTreeId)) {
            lastTreeId = treeId;
            selectedNodeId = null;
            detailScroll = 0;
            cameraX = 80.0;
            cameraY = 40.0;
            zoom = 1.0;
        }
        if (terminalMode && !focusedTerminalTarget && focusTerminalTargetIfPresent()) {
            treeId = ClientResearchTreeData.selectedTreeId();
            lastTreeId = treeId;
        }

        ResearchTreeLayout layout = ClientResearchTreeData.layout(treeId);
        if (pendingTerminalTargetCamera) {
            centerCameraOnSelectedNode(layout, viewW, viewH);
            pendingTerminalTargetCamera = false;
        }
        clampCamera(layout, viewW, viewH);
        hoveredNodeId = findNodeAt(layout, contentMouseX, contentMouseY, viewX, viewY);

        canvas.enableScissor(graphics, viewX, viewY, canvas.width(), viewH);
        graphics.pose().pushPose();
        graphics.pose().translate(viewX + cameraX, viewY + cameraY, 0);
        graphics.pose().scale((float) zoom, (float) zoom, 1.0f);
        renderLinks(graphics, layout);
        renderNodes(graphics, layout);
        graphics.pose().popPose();
        graphics.disableScissor();

        renderDetails(graphics, treeId, contentMouseX, contentMouseY);
        renderRequirementsPanel(graphics, treeId, contentMouseX, contentMouseY);

        boolean isActivelyResearchingNode = false;
        if (terminalMode) {
            renderTerminalOwnerTab(graphics, contentMouseX, contentMouseY);
            isActivelyResearchingNode = renderTerminalTargetControl(graphics, treeId, contentMouseX, contentMouseY);
        }
        if (!isActivelyResearchingNode)
            renderRequirementProgressBar(graphics, treeId);

        graphics.drawString(font, "x" + String.format("%.2f", zoom), canvas.width() - REQUIREMENTS_PANEL_WIDTH, 10, MUTED, false);

        canvas.pop(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!hoveredDetailStack.isEmpty()) {
            graphics.renderTooltip(font, hoveredDetailStack, mouseX, mouseY);
        }
    }

    private void updateCanvas() {
        canvas = ScaledGuiCanvas.referenceWidth(width, height, GUI_WINDOW_WIDTH_REFERENCE);
    }

    private void renderTreeList(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, LEFT_PANEL_WIDTH, canvas.height(), PANEL);
        graphics.fill(LEFT_PANEL_WIDTH - 1, 0, LEFT_PANEL_WIDTH, canvas.height(), BORDER);
        graphics.drawString(font, Component.literal("Trees"), 12, 14, TEXT, false);

        int y = 38 - treeScroll;
        ResourceLocation selectedTree = ClientResearchTreeData.selectedTreeId();
        for (ResearchTreeSyncPayload.TreeEntry tree : ClientResearchTreeData.trees()) {
            if (y > 30 && y < canvas.height() - 8) {
                boolean selected = tree.id().equals(selectedTree);
                int background = selected ? 0xFF15233C : (mouseX < LEFT_PANEL_WIDTH && mouseY >= y && mouseY < y + 32
                        ? 0xFF10192B : 0xC0090E18);
                graphics.fill(8, y, LEFT_PANEL_WIDTH - 8, y + 32, background);
                if (selected) graphics.fill(8, y, 11, y + 32, ACCENT);
                graphics.renderItem(tree.icon(), 16, y + 8);
                int textX = 38;
                int textWidth = LEFT_PANEL_WIDTH - textX - 14;
                GuiTextUtilities.drawFittedString(graphics, font, tree.name(), textX, y + 6, textWidth, TEXT, false);
                GuiTextUtilities.drawFittedString(graphics, font, tree.id().toString(), textX, y + 18, textWidth,
                        MUTED, false);
            }
            y += 36;
        }
    }

    private void renderGrid(GuiGraphics graphics, int x, int y, int width, int height) {
        int line = 0x221B2B40;
        int offsetX = (int) Math.floor(cameraX % 24.0);
        int offsetY = (int) Math.floor(cameraY % 24.0);
        for (int gx = x + offsetX; gx < x + width; gx += 24) {
            graphics.fill(gx, y, gx + 1, y + height, line);
        }
        for (int gy = y + offsetY; gy < y + height; gy += 24) {
            graphics.fill(x, gy, x + width, gy + 1, line);
        }
    }

    private boolean focusTerminalTargetIfPresent() {
        Optional<ResearchTerminalStatePayload> optional = ClientResearchTerminalData.current();
        if (optional.isEmpty() || optional.get().targetNodeId() == null) {
            focusedTerminalTarget = true;
            return false;
        }
        ResourceLocation targetNodeId = optional.get().targetNodeId();
        ResearchTreeSyncPayload.NodeEntry targetNode = ClientResearchTreeData.node(targetNodeId).orElse(null);
        if (targetNode == null) {
            return false;
        }
        ClientResearchTreeData.selectTree(targetNode.treeId());
        selectedNodeId = targetNodeId;
        detailScroll = 0;
        focusedTerminalTarget = true;
        pendingTerminalTargetCamera = true;
        return true;
    }

    private void centerCameraOnSelectedNode(ResearchTreeLayout layout, int viewW, int viewH) {
        if (selectedNodeId == null) return;
        ResearchTreeLayout.PositionedNode positioned = layout.node(selectedNodeId);
        if (positioned == null) return;
        double nodeCenterX = positioned.x() + ResearchTreeLayout.NODE_SIZE / 2.0;
        double nodeCenterY = positioned.y() + ResearchTreeLayout.NODE_SIZE / 2.0;
        cameraX = viewW / 2.0 - nodeCenterX * zoom;
        cameraY = viewH / 2.0 - nodeCenterY * zoom;
    }

    private void renderLinks(GuiGraphics graphics, ResearchTreeLayout layout) {
        for (ResearchTreeLayout.PositionedNode node : layout.nodes()) {
            int childX = node.x() + ResearchTreeLayout.NODE_SIZE / 2;
            int childY = node.y() + ResearchTreeLayout.NODE_SIZE / 2;
            for (ResourceLocation parentId : node.entry().parents()) {
                ResearchTreeLayout.PositionedNode parent = layout.parentNode(parentId);
                if (parent == null) continue;
                int parentX = parent.x() + ResearchTreeLayout.NODE_SIZE / 2;
                int parentY = parent.y() + ResearchTreeLayout.NODE_SIZE / 2;
                boolean highlight = node.entry().id().equals(hoveredNodeId) || parentId.equals(hoveredNodeId)
                        || node.entry().id().equals(selectedNodeId) || parentId.equals(selectedNodeId);
                int color = highlight ? 0xDDC6D6EA : (node.entry().unlocked() ? 0xAA8DBB9A : 0x88717E8F);
                int bendX = parentX + Math.max(24, (childX - parentX) / 2);
                if (node.entry().unlocked()) {
                    drawSolidLine(graphics, parentX, parentY, bendX, parentY, color);
                    drawSolidLine(graphics, bendX, parentY, bendX, childY, color);
                    drawSolidLine(graphics, bendX, childY, childX, childY, color);
                } else {
                    drawDashedLine(graphics, parentX, parentY, bendX, parentY, color);
                    drawDashedLine(graphics, bendX, parentY, bendX, childY, color);
                    drawDashedLine(graphics, bendX, childY, childX, childY, color);
                }
            }
        }
    }

    private void drawSolidLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        if (x1 == x2) {
            graphics.fill(x1 - 2, Math.min(y1, y2), x1 + 2, Math.max(y1, y2) + 1, color);
        } else {
            graphics.fill(Math.min(x1, x2), y1 - 2, Math.max(x1, x2) + 1, y1 + 2, color);
        }
    }

    private void drawDashedLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);
        int length = Math.abs(x2 - x1) + Math.abs(y2 - y1);
        for (int offset = 0; offset < length; offset += 12) {
            int segment = Math.min(7, length - offset);
            int sx = x1 + dx * offset;
            int sy = y1 + dy * offset;
            int ex = x1 + dx * (offset + segment);
            int ey = y1 + dy * (offset + segment);
            if (dx != 0) {
                graphics.fill(Math.min(sx, ex), sy - 2, Math.max(sx, ex) + 1, sy + 2, color);
            } else {
                graphics.fill(sx - 2, Math.min(sy, ey), sx + 2, Math.max(sy, ey) + 1, color);
            }
        }
    }

    private void renderNodes(GuiGraphics graphics, ResearchTreeLayout layout) {
        for (ResearchTreeLayout.PositionedNode positioned : layout.nodes()) {
            ResearchTreeSyncPayload.NodeEntry node = positioned.entry();
            boolean hovered = node.id().equals(hoveredNodeId);
            boolean selected = node.id().equals(selectedNodeId);
            boolean unlocked = node.unlocked();
            int x = positioned.x();
            int y = positioned.y();
            int border = selected ? ACCENT : (hovered ? 0xFFD6E4F7 : BORDER);
            int fill = unlocked ? 0xFF10253A : 0xFF11131B;
            graphics.fill(x - 3, y - 3, x + ResearchTreeLayout.NODE_SIZE + 3, y + ResearchTreeLayout.NODE_SIZE + 3,
                    border);
            graphics.fill(x, y, x + ResearchTreeLayout.NODE_SIZE, y + ResearchTreeLayout.NODE_SIZE, fill);
            graphics.fill(x + 5, y + 5, x + ResearchTreeLayout.NODE_SIZE - 5, y + ResearchTreeLayout.NODE_SIZE - 5,
                    unlocked ? 0xFF173D5B : 0xFF1E2430);

            graphics.pose().pushPose();
            float iconScale = hovered ? 2f : 1.5f;
            float iconX = x + 13;
            float iconY = y + 13;
            graphics.pose().translate(iconX + 8, iconY + 8, 80);
            graphics.pose().scale(iconScale, iconScale, 1.0f);
            graphics.renderItem(node.icon(), -8, -8);
            graphics.pose().popPose();

            if (!unlocked) {
                int lockColor = node.serverUnlocked() ? LOCKED_FOR_PLAYER : LOCKED;
                drawLock(graphics, x + ResearchTreeLayout.NODE_SIZE - 14, y + ResearchTreeLayout.NODE_SIZE - 14,
                        lockColor);
            }
        }
    }

    private void drawLock(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x + 2, y + 5, x + 10, y + 12, color);
        graphics.fill(x + 3, y + 3, x + 5, y + 7, color);
        graphics.fill(x + 7, y + 3, x + 9, y + 7, color);
        graphics.fill(x + 4, y + 2, x + 8, y + 4, color);
    }

    private void renderDetails(GuiGraphics graphics, ResourceLocation treeId, int mouseX, int mouseY) {
        int y = canvas.height() - DETAILS_HEIGHT;
        graphics.fill(LEFT_PANEL_WIDTH, y, canvas.width(), canvas.height(), PANEL_ALT);
        graphics.fill(LEFT_PANEL_WIDTH, y, canvas.width(), y + 1, BORDER);

        ResearchTreeSyncPayload.NodeEntry node = selectedNodeId == null
                ? null
                : ClientResearchTreeData.node(selectedNodeId).orElse(null);
        if (node == null || !node.treeId().equals(treeId)) {
            ClientResearchTreeData.tree(treeId).ifPresent(tree -> {
                graphics.renderItem(tree.icon(), LEFT_PANEL_WIDTH + 18, y + 18);
                graphics.drawString(font, tree.name(), LEFT_PANEL_WIDTH + 44, y + 16, TEXT, false);
                List<FormattedCharSequence> lines = font.split(tree.description(), Math.max(120, canvas.width() - LEFT_PANEL_WIDTH - 80));
                for (int i = 0; i < Math.min(4, lines.size()); i++) {
                    graphics.drawString(font, lines.get(i), LEFT_PANEL_WIDTH + 18, y + 44 + i * 10, MUTED, false);
                }
            });
            return;
        }

        graphics.renderItem(node.icon(), LEFT_PANEL_WIDTH + 18, y + 18);
        graphics.drawString(font, node.title(), LEFT_PANEL_WIDTH + 44, y + 13, TEXT, false);
        Component status = lockStatus(node);
        graphics.drawString(font, status, LEFT_PANEL_WIDTH + 44, y + 27, lockStatusColor(node), false);

        int descriptionWidth = Math.max(140, canvas.width() - LEFT_PANEL_WIDTH - 360);
        List<FormattedCharSequence> description = font.split(node.description(), descriptionWidth);
        for (int i = 0; i < Math.min(5, description.size()); i++) {
            graphics.drawString(font, description.get(i), LEFT_PANEL_WIDTH + 18, y + 52 + i * 10, MUTED, false);
        }

        int rewardsX = Math.max(LEFT_PANEL_WIDTH + 300, canvas.width() - 330);
        int recipesX = Math.max(rewardsX + 120, canvas.width() - 190);
        int scrollTop = y + 12;
        int scrollBottom = canvas.height() - 8;
        int rewardsWidth = Math.max(90, recipesX - rewardsX - 18);
        int unlocksWidth = Math.max(90, canvas.width() - recipesX - 14);

        detailScroll = Math.min(detailScroll, maxDetailScroll(node, rewardsWidth, unlocksWidth, scrollBottom - scrollTop));
        canvas.enableScissor(graphics, rewardsX - 4, scrollTop, canvas.width() - 6, scrollBottom);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -detailScroll, 0);
        graphics.drawString(font, Component.literal("Rewards"), rewardsX, y + 14, TEXT, false);
        drawStackGrid(graphics, node.rewards(), rewardsX, y + 30, rewardsWidth, true, scrollTop, scrollBottom, mouseX,
                mouseY);
        graphics.drawString(font, Component.literal("Unlocked Recipes"), recipesX, y + 14, TEXT, false);
        int unlockY = drawStackGrid(graphics, node.lockedItems(), recipesX, y + 30, unlocksWidth, false, scrollTop,
                scrollBottom, mouseX, mouseY);
        int recipeY = unlockY + 4;
        for (int i = 0; i < node.recipes().size(); i++) {
            String recipe = node.recipes().get(i).toString();
            graphics.drawString(font, font.plainSubstrByWidth(recipe, Math.max(60, unlocksWidth)), recipesX,
                    recipeY + i * 10, MUTED, false);
        }
        graphics.pose().popPose();
        graphics.disableScissor();

        int maxScroll = maxDetailScroll(node, rewardsWidth, unlocksWidth, scrollBottom - scrollTop);
        if (maxScroll > 0) {
            int trackX = canvas.width() - 8;
            int trackTop = scrollTop + 2;
            int trackBottom = scrollBottom - 2;
            int thumbHeight = Math.max(16, (trackBottom - trackTop) * (scrollBottom - scrollTop)
                    / ((scrollBottom - scrollTop) + maxScroll));
            int thumbY = trackTop + (trackBottom - trackTop - thumbHeight) * detailScroll / maxScroll;
            graphics.fill(trackX, trackTop, trackX + 2, trackBottom, 0x55384655);
            graphics.fill(trackX - 1, thumbY, trackX + 3, thumbY + thumbHeight, 0xCC8A98AA);
        }
    }

    private void renderRequirementsPanel(GuiGraphics graphics, ResourceLocation treeId, int mouseX, int mouseY) {
        ResearchTreeSyncPayload.NodeEntry node = selectedNodeId == null
                ? null
                : ClientResearchTreeData.node(selectedNodeId).orElse(null);
        if (node == null || !node.treeId().equals(treeId) || node.requirements().isEmpty()) {
            return;
        }

        int x = requirementsPanelX();
        int y = REQUIREMENTS_PANEL_TOP;
        int bottom = canvas.height() - DETAILS_HEIGHT - 8;
        if (bottom <= y + 28) return;

        graphics.fill(x, y, canvas.width(), bottom, PANEL);
        graphics.renderOutline(x, y, canvas.width() - x, bottom - y, BORDER);
        graphics.drawCenteredString(font, Component.literal("Requirements"), x + (canvas.width() - x) / 2,
                y + 12, TEXT);

        List<ResearchTreeSyncPayload.RequirementEntry> requirements = node.requirements().stream()
                .sorted(Comparator.comparing(ResearchTreeSyncPayload.RequirementEntry::complete)
                        .thenComparing(entry -> entry.id().toString()))
                .toList();
        int rowY = y + 34;
        for (ResearchTreeSyncPayload.RequirementEntry requirement : requirements) {
            if (rowY + REQUIREMENT_ROW_HEIGHT > bottom - 8) break;
            boolean complete = requirement.complete();
            graphics.fill(x + 8, rowY, canvas.width() - 8, rowY + REQUIREMENT_ROW_HEIGHT - 2,
                    complete ? 0x80000000 : 0xCC000000);
            int iconX = x + 16;
            int iconY = rowY + 6;
            ItemStack icon = requirement.icon();
            graphics.renderItem(icon, iconX, iconY);
            if (isMouseOver(mouseX, mouseY, iconX, iconY, 16, 16)) {
                hoveredDetailStack = icon;
            }
            if (complete) {
                graphics.drawString(font, Component.literal("✓"), x + 30, rowY + 12, UNLOCKED, false);
            }
            String amount = requirement.fluid()
                    ? (requirement.current() / 1000) + " / " + (requirement.required() / 1000) + " B"
                    : requirement.current() + " / " + requirement.required();
            graphics.drawString(font, amount, canvas.width() - 12 - font.width(amount), rowY + 10,
                    complete ? COMPLETE_MUTED : TEXT, false);
            rowY += REQUIREMENT_ROW_HEIGHT;
        }
    }

    private void renderTerminalOwnerTab(GuiGraphics graphics, int mouseX, int mouseY) {
        Optional<ResearchTerminalStatePayload> optional = ClientResearchTerminalData.current();
        if (optional.isEmpty()) return;
        ResearchTerminalStatePayload terminal = optional.get();
        int x = terminalTabX();
        int y = 8;
        boolean unowned = terminal.ownerUuid() == null;
        boolean hovered = isMouseOver(mouseX, mouseY, x, y, TERMINAL_TAB_WIDTH, TERMINAL_TAB_HEIGHT);
        graphics.fill(x, y, x + TERMINAL_TAB_WIDTH, y + TERMINAL_TAB_HEIGHT,
                hovered && unowned ? 0xF0182838 : PANEL);
        graphics.renderOutline(x, y, TERMINAL_TAB_WIDTH, TERMINAL_TAB_HEIGHT, unowned ? ACCENT : BORDER);

        int faceX = x + 7;
        int faceY = y + (TERMINAL_TAB_HEIGHT - 16) / 2;
        if (!unowned && isLocalOwner(terminal)) {
            PlayerFaceRenderer.draw(graphics, minecraft.player.getSkin(), faceX, faceY, 16);
        } else {
            graphics.fill(faceX, faceY, faceX + 16, faceY + 16, unowned ? 0xFF3C2D16 : 0xFF1A2431);
            graphics.renderOutline(faceX, faceY, 16, 16, unowned ? ACCENT : MUTED);
            String mark = unowned ? "+" : "?";
            graphics.drawCenteredString(font, Component.literal(mark), faceX + 8, faceY + centeredTextOffset(16),
                    unowned ? ACCENT : MUTED);
        }

        Component title = unowned
                ? Component.literal("Unassigned Terminal")
                : Component.literal("Owner: " + terminal.ownerName());
        Component hint = unowned
                ? Component.literal("Click to claim")
                : Component.literal(terminal.ownerUuid().toString());
        int textY = y + (TERMINAL_TAB_HEIGHT - font.lineHeight * 2 - 2) / 2;
        graphics.drawString(font, title, x + 29, textY, unowned ? ACCENT : TEXT, false);
        GuiTextUtilities.drawFittedString(graphics, font, hint, x + 29, textY + font.lineHeight + 2,
                TERMINAL_TAB_WIDTH - 36, unowned ? TEXT : MUTED, false);
    }

    private boolean renderTerminalTargetControl(GuiGraphics graphics, ResourceLocation treeId, int mouseX, int mouseY) {
        Optional<ResearchTerminalStatePayload> optional = ClientResearchTerminalData.current();
        if (optional.isEmpty() || !isLocalOwner(optional.get())) return false;
        ResearchTreeSyncPayload.NodeEntry node = selectedNodeId == null
                ? null
                : ClientResearchTreeData.node(selectedNodeId).orElse(null);
        if (node == null || !node.treeId().equals(treeId) || node.unlocked()) {
            return false;
        }

        int x = terminalButtonX();
        int y = terminalButtonY();
        if (node.id().equals(optional.get().targetNodeId())) {

            int midpoint = canvas.width()/2;
            int maxHalfWidth = Math.min(midpoint - LEFT_PANEL_WIDTH, midpoint - REQUIREMENTS_PANEL_WIDTH) - 12;   // Add padding of 12px

            renderTerminalProgress(graphics, optional.get(), node, midpoint - maxHalfWidth, y+8, maxHalfWidth * 2);
            return true;    // Return TRUE that this node is the current ongoing research target
        }

        boolean prerequisites = prerequisitesUnlocked(node);
        boolean hovered = isMouseOver(mouseX, mouseY, x, y, TERMINAL_BUTTON_WIDTH, TERMINAL_BUTTON_HEIGHT);
        int fill = prerequisites ? (hovered ? 0xFF26374D : 0xFF19283A) : 0xFF141821;
        int border = prerequisites ? ACCENT : COMPLETE_MUTED;
        graphics.fill(x, y, x + TERMINAL_BUTTON_WIDTH, y + TERMINAL_BUTTON_HEIGHT, fill);
        graphics.renderOutline(x, y, TERMINAL_BUTTON_WIDTH, TERMINAL_BUTTON_HEIGHT, border);
        Component label = prerequisites ? Component.literal("Research This Node") : Component.literal("Node Locked");
        graphics.drawCenteredString(font, label, x + TERMINAL_BUTTON_WIDTH / 2,
                y + centeredTextOffset(TERMINAL_BUTTON_HEIGHT),
                prerequisites ? TEXT : COMPLETE_MUTED);

        return false;
    }

    private void renderTerminalProgress(GuiGraphics graphics, ResearchTerminalStatePayload terminal,
            ResearchTreeSyncPayload.NodeEntry node, int x, int y, int width) {
        int required = 0;
        int current = 0;
        for (ResearchTreeSyncPayload.RequirementEntry requirement : node.requirements()) {
            int requiredUnits = requirement.fluid() ? requirement.required() / 1000 : requirement.required();
            int currentUnits = requirement.fluid() ? requirement.current() / 1000 : requirement.current();
            required += requiredUnits;
            current += Math.min(requiredUnits, currentUnits);
        }
        double researchRatio = required <= 0 ? 1.0 : Math.min(1.0, current / (double) required);
        double cycleRatio = terminal.cycleKind() == ResearchTerminalCycleKind.IDLE
                ? 0.0
                : Math.min(1.0, terminal.cycleTicks() / (double) terminal.cycleDuration());

        graphics.drawCenteredString(font, Component.literal("Researching this node (" + (int)(researchRatio*100) + "%)..."),
                x + width / 2, y - 12, PROGRESS);
        graphics.fill(x, y, x + width, y + TERMINAL_BUTTON_HEIGHT, 0xFF111822);
        graphics.renderOutline(x, y, width, TERMINAL_BUTTON_HEIGHT, BORDER);
        int barTop = y + (TERMINAL_BUTTON_HEIGHT - 14) / 2;
        graphics.fill(x + 2, barTop, x + 2 + (int) ((width - 4) * researchRatio), barTop + 7,
                PROGRESS);
        if (cycleRatio > 0.0) {
            graphics.fill(x + 2, barTop + 10, x + 2 + (int) ((width - 4) * cycleRatio),
                    barTop + 14, ACCENT);
        }
    }

    private void renderRequirementProgressBar(GuiGraphics graphics, ResourceLocation treeId) {
        ResearchTreeSyncPayload.NodeEntry node = selectedNodeId == null
                ? null
                : ClientResearchTreeData.node(selectedNodeId).orElse(null);
        if (node == null || !node.treeId().equals(treeId) || node.unlocked() || node.requirements().isEmpty()) {
            return;
        }
        int requiredUnits = 0;
        int completedUnits = 0;
        for (ResearchTreeSyncPayload.RequirementEntry requirement : node.requirements()) {
            int required = requirement.fluid() ? requirement.required() / 1000 : requirement.required();
            int current = requirement.fluid() ? requirement.current() / 1000 : requirement.current();
            requiredUnits += required;
            completedUnits += Math.min(required, current);
        }
        if (requiredUnits <= 0 || completedUnits <= 0) return;
        double ratio = Math.min(1.0, completedUnits / (double) requiredUnits);

        int midpoint = canvas.width()/2;
        int rightLimit = node.requirements().isEmpty() ? 0: REQUIREMENTS_PANEL_WIDTH;
        int maxHalfWidth = Math.min(midpoint - LEFT_PANEL_WIDTH, midpoint - rightLimit) - 12;   // Add padding of 12px
        if (maxHalfWidth <= 16) return;     // Don't draw progress bar if screen is way too small!

        int detailsTop = canvas.height() - DETAILS_HEIGHT;
        int barX = midpoint - maxHalfWidth;
        int barY = detailsTop - 16;

        int barW = maxHalfWidth * 2;


        String label = Math.round(ratio * 100.0) + "% Researched";
        graphics.drawCenteredString(font, Component.literal(label), barX + barW / 2, barY - 12, PROGRESS);
        graphics.fill(barX, barY, barX + barW, barY + 8, 0xFF1B2430);
        graphics.renderOutline(barX, barY, barW, 8, BORDER);
        graphics.fill(barX + 2, barY + 2, barX + 2 + (int) ((barW - 4) * ratio), barY + 6, PROGRESS);
    }

    private int requirementsPanelX() {
        return Math.max(LEFT_PANEL_WIDTH + 260, canvas.width() - REQUIREMENTS_PANEL_WIDTH);
    }

    private int terminalTabX() {
        return canvas.width()/2 - TERMINAL_TAB_WIDTH/2;
    }

    private int terminalButtonX() {
        int rightLimit = requirementsPanelX() - 12;
        return Math.max(LEFT_PANEL_WIDTH + 214, rightLimit - TERMINAL_BUTTON_WIDTH);
    }

    private int terminalButtonY() {
        return canvas.height() - DETAILS_HEIGHT - 34;
    }

    private int centeredTextOffset(int height) {
        return (height - font.lineHeight) / 2;
    }

    private boolean isOverTerminalOwnerTab(double canvasMouseX, double canvasMouseY) {
        return terminalMode && ClientResearchTerminalData.current()
                .map(terminal -> terminal.ownerUuid() == null
                        && isMouseOver(canvasMouseX, canvasMouseY, terminalTabX(), 8,
                        TERMINAL_TAB_WIDTH, TERMINAL_TAB_HEIGHT))
                .orElse(false);
    }

    private boolean isOverTerminalTargetControl(double canvasMouseX, double canvasMouseY) {
        if (!terminalMode) return false;
        return isMouseOver(canvasMouseX, canvasMouseY, terminalButtonX(), terminalButtonY(),
                TERMINAL_BUTTON_WIDTH, TERMINAL_BUTTON_HEIGHT);
    }

    private boolean isLocalOwner(ResearchTerminalStatePayload terminal) {
        return minecraft.player != null && terminal.ownerUuid() != null
                && terminal.ownerUuid().equals(minecraft.player.getUUID());
    }

    private boolean prerequisitesUnlocked(ResearchTreeSyncPayload.NodeEntry node) {
        for (ResourceLocation parentId : node.parents()) {
            ResearchTreeSyncPayload.NodeEntry parent = ClientResearchTreeData.node(parentId).orElse(null);
            if (parent == null || !parent.unlocked()) {
                return false;
            }
        }
        return true;
    }

    private boolean isOverRequirementsPanel(double canvasMouseX, double canvasMouseY) {
        ResearchTreeSyncPayload.NodeEntry node = selectedNodeId == null
                ? null
                : ClientResearchTreeData.node(selectedNodeId).orElse(null);
        if (node == null || node.requirements().isEmpty()) return false;
        return canvasMouseX >= requirementsPanelX() && canvasMouseY >= REQUIREMENTS_PANEL_TOP
                && canvasMouseY < canvas.height() - DETAILS_HEIGHT - 8;
    }

    private Component lockStatus(ResearchTreeSyncPayload.NodeEntry node) {
        if (node.unlocked()) {
            return Component.literal("UNLOCKED").withStyle(ChatFormatting.GREEN);
        }
        if (node.serverUnlocked()) {
            return Component.literal("LOCKED FOR YOU").withStyle(ChatFormatting.YELLOW);
        }
        return Component.literal("GLOBALLY LOCKED").withStyle(ChatFormatting.RED);
    }

    private int lockStatusColor(ResearchTreeSyncPayload.NodeEntry node) {
        if (node.unlocked()) return UNLOCKED;
        return node.serverUnlocked() ? LOCKED_FOR_PLAYER : LOCKED;
    }

    private int drawStackGrid(GuiGraphics graphics, List<ItemStack> stacks, int x, int y, int width,
                              boolean showCounts, int scissorTop, int scissorBottom, int mouseX, int mouseY) {
        int columns = Math.max(1, width / 20);
        for (int i = 0; i < stacks.size(); i++) {
            int column = i % columns;
            int row = i / columns;
            int itemX = x + column * 20;
            int itemY = y + row * 20;
            ItemStack stack = stacks.get(i);
            graphics.renderItem(stack, itemX, itemY);
            if (showCounts) {
                graphics.renderItemDecorations(font, stack, itemX, itemY);
            }
            int screenItemY = itemY - detailScroll;
            if (screenItemY >= scissorTop - 16 && screenItemY < scissorBottom
                    && isMouseOver(mouseX, mouseY, itemX, screenItemY, 16, 16)) {
                hoveredDetailStack = stack;
            }
        }
        return y + Math.max(1, (stacks.size() + columns - 1) / columns) * 20;
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private int maxDetailScroll(ResearchTreeSyncPayload.NodeEntry node, int rewardsWidth, int unlocksWidth, int viewHeight) {
        int rewardRows = gridRows(node.rewards().size(), rewardsWidth);
        int unlockRows = gridRows(node.lockedItems().size(), unlocksWidth);
        int recipeTextHeight = Math.max(0, node.recipes().size() * 10 + 4);
        int contentHeight = 22 + Math.max(rewardRows * 20, unlockRows * 20 + recipeTextHeight);
        return Math.max(0, contentHeight - viewHeight);
    }

    private int gridRows(int stackCount, int width) {
        if (stackCount <= 0) return 1;
        int columns = Math.max(1, width / 20);
        return (stackCount + columns - 1) / columns;
    }

    private ResourceLocation findNodeAt(ResearchTreeLayout layout, int mouseX, int mouseY, int viewX, int viewY) {
        double worldX = (mouseX - viewX - cameraX) / zoom;
        double worldY = (mouseY - viewY - cameraY) / zoom;
        for (ResearchTreeLayout.PositionedNode node : layout.nodes()) {
            if (worldX >= node.x() - 4 && worldX <= node.x() + ResearchTreeLayout.NODE_SIZE + 4
                    && worldY >= node.y() - 4 && worldY <= node.y() + ResearchTreeLayout.NODE_SIZE + 4) {
                return node.entry().id();
            }
        }
        return null;
    }

    private void clampCamera(ResearchTreeLayout layout, int viewW, int viewH) {
        ResearchTreeLayout.Bounds bounds = layout.bounds();
        double minX = viewW - (bounds.maxX() + VIEW_PADDING) * zoom;
        double maxX = -bounds.minX() * zoom + VIEW_PADDING;
        double minY = viewH - (bounds.maxY() + VIEW_PADDING) * zoom;
        double maxY = -bounds.minY() * zoom + VIEW_PADDING;
        if (minX > maxX) {
            cameraX = (viewW - bounds.width() * zoom) / 2.0;
        } else {
            cameraX = Math.max(minX, Math.min(maxX, cameraX));
        }
        if (minY > maxY) {
            cameraY = (viewH - bounds.height() * zoom) / 2.0;
        } else {
            cameraY = Math.max(minY, Math.min(maxY, cameraY));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        updateCanvas();
        double contentMouseX = canvas.toCanvasX(mouseX);
        double contentMouseY = canvas.toCanvasY(mouseY);
        if (button == 0 && isOverTerminalOwnerTab(contentMouseX, contentMouseY)) {
            ClientResearchTerminalData.current().ifPresent(terminal ->
                    PacketDistributor.sendToServer(ResearchTerminalActionPayload.claim(terminal.pos())));
            return true;
        }
        if (button == 0 && isOverTerminalTargetControl(contentMouseX, contentMouseY)) {
            Optional<ResearchTerminalStatePayload> terminal = ClientResearchTerminalData.current();
            ResearchTreeSyncPayload.NodeEntry node = selectedNodeId == null
                    ? null
                    : ClientResearchTreeData.node(selectedNodeId).orElse(null);
            if (terminal.isPresent() && node != null && isLocalOwner(terminal.get()) && !node.unlocked()) {
                if (!node.id().equals(terminal.get().targetNodeId()) && prerequisitesUnlocked(node)) {
                    PacketDistributor.sendToServer(ResearchTerminalActionPayload.setTarget(terminal.get().pos(), node.id()));
                }
            }
            return true;
        }
        if (button == 0 && contentMouseX < LEFT_PANEL_WIDTH) {
            int y = 38 - treeScroll;
            for (ResearchTreeSyncPayload.TreeEntry tree : ClientResearchTreeData.trees()) {
                if (contentMouseY >= y && contentMouseY < y + 32) {
                    ClientResearchTreeData.selectTree(tree.id());
                    return true;
                }
                y += 36;
            }
        }
        if (button == 0 && isOverRequirementsPanel(contentMouseX, contentMouseY)) {
            return true;
        }
        if (button == 0 && contentMouseY < canvas.height() - DETAILS_HEIGHT) {
            if (hoveredNodeId != null) {
                if (!hoveredNodeId.equals(selectedNodeId)) {
                    detailScroll = 0;
                }
                selectedNodeId = hoveredNodeId;
            } else if (contentMouseX >= LEFT_PANEL_WIDTH) {
                draggingTree = true;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingTree && button == 0) {
            cameraX += canvas.toCanvasDelta(dragX);
            cameraY += canvas.toCanvasDelta(dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingTree) {
            draggingTree = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverCloseButton(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        updateCanvas();
        double contentMouseX = canvas.toCanvasX(mouseX);
        double contentMouseY = canvas.toCanvasY(mouseY);
        if (contentMouseX < LEFT_PANEL_WIDTH) {
            int maxScroll = Math.max(0, ClientResearchTreeData.trees().size() * 36 - canvas.height() + 48);
            treeScroll = (int) Math.max(0, Math.min(maxScroll, treeScroll - scrollY * 18));
            return true;
        }
        if (contentMouseY >= canvas.height() - DETAILS_HEIGHT) {
            ResearchTreeSyncPayload.NodeEntry node = selectedNodeId == null
                    ? null
                    : ClientResearchTreeData.node(selectedNodeId).orElse(null);
            if (node != null) {
                int rewardsX = Math.max(LEFT_PANEL_WIDTH + 300, canvas.width() - 330);
                int recipesX = Math.max(rewardsX + 120, canvas.width() - 190);
                int rewardsWidth = Math.max(90, recipesX - rewardsX - 18);
                int unlocksWidth = Math.max(90, canvas.width() - recipesX - 14);
                int maxScroll = maxDetailScroll(node, rewardsWidth, unlocksWidth, DETAILS_HEIGHT - 20);
                detailScroll = (int) Math.max(0, Math.min(maxScroll, detailScroll - scrollY * DETAILS_SCROLL_STEP));
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (isOverRequirementsPanel(contentMouseX, contentMouseY)) {
            return true;
        }
        if (contentMouseX >= LEFT_PANEL_WIDTH && contentMouseY < canvas.height() - DETAILS_HEIGHT) {
            double oldZoom = zoom;
            double nextZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * (scrollY > 0 ? 1.12 : 0.88)));
            double viewMouseX = contentMouseX - LEFT_PANEL_WIDTH;
            double viewMouseY = contentMouseY;
            double worldX = (viewMouseX - cameraX) / oldZoom;
            double worldY = (viewMouseY - cameraY) / oldZoom;
            zoom = nextZoom;
            cameraX = viewMouseX - worldX * zoom;
            cameraY = viewMouseY - worldY * zoom;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isOverCloseButton(double mouseX, double mouseY) {
        int x = width - CLOSE_BUTTON_SIZE - 6;
        int y = 6;
        return mouseX >= x && mouseX < x + CLOSE_BUTTON_SIZE && mouseY >= y && mouseY < y + CLOSE_BUTTON_SIZE;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_K || keyCode == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    private void maybeRequestAgain() {
        long now = System.currentTimeMillis();
        if (now - lastRequestMillis > 1500L) {
            requestSync();
        }
    }

    private void requestSync() {
        lastRequestMillis = System.currentTimeMillis();
        PacketDistributor.sendToServer(new RequestResearchTreeDataPayload());
    }

    @Override
    public void onClose() {
        if (terminalMode) {
            ClientResearchTerminalData.clear();
        }
        super.onClose();
    }
}
