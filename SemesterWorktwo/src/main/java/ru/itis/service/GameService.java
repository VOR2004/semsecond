package ru.itis.service;

import ru.itis.game.dialog.DeathDialog;
import ru.itis.game.objects.BlackBall;
import ru.itis.game.objects.Food;
import ru.itis.game.objects.GameObject;
import ru.itis.game.objects.Hp;
import ru.itis.game.panel.GamePanel;
import ru.itis.game.player.Player;
import ru.itis.network.NetworkService;
import ru.itis.res.IntValues;
import ru.itis.cfg.ConfigValues;
import ru.itis.util.Broadcaster;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GameService {
    private static final int WORLD_WIDTH = ConfigValues.WORLD_WIDTH;
    private static final int WORLD_HEIGHT = ConfigValues.WORLD_HEIGHT;
    private static final int BLACK_BALL_SIZE = 60;
    private static final double PUSH_BACK_SPEED = 10.0;
    private static final int PUSH_BACK_FRAMES = 15;
    private final GamePanel gamePanel;
    private Player player;
    private int cameraX;
    private int cameraY;
    private int mouseX;
    private int mouseY;
    private boolean mousePressed = false;
    private final List<Food> foodList;
    private final List<Hp> hpList;
    private final List<BlackBall> blackBallList;
    private final Random random = new Random();
    private int score = 0;
    private int pushBackFrame = 0;
    private double pushX = 0;
    private double pushY = 0;
    private boolean showPanel = false;
    private int nowShowing = IntValues.FIRST_PANEL;
    private boolean gameStarted = false;
    private NetworkService networkService;
    private final Map<Integer, Player> otherPlayers = new HashMap<>();
    private boolean playerIdReceived = false;
    private String ipAddress = ConfigValues.LOCALHOST;
    private String playerName = "name";
    private int playerId;


    public GameService(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.foodList = new ArrayList<>();
        this.hpList = new ArrayList<>();
        this.blackBallList = new ArrayList<>();
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gamePanel.getDeathDialog() != null && gamePanel.isDialogShowed()) {
                    if(gamePanel.getDeathDialog().checkButtonClick(e.getX(), e.getY())) {
                        gamePanel.setDialogShowed(false);
                        gamePanel.setDeathDialog(null);
                    } else{
                        gamePanel.getDeathDialog().setClickMainMenu(true);
                        gamePanel.getDeathDialog().setClickRestart(true);
                    }
                }
            }
        });
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (gamePanel.getDeathDialog() != null && gamePanel.isDialogShowed()) {
                    gamePanel.getDeathDialog().setClickMainMenu(false);
                    gamePanel.getDeathDialog().setClickRestart(false);
                }
            }
        });
    }


    public void startGame(String playerName) {
        this.playerName = playerName;
        gameStarted = true;
        player = new Player(
                random.nextInt(WORLD_WIDTH - 200) + 100,
                random.nextInt(WORLD_HEIGHT - 200) + 100
        );

        player.setPlayerName(playerName);

        try {
            networkService = new NetworkService(ipAddress, ConfigValues.PORT, this::processMessage);
        } catch (IOException e) {
            gameStarted = false;
            player = null;
            return;
        }

        if (gamePanel.getMenu() != null) {
            gamePanel.remove(gamePanel.getMenu());
        }
        synchronized (this) {
            foodList.clear();
        }
        String connectMessage = "CONNECT|" + playerName;
        networkService.sendMessage(connectMessage);
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\|", 2);
        if (parts.length < 1) return;
        String messageType = parts[0];
        String data = (parts.length > 1) ? parts[1] : "";

        switch (messageType) {
            case "ID" -> {
                if (!playerIdReceived) {
                    playerId = Integer.parseInt(data);
                    player.setPlayerId(playerId);
                    playerIdReceived = true;

                    gamePanel.requestFocusInWindow();
                    gamePanel.revalidate();
                    gamePanel.repaint();

                    String updateMessage =
                            Broadcaster.updateBroadcastAll(
                                    player.getPlayerId(),
                                    player.getX(),
                                    player.getY(),
                                    (int) Math.toDegrees(player.getPlayerAngle()),
                                    player.getPlayerName(),
                                    player.isHavingTwoTails(),
                                    player.isHavingThorns(),
                                    player.getScore()
                            );
                    networkService.sendMessage(updateMessage);
                }
            }
            case "CONNECT" -> {
                String[] connectParts = data.split("\\|");
                if (connectParts.length == 2) {
                    int playerId = Integer.parseInt(connectParts[0]);
                    String playerName = connectParts[1];
                    if (playerId != player.getPlayerId()) {
                        Player newPlayer = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
                        newPlayer.setPlayerId(playerId);
                        otherPlayers.put(playerId, newPlayer);
                    }
                }
            }
            case "PLAYER_UPDATE" -> {
                String[] playerParts = data.split("\\|");
                if (playerParts.length >= 5) {
                    int playerId = Integer.parseInt(playerParts[0]);
                    int x = Integer.parseInt(playerParts[1]);
                    int y = Integer.parseInt(playerParts[2]);
                    int angle = Integer.parseInt(playerParts[3]);
                    String name = playerParts[4];
                    boolean thorns = false;
                    boolean manyTails = false;
                    int score = 0;
                    if (playerParts.length == 8) {
                        manyTails = Boolean.parseBoolean(playerParts[5]);
                        thorns = Boolean.parseBoolean(playerParts[6]);
                        score = Integer.parseInt(playerParts[7]);
                    }
                    if (player != null) {
                        if (playerId != player.getPlayerId()) {
                            if (otherPlayers.containsKey(playerId)) {
                                Player otherPlayer = otherPlayers.get(playerId);
                                otherPlayer.setX(x);
                                otherPlayer.setY(y);
                                otherPlayer.setPlayerAngle(Math.toRadians(angle));
                                otherPlayer.updateTailState();
                                otherPlayer.setPlayerName(name);
                                otherPlayer.setManyTails(manyTails);
                                otherPlayer.setThorns(thorns);
                                otherPlayer.setScore(score);
                            }
                        }
                    }
                }
            }
            case "SCORE_UPDATE" -> {
                String[] scoreParts = data.split("\\|");
                if (scoreParts.length == 2) {
                    int playerId = Integer.parseInt(scoreParts[0]);
                    int score = Integer.parseInt(scoreParts[1]);
                    if (otherPlayers.containsKey(playerId)) {
                        Player otherPlayer = otherPlayers.get(playerId);
                        otherPlayer.setScore(score);
                    }
                }
            }
            case "DISCONNECT" -> {
                String[] disconnectParts = data.split("\\|");
                if (disconnectParts.length == 1) {
                    int playerId = Integer.parseInt(disconnectParts[0]);
                    otherPlayers.remove(playerId);
                }
            }
            case "FOOD_UPDATE" -> {
                synchronized (this) {
                    foodList.clear();
                    String[] foodParts = data.split("\\|");
                    for (int i = 1; i < foodParts.length; i++) {
                        String[] xy = foodParts[i].split(",");
                        if (xy.length == 2) {
                            int x = Integer.parseInt(xy[0]);
                            int y = Integer.parseInt(xy[1]);
                            foodList.add(new Food(x, y));
                        }
                    }
                }
            }
            case "HP_UPDATE" -> {
                synchronized (this) {
                    hpList.clear();
                    String[] hpParts = data.split("\\|");
                    for (int i = 1; i < hpParts.length; i++) {
                        String[] xy = hpParts[i].split(",");
                        if (xy.length == 2) {
                            int x = Integer.parseInt(xy[0]);
                            int y = Integer.parseInt(xy[1]);
                            hpList.add(new Hp(x, y));
                        }
                    }
                }
            }
            case "BLACK_BALL_UPDATE" -> {
                synchronized (this) {
                    blackBallList.clear();
                    String[] blackBallParts = data.split("\\|");
                    for (int i = 1; i < blackBallParts.length; i++) {
                        String[] xy = blackBallParts[i].split(",");
                        if (xy.length == 2) {
                            int x = Integer.parseInt(xy[0]);
                            int y = Integer.parseInt(xy[1]);
                            blackBallList.add(new BlackBall(x, y));
                        }
                    }
                }
            }
            case "TAILS_UPDATE" -> {
                String[] tailsParts = data.split("\\|");
                if (tailsParts.length == 2) {
                    int playerId = Integer.parseInt(tailsParts[0]);
                    boolean manyTails = Boolean.parseBoolean(tailsParts[1]);
                    if (playerId != player.getPlayerId()) {
                        if (otherPlayers.containsKey(playerId)) {
                            Player otherPlayer = otherPlayers.get(playerId);
                            otherPlayer.setManyTails(manyTails);
                        }
                    }
                }
            }
            case "THORNS_UPDATE" -> {
                String[] thornsParts = data.split("\\|");
                if (thornsParts.length == 2) {
                    int playerId = Integer.parseInt(thornsParts[0]);
                    if (playerId != player.getPlayerId()) {
                        if (otherPlayers.containsKey(playerId)) {
                            Player otherPlayer = otherPlayers.get(playerId);
                            otherPlayer.setThorns(true);
                        }
                    }
                }
            }
        }
    }
    public void checkPlayerCollision() {
        for (Map.Entry<Integer, Player> entry : otherPlayers.entrySet()) {
            Player anotherHim = entry.getValue();
            if (player.isGettingHit(anotherHim)) {
                int healthCur = player.getHealth();
                if (healthCur > 0) {
                    player.setHealth(healthCur - 1);
                }
                pushBackFrame = PUSH_BACK_FRAMES;
                pushX = player.getX() - anotherHim.getX();
                pushY = player.getY() - anotherHim.getY();
                double distance = Math.sqrt(pushX * pushX + pushY * pushY);
                if (distance > 0) {
                    pushX /= distance;
                    pushY /= distance;
                }
            } else if (player.isGettingHitByThorn(anotherHim)) {
                int healthCur = player.getHealth();
                if (healthCur > 0) {
                    player.setHealth(healthCur - 1);
                }
                pushBackFrame = PUSH_BACK_FRAMES;
                pushX = player.getX() - anotherHim.getX();
                pushY = player.getY() - anotherHim.getY();
                double distance = Math.sqrt(pushX * pushX + pushY * pushY);
                if (distance > 0) {
                    pushX /= distance;
                    pushY /= distance;
                }
            }
        }
    }

    public void checkBlackBallCollision() {
        synchronized (this) {
            for (int i = 0; i < blackBallList.size(); i++) {
                GameObject blackBall = blackBallList.get(i);
                if (player.isCollidingWithBlackBall(blackBall, BLACK_BALL_SIZE)) {
                    int healthCur = player.getHealth();
                    if (healthCur > 0) {
                        player.setHealth(healthCur - 1);
                    }
                    pushBackFrame = PUSH_BACK_FRAMES;
                    pushX = player.getX() - blackBall.getX();
                    pushY = player.getY() - blackBall.getY();
                    double distance = Math.sqrt(pushX * pushX + pushY * pushY);
                    if (distance > 0) {
                        pushX /= distance;
                        pushY /= distance;
                    }
                    String message = "BLACK_BALL_EATEN|" + blackBall.getX() + "|" + blackBall.getY();
                    if (networkService != null) {
                        networkService.sendMessage(message);
                    }
                    blackBallList.remove(i);
                    i--;
                }
                else if (player.isThornCollidingWithBlackBall(blackBall, BLACK_BALL_SIZE)) {
                    String message = "BLACK_BALL_EATEN|" + blackBall.getX() + "|" + blackBall.getY();
                    if (networkService != null) {
                        networkService.sendMessage(message);
                    }
                    blackBallList.remove(i);
                    i--;
                }
            }
        }
    }
    public void checkHpCollision() {
        synchronized (this) {
            for (int i = 0; i < hpList.size(); i++) {
                Hp hp = hpList.get(i);
                if (player.isCollidingWithHp(hp)) {

                    String message = "HP_EATEN|" + hp.getX() + "|" + hp.getY();
                    player.setHealth(player.getHealth() + 1);
                    score++;
                    if (score == 15 || score == 40 || score == 100) {
                        showPanel = true;
                        score++;
                    }
                    String updateMessage =
                            Broadcaster.updateBroadcastAll(
                                    player.getPlayerId(),
                                    player.getX(),
                                    player.getY(),
                                    (int) Math.toDegrees(player.getPlayerAngle()),
                                    player.getPlayerName(),
                                    player.isHavingTwoTails(),
                                    player.isHavingThorns(),
                                    score
                            );
                    if (networkService != null) {
                        networkService.sendMessage(message);
                        networkService.sendMessage(updateMessage);
                    }

                        hpList.remove(i);
                    i--;
                }
            }
        }
    }

    public void checkFoodCollision() {
        synchronized (this) {
            for (int i = 0; i < foodList.size(); i++) {
                Food food = foodList.get(i);
                if (player.isCollidingWithFood(food)) {

                    String message = "FOOD_EATEN|" + food.getX() + "|" + food.getY();
                    score++;
                    if (score == 15 || score == 40 || score == 100) {
                        showPanel = true;
                        score++;
                    }
                    String updateMessage =
                            Broadcaster.updateBroadcastAll(
                                    player.getPlayerId(),
                                    player.getX(),
                                    player.getY(),
                                    (int) Math.toDegrees(player.getPlayerAngle()),
                                    player.getPlayerName(),
                                    player.isHavingTwoTails(),
                                    player.isHavingThorns(),
                                    score
                            );
                    if (networkService != null) {
                        networkService.sendMessage(message);
                        networkService.sendMessage(updateMessage);
                    }

                    foodList.remove(i);
                    i--;
                }
            }
        }
    }

    public void updateCamera() {
        if (gameStarted) {
            cameraX = player.getX() - gamePanel.getWidth() / 2;
            cameraY = player.getY() - gamePanel.getHeight() / 2;
            cameraX = Math.max(0, Math.min(cameraX, WORLD_WIDTH - gamePanel.getWidth()));
            cameraY = Math.max(0, Math.min(cameraY, WORLD_HEIGHT - gamePanel.getHeight()));
        }
    }


    public void updateGame(int mouseX, int mouseY, boolean mousePressed, long delay) {

        if (gameStarted && playerIdReceived) {
            if (player.getHealth() > 0) {
                player.move(mouseX, mouseY, cameraX, cameraY, mousePressed, networkService, delay);
                if(pushBackFrame > 0) {
                    applyPushBack();
                    String updateMessage =
                            Broadcaster.updateBroadcastLow(
                                    player.getPlayerId(),
                                    player.getX(),
                                    player.getY(),
                                    (int) Math.toDegrees(player.getPlayerAngle()),
                                    player.getPlayerName()
                            );
                    networkService.sendMessage(updateMessage);
                }
            }
            updateCamera();
            checkFoodCollision();
            checkHpCollision();
            checkBlackBallCollision();
            checkPlayerCollision();
            if (player.getHealth() == 0 && !player.isDead()) {
                player.setDead(true);
                String deathMessage = "PLAYER_DIED|" + playerId;
                if (networkService != null) {
                    networkService.sendMessage(deathMessage);
                }
                gamePanel.setDialogShowed(true);
                gamePanel.setDeathDialog(new DeathDialog(0, 0, e -> returnToMainMenu(), e -> restartGame()));
                gamePanel.getDeathDialog().show();
            }
        }
    }

    public void applyPushBack() {
        if(pushBackFrame > 0) {
            double speedX = pushX * PUSH_BACK_SPEED;
            double speedY = pushY * PUSH_BACK_SPEED;
            player.setX(player.getX() + (int)speedX);
            player.setY(player.getY() + (int)speedY);
            pushBackFrame--;
        } else {
            pushX = 0;
            pushY = 0;
        }
    }

    public void returnToMainMenu() {
        gameStarted = false;
        score = 0;
        showPanel = false;
        blackBallList.clear();
        foodList.clear();
        pushBackFrame = 0;
        pushX = 0;
        pushY = 0;
        nowShowing = IntValues.FIRST_PANEL;
        gamePanel.add(gamePanel.getMenu());
        gamePanel.setDialogShowed(false);
        gamePanel.setDeathDialog(null);
        gamePanel.revalidate();
        gamePanel.repaint();
        gamePanel.requestFocusInWindow();

        if(playerIdReceived) {
            if (player != null) {
                String disconnectMessage = "DISCONNECT|" + playerId;
                if (networkService != null) {
                    networkService.sendMessage(disconnectMessage);
                }
            }

        }

        try {
            if (networkService != null) {
                networkService.close();
                networkService = null;
            }
        } catch (IOException e) {
            System.out.println("Не удалось выйти");
        }

        playerIdReceived = false;
        player = null;
    }

    private void restartGame() {
        returnToMainMenu();
        gamePanel.setDialogShowed(false);
        gamePanel.setDeathDialog(null);
        startGame(playerName);
    }

    public void setManyTails(boolean manyTails) {
        String message = "TAILS_UPDATE|" + player.getPlayerId() + "|" + manyTails;
        if(networkService != null) {
            networkService.sendMessage(message);
        }
    }
    public void setThorns(){
        String message = "THORNS_UPDATE|" + player.getPlayerId() + "|" + player.isHavingThorns();
        if(networkService != null) {
            networkService.sendMessage(message);
        }
    }
    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isPanelShowed() {
        return showPanel;
    }

    public int getNowShowing() {
        return nowShowing;
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }

    public Player getPlayer() {
        return player;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public int getScore() {
        return score;
    }

    public void setShowPanel(boolean showPanel) {
        this.showPanel = showPanel;
    }

    public void setNowShowing(int nowShowing) {
        this.nowShowing = nowShowing;
    }

    public List<Food> getFoodList() {
        return foodList;
    }

    public List<Hp> getHpList() {
        return hpList;
    }

    public List<BlackBall> getBlackBallList() {
        return blackBallList;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getMouseX() {
        return mouseX;
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public Map<Integer, Player> getOtherPlayers(){
        return otherPlayers;
    }
}