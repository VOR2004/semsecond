package ru.itis.listener;

import ru.itis.service.GameService;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class GameMouseMotionListener extends MouseAdapter implements MouseMotionListener {
    private final GameService gameService;

    public GameMouseMotionListener(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gameService.setMouseX(e.getX());
        gameService.setMouseY(e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gameService.setMouseX(e.getX());
        gameService.setMouseY(e.getY());
    }
}
