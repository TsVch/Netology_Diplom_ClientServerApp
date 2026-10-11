package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server {
    private static final ConcurrentMap<Socket, PrintWriter> clients =
            new ConcurrentHashMap<>();
    private static final ChatLogger logger =
            new ChatLogger(java.nio.file.Path.of("server.log"));
    public static void main(String[] args) {
        try {
            int port = Settings.loadPort(java.nio.file.Path.of("settings.txt"));

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Сервер запущен на порту " + port);
                logger.log("Сервер запущен на порту " + port);
                System.out.println("Ожидание подключений...");

                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    System.out.println(
                            "Подключился клиент: "
                                    + clientSocket.getInetAddress()
                                    + ":"
                                    + clientSocket.getPort()
                    );

                    Thread clientThread = new Thread(
                            () -> handleClient(clientSocket)
                    );

                    clientThread.start();
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }


    private static void handleClient(Socket clientSocket) {
        PrintWriter writer = null;

        try (
                Socket socket = clientSocket;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            // Первая строка от клиента — его имя
            String username = reader.readLine();

            if (username == null || username.isBlank()) {
                System.out.println("Клиент отключился, не указав имя.");
                return;
            }

            username = username.replace("\uFEFF", "").trim();

            // Создаём средство отправки сообщений клиенту
            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
            );

            // Регистрируем клиента для рассылки
            clients.put(socket, writer);

            System.out.println("Пользователь подключился: " + username);
            logger.log("Пользователь подключился: " + username);

            // Сообщаем всем, что пользователь вошёл в чат
            broadcast("Пользователь " + username + " подключился к чату.");

            String message;

            while ((message = reader.readLine()) != null) {
                if (message.equals("/exit")) {
                    break;
                }

                String chatMessage = username + ": " + message;

                System.out.println(chatMessage);
                logger.log(chatMessage);
                broadcast(chatMessage);
            }

            System.out.println("Пользователь отключился: " + username);
            logger.log("Пользователь отключился: " + username);
        } catch (IOException e) {
            System.err.println(
                    "Ошибка при обработке клиента: " + e.getMessage()
            );
        } finally {
            // Удаляем отключившегося клиента из рассылки
            clients.remove(clientSocket);

            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void broadcast(String message) {
        for (PrintWriter writer : clients.values()) {
            writer.println(message);
        }
    }
}