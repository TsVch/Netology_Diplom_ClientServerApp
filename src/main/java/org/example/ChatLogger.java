package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatLogger {

    private final Path logPath;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatLogger(Path logPath) {
        this.logPath = logPath;
    }

    public synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logMessage = "[" + timestamp + "] " + message
                + System.lineSeparator();

        try {
            Files.writeString(
                    logPath,
                    logMessage,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Ошибка записи лога: " + e.getMessage());
        }
    }
}