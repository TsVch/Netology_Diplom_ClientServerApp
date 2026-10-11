
package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReadPortFromFile() throws IOException {
        Path settingsFile = tempDir.resolve("settings.txt");

        Files.writeString(settingsFile, "port=8080");

        int port = Settings.loadPort(settingsFile);

        assertEquals(8080, port);
    }

    @Test
    void shouldThrowExceptionWhenPortIsMissing() throws IOException {
        Path settingsFile = tempDir.resolve("settings.txt");

        Files.writeString(settingsFile, "host=localhost");

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Settings.loadPort(settingsFile)
        );
    }

    @Test
    void shouldThrowExceptionWhenPortIsOutOfRange() throws IOException {
        Path settingsFile = tempDir.resolve("settings.txt");

        Files.writeString(settingsFile, "port=70000");

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Settings.loadPort(settingsFile)
        );
    }
}