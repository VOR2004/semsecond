package ru.itis.game.player;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.Random;

import ru.itis.cfg.ConfigValues;
import ru.itis.game.objects.Food;
import ru.itis.game.objects.GameObject;
import ru.itis.game.objects.Hp;
import ru.itis.network.NetworkService;
import ru.itis.res.Colors;
import ru.itis.util.Broadcaster;

import static ru.itis.cfg.ConfigValues.*;

public class Player extends GameObject {

    private static final Color[] POSSIBLE_COLORS = Colors.POSSIBLE_COLORS;
    private static final Color THORN_COLOR = Colors.THORN_COLOR;
    private final Color playerColor;

    private String playerName = ConfigValues.DEFAULT_NAME;
    private int moveSpeed = 5;
    private double playerAngle = 0;
    private double tailWiggle = 0;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean isMoving = false;
    private boolean isHavingTwoTails = false;
    private boolean isHavingThorns = false;
    private int health = 6;
    private int blinkTimer = 0;
    private int playerId;
    private boolean isBlinking = false;
    private boolean isDead = false;
    private int score = 0;
    private boolean isHavingElectro = false;

    private static final int JAW_RADIUS = 30;
    private static double JAW_ANGLE_SPEED = 0.06;
    private static final int JAW_BASE_OFFSET = 30;
    private static final double JAW_ANGLE_OFFSET = Math.PI / 3.5;
    private static final double JAW_MAX_OPEN_ANGLE = Math.PI / 2.5;
    private static final int JAW_THICKNESS = 5;
    private double jawAngle = 0;


    public Player(int x, int y) {
        super(x, y);

        Random random = new Random();
        int colorIndex = random.nextInt(POSSIBLE_COLORS.length);
        playerColor = POSSIBLE_COLORS[colorIndex];
    }

