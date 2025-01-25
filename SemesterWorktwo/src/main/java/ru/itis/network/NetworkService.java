package ru.itis.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkService {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private Thread receiveThread;
    private final Consumer<String> messageCallback;

    public NetworkService(String serverAddress, int serverPort, Consumer<String> callback) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.messageCallback = callback;
        startReceiving();
    }

    private void startReceiving() {
        receiveThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (messageCallback != null) {
                        messageCallback.accept(message);
                    }
                }
            } catch (IOException e) {
                if (!socket.isClosed()){
                    System.out.println("Cервер выключен");
                }

            }
        });
        receiveThread.start();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
    }
}