package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.network.TelemetryTerminalActionPayload;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Shared terminal frame; mode-specific controls live in independent page classes. */
public class TelemetryTerminalScreen extends AbstractContainerScreen<TelemetryTerminalMenu> {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 230;
    private final List<AbstractWidget> pageWidgets = new ArrayList<>();
    private TelemetryTerminalPage page;
    private TelemetryTerminalMode displayedMode;
    private final List<Button> modeButtons = new ArrayList<>();

    public TelemetryTerminalScreen(TelemetryTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        inventoryLabelY = 1000;
    }

    @Override protected void init() {
        super.init();
        modeButtons.clear();
        modeButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.manual"), b -> setMode(TelemetryTerminalMode.MANUAL))
                .bounds(leftPos + 8, topPos + 5, 96, 20).build()));
        modeButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.auto_send"), b -> setMode(TelemetryTerminalMode.AUTO_SEND))
                .bounds(leftPos + 106, topPos + 5, 96, 20).build()));
        modeButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.auto_receive"), b -> setMode(TelemetryTerminalMode.AUTO_RECEIVE))
                .bounds(leftPos + 204, topPos + 5, 108, 20).build()));
        Button closeButton = addRenderableWidget(Button.builder(Component.literal("x"), button -> onClose())
                .bounds(leftPos + WIDTH + 4, topPos + 4, 18, 18).build());
        closeButton.setTooltip(Tooltip.create(Component.translatable("gui.resourceful_refinement.telemetry_terminal.close")));
        switchPage(menu.getSnapshot().mode());
    }

    private void setMode(TelemetryTerminalMode mode) {
        if (mode == menu.getSnapshot().mode()) return;
        if (page != null) page.flush();
        send(new TelemetryTerminalActionPayload(menu.getBlockPos(), TelemetryTerminalActionPayload.Action.SET_MODE,
                mode, com.resourceful_refinement.content.glare.GlareAddress.empty(), "", false, new java.util.UUID(0, 0)));
    }

    private void switchPage(TelemetryTerminalMode mode) {
        if (page != null) page.removed();
        for (AbstractWidget widget : List.copyOf(pageWidgets)) removeWidget(widget);
        pageWidgets.clear();
        displayedMode = mode;
        for (int i = 0; i < modeButtons.size(); i++) modeButtons.get(i).active = i != mode.ordinal();
        page = switch (mode) {
            case MANUAL -> new ManualTelemetryTerminalPage(this);
            case AUTO_SEND -> new AutoSendTelemetryTerminalPage(this);
            case AUTO_RECEIVE -> new AutoReceiveTelemetryTerminalPage(this);
        };
        page.init();
    }

    public <T extends AbstractWidget> T addPageWidget(T widget) {
        pageWidgets.add(widget);
        return addRenderableWidget(widget);
    }

    public void clearPageWidgets() {
        for (AbstractWidget widget : List.copyOf(pageWidgets)) removeWidget(widget);
        pageWidgets.clear();
    }

    public TelemetryTerminalSnapshot snapshot() { return menu.getSnapshot(); }
    public int contentLeft() { return leftPos + 8; }
    public int contentTop() { return topPos + 31; }
    public void send(TelemetryTerminalActionPayload payload) { PacketDistributor.sendToServer(payload); }

    public void applySnapshot(TelemetryTerminalSnapshot snapshot) {
        menu.applySnapshot(snapshot);
        if (displayedMode != snapshot.mode()) switchPage(snapshot.mode());
        else if (page != null) page.snapshotUpdated(snapshot);
    }

    void rebuildCurrentPage() { switchPage(menu.getSnapshot().mode()); }

    @Override public void containerTick() {
        super.containerTick();
        if (page != null) page.tick();
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF252A2D);
        graphics.fill(leftPos + 5, topPos + 28, leftPos + WIDTH - 5, topPos + HEIGHT - 5, 0xFF343B3F);
        graphics.renderOutline(leftPos, topPos, WIDTH, HEIGHT, 0xFF69757A);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (page != null) page.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (page != null && page.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (page != null && page.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (AbstractWidget widget : pageWidgets) {
            if (widget instanceof VerticalScrollBar scrollBar && scrollBar.isDraggingThumb()
                    && scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : pageWidgets) {
            if (widget instanceof VerticalScrollBar scrollBar && scrollBar.isDraggingThumb()
                    && scrollBar.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        // Give editors and focused buttons first refusal, then consume the key so gameplay/menu bindings cannot fire.
        if (getFocused() != null && getFocused().keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return true;
    }

    @Override public void onClose() {
        if (page != null) page.flush();
        super.onClose();
    }
}
