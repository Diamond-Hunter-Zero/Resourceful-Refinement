package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.client.gui.ScaledContainerScreen;
import com.resourceful_refinement.client.gui.widget.ThreeStateButton;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures.ButtonStyle;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshot;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshotProvider;
import com.resourceful_refinement.content.gui.SqrHoverButton;
import com.resourceful_refinement.network.TelemetryTerminalActionPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.resourceful_refinement.content.glare.terminal.TelemetryTerminalLayout.*;

/**
 * Shared terminal frame; mode-specific controls live in independent page classes.
 * <p>
 * Everything is laid out in panel texels and scaled to the window by {@link ScaledContainerScreen}, so the panel
 * stays fully visible and legible at any window size and GUI-scale setting.
 */
public class TelemetryTerminalScreen extends ScaledContainerScreen<TelemetryTerminalMenu> {
    public static final int WIDTH = TelemetryTerminalGuiTextures.PANEL_WIDTH;
    public static final int HEIGHT = TelemetryTerminalGuiTextures.PANEL_HEIGHT;
    private static final String[] TAB_KEYS = {
            "gui.resourceful_refinement.telemetry_terminal.manual",
            "gui.resourceful_refinement.telemetry_terminal.auto_send",
            "gui.resourceful_refinement.telemetry_terminal.auto_receive"};

    private final List<AbstractWidget> pageWidgets = new ArrayList<>();
    private final List<ThreeStateButton> modeButtons = new ArrayList<>();
    private TelemetryTerminalPage page;
    private TelemetryTerminalMode displayedMode;

    public TelemetryTerminalScreen(TelemetryTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        inventoryLabelY = 1000;
        titleLabelY = 1000;
    }

    @Override protected void init() {
        super.init();
        modeButtons.clear();
        pageWidgets.clear();

        // All three tabs share one label size: the largest that fits the widest label.
        float tabScale = SCALE_TAB;
        for (String key : TAB_KEYS) {
            tabScale = Math.min(tabScale, (TAB_WIDTH - 8f) / font.width(Component.translatable(key)));
        }
        TelemetryTerminalMode[] modes = TelemetryTerminalMode.values();
        for (int i = 0; i < modes.length; i++) {
            TelemetryTerminalMode mode = modes[i];
            modeButtons.add(addRenderableWidget(new ThreeStateButton(TAB_X[i], TAB_Y, TAB_WIDTH, TAB_HEIGHT,
                    Component.translatable(TAB_KEYS[i]), ButtonStyle.BRASS, () -> setMode(mode))
                    .label(tabScale, true).snapLabelDown()));
        }
        addRenderableWidget(new SqrHoverButton(CLOSE_X, CLOSE_Y, CommonSqrButtonTextures.HOVER_CLOSE, this::onClose));
        switchPage(menu.getSnapshot().mode());
    }

    private void setMode(TelemetryTerminalMode mode) {
        if (mode == menu.getSnapshot().mode()) return;
        if (page != null) page.flush();
        send(new TelemetryTerminalActionPayload(menu.getBlockPos(), TelemetryTerminalActionPayload.Action.SET_MODE,
                mode, GlareAddress.empty(), "", false, new UUID(0, 0)));
    }

    private void switchPage(TelemetryTerminalMode mode) {
        if (page != null) page.removed();
        clearPageWidgets();
        displayedMode = mode;
        for (int i = 0; i < modeButtons.size(); i++) modeButtons.get(i).setToggled(i == mode.ordinal());
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
        RenderSystem.enableBlend();
        (displayedMode == TelemetryTerminalMode.AUTO_RECEIVE
                ? TelemetryTerminalGuiTextures.FILTER_PANEL
                : TelemetryTerminalGuiTextures.SEND_PANEL).render(graphics, 0, 0);
        RenderSystem.disableBlend();
        if (page != null) page.renderBackground(graphics, mouseX, mouseY, partialTick);
    }

    @Override protected void renderPanelForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (page != null) page.render(graphics, mouseX, mouseY, partialTick);
        drawLocalAddressTab(graphics);
    }

    /** The floating tab above the frame: this terminal's own address, plus a No-Lux warning when underpowered. */
    private void drawLocalAddressTab(GuiGraphics graphics) {
        RenderSystem.enableBlend();
        graphics.pose().pushPose();
        graphics.pose().translate(HOME_ICON_X, HOME_ICON_Y, 0);
        graphics.pose().scale(SCALE_SMALL, SCALE_SMALL, 1);
        CommonSqrButtonTextures.HOME_ICON.render(graphics, 0, 0);
        graphics.pose().popPose();
        RenderSystem.disableBlend();

        float labelScale = PanelText.snap(graphics, LOCAL_LABEL_SCALE);
        PanelText.draw(graphics, font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.local_address_line_1"),
                LOCAL_LABEL_X, LOCAL_LABEL_Y, TEXT_CREAM, false, labelScale);
        PanelText.draw(graphics, font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.local_address_line_2"),
                LOCAL_LABEL_X, LOCAL_LABEL_Y + LOCAL_LABEL_LINE, TEXT_CREAM, false, labelScale);
        TelemetryAddressRenderer.render(graphics, snapshot().ownAddress(), LOCAL_ADDRESS_X, LOCAL_ADDRESS_Y, ADDRESS_SPACING, 1f);

        if (networkSnapshot().overloaded()) {
            PanelText.drawRight(graphics, font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.no_lux"),
                    WARNING_RIGHT, WARNING_Y, 0xFFFF5A1F, true, 1f);
        }
    }

    private GlareNetworkSnapshot networkSnapshot() {
        if (getMinecraft().level == null) return GlareNetworkSnapshot.EMPTY;
        return getMinecraft().level.getBlockEntity(menu.getBlockPos()) instanceof GlareNetworkSnapshotProvider provider
                ? provider.getSyncedGlareNetworkSnapshot() : GlareNetworkSnapshot.EMPTY;
    }

    @Override protected boolean panelMouseClicked(double mouseX, double mouseY, int button) {
        if (page != null && page.mouseClicked(mouseX, mouseY, button)) return true;
        return super.panelMouseClicked(mouseX, mouseY, button);
    }

    @Override protected boolean panelMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (page != null && page.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.panelMouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override protected boolean panelMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (AbstractWidget widget : pageWidgets) {
            if (widget instanceof VerticalScrollBar scrollBar && scrollBar.isDraggingThumb()
                    && scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return super.panelMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override protected boolean panelMouseReleased(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : pageWidgets) {
            if (widget instanceof VerticalScrollBar scrollBar && scrollBar.isDraggingThumb()
                    && scrollBar.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.panelMouseReleased(mouseX, mouseY, button);
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
