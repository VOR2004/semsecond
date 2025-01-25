package ru.itis.listener;

import ru.itis.game.panel.GamePanel;
import ru.itis.service.GameService;
import ru.itis.res.IntValues;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameKeyListener implements KeyListener {
    private final GamePanel gamePanel;
    private final GameService gameService;
    public GameKeyListener(GameService gameService, GamePanel gamePanel) {
        this.gameService = gameService;
        this.gamePanel = gamePanel;
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameService.isGameStarted()) {
            gameService.getPlayer().keyPressed(e);
            if (gameService.isPanelShowed() && gameService.getScore() >= 15) {
                if (gameService.getNowShowing() == IntValues.FIRST_PANEL) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_1 -> {
                            gameService.getPlayer().setManyTails(true);
                            gameService.setManyTails(true);
                            gameService.getPlayer().setSpeed(7);
                            if (!(gameService.getScore() >= 40)) {
                                gameService.setShowPanel(false);
                            }
                            gameService.setNowShowing(IntValues.SECOND_PANEL);
                            gamePanel.repaint();
                        }
                        case KeyEvent.VK_2 -> {
                            gameService.getPlayer().setHealth(gameService.getPlayer().getHealth() + 10);
                            if (!(gameService.getScore() >= 40)) {
                                gameService.setShowPanel(false);
                            }
                            gameService.setNowShowing(IntValues.SECOND_PANEL);
                            gamePanel.repaint();
                        }
                    }
                }
                else if (gameService.getNowShowing() == IntValues.SECOND_PANEL) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_1 -> {
                            gameService.getPlayer().setThorns(true);
                            gameService.setThorns();
                            if (!(gameService.getScore() >= 100)) {
                                gameService.setShowPanel(false);
                            }
                            gameService.setNowShowing(IntValues.THIRD_PANEL);
                            gamePanel.repaint();
                        }
                        case KeyEvent.VK_2 -> {
                            gameService.getPlayer().setHealth(gameService.getPlayer().getHealth() + 10);
                            if (!(gameService.getScore() >= 100)) {
                                gameService.setShowPanel(false);
                            }
                            gameService.setNowShowing(IntValues.THIRD_PANEL);
                            gamePanel.repaint();
                        }
                    }
                }
                else if (gameService.getNowShowing() == IntValues.THIRD_PANEL) {
                    if (e.getKeyCode() == KeyEvent.VK_1) {
                        gameService.getPlayer().setHavingElectro(true);
                        gameService.getPlayer().setSpeed(gameService.getPlayer().getSpeed() + 3);
                        gameService.setShowPanel(false);
                        gamePanel.repaint();
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameService.isGameStarted()) {
            gameService.getPlayer().keyReleased(e);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}