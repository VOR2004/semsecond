package ru.itis.game.player;

import ru.itis.game.objects.Food;
import ru.itis.game.objects.GameObject;
import ru.itis.game.objects.Hp;

import static ru.itis.cfg.ConfigValues.*;
import static ru.itis.cfg.ConfigValues.THORN_LENGTH;

public final class BooleanPlayerHandler {
    private  BooleanPlayerHandler() {}

    public static boolean isCollidingWithFood(Food food, int x, int y, double playerAngle) {
        int playerCenterX = x + PLAYER_WIDTH / 2;
        int playerCenterY = y + PLAYER_HEIGHT / 2;

        int foodCenterX = food.getX() + food.getSize() / 2;
        int foodCenterY = food.getY() + food.getSize() / 2;

        int arrowX = (int) (playerCenterX + ARROW_LENGTH * Math.cos(playerAngle));
        int arrowY = (int) (playerCenterY + ARROW_LENGTH * Math.sin(playerAngle));

        double distance = Math.sqrt((arrowX - foodCenterX) * (arrowX - foodCenterX) + (arrowY - foodCenterY) * (arrowY - foodCenterY));

        double combinedRadius = (double) food.getSize() / 2 + ARROW_WIDTH;
        return distance <= combinedRadius;
    }

    public static boolean isCollidingWithBlackBall(GameObject blackBall, int blackBallSize, int x, int y, double playerAngle) {
        int playerCenterX = x + PLAYER_WIDTH / 2;
        int playerCenterY = y + PLAYER_HEIGHT / 2;
        int blackBallCenterX = blackBall.getX() + blackBallSize / 2;
        int blackBallCenterY = blackBall.getY() + blackBallSize / 2;

        int arrowX = (int) (playerCenterX + ARROW_LENGTH * Math.cos(playerAngle));
        int arrowY = (int) (playerCenterY + ARROW_LENGTH * Math.sin(playerAngle));

        double distanceArrow = Math.sqrt((arrowX - blackBallCenterX) * (arrowX - blackBallCenterX) + (arrowY - blackBallCenterY) * (arrowY - blackBallCenterY));
        double combinedRadiusArrow = (double) blackBallSize / 2 + ARROW_WIDTH;
        if (distanceArrow <= combinedRadiusArrow) return true;

        double distanceBody = Math.sqrt((playerCenterX - blackBallCenterX) * (playerCenterX - blackBallCenterX) + (playerCenterY - blackBallCenterY) * (playerCenterY - blackBallCenterY));
        double combinedRadiusBody = (double) blackBallSize / 2 + ((double) Math.max(PLAYER_HEIGHT, PLAYER_WIDTH) /2);
        return distanceBody <= combinedRadiusBody;
    }

    public static boolean isCollidingWithHp(Hp hp, int x, int y, double playerAngle) {
        int playerCenterX = x + PLAYER_WIDTH / 2;
        int playerCenterY = y + PLAYER_HEIGHT / 2;

        int foodCenterX = hp.getX() + hp.getSize() / 2;
        int foodCenterY = hp.getY() + hp.getSize() / 2;

        int arrowX = (int) (playerCenterX + ARROW_LENGTH * Math.cos(playerAngle));
        int arrowY = (int) (playerCenterY + ARROW_LENGTH * Math.sin(playerAngle));

        double distance = Math.sqrt((arrowX - foodCenterX) * (arrowX - foodCenterX) + (arrowY - foodCenterY) * (arrowY - foodCenterY));

        double combinedRadius = (double) hp.getSize() / 2 + ARROW_WIDTH;
        return distance <= combinedRadius;
    }

    public static boolean isGettingHit(Player otherPlayer, int x, int y) {
        int playerCenterX = x + PLAYER_WIDTH / 2;
        int playerCenterY = y + PLAYER_HEIGHT / 2;

        int otherPlayerCenterX = otherPlayer.getX() + PLAYER_WIDTH / 2;
        int otherPlayerCenterY = otherPlayer.getY() + PLAYER_HEIGHT / 2;

        int arrowX = (int) (otherPlayerCenterX + ARROW_LENGTH * Math.cos(otherPlayer.getPlayerAngle()));
        int arrowY = (int) (otherPlayerCenterY + ARROW_LENGTH * Math.sin(otherPlayer.getPlayerAngle()));

        double distanceArrow = Math.sqrt((arrowX - playerCenterX) * (arrowX - playerCenterX) + (arrowY - playerCenterY) * (arrowY - playerCenterY));
        double combinedRadiusArrow = ((double) Math.max(PLAYER_HEIGHT, PLAYER_WIDTH) / 2) / 2 + ARROW_WIDTH;
        return distanceArrow <= combinedRadiusArrow;
    }

