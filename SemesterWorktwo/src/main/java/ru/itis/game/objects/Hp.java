package ru.itis.game.objects;

import ru.itis.cfg.ConfigValues;
import ru.itis.res.Colors;

import java.awt.*;

public class Hp extends GameObject {
    private static final int SIZE = ConfigValues.HP_SIZE;

    public Hp(int x, int y) {
        super(x, y);
    }

    @Override
    public void draw(Graphics g, int offsetX, int offsetY) {
        g.setColor(Colors.HP_COLOR);
        Color meatColor1 = new Color(208, 26, 68);

        g.setColor(meatColor1);
        g.fillOval(x + offsetX, y + offsetY, SIZE, SIZE);
        ((Graphics2D) g).setStroke(new BasicStroke(1));
    }
    public int getSize() {
        return SIZE;
    }
}
