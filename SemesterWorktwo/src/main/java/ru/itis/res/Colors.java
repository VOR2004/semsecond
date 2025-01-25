package ru.itis.res;

import java.awt.Color;

public final class Colors {
    private Colors() {}

    public static Color[] POSSIBLE_COLORS = {
            Color.GREEN,
            Color.BLUE,
            Color.RED,
            new Color(128, 0, 128),
            Color.YELLOW,
            new Color(0, 255, 177),
            Color.pink
    };
    public static final Color BACKGROUND_COLOR = new Color(220, 240, 255);
    public static final Color ACCENT_COLOR = new Color(192, 224, 248);
    public static final Color GRID_COLOR = new Color(170, 210, 240);
    public final static Color THORN_COLOR = new Color(200, 200, 200);
    public static final Color PANEL_MENU = new Color(244, 246, 246);
    public static final Color DIALOG_BACKGROUND = new Color(244, 246, 246);
    public static final Color TEXT_COLOR = new Color(0, 0, 0);
    public static final Color BUTTON_COLOR = new Color(213, 213, 213);
    public static final Color HP_COLOR = new Color(224, 112, 255);

}
