package ru.itis.game.panel;

import javax.swing.*;

import ru.itis.cfg.ConfigValues;
import ru.itis.game.player.Player;
import ru.itis.game.dialog.DeathDialog;
import ru.itis.game.objects.Food;
import ru.itis.game.objects.GameObject;
import ru.itis.game.objects.Hp;
import ru.itis.listener.GameKeyListener;
import ru.itis.listener.GameMouseListener;
import ru.itis.listener.GameMouseMotionListener;
import ru.itis.res.Colors;
import ru.itis.res.IntValues;
import ru.itis.res.Strings;
import ru.itis.service.GameService;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;

public class GamePanel extends JPanel implements ActionListener {
    private static final int WORLD_WIDTH = ConfigValues.WORLD_WIDTH;
    private static final int WORLD_HEIGHT = ConfigValues.WORLD_HEIGHT;
    private static final int GRID_SIZE = ConfigValues.GRID_SIZE;
    private static final int PANEL_WIDTH = ConfigValues.PANEL_WIDTH;
    private static final int PANEL_HEIGHT = ConfigValues.PANEL_HEIGHT;

    private final Timer timer;
    private final MainMenu mainMenu;
    private final GameService gameService;

    private DeathDialog deathDialog;
    private boolean isDialogShowed = false;

    private long prevTime = System.currentTimeMillis();

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Colors.BACKGROUND_COLOR);
        setFocusable(true);
        setLayout(new CardLayout());
        this.gameService = new GameService(this);
        this.mainMenu = new MainMenu(this, gameService);
        add(mainMenu);
        addMouseMotionListener(new GameMouseMotionListener(gameService));
        addMouseListener(new GameMouseListener(gameService));
        addKeyListener(new GameKeyListener(gameService, this));
        timer = new Timer(20, this);
    }

    public void startGame(String playerName) {
        remove(mainMenu);
        gameService.startGame(playerName);
        timer.start();
        revalidate();
        repaint();
    }

    private void drawDeathDialog(Graphics g){
        if(deathDialog != null){
            deathDialog.draw(g, 0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Colors.BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (gameService.isGameStarted()) {
            gameService.updateCamera();

            int offsetX = -gameService.getCameraX();
            int offsetY = -gameService.getCameraY();
            drawGrid(g, offsetX, offsetY);

            synchronized (gameService){
                for (Food food : gameService.getFoodList()) {
                    food.draw(g, offsetX, offsetY);
                }
            }

            synchronized (gameService){
                for (GameObject blackBall : gameService.getBlackBallList()) {
                    blackBall.draw(g, offsetX, offsetY);
                }
            }

            synchronized (gameService){
                for (Hp hp : gameService.getHpList()) {
                    hp.draw(g, offsetX, offsetY);
                }
            }

            drawScore(g);
            drawHealth(g);

            Player player = gameService.getPlayer();
            if (player != null) {
                player.draw(g, offsetX, offsetY);
            }

            synchronized (gameService) {
                for (Player otherPlayer : gameService.getOtherPlayers().values()) {
                    otherPlayer.draw(g, offsetX, offsetY);
                }
            }

            drawScorePanel(g);

            if(isDialogShowed) {
                drawDeathDialog(g);
            }

            if (gameService.isPanelShowed()) {
                drawPanel(g, gameService.getNowShowing());
            }

        } else {
            add(mainMenu);
        }
    }

    private void drawGrid(Graphics g, int offsetX, int offsetY) {
        g.setColor(Colors.GRID_COLOR);
        for (int x = 0; x <= WORLD_WIDTH; x += GRID_SIZE) {
            g.drawLine(x + offsetX, offsetY, x + offsetX, WORLD_HEIGHT + offsetY);
        }
        for (int y = 0; y <= WORLD_HEIGHT; y += GRID_SIZE) {
            g.drawLine(offsetX, y + offsetY, WORLD_WIDTH + offsetX, y + offsetY);
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Счет: " + gameService.getScore(), 10, 30);
    }
    private void drawHealth(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Health: " + gameService.getPlayer().getHealth(), 10, 55);
    }

    private void drawPanel(Graphics g, int number) {
        int panelX = getWidth() - PANEL_WIDTH; //  располагаем панель справа
        int panelY = (getHeight() - PANEL_HEIGHT) / 2; //  располагаем панель по середине по вертикали
        g.setColor(new Color(200, 200, 200, 150));
        g.fillRect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String textUp = Strings.UPGRADE;

        int textWidth = fm.stringWidth(textUp);
        int textX = panelX + (PANEL_WIDTH - textWidth) / 2;
        int textY = panelY + fm.getHeight() + 10;
        int optionX = textX - 10;
        g.drawString(textUp, textX, textY);
        g.drawString(Strings.PRESS_KEY, optionX, textY + 30);
        if (number == IntValues.FIRST_PANEL) {
            g.drawString(Strings.SPEED_BOOST, optionX, textY + 50);
            g.drawString(Strings.HEALTH_BOOST,optionX, textY + 70);
        }
        else if (number == IntValues.SECOND_PANEL) {
            g.drawString(Strings.THORN_BOOST, optionX, textY + 50);
            g.drawString(Strings.HB2, optionX, textY + 70);
        }
        else if (number == IntValues.THIRD_PANEL) {
            g.drawString(Strings.ELECTRO, optionX, textY + 50);
        }

    }

    private void drawScorePanel(Graphics g) {
        int panelX = getWidth() - PANEL_WIDTH - 10;
        int panelY = 10;
        int panelHeight = 170;
        g.setColor(Colors.PANEL_MENU);
        g.fillRoundRect(panelX, panelY, PANEL_WIDTH, panelHeight, 20, 20);

        if(gameService.isGameStarted() && gameService.getPlayer() != null) {
            Graphics2D g2d = (Graphics2D) g;
            ArrayList<Player> players = new java.util.ArrayList<>(gameService.getOtherPlayers().values());
            Player player = gameService.getPlayer();
            player.setScore(gameService.getScore());
            players.add(player);
            players.sort(Comparator.comparingInt(Player::getScore).reversed());
            g2d.setColor(Colors.TEXT_COLOR);
            Font font = new Font("Arial", Font.BOLD, 16);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics(font);
            int textY = panelY + fm.getHeight();
            for (Player otherPlayer : players) {
                if(otherPlayer != null){
                    String text = otherPlayer.getPlayerName() + ": " + otherPlayer.getScore();
                    int textX = panelX + (PANEL_WIDTH - fm.stringWidth(text)) / 2;
                    g2d.drawString(text, textX, textY);
                    textY += fm.getHeight();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameService.isGameStarted()) {
            long now = System.currentTimeMillis();
            long deltaTime = now - prevTime;
            prevTime = now;
            gameService.updateGame(gameService.getMouseX(), gameService.getMouseY(), gameService.isMousePressed(), deltaTime);
            }
            repaint();
    }

    public MainMenu getMenu() {
        return mainMenu;
    }

    public void setDialogShowed(boolean showed){
        this.isDialogShowed = showed;
    }
    public boolean isDialogShowed(){
        return  isDialogShowed;
    }
    public void setDeathDialog(DeathDialog dialog){
        this.deathDialog = dialog;
    }
    public DeathDialog getDeathDialog(){
        return deathDialog;
    }
}