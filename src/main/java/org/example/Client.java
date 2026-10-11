package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        int port;

        // Читаем настройки сервера
        try {
            port = Settings.loadPort(Path.of("settings.txt"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(
                    "Ошибка чтения настроек: " + e.getMessage()
            );
            return;
        }

        // Подключаемся к серверу
        try (
                Socket socket = new Socket("localhost", port);
                BufferedReader serverReader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
                PrintWriter serverWriter = new PrintWriter(
                        socket.getOutputStream(),
                        true,
                        StandardCharsets.UTF_8
                );
                Scanner console = new Scanner(
                        System.in,
                        StandardCharsets.UTF_8
                )
        ) {
            // Запрашиваем имя пользователя
            System.out.print("Введите ваше имя: ");
            String username = console.nextLine().trim();

            if (username.isBlank()) {
                System.out.println("Имя не может быть пустым.");
                return;
            }

            // Создаём отдельный файл лога для пользователя
            String safeUsername = username.replaceAll(
                    "[\\\\/:*?\"<>|]",
                    "_"
            );

            ChatLogger logger = new ChatLogger(
                    Path.of("client-" + safeUsername + ".log")
            );

            // Отправляем имя серверу
            serverWriter.println(username);

            System.out.println("Вы подключились к чату.");
            logger.log(
                    "Вы подключились к чату под именем: " + username
            );

            System.out.println(
                    "Введите сообщение или /exit для выхода."
            );

            // Отдельный поток получает сообщения от сервера
            Thread readerThread = new Thread(() -> {
                try {
                    String message;

                    while ((message = serverReader.readLine()) != null) {
                        System.out.println(message);
                        logger.log("Получено сообщение: " + message);
                    }
                } catch (IOException e) {
                    System.err.println(
                            "Ошибка чтения сообщений от сервера: "
                                    + e.getMessage()
                    );
                }
            });

            readerThread.setDaemon(true);
            readerThread.start();

            // Основной поток отправляет сообщения серверу
            while (console.hasNextLine()) {
                String message = console.nextLine();

                serverWriter.println(message);

                if (message.equals("/exit")) {
                    logger.log(
                            "Пользователь " + username + " вышел из чата."
                    );
                    break;
                }

                logger.log("Отправлено сообщение: " + message);
            }

        } catch (IOException e) {
            System.err.println(
                    "Не удалось подключиться к серверу: " + e.getMessage()
            );
        }
    }
}
