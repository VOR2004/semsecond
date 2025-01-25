package ru.itis.game.objects;

import ru.itis.util.DrawUtil;

import java.awt.*;

public class BlackBall extends GameObject {
    public BlackBall(int x, int y) {
        super(x, y);
    }

    @Override
    public void draw(Graphics g, int offsetX, int offsetY) {
        g.setColor(Color.BLUE);
        DrawUtil.drawSpiral(g, x, y, offsetX, offsetY);
    }
}

