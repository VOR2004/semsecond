package ru.itis.game.objects;

import ru.itis.cfg.ConfigValues;

import java.awt.*;

public class Food extends GameObject {
    private static final int FOOD_SIZE = ConfigValues.FOOD_SIZE;

    public Food(int x, int y) {
        super(x, y);
    }

    @Override
    public void draw(Graphics g, int offsetX, int offsetY) {
        g.setColor(Color.GREEN);
        g.fillOval(x + offsetX, y + offsetY, FOOD_SIZE, FOOD_SIZE);
    }
    public int getSize() {
        return FOOD_SIZE;
    }
}
