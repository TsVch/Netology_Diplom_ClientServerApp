package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {

    public static int loadPort(Path settingsPath) throws IOException {
        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(settingsPath)) {
            properties.load(input);
        }

        String portValue = properties.getProperty("port");

        if (portValue == null) {
            throw new IllegalArgumentException(
                    "В файле настроек не указан параметр port"
            );
        }

        int port = Integer.parseInt(portValue.trim());

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Номер порта должен быть от 1 до 65535"
            );
        }

        return port;
    }
}
