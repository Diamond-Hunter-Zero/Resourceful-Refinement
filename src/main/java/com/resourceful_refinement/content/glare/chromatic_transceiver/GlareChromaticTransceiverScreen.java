package com.resourceful_refinement.content.glare.chromatic_transceiver;

import com.mojang.blaze3d.systems.RenderSystem;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.client.gui.ScaledContainerScreen;
import com.resourceful_refinement.client.gui.widget.HoverTintButton;
import com.resourceful_refinement.client.gui.widget.ThreeStateButton;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.content.glare.GlareComparison;
import com.resourceful_refinement.content.glare.GlareLogicMode;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures.ButtonStyle;
import com.resourceful_refinement.content.gui.GlareNetworkGuiData;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshot;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshotProvider;
import com.resourceful_refinement.content.gui.PowerTerminal;
import com.resourceful_refinement.network.ConfigureGlareTransceiverPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Chromatic Transceiver configuration, with the shared power terminal on the left.
 * <p>
 * Laid out in texels of {@code chromatic_transceiver_gui.png} and scaled to the window by
 * {@link ScaledContainerScreen}. Like Create's screens, changes are kept locally and sent to the server once, when
 * the screen closes (close button, Escape, or any other way the screen is removed).
 */
public class GlareChromaticTransceiverScreen extends ScaledContainerScreen<GlareChromaticTransceiverMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/chromatic_transceiver_gui.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int CONFIG_WIDTH = 256;
    private static final int CONFIG_HEIGHT = 236;
    private static final int POWER_TERMINAL_WIDTH = PowerTerminal.WIDTH;
    /** Power terminal art is shorter than the transceiver panel; centre it vertically. */
    private static final int POWER_TERMINAL_Y = (CONFIG_HEIGHT - PowerTerminal.HEIGHT) / 2;
    private static final int PANEL_GAP = 8;
    /** Left edge of the transceiver panel; every config coordinate below is relative to it. */
    private static final int CX = POWER_TERMINAL_WIDTH + PANEL_GAP;

    // Baked boxes on the panel art, {x, y, width, height} relative to CX
    private static final int[] TITLE_BOX = {6, 18, 178, 13};
    private static final int[] FILTERS_LABEL_BOX = {12, 36, 63, 9};
    private static final int[] PAGE_LABEL_BOX = {123, 36, 47, 9};
    private static final int[] LOGIC_LABEL_BOX = {191, 35, 53, 13};
    private static final int[] LOGIC_BUTTON = {191, 51, 53, 17};
    private static final int[] LIST = {13, 53, 155, 121};
    private static final int[] SCROLL = {174, 51, 5, 125};
    private static final int[] CLOSE = {232, 4, 18, 18};
    private static final int CHARGES_LABEL_X = 8, CHARGES_LABEL_Y = 188;
    private static final int CHARGE_BOX_Y0 = 200, CHARGE_BOX_ROW_STEP = 17, CHARGE_BOX_W = 12, CHARGE_BOX_H = 8;
    private static final int[] CHARGE_BOX_X = {21, 51, 81, 111, 142, 172, 202, 232};

    // Filter rows
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_HEIGHT = 17;
    private static final int ROW_TOP = LIST[1] + 1;
    private static final int TOGGLE_X = 15, BUTTON_SIZE = 15;
    private static final int SWATCH_X = 34, SWATCH_SIZE = 9;
    private static final int NAME_X = 47;
    private static final int COMPARISON_X = 113, COMPARISON_W = 16;
    private static final int THRESHOLD_X = 131, THRESHOLD_W = 35;

    private static final int TEXT = 0xFFE6E6E6;
    private static final int TEXT_LABEL = 0xFFBDBDBD;
    private static final int TEXT_DIM = 0xFF7A7A7A;

    private final boolean[] enabled = new boolean[16];
    private final GlareComparison[] comparisons = new GlareComparison[16];
    private final String[] thresholdText = new String[16];
    private final ThreeStateButton[] toggles = new ThreeStateButton[16];
    private final ThreeStateButton[] comparisonButtons = new ThreeStateButton[16];
    private final EditBox[] thresholdFields = new EditBox[16];
    private GlareLogicMode mode;
    private ThreeStateButton modeButton;
    private VerticalScrollBar scrollBar;
    private PowerTerminal powerTerminal;
    private int scrollOffset;
    private boolean sent;

    public GlareChromaticTransceiverScreen(GlareChromaticTransceiverMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CX + CONFIG_WIDTH, CONFIG_HEIGHT);
        inventoryLabelY = 1000;
        titleLabelY = 1000;
        mode = menu.getInitialMode();
        for (DyeColor colour : DyeColor.values()) {
            int index = colour.ordinal();
            int threshold = menu.getInitialThreshold(colour);
            enabled[index] = threshold >= 0;
            comparisons[index] = menu.getInitialComparison(colour);
            // Unset (disabled) filters default to 1 so that enabling one does not power the transceiver by default
            // (charge >= 0 is always true).
            thresholdText[index] = Integer.toString(threshold >= 0 ? threshold : 1);
        }
    }

    @Override
    protected void init() {
        super.init();
        powerTerminal = new PowerTerminal(0, POWER_TERMINAL_Y, menu.getBlockPos(), this::networkSnapshot);

        for (DyeColor colour : DyeColor.values()) {
            int index = colour.ordinal();
            toggles[index] = addRenderableWidget(new ThreeStateButton(0, 0, BUTTON_SIZE, BUTTON_SIZE, toggleLabel(index),
                    ButtonStyle.GREY_BORDERLESS, () -> toggleFilter(index)));
            comparisonButtons[index] = addRenderableWidget(new ThreeStateButton(0, 0, COMPARISON_W, BUTTON_SIZE, comparisonLabel(index),
                    ButtonStyle.GREY_BORDERLESS, () -> cycleComparison(index)));
            EditBox field = new EditBox(font, 0, 0, THRESHOLD_W, BUTTON_SIZE,
                    Component.translatable("gui.resourceful_refinement.glare_transceiver.threshold"));
            field.setMaxLength(7);
            field.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
            field.setValue(thresholdText[index]);
            field.setResponder(value -> thresholdText[index] = value);
            thresholdFields[index] = addRenderableWidget(field);
        }
        modeButton = addRenderableWidget(new ThreeStateButton(CX + LOGIC_BUTTON[0], LOGIC_BUTTON[1], LOGIC_BUTTON[2], LOGIC_BUTTON[3],
                modeLabel(), ButtonStyle.GREY_SLATE, this::cycleMode));
        addRenderableWidget(new HoverTintButton(CX + CLOSE[0], CLOSE[1], CLOSE[2], CLOSE[3],
                Component.translatable("gui.resourceful_refinement.telemetry_terminal.close"), 0x40FFFFFF, this::onClose));
        scrollBar = addRenderableWidget(new VerticalScrollBar(CX + SCROLL[0], SCROLL[1], SCROLL[2], SCROLL[3],
                DyeColor.values().length, VISIBLE_ROWS, scrollOffset, this::setScrollOffset)
                .withStyle(false, 0xFF8E8E8E, 0xFFC4C4C4, 1));
        updateRows();
    }

    private GlareNetworkGuiData networkSnapshot() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return GlareNetworkSnapshot.EMPTY;
        BlockEntity blockEntity = minecraft.level.getBlockEntity(menu.getBlockPos());
        return blockEntity instanceof GlareNetworkSnapshotProvider provider
                ? provider.getSyncedGlareNetworkSnapshot() : GlareNetworkSnapshot.EMPTY;
    }

    private void toggleFilter(int index) {
        enabled[index] = !enabled[index];
        toggles[index].setMessage(toggleLabel(index));
        if (enabled[index] && thresholdFields[index].getValue().isEmpty()) thresholdFields[index].setValue("1");
        updateRows();
    }

    private Component toggleLabel(int index) {
        return Component.literal(enabled[index] ? "x" : "");
    }

    private void cycleComparison(int index) {
        GlareComparison[] values = GlareComparison.values();
        comparisons[index] = values[(comparisons[index].ordinal() + 1) % values.length];
        comparisonButtons[index].setMessage(comparisonLabel(index));
    }

    private Component comparisonLabel(int index) {
        return Component.literal(comparisons[index].symbol());
    }

    private void cycleMode() {
        GlareLogicMode[] values = GlareLogicMode.values();
        mode = values[(mode.ordinal() + 1) % values.length];
        modeButton.setMessage(modeLabel());
    }

    private Component modeLabel() {
        return Component.translatable("gui.resourceful_refinement.glare_transceiver.mode", mode.name());
    }

    private void updateRows() {
        for (int i = 0; i < 16; i++) {
            int visibleRow = i - scrollOffset;
            boolean visible = visibleRow >= 0 && visibleRow < VISIBLE_ROWS;
            int y = ROW_TOP + visibleRow * ROW_HEIGHT + 1;
            toggles[i].setPosition(CX + TOGGLE_X, y);
            toggles[i].visible = visible;
            comparisonButtons[i].setPosition(CX + COMPARISON_X, y);
            comparisonButtons[i].visible = visible;
            comparisonButtons[i].active = enabled[i];
            thresholdFields[i].setPosition(CX + THRESHOLD_X, y);
            thresholdFields[i].visible = visible;
            thresholdFields[i].setEditable(enabled[i]);
        }
    }

    private void setScrollOffset(int offset) {
        scrollOffset = offset;
        updateRows();
    }

    /** Sends the edited configuration. Called once when the screen goes away, however it was closed. */
    private void sendConfiguration() {
        if (sent) return;
        sent = true;
        int[] thresholds = new int[16];
        for (int i = 0; i < thresholds.length; i++) {
            if (!enabled[i]) {
                thresholds[i] = GlareChromaticTransceiverBlockEntity.DISABLED_FILTER;
                continue;
            }
            try {
                thresholds[i] = Math.min(Integer.parseInt(thresholdText[i]), GlareChromaticTransceiverBlockEntity.MAX_THRESHOLD);
            } catch (NumberFormatException ignored) {
                thresholds[i] = 0;
            }
        }
        int[] comparisonOrdinals = new int[comparisons.length];
        for (int i = 0; i < comparisons.length; i++) comparisonOrdinals[i] = comparisons[i].ordinal();
        PacketDistributor.sendToServer(new ConfigureGlareTransceiverPayload(menu.getBlockPos(), mode, thresholds, comparisonOrdinals));
    }

    @Override
    public void removed() {
        sendConfiguration();
        super.removed();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Keep typing in a threshold field from triggering the inventory key and closing the screen.
        if (keyCode != GLFW.GLFW_KEY_ESCAPE && getFocused() instanceof EditBox box && box.isFocused()) {
            return box.keyPressed(keyCode, scanCode, modifiers) || box.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean panelMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean overList = mouseX >= CX + LIST[0] && mouseX < CX + SCROLL[0] + SCROLL[2]
                && mouseY >= LIST[1] && mouseY < LIST[1] + LIST[3];
        if (overList && scrollBar != null && scrollBar.scrollBy(-(int) Math.signum(scrollY))) return true;
        return super.panelMouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected boolean panelMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollBar != null && scrollBar.isDraggingThumb() && scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.panelMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean panelMouseReleased(double mouseX, double mouseY, int button) {
        if (scrollBar != null && scrollBar.isDraggingThumb() && scrollBar.mouseReleased(mouseX, mouseY, button)) return true;
        return super.panelMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean panelMouseClicked(double mouseX, double mouseY, int button) {
        return powerTerminal != null && powerTerminal.mouseClicked(mouseX, mouseY, button)
                || super.panelMouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (powerTerminal != null) powerTerminal.render(graphics, font, mouseX, mouseY);
        RenderSystem.enableBlend();
        graphics.blit(BACKGROUND, CX, 0, 0, 0, CONFIG_WIDTH, CONFIG_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        RenderSystem.disableBlend();

        // Row hover backing, beneath the row widgets
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int y = ROW_TOP + row * ROW_HEIGHT;
            if (mouseX >= CX + LIST[0] && mouseX < CX + LIST[0] + LIST[2] && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                graphics.fill(CX + LIST[0], y, CX + LIST[0] + LIST[2], y + ROW_HEIGHT, 0xFF262626);
            }
        }
    }

    @Override
    protected void renderPanelForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float body = PanelText.snap(graphics, 1f);
        float small = PanelText.snap(graphics, 0.75f);

        PanelText.draw(graphics, font, title, CX + TITLE_BOX[0] + 4, centredY(TITLE_BOX, body), TEXT, false, body);
        PanelText.drawCentered(graphics, font, Component.translatable("gui.resourceful_refinement.glare_transceiver.filters"),
                CX + FILTERS_LABEL_BOX[0] + FILTERS_LABEL_BOX[2] / 2f, centredY(FILTERS_LABEL_BOX, small), TEXT_LABEL, false, small);
        PanelText.drawCentered(graphics, font, Component.literal((scrollOffset + 1) + "-" + (scrollOffset + VISIBLE_ROWS) + "/16"),
                CX + PAGE_LABEL_BOX[0] + PAGE_LABEL_BOX[2] / 2f, centredY(PAGE_LABEL_BOX, small), TEXT_LABEL, false, small);
        PanelText.drawCentered(graphics, font, Component.translatable("gui.resourceful_refinement.glare_transceiver.logic"),
                CX + LOGIC_LABEL_BOX[0] + LOGIC_LABEL_BOX[2] / 2f, centredY(LOGIC_LABEL_BOX, small), TEXT_LABEL, false, small);

        DyeColor[] colours = DyeColor.values();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            DyeColor colour = colours[row + scrollOffset];
            int y = ROW_TOP + row * ROW_HEIGHT;
            int swatchY = y + (ROW_HEIGHT - SWATCH_SIZE) / 2;
            graphics.fill(CX + SWATCH_X, swatchY, CX + SWATCH_X + SWATCH_SIZE, swatchY + SWATCH_SIZE, 0xFF000000 | colour.getTextureDiffuseColor());
            PanelText.draw(graphics, font, Component.translatable("color.minecraft." + colour.getName()), CX + NAME_X,
                    y + (ROW_HEIGHT - 7) / 2, enabled[colour.ordinal()] ? TEXT : TEXT_DIM, false, body);
        }

        PanelText.draw(graphics, font, Component.translatable("gui.resourceful_refinement.glare_transceiver.live_charges"),
                CX + CHARGES_LABEL_X, CHARGES_LABEL_Y, TEXT_LABEL, false, small);
        for (int i = 0; i < colours.length; i++) {
            int x = CX + CHARGE_BOX_X[i % 8];
            int y = CHARGE_BOX_Y0 + (i / 8) * CHARGE_BOX_ROW_STEP;
            int charge = menu.getColourCharge(colours[i]);
            Component text = Component.literal(Integer.toString(charge));
            float scale = Math.min(small, PanelText.snapDown(graphics, (CHARGE_BOX_W - 1f) / Math.max(1, font.width(text))));
            PanelText.drawCentered(graphics, font, text, x + CHARGE_BOX_W / 2f,
                    Math.round(y + CHARGE_BOX_H / 2f - 7 * scale / 2f), charge > 0 ? chargeColour(colours[i]) : TEXT_DIM, false, scale);
        }
    }

    /** The dye's text colour, lifted for very dark dyes (e.g. black) so it stays readable on the dark boxes. */
    private static int chargeColour(DyeColor colour) {
        int rgb = colour.getTextColor();
        int brightest = Math.max((rgb >> 16) & 0xFF, Math.max((rgb >> 8) & 0xFF, rgb & 0xFF));
        return brightest < 0x50 ? 0xFF5E5E66 : 0xFF000000 | rgb;
    }

    private static float centredY(int[] box, float scale) {
        return Math.round(box[1] + box[3] / 2f - 7 * scale / 2f);
    }
}
