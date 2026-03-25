package fr.univ.bordeaux.technical.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Test suite for the {@link ConfigSerializer} class. */
class ConfigSerializerTest {

  @TempDir Path tempDir;

  @Test
  void testCreateDefault() throws IOException {
    Path configFile = tempDir.resolve("default.agonrc");
    ConfigSerializer serializer = new ConfigSerializer();

    serializer.createDefault(configFile.toString());

    assertTrue(Files.exists(configFile), "The default config file should be created.");

    String content = Files.readString(configFile);

    assertTrue(content.contains("verbose = false"));
    assertTrue(content.contains("timeout = 1800"));
    assertTrue(content.contains("ai_mode = minimax"));
  }

  @Test
  void testCreateDefaultAlreadyExists() throws IOException {
    Path configFile = tempDir.resolve(".agonrc");
    Files.createFile(configFile);
    ConfigSerializer serializer = new ConfigSerializer();

    assertThrows(
        IOException.class,
        () -> {
          serializer.createDefault(configFile.toString());
        });
  }

  @Test
  void testSaveCustomConfig() throws IOException {
    Path configFile = tempDir.resolve("custom.agonrc");

    GameConfig config = new GameConfig();
    config.setTimeout(999);
    config.setBlitzMode(true);
    config.setAiDepth(8);
    config.setWhiteAi(true);
    config.setBlackAi(true);

    ConfigSerializer serializer = new ConfigSerializer();
    serializer.save(config, configFile.toString());

    assertTrue(Files.exists(configFile));
    String content = Files.readString(configFile);

    assertTrue(content.contains("timeout = 999"));
    assertTrue(content.contains("blitz = true"));
    assertTrue(content.contains("ai_depth = 8"));
    assertTrue(content.contains("ai_color = ALL"));
  }
}
