package com.resourceful_refinement.content.glare.terminal;

/**
 * Element positions on the Telemetry Terminal panel art, in panel texels. Rectangles are {x, y, width, height}
 * and match the boxes baked into {@link TelemetryTerminalGuiTextures}.
 */
final class TelemetryTerminalLayout {
    private TelemetryTerminalLayout() {}

    // Shared colours
    static final int TEXT_BRIGHT = 0xFFE6E6E6;
    static final int TEXT_LABEL = 0xFFBDBDBD;
    static final int TEXT_DIM = 0xFF9A9A9A;
    static final int TEXT_CREAM = 0xFFF4E1BF;
    static final int TEXT_ERROR = 0xFFD02A24;
    static final int TEXT_OK = 0xFF57B87A;
    static final int TEXT_HINT = 0xFF3F8A62;
    static final int ROW_HOVER = 0xFF2C2C2C;
    static final int ROW_SELECTED = 0xFF3D4248;
    static final int SCROLL_THUMB = 0xFF8E8E8E;
    static final int SCROLL_THUMB_HOVER = 0xFFC4C4C4;
    static final int SLOT_HOVER = 0xFF3A3F44;

    // Text scales (snapped to the pixel grid at draw time)
    static final float SCALE_TAB = 1.5f;
    static final float SCALE_SMALL = 0.75f;
    static final float SCALE_TINY = 0.5f;

    // Floating local-address tab
    static final int HOME_ICON_X = 4, HOME_ICON_Y = 4;
    static final int LOCAL_LABEL_X = 22, LOCAL_LABEL_Y = 4, LOCAL_LABEL_LINE = 8;
    static final float LOCAL_LABEL_SCALE = 0.85f;
    static final int LOCAL_ADDRESS_X = 66, LOCAL_ADDRESS_Y = 3, ADDRESS_SPACING = 20;
    static final int WARNING_RIGHT = 345, WARNING_Y = 9;

    // Brass header
    static final int TAB_Y = 29, TAB_WIDTH = 87, TAB_HEIGHT = 20;
    static final int[] TAB_X = {4, 97, 190};
    static final int CLOSE_X = 327, CLOSE_Y = 28;

    // Send panel: left column
    static final int[] VIEW_TOGGLE_BOX = {5, 56, 85, 22};
    static final int[] INBOX_BUTTON = {6, 57, 41, 20};
    static final int[] COMPOSE_BUTTON = {48, 57, 41, 20};
    static final int[] LIST_HEADER = {5, 82, 85, 11};
    static final int[] LIST = {5, 97, 78, 115};
    static final int[] LIST_SCROLL = {86, 97, 5, 115};

    // Send panel: header row
    static final int ADDRESS_LABEL_X = 102, ADDRESS_LABEL_Y = 60, ADDRESS_LABEL_LINE = 7;
    static final int ADDRESS_SLOTS_X = 145, ADDRESS_SLOTS_Y = 59;
    static final int[] SAVE_BUTTON = {209, 56, 22, 22};
    static final int[] INFO_BOX = {240, 56, 106, 22};

    // Send panel: body and footer
    static final int[] MESSAGE_AREA = {100, 84, 244, 104};
    static final int[] STATUS_BAR = {122, 194, 165, 18};
    static final int[] CLEAR_BUTTON = {98, 194, 20, 18};
    static final int[] ACTION_BUTTON = {295, 194, 47, 18};

    // Filter panel
    static final int[] FILTER_HEADER = {54, 58, 178, 25};
    static final int[] DISCARD_TOGGLE = {236, 58, 108, 24};
    static final int[] FILTER_LIST = {54, 88, 243, 95};
    static final int[] FILTER_SCROLL = {302, 88, 5, 95};
    static final int[] FILTER_INPUT = {54, 188, 243, 20};
    static final int[] ADD_FILTER_BUTTON = {300, 188, 45, 20};

    static boolean inside(int[] rect, double x, double y) {
        return x >= rect[0] && x < rect[0] + rect[2] && y >= rect[1] && y < rect[1] + rect[3];
    }
}