    public void move(int mouseX, int mouseY, int cameraX, int cameraY, boolean mousePressed, NetworkService networkService, long delay) {
        double targetAngle = playerAngle;
        double speedCoefficient = (double) (delay / 20);
        int dx = 0;
        int dy = 0;
        isMoving = false;
        if (mousePressed) {
            dx = mouseX + cameraX - (x + PLAYER_WIDTH / 2);
            dy = mouseY + cameraY - (y + PLAYER_HEIGHT / 2);

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > moveSpeed) {
                targetAngle = Math.atan2(dy, dx);
                double speedX = dx / distance * moveSpeed * speedCoefficient;
                double speedY = dy / distance * moveSpeed * speedCoefficient;

                x += (int) speedX;
                y += (int) speedY;

                isMoving = true;
            } else {
                x += dx;
                y += dy;
                isMoving = true;
            }
        } else {
            if (upPressed) {
                dy -= 1;
            }
            if (downPressed) {
                dy += 1;
            }
            if (leftPressed) {
                dx -= 1;
            }
            if (rightPressed) {
                dx += 1;
            }

            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                targetAngle = Math.atan2(dy, dx);
                double speedX = dx / distance * moveSpeed * speedCoefficient;
                double speedY = dy / distance * moveSpeed * speedCoefficient;

                x += (int) speedX;
                y += (int) speedY;
                isMoving = true;
            }
        }
        playerAngle = targetAngle;

        x = Math.max(0, Math.min(x, WORLD_WIDTH - PLAYER_WIDTH));
        y = Math.max(0, Math.min(y, WORLD_HEIGHT - PLAYER_HEIGHT));

        if (isMoving) {
            String message =
                    Broadcaster.updateBroadcastAll(
                            playerId,
                            x,
                            y,
                            (int) Math.toDegrees(playerAngle),
                            playerName,
                            isHavingTwoTails,
                            isHavingThorns,
                            score
                    );
            networkService.sendMessage(message);
        }
    }

    private void drawLightning(Graphics2D g2d, int centerX, int centerY, double jawSideAngle, double playerAngle) {
        double baseAngle = playerAngle + jawSideAngle;
        int jawBaseX = (int) (centerX + JAW_BASE_OFFSET * Math.cos(baseAngle));
        int jawBaseY = (int) (centerY + JAW_BASE_OFFSET * Math.sin(baseAngle));
        double startAngle = baseAngle - JAW_ANGLE_OFFSET + jawAngle;
        double endAngle = baseAngle + JAW_ANGLE_OFFSET - jawAngle;
        int x1 = (int) (jawBaseX + JAW_RADIUS * Math.cos(startAngle));
        int y1 = (int) (jawBaseY + JAW_RADIUS * Math.sin(startAngle));
        int x2 = (int) (jawBaseX + JAW_RADIUS * Math.cos(endAngle));
        int y2 = (int) (jawBaseY + JAW_RADIUS * Math.sin(endAngle));

        g2d.setColor(Color.yellow);
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(JAW_THICKNESS));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setStroke(oldStroke);
    }

    private void drawName(Graphics2D g2d, int offsetX, int offsetY) {
        if (playerName != null && !playerName.isEmpty()) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.setColor(Color.black);
            FontMetrics fm = g2d.getFontMetrics();
            int nameWidth = fm.stringWidth(playerName);
            int nameX = x + offsetX - nameWidth / 2 + 35;
            int nameY = y + offsetY - (Math.max(PLAYER_HEIGHT, PLAYER_WIDTH) / 2);
            g2d.drawString(playerName, nameX, nameY);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    @Override
    public void draw(Graphics g, int offsetX, int offsetY) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        int centerX = x + offsetX + PLAYER_WIDTH / 2;
        int centerY = y + offsetY + PLAYER_HEIGHT / 2;

        drawArrow(g, centerX, centerY);
        drawTail(g2d, centerX, centerY, playerAngle, 0);
        drawName(g2d, offsetX, offsetY);
        if (isHavingTwoTails) {
            drawTails(g2d, centerX, centerY, playerAngle);
        }
        if (isHavingThorns) {
            drawThorns(g2d, centerX, centerY, playerAngle);
        }

        g2d.rotate(playerAngle, centerX, centerY);

        if (isHavingElectro) {
            drawLightning(g2d, centerX, centerY,-Math.PI / 2 , playerAngle);
            drawLightning(g2d, centerX, centerY, Math.PI / 2, playerAngle);
        }

        drawBody(g2d, offsetX, offsetY);

        drawEye(g2d, offsetX, offsetY);

        g2d.setTransform(originalTransform);

        updateTailWiggle();
        updateBlink();
        updateLightning();
    }
    private void updateLightning() {
        jawAngle += JAW_ANGLE_SPEED;
        if (jawAngle > JAW_MAX_OPEN_ANGLE) {
            jawAngle = JAW_MAX_OPEN_ANGLE;
            JAW_ANGLE_SPEED *= -1;
        } else if (jawAngle < 0) {
            jawAngle = 0;
            JAW_ANGLE_SPEED *= -1;
        }
    }

    private void drawBody(Graphics2D g2d, int offsetX, int offsetY) {
        g2d.setColor(playerColor);
        g2d.fillOval(x + offsetX, y + offsetY, PLAYER_WIDTH, PLAYER_HEIGHT);
    }

    private void drawEye(Graphics2D g2d, int offsetX, int offsetY) {

        Color eyeColor = (isBlinking && blinkTimer <= BLINK_DURATION) ? playerColor.darker() : Color.WHITE;
        g2d.setColor(eyeColor);
        int eyeX = x + offsetX + PLAYER_WIDTH / 2 - EYE_SIZE / 2;
        int eyeY = y + offsetY + PLAYER_HEIGHT / 2 - EYE_SIZE / 2;
        g2d.fillOval(eyeX, eyeY, EYE_SIZE, EYE_SIZE);

        if (!(isBlinking && blinkTimer <= BLINK_DURATION)) {
            g2d.setColor(Color.BLACK);
            int pupilX = eyeX + EYE_SIZE / 2 - PUPIL_SIZE / 2;
            int pupilY = eyeY + EYE_SIZE / 2 - PUPIL_SIZE / 2;
            g2d.fillOval(pupilX, pupilY, PUPIL_SIZE, PUPIL_SIZE);
        }
    }

    private void drawTail(Graphics2D g2d, int centerX, int centerY, double playerAngle, double coefDist) {
        g2d.setColor(Color.BLACK);

        double tailBaseX = centerX + ((double) PLAYER_WIDTH / 2) * Math.cos(playerAngle + Math.PI);
        double tailBaseY = centerY + ((double) PLAYER_HEIGHT / 2) * Math.sin(playerAngle + Math.PI);

        double currentX = tailBaseX;
        double currentY = tailBaseY;

        for (int i = 0; i < TAIL_SEGMENTS; i++) {
            double angle = coefDist + playerAngle + Math.PI + (Math.sin(tailWiggle + i * 0.8) * 0.8);
            double nextX = currentX + TAIL_SEGMENT_LENGTH * Math.cos(angle);
            double nextY = currentY + TAIL_SEGMENT_LENGTH * Math.sin(angle);

            g2d.drawLine((int) currentX,(int) currentY, (int) nextX,(int) nextY);

            currentX = nextX;
            currentY = nextY;
        }
    }

    private void drawTails(Graphics2D g2d, int centerX, int centerY, double playerAngle) {
        drawTail(g2d, centerX, centerY, playerAngle, 0.2);
        drawTail(g2d, centerX, centerY, playerAngle, -0.2);
    }

    private void drawArrow(Graphics g, int centerX, int centerY) {
        int arrowX = (int) ( ARROW_LENGTH * Math.cos(playerAngle));
        int arrowY = (int) ( ARROW_LENGTH * Math.sin(playerAngle));

        g.setColor(Color.BLACK);
        g.drawLine(centerX, centerY, centerX + arrowX, centerY + arrowY);
    }

    private void drawThorn(Graphics g, int centerX, int centerY, double playerAngle, double distCoef) {
        double thornAngle = playerAngle + Math.PI + distCoef;

        int thornX = (int) (THORN_LENGTH * Math.cos(thornAngle));
        int thornY = (int) (THORN_LENGTH * Math.sin(thornAngle));

        Graphics2D g2d = (Graphics2D) g;

        double tipX = centerX + thornX;
        double tipY = centerY + thornY;

        int[] xPoints = {(int)tipX, (int)(centerX + THORN_BASE_WIDTH / 2 * Math.cos(thornAngle + Math.PI/2)), (int)(centerX + THORN_BASE_WIDTH / 2 * Math.cos(thornAngle - Math.PI/2))};
        int[] yPoints = {(int)tipY, (int)(centerY + THORN_BASE_WIDTH / 2 * Math.sin(thornAngle+ Math.PI/2)), (int)(centerY + THORN_BASE_WIDTH / 2 * Math.sin(thornAngle - Math.PI/2))};

        g2d.setColor(THORN_COLOR);
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawThorns(Graphics g2d, int centerX, int centerY, double playerAngle) {
        drawThorn(g2d, centerX, centerY, playerAngle, Math.PI / 2.2);
        drawThorn(g2d, centerX, centerY, playerAngle, -Math.PI / 2.2);
    }

    private void updateTailWiggle() {
        if (isMoving) {
            tailWiggle += TAIL_WIGGLE_SPEED_MOVE;
        } else {
            tailWiggle += TAIL_WIGGLE_SPEED_IDLE;
        }
    }
    public void updateTailState() {
        long startTime = System.currentTimeMillis();
        long currentTime = startTime + 1;
        tailWiggle += TAIL_WIGGLE_SPEED_MOVE;
        while (!(currentTime - startTime < 100)) {
            currentTime = System.currentTimeMillis();
        }
        tailWiggle += TAIL_WIGGLE_SPEED_IDLE;
    }


    private void updateBlink() {
        blinkTimer++;
        if (blinkTimer > BLINK_INTERVAL * 60) {
            isBlinking = true;
            blinkTimer = 0;
        } else if (isBlinking && blinkTimer > BLINK_DURATION) {
            isBlinking = false;
        }
    }
    public boolean isCollidingWithFood(Food food) {
        return BooleanPlayerHandler.isCollidingWithFood(food, x, y, playerAngle);
    }

    public boolean isCollidingWithBlackBall(GameObject blackBall, int blackBallSize) {
        return BooleanPlayerHandler.isCollidingWithBlackBall(blackBall, blackBallSize, x, y, playerAngle);
    }

    public boolean isCollidingWithHp(Hp hp) {
        return BooleanPlayerHandler.isCollidingWithHp(hp, x, y, playerAngle);
    }

    public boolean isGettingHit(Player otherPlayer) {
        return BooleanPlayerHandler.isGettingHit(otherPlayer, x, y);
    }

    public boolean isGettingHitByThorn(Player otherPlayer) {
        return BooleanPlayerHandler.isGettingHitByThorn(otherPlayer, x, y, playerAngle);
    }

    public boolean isThornCollidingWithBlackBall(GameObject blackBall, int blackBallSize) {
        return BooleanPlayerHandler.isThornCollidingWithBlackBall(blackBall, blackBallSize, isHavingThorns, x, y, playerAngle);
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setManyTails(boolean b) {
        isHavingTwoTails = b;
    }

    public void setSpeed(int speed) {
        moveSpeed = speed;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public void setThorns(boolean b) {
        isHavingThorns = b;
    }

    public double getPlayerAngle() {
        return playerAngle;
    }

    public void setPlayerAngle(double playerAngle) {
        this.playerAngle = playerAngle;
    }

    public boolean isHavingThorns() {
        return isHavingThorns;
    }

    public boolean isHavingTwoTails(){
        return isHavingTwoTails;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setHavingElectro(boolean havingElectro) {
        isHavingElectro = havingElectro;
    }

    public int getSpeed() {
        return moveSpeed;
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> upPressed = true;
            case KeyEvent.VK_S -> downPressed = true;
            case KeyEvent.VK_A -> leftPressed = true;
            case KeyEvent.VK_D -> rightPressed = true;
        }
    }


    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> upPressed = false;
            case KeyEvent.VK_S -> downPressed = false;
            case KeyEvent.VK_A -> leftPressed = false;
            case KeyEvent.VK_D -> rightPressed = false;
        }
    }
}