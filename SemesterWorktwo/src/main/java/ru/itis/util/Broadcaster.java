package ru.itis.util;

public final class Broadcaster {

    private Broadcaster() {}
    public static String updateBroadcastAll(int otherPlayerId, int x, int y, int angle, String otherPlayerName, boolean manyTails, boolean thorns, int score) {
        return "PLAYER_UPDATE|" + otherPlayerId + "|" + x + "|" + y + "|" + angle + "|" + otherPlayerName + "|" + manyTails + "|" + thorns + "|" + score;
    }
    public static String updateBroadcastReduced(int playerId, int x, int y, int angle, String playerName, boolean manyTails, boolean thorns) {
        return "PLAYER_UPDATE|" + playerId + "|" + x + "|" + y + "|" + angle + "|" + playerName + "|" + manyTails + "|" + thorns;
    }

    public static String updateBroadcastLow(int id, int x, int y, int angle, String name) {
        return "PLAYER_UPDATE|" + id + "|" + x + "|" + y + "|" + angle + "|" + name;

    }
}
