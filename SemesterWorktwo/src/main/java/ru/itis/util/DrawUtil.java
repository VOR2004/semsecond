package ru.itis.util;

import java.awt.*;

public final class DrawUtil {
    private DrawUtil() {}

    public static void drawSpiral(Graphics g, int x, int y, int offsetX, int offsetY) {
        int xStart = x+20;
        int yStart = y+20;
        int width = 10;
        int height = 10;
        int arcAngle = 180;
        int startAngle = 0;
        int depth = 10;
        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {
                yStart = yStart - depth;
                width = width + 2 * depth;
                height = height + 2 * depth;
                g.drawArc(xStart + offsetX, yStart + offsetY, width, height, startAngle, -arcAngle);
            } else {
                xStart = xStart - 2 * depth;
                yStart = yStart - depth;
                width = width + 2 * depth;
                height = height + 2 * depth;
                g.drawArc(xStart + offsetX, yStart + offsetY, width, height, startAngle, arcAngle);
            }
        }
    }
}

