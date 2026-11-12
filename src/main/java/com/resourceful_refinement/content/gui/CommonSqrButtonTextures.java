package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Common GUI sprites for buttons and elements in RR screens.
 * <p>
 * Entries fall into three groups:
 * <ul>
 *     <li>Fixed-size hover overlays (18x18) drawn over icons baked into panel art.</li>
 *     <li>8x8 nine-slice button frames (2px corners), grouped into {@link ButtonStyle}s.</li>
 *     <li>16x16 white icons, intended to be tinted/drawn on top of button frames.</li>
 * </ul>
 */
public enum CommonSqrButtonTextures implements ScreenElement, TextureSheetSegment {

    // Hover overlays for baked icons
    HOVER_CLEAR("gui_trash_select", 18),
    HOVER_SAVE("gui_confirm_select", 18),
    HOVER_CLOSE("gui_cancel_header_select", 18),

    // Nine-slice button frames
    BRASS_BUTTON("brass_button", 8),
    BRASS_BUTTON_HIGHLIGHTED("brass_button_highlighted", 8),
    BRASS_BUTTON_TOGGLED("brass_button_toggled", 8),
    GREY_BORDERLESS_BUTTON("grey_borderless_button", 8),
    GREY_BORDERLESS_BUTTON_HIGHLIGHTED("grey_borderless_button_highlighted", 8),
    GREY_SLATE_BUTTON("grey_slate_button", 8),
    GREY_SLATE_BUTTON_HIGHLIGHTED("grey_slate_button_highlighted", 8),
    GREY_SLATE_BUTTON_TOGGLED("grey_slate_button_toggled", 8),

    // White 16x16 icons
    HOME_ICON("home_icon", 16),
    MAIL_ICON("mail_icon", 16),
    SAVE_ICON("save_icon", 16),
    SEND_ICON("send_icon", 16),
    TRASH_ICON("trash_icon", 16),
    WRITE_ICON("write_icon", 16);

    /** Corner size used when nine-slicing the 8x8 button frames. */
    public static final int NINE_SLICE_BORDER = 2;

    public final ResourceLocation location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;
    private final int sheetWidth;
    private final int sheetHeight;

    CommonSqrButtonTextures(String location, int size) {
        this(location, 0, 0, size, size, size, size);
    }

    CommonSqrButtonTextures(String location, int startX, int startY, int width, int height, int sheetWidth, int sheetHeight) {
        this.location = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/" + location + ".png");
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.sheetWidth = sheetWidth;
        this.sheetHeight = sheetHeight;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height, sheetWidth, sheetHeight);
    }

    /** Draws the sprite stretched into the given rectangle. */
    @OnlyIn(Dist.CLIENT)
    public void renderStretched(GuiGraphics graphics, int x, int y, int drawWidth, int drawHeight) {
        graphics.blit(location, x, y, drawWidth, drawHeight, startX, startY, width, height, sheetWidth, sheetHeight);
    }

    /**
     * Draws the sprite as a nine-slice frame: corners are copied 1:1, edges and centre are stretched.
     * Stretching (rather than tiling) is correct for the flat-coloured frames in this sheet.
     */
    @OnlyIn(Dist.CLIENT)
    public void renderNineSlice(GuiGraphics graphics, int x, int y, int drawWidth, int drawHeight) {
        int b = NINE_SLICE_BORDER;
        int innerU = width - b * 2;
        int innerV = height - b * 2;
        int innerW = Math.max(0, drawWidth - b * 2);
        int innerH = Math.max(0, drawHeight - b * 2);
        int right = x + drawWidth - b;
        int bottom = y + drawHeight - b;
        int u0 = startX;
        int u1 = startX + b;
        int u2 = startX + width - b;
        int v0 = startY;
        int v1 = startY + b;
        int v2 = startY + height - b;

        // Corners
        blitPart(graphics, x, y, b, b, u0, v0, b, b);
        blitPart(graphics, right, y, b, b, u2, v0, b, b);
        blitPart(graphics, x, bottom, b, b, u0, v2, b, b);
        blitPart(graphics, right, bottom, b, b, u2, v2, b, b);
        // Edges
        blitPart(graphics, x + b, y, innerW, b, u1, v0, innerU, b);
        blitPart(graphics, x + b, bottom, innerW, b, u1, v2, innerU, b);
        blitPart(graphics, x, y + b, b, innerH, u0, v1, b, innerV);
        blitPart(graphics, right, y + b, b, innerH, u2, v1, b, innerV);
        // Centre
        blitPart(graphics, x + b, y + b, innerW, innerH, u1, v1, innerU, innerV);
    }

    private void blitPart(GuiGraphics graphics, int x, int y, int w, int h, int u, int v, int uw, int vh) {
        if (w <= 0 || h <= 0) return;
        graphics.blit(location, x, y, w, h, u, v, uw, vh, sheetWidth, sheetHeight);
    }

    /**
     * A three-state nine-slice button skin. {@code normal} may be null when the default state is baked into
     * the underlying panel art, in which case nothing is drawn while idle.
     */
    public record ButtonStyle(CommonSqrButtonTextures normal, CommonSqrButtonTextures highlighted,
                              CommonSqrButtonTextures toggled) {
        /** Brass tabs: idle art is baked into the panel backgrounds. */
        public static final ButtonStyle BRASS_BAKED = new ButtonStyle(null, BRASS_BUTTON_HIGHLIGHTED, BRASS_BUTTON_TOGGLED);
        public static final ButtonStyle BRASS = new ButtonStyle(BRASS_BUTTON, BRASS_BUTTON_HIGHLIGHTED, BRASS_BUTTON_TOGGLED);
        /** Borderless grey buttons; toggled state is "sunk" into the dark panel, so nothing is drawn. */
        public static final ButtonStyle GREY_BORDERLESS = new ButtonStyle(GREY_BORDERLESS_BUTTON, GREY_BORDERLESS_BUTTON_HIGHLIGHTED, null);
        public static final ButtonStyle GREY_SLATE = new ButtonStyle(GREY_SLATE_BUTTON, GREY_SLATE_BUTTON_HIGHLIGHTED, GREY_SLATE_BUTTON_TOGGLED);

        public CommonSqrButtonTextures forState(boolean toggled, boolean highlighted) {
            if (toggled) return this.toggled;
            return highlighted ? this.highlighted : normal;
        }
    }
}