    public static boolean isGettingHitByThorn(Player otherPlayer, int x, int y, double playerAngle) {
        if (otherPlayer.isHavingThorns()) {
            int centerX = x + PLAYER_WIDTH / 2;
            int centerY = y + PLAYER_HEIGHT / 2;

            int otherPlayerCenterX = otherPlayer.getX() + PLAYER_WIDTH / 2;
            int otherPlayerCenterY = otherPlayer.getY() + PLAYER_HEIGHT / 2;

            double thornAngle1 = playerAngle + Math.PI +  Math.PI / 2.2;
            double thornAngle2 = playerAngle + Math.PI - Math.PI / 2.2;

            int thornX1 = (int) (THORN_LENGTH * Math.cos(thornAngle1));
            int thornY1 = (int) (THORN_LENGTH * Math.sin(thornAngle1));

            int thornX2 = (int) (THORN_LENGTH * Math.cos(thornAngle2));
            int thornY2 = (int) (THORN_LENGTH * Math.sin(thornAngle2));

            int tipX1 = centerX + thornX1;
            int tipY1 = centerY + thornY1;

            int tipX2 = centerX + thornX2;
            int tipY2 = centerY + thornY2;

            double distance1 = Math.sqrt((tipX1 - otherPlayerCenterX) * (tipX1 - otherPlayerCenterX) + (tipY1 - otherPlayerCenterY) * (tipY1 - otherPlayerCenterY));
            double distance2 = Math.sqrt((tipX2 - otherPlayerCenterX) * (tipX2 - otherPlayerCenterX) + (tipY2 - otherPlayerCenterY) * (tipY2 - otherPlayerCenterY));

            double combinedRadius = ((double) Math.max(PLAYER_HEIGHT, PLAYER_WIDTH) / 2);
            return distance1 <= combinedRadius || distance2 <= combinedRadius;
        }
        return false;
    }

    public static boolean isThornCollidingWithBlackBall(GameObject blackBall, int blackBallSize, boolean isHavingThorns, int x, int y, double playerAngle) {
        if (isHavingThorns) {
            int centerX = x + PLAYER_WIDTH / 2;
            int centerY = y + PLAYER_HEIGHT / 2;
            double thornAngle1 = playerAngle + Math.PI +  Math.PI / 2.2;
            double thornAngle2 = playerAngle + Math.PI - Math.PI / 2.2;

            int thornX1 = (int) (THORN_LENGTH * Math.cos(thornAngle1));
            int thornY1 = (int) (THORN_LENGTH * Math.sin(thornAngle1));

            int thornX2 = (int) (THORN_LENGTH * Math.cos(thornAngle2));
            int thornY2 = (int) (THORN_LENGTH * Math.sin(thornAngle2));

            int tipX1 = centerX + thornX1;
            int tipY1 = centerY + thornY1;

            int tipX2 = centerX + thornX2;
            int tipY2 = centerY + thornY2;

            int blackBallCenterX = blackBall.getX() + blackBallSize / 2;
            int blackBallCenterY = blackBall.getY() + blackBallSize / 2;

            double distance1 = Math.sqrt((tipX1 - blackBallCenterX) * (tipX1 - blackBallCenterX) + (tipY1 - blackBallCenterY) * (tipY1 - blackBallCenterY));
            double distance2 = Math.sqrt((tipX2 - blackBallCenterX) * (tipX2 - blackBallCenterX) + (tipY2 - blackBallCenterY) * (tipY2 - blackBallCenterY));
            double combinedRadius = (double) blackBallSize / 2;
            return distance1 <= combinedRadius || distance2 <= combinedRadius;
        }
        return false;
    }
}
