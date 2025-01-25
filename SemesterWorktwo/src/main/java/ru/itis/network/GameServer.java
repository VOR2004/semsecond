package ru.itis.network;

import ru.itis.game.objects.BlackBall;
import ru.itis.game.objects.Food;
import ru.itis.cfg.ConfigValues;
import ru.itis.game.objects.Hp;
import ru.itis.res.Strings;
import ru.itis.util.Broadcaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameServer {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Map<Integer, String> playerNames = new HashMap<>();
    private final Map<Integer, PlayerState> playerStates = new HashMap<>();
    private final List<Food> foodList = new ArrayList<>();
    private final List<Hp> hpList = new ArrayList<>();
    private final List<BlackBall> blackBallList = new ArrayList<>();
    private final Random random = new Random();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private int nextClientId = 1;

    private static final int WORLD_WIDTH = ConfigValues.WORLD_WIDTH;
    private static final int WORLD_HEIGHT = ConfigValues.WORLD_HEIGHT;
    private static final int INITIAL_FOOD_COUNT = 60;
    private static final int INITIAL_HP_COUNT = 10;
    private static final int MIN_HP_COUNT = 5;
    private static final int MIN_FOOD_COUNT = 20;
    private static final int INITIAL_BLACK_BALL_COUNT = 50;
    private static final int MIN_BLACK_BALL_COUNT = 20;

    private volatile boolean running = true;

    public GameServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущен на порту: " + port);
        generateInitialHp();
        generateInitialFood();
        generateInitialBlackBalls();
        playerNames.clear();
        playerStates.clear();
    }

    private void generateInitialBlackBalls() {
        while(blackBallList.size() < INITIAL_BLACK_BALL_COUNT) {
            blackBallList.add(new BlackBall(random.nextInt(WORLD_WIDTH - 20) + 10, random.nextInt(WORLD_HEIGHT - 20) + 10));
        }
        sendBlackBallUpdate();
    }

    private void generateInitialFood() {
        while(foodList.size() < INITIAL_FOOD_COUNT) {
            foodList.add(new Food(random.nextInt(WORLD_WIDTH- 20) + 10, random.nextInt(WORLD_HEIGHT - 20) + 10));
        }
        sendFoodUpdate();
    }

    private void generateInitialHp() {
        while(hpList.size() < INITIAL_HP_COUNT) {
            hpList.add(new Hp(random.nextInt(WORLD_WIDTH- 20) + 10, random.nextInt(WORLD_HEIGHT - 20) + 10));
        }
        sendHpUpdate();
    }

    private void generateHp() {
        while(hpList.size() < MIN_HP_COUNT) {
            hpList.add(new Hp(random.nextInt(WORLD_WIDTH - 20) + 10, random.nextInt(WORLD_HEIGHT - 20) + 10));
        }
        sendHpUpdate();
    }
    private void generateFood() {
        while(foodList.size() < MIN_FOOD_COUNT) {
            foodList.add(new Food(random.nextInt(WORLD_WIDTH - 20) + 10, random.nextInt(WORLD_HEIGHT - 20) + 10));
        }
        sendFoodUpdate();
    }

    private void generateBlackBall(){
        while(blackBallList.size() < MIN_BLACK_BALL_COUNT){
            blackBallList.add(new BlackBall(random.nextInt(WORLD_WIDTH - 20) + 10, random.nextInt(WORLD_HEIGHT - 20) + 10));
        }
        sendBlackBallUpdate();
    }

    private void removeBlackBall(int x, int y) {
        for(int i = 0; i < blackBallList.size(); i++){
            BlackBall blackBall = blackBallList.get(i);
            if(blackBall.getX() == x && blackBall.getY() == y){
                blackBallList.remove(i);
                break;
            }
        }
    }
    private void sendFoodUpdate() {
        StringBuilder sb = new StringBuilder("FOOD_UPDATE");
        for (Food food : foodList) {
            sb.append("|").append(food.getX()).append(",").append(food.getY());
        }
        broadcastMessage(sb.toString());
    }

    private void sendHpUpdate() {
        StringBuilder sb = new StringBuilder("HP_UPDATE");
        for (Hp hp : hpList) {
            sb.append("|").append(hp.getX()).append(",").append(hp.getY());
        }
        broadcastMessage(sb.toString());
    }

    private void sendBlackBallUpdate() {
        StringBuilder sb = new StringBuilder("BLACK_BALL_UPDATE");
        for (BlackBall blackBall : blackBallList) {
            sb.append("|").append(blackBall.getX()).append(",").append(blackBall.getY());
        }
        broadcastMessage(sb.toString());
    }
    public void start() {
        try {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключился новый клиент: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, nextClientId++);
                clients.add(clientHandler);
                executor.submit(clientHandler);
            }
        } catch (IOException e) {
            System.out.println(Strings.SERVER_INTERRUPTED);
        } finally {
            shutdown();
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Сервер не был остановлен");
        }
    }

    private void shutdown() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Сервер остановится спустя время");
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;
        private PrintWriter out;
        private BufferedReader in;
        private boolean isConnectionValid = true;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Ошибка при создании потоков для клиента " + clientId + ": " + e.getMessage());
                isConnectionValid = false;
                try {
                    socket.close();
                } catch (IOException closeException) {
                    System.out.println("Ошибка при закрытии сокета для клиента " + clientId + ": " + closeException.getMessage());
                }
            }
        }

        @Override
        public void run() {
            if(!isConnectionValid) {
                System.out.println("Соединение с клиентом " + clientId + " недействительно, поток завершен.");
                return;
            }

            try {
                out.println("ID|" + clientId);

                for (Map.Entry<Integer, String> entry : playerNames.entrySet()) {
                    int otherPlayerId = entry.getKey();
                    String otherPlayerName = entry.getValue();
                    out.println("CONNECT|" + otherPlayerId + "|" + otherPlayerName);
                    PlayerState state = playerStates.get(otherPlayerId);
                    if (state != null) {
                        String updateMessage = Broadcaster.updateBroadcastAll(
                                otherPlayerId,
                                state.x,
                                state.y,
                                state.angle,
                                otherPlayerName,
                                state.manyTails,
                                state.thorns,
                                state.score
                        );
                        out.println(updateMessage);
                    }
                }

                sendFoodUpdate();
                sendHpUpdate();
                sendBlackBallUpdate();
                String message;

                while ((message = in.readLine()) != null) {
                    System.out.println("Получено от клиента " + clientId + ": " + message);
                    processMessage(message, clientId);
                }
                System.out.println("Клиент " + clientId + " отключился.");
                clients.remove(this);
                broadcastMessage("DISCONNECT|" + clientId);
                socket.close();

            } catch (IOException e) {
                System.out.println("Клиент " + clientId + " отключился.");
                clients.remove(this);
                broadcastMessage("DISCONNECT|" + clientId);
            }
        }

        private void processMessage(String message, int clientId) {
            String[] parts = message.split("\\|", 2);
            if (parts.length < 1) return;
            String messageType = parts[0];
            String data = (parts.length > 1) ? parts[1] : "";

            switch (messageType) {
                case "CONNECT" -> {
                    playerNames.put(clientId, data);
                    broadcastMessage("CONNECT|" + clientId + "|" + data);
                    PlayerState state = playerStates.get(clientId);
                    if (state != null) {
                        broadcastMessage(
                                Broadcaster.updateBroadcastAll(
                                        clientId,
                                        state.x,
                                        state.y,
                                        state.angle,
                                        state.playerName,
                                        state.manyTails,
                                        state.thorns,
                                        state.score
                                )
                        );

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
                        PlayerState playerState = playerStates.get(clientId);
                        if (playerState != null) {
                            playerStates.put(clientId, new PlayerState(x, y, angle, name, manyTails, thorns, score));
                        } else {
                            playerStates.put(clientId, new PlayerState(x, y, angle, name));
                        }
                    }
                    broadcastMessage(message);
                }
                case "SCORE_UPDATE" -> {
                    String[] scoreParts = data.split("\\|");
                    if (scoreParts.length == 2) {
                        int playerId = Integer.parseInt(scoreParts[0]);
                        int score = Integer.parseInt(scoreParts[1]);
                        if (playerStates.containsKey(playerId)) {
                            PlayerState playerState = playerStates.get(playerId);
                            playerState.score = score;
                            playerStates.put(playerId, playerState);
                        }
                    }
                    broadcastMessage(message);
                }
                case "DISCONNECT" -> {
                    playerNames.remove(clientId);
                    playerStates.remove(clientId);
                    broadcastMessage(message);
                }
                case "PLAYER_DIED" -> {
                    String[] deathParts = data.split("\\|");
                    if (deathParts.length == 1) {
                        int playerId = Integer.parseInt(deathParts[0]);
                        playerNames.remove(playerId);
                        playerStates.remove(playerId);
                        broadcastMessage("DISCONNECT|" + playerId);
                    }
                }
                case "FOOD_EATEN" -> {
                    String[] foodParts = data.split("\\|");
                    if (foodParts.length == 2) {
                        int x = Integer.parseInt(foodParts[0]);
                        int y = Integer.parseInt(foodParts[1]);
                        removeFood(x, y);
                    }
                    generateFood();
                }
                case "HP_EATEN" -> {
                    String[] hpParts = data.split("\\|");
                    if (hpParts.length == 2) {
                        int x = Integer.parseInt(hpParts[0]);
                        int y = Integer.parseInt(hpParts[1]);
                        removeHp(x, y);
                    }
                    generateHp();
                }
                case "BLACK_BALL_EATEN" -> {
                    String[] blackBallParts = data.split("\\|");
                    if (blackBallParts.length == 2) {
                        int x = Integer.parseInt(blackBallParts[0]);
                        int y = Integer.parseInt(blackBallParts[1]);
                        removeBlackBall(x, y);
                    }
                    generateBlackBall();
                }
                case "TAILS_UPDATE" -> {
                    String[] tailsParts = data.split("\\|");
                    if (tailsParts.length == 2) {
                        int playerId = Integer.parseInt(tailsParts[0]);
                        boolean manyTails = Boolean.parseBoolean(tailsParts[1]);
                        if (playerStates.containsKey(playerId)) {
                            PlayerState playerState = playerStates.get(playerId);
                            playerState.manyTails = manyTails;
                            playerStates.put(playerId, playerState);
                            broadcastMessage(
                                    Broadcaster.updateBroadcastReduced(
                                            playerId,
                                            playerState.x,
                                            playerState.y,
                                            playerState.angle,
                                            playerState.playerName,
                                            playerState.manyTails,
                                            playerState.thorns
                                    )
                            );
                        }
                    }
                }
                case "THORNS_UPDATE" -> {
                    String[] thornsParts = data.split("\\|");
                    if (thornsParts.length == 2) {
                        int playerId = Integer.parseInt(thornsParts[0]);
                        boolean thorns = Boolean.parseBoolean(thornsParts[1]);
                        if (playerStates.containsKey(playerId)) {
                            PlayerState playerState = playerStates.get(playerId);
                            playerState.thorns = thorns;
                            playerStates.put(playerId, playerState);
                            broadcastMessage(
                                    Broadcaster.updateBroadcastReduced(
                                            playerId,
                                            playerState.x,
                                            playerState.y,
                                            playerState.angle,
                                            playerState.playerName,
                                            playerState.manyTails,
                                            playerState.thorns
                                    )
                            );
                        }
                    }
                }
                default -> System.out.println("Неизвестный тип сообщения: " + messageType);
            }
        }

        private void removeFood(int x, int y) {
            for (int i = 0; i < foodList.size(); i++) {
                Food food = foodList.get(i);
                if (food.getX() == x && food.getY() == y) {
                    foodList.remove(i);
                    break;
                }
            }
        }

        private void removeHp(int x, int y) {
            for (int i = 0; i < hpList.size(); i++) {
                Hp hp = hpList.get(i);
                if (hp.getX() == x && hp.getY() == y) {
                    hpList.remove(i);
                    break;
                }
            }
        }
        public void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    private static class PlayerState {
        int x;
        int y;
        int angle;
        boolean manyTails;
        boolean thorns;
        String playerName;
        int score;

        public PlayerState(int x, int y, int angle, String name, boolean manyTails, boolean thorns, int score) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.manyTails = manyTails;
            this.thorns = thorns;
            this.playerName = name;
            this.score = score;
        }
        public PlayerState(int x, int y, int angle, String name) {
            this(x, y, angle, name, false, false, 0);
        }
    }
}