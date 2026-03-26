package fr.univ.bordeaux.technical.io.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Test suite for the {@link ConfigParser} class. */
class ConfigParserTest {

  @TempDir Path tempDir;

  @Test
  void testParseValidFile() throws IOException {
    Path configFile = tempDir.resolve("valid.agonrc");
    String content =
        "verbose = true\n"
            + "timeout = 600\n"
            + "ai_mode = MCTS\n"
            + "ai_color = BLACK\n"
            + "ai_depth = 3\n"
            + "ai_time_limit = 5\n"
            + "ai_iterative_deepening = true\n"
            + "ai_heuristic = centrality\n"
            + "placement = false";
    Files.writeString(configFile, content);

    ConfigParser parser = new ConfigParser();
    GameConfig config = parser.parse(configFile.toString());

    assertTrue(config.isVerbose());
    assertEquals(600, config.getTimeout());
    assertEquals("MCTS", config.getAiMode());
    assertFalse(config.isManualPlacement());
    assertFalse(config.isWhiteAi());
    assertTrue(config.isBlackAi());
  }

  @Test
  void testParseWithCommentsAndSections() throws IOException {
    Path configFile = tempDir.resolve("messy.agonrc");
    String content =
        "# This is a comment\n"
            + "\n"
            + "[system]\n"
            + "  debug   =   true  \n"
            + "[game]\n"
            + "# another comment\n"
            + "blitz = true\n";
    Files.writeString(configFile, content);

    ConfigParser parser = new ConfigParser();
    GameConfig config = parser.parse(configFile.toString());

    assertTrue(config.isDebug());
    assertTrue(config.isBlitzMode());
  }

  @Test
  void testParseFileNotFoundThrowsException() {
    ConfigParser parser = new ConfigParser();
    String nonExistentPath = tempDir.resolve("ghost.agonrc").toString();

    assertThrows(
        IOException.class,
        () -> {
          parser.parse(nonExistentPath);
        });
  }

  @Test
  void testParseMalformedLineThrowsException() throws IOException {
    Path configFile = tempDir.resolve("malformed.agonrc");
    Files.writeString(configFile, "timeout 300\n");

    ConfigParser parser = new ConfigParser();

    assertThrows(
        IOException.class,
        () -> {
          parser.parse(configFile.toString());
        });
  }

  @Test
  void testParseInvalidValueThrowsException() throws IOException {
    Path configFile = tempDir.resolve("invalid_value.agonrc");
    Files.writeString(configFile, "timeout = abc\n");

    ConfigParser parser = new ConfigParser();

    assertThrows(
        IOException.class,
        () -> {
          parser.parse(configFile.toString());
        });
  }

  @Test
  void testParseUnknownKeyIgnored() throws IOException {
    Path configFile = tempDir.resolve("unknown_key.agonrc");
    String content = "timeout = 120\n" + "unknown_setting = 42\n" + "blitz = true\n";
    Files.writeString(configFile, content);

    ConfigParser parser = new ConfigParser();

    assertThrows(
        IOException.class,
        () -> {
          parser.parse(configFile.toString());
        });
  }

  @Test
  void testParseAiActiveWithNoColor() throws IOException {
    Path configFile = tempDir.resolve("ai_active.agonrc");
    String content = "ai = true\n" + "ai_color = NONE\n";
    Files.writeString(configFile, content);

    ConfigParser parser = new ConfigParser();

    assertThrows(
        IOException.class,
        () -> {
          parser.parse(configFile.toString());
        });
  }

  @Test
  void testParseAiActiveWithInvalidColor() throws IOException {
    Path configFile = tempDir.resolve("ai_active.agonrc");
    String content = "ai = true\n" + "ai_color = invalid\n";
    Files.writeString(configFile, content);

    ConfigParser parser = new ConfigParser();

    assertThrows(
        IOException.class,
        () -> {
          parser.parse(configFile.toString());
        });
  }

  @Test
  void testParseAiColorVariations() throws IOException {
    Path configFile = tempDir.resolve("colors.agonrc");
    ConfigParser parser = new ConfigParser();

    Files.writeString(configFile, "ai_color = ALL\n");
    GameConfig configAll = parser.parse(configFile.toString());
    assertTrue(configAll.isWhiteAi());
    assertTrue(configAll.isBlackAi());

    Files.writeString(configFile, "ai_color = WHITE\n");
    GameConfig configWhite = parser.parse(configFile.toString());
    assertTrue(configWhite.isWhiteAi());
    assertFalse(configWhite.isBlackAi());

    String content = "ai = false\n" + "ai_color = NONE\n";
    Files.writeString(configFile, content);
    GameConfig configNone = parser.parse(configFile.toString());
    assertFalse(configNone.isWhiteAi());
    assertFalse(configNone.isBlackAi());
  }
}
