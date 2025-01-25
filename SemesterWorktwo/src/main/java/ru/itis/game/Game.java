package ru.itis.game;

import ru.itis.game.panel.GamePanel;
import ru.itis.res.Strings;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

public class Game extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    public Game() {
        setTitle(Strings.TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}
