package fr.univ.bordeaux.technical.io.storage;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.io.storage.states.GameState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration test for the Save/Load system. Validates GameSaveSerializer, GameSaveParser, and
 * TextScrubber simultaneously.
 */
class GameSaveIntegrationTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("Should successfully serialize and parse a complete game save file")
  void testSaveAndLoadIntegration() throws IOException {
    GameConfig originalConfig = new GameConfig();
    originalConfig.setAiMode("MCTS");
    originalConfig.setTimeout(300);

    Color currentPlayer = Color.BLACK;

    List<String> boardLines =
        Arrays.asList(
            "     . . . . .",
            "    . . . . . .",
            "   . . . . . . .",
            "  . . . . . . . .",
            " . . . . . . . . .",
            ". . . . Q q . . . .",
            " . . . . . . . . .",
            "  . . . . . . . .",
            "   . . X . O . .",
            "    . . . . . .",
            "     . . . . .");

    List<String> historyMoves = Arrays.asList("O a1 a2", "X c3 c5", "O e6 f5");

    GameSaveData originalSaveData =
        new GameSaveData(originalConfig, currentPlayer, boardLines, historyMoves);

    Path saveFile = tempDir.resolve("test_save.asv");
    GameSaveSerializer serializer = new GameSaveSerializer();
    serializer.save(originalSaveData, saveFile.toString());

    assertTrue(Files.exists(saveFile), "The save file should be created on the disk");

    GameSaveParser parser = new GameSaveParser();
    GameSaveData loadedSaveData = parser.parse(saveFile.toString());

    assertNotNull(loadedSaveData, "The loaded save data should not be null");

    assertEquals("MCTS", loadedSaveData.getConfig().getAiMode(), "Ai Mode should match");
    assertEquals(300, loadedSaveData.getConfig().getTimeout(), "Timeout should match");

    assertEquals(Color.BLACK, loadedSaveData.getCurrentPlayer(), "Current player should be Black");

    List<String> loadedBoard = loadedSaveData.getBoardLines();
    assertEquals(11, loadedBoard.size(), "Board should have exactly 11 lines");
    assertEquals(
        ". . . . Q q . . . .", loadedBoard.get(5), "Specific board line should match exactly");

    List<String> loadedHistory = loadedSaveData.getHistoryMoves();
    assertEquals(
        historyMoves.size(), loadedHistory.size(), "History should have the same number of moves");
    assertEquals("X c3 c5", loadedHistory.get(1), "Specific move should match exactly");
  }

  @Test
  @DisplayName("TextScrubber should successfully ignore comments and empty lines")
  void testTextScrubberIntegrity() throws IOException {
    Path messyFile = tempDir.resolve("messy_save.asv");
    String messyContent =
        "# This is a comment\n"
            + "\n"
            + "{ Block\n"
            + "  comment }\n"
            + "[settings]\n"
            + "timeout = 120\n"
            + "[game]\n"
            + "X # Black to play\n"
            + "  . X o .  { ignore this too }\n"
            + "[history]\n"
            + "O a1 a2; X c3 c5;\n";

    Files.writeString(messyFile, messyContent);

    GameSaveParser parser = new GameSaveParser();
    GameSaveData data = parser.parse(messyFile.toString());

    assertNotNull(data, "Parser should survive a messy file");
    assertEquals(120, data.getConfig().getTimeout(), "Config should be parsed despite comments");
    assertEquals(
        Color.BLACK, data.getCurrentPlayer(), "Player should be parsed ignoring inline comment");
    assertEquals(
        ". X o .", data.getBoardLines().get(0), "Board line should be stripped of block comment");
  }

  @Test
  @DisplayName("Should handle missing data and throw appropriate exceptions in Builder")
  void testBuilderExceptions() {
    GameSaveBuilder builder = new GameSaveBuilder();

    builder.setConfig(new GameConfig());

    builder.addBoardLine(". . .");
    assertThrows(IOException.class, builder::build, "Should throw if player is missing");

    GameSaveBuilder builder2 = new GameSaveBuilder();
    builder2.setCurrentPlayer(Color.WHITE);
    assertThrows(IOException.class, builder2::build, "Should throw if board is missing");
  }

  @Test
  @DisplayName("Should trigger parser exceptions with corrupted headers")
  void testParserExceptions() throws IOException {
    Path badHeaderFile = tempDir.resolve("bad_header.asv");
    Files.writeString(badHeaderFile, "[unknown_section]\nfoo = bar");

    GameSaveParser parser = new GameSaveParser();

    GameSaveData data = parser.parse(badHeaderFile.toString());
    assertNull(data, "Parser should return null if the file structure is fundamentally broken");
  }

  @Test
  @DisplayName("Should serialize White player turn and edge case Ai configurations")
  void testSerializerEdgeCases() throws IOException {
    GameConfig config = new GameConfig();
    config.setWhiteAi(true);
    config.setBlackAi(true);

    GameSaveData data = new GameSaveData(config, Color.WHITE, List.of(". X o ."), List.of());

    Path edgeCaseFile = tempDir.resolve("edge_case.asv");
    GameSaveSerializer serializer = new GameSaveSerializer();
    serializer.save(data, edgeCaseFile.toString());

    String content = Files.readString(edgeCaseFile);
    assertTrue(content.contains("O\n"), "Should write 'O' for White player");
    assertTrue(content.contains("ai_color = ALL\n"), "Should write 'ALL' for Ai color");

    config.setBlackAi(false);
    serializer.save(data, edgeCaseFile.toString());
    assertTrue(Files.readString(edgeCaseFile).contains("ai_color = WHITE\n"));
  }

  @Test
  @DisplayName("Should cover builder edge cases and exceptions")
  void testBuilderEdgeCases() {
    GameSaveBuilder builder = new GameSaveBuilder();
    builder.setConfig(new GameConfig());

    builder.addBoardLine(". . .");
    assertThrows(IOException.class, builder::build, "Should throw if player is missing");

    GameSaveBuilder builder2 = new GameSaveBuilder();
    builder2.setCurrentPlayer(Color.WHITE);
    assertThrows(IOException.class, builder2::build, "Should throw if board is missing");
  }

  @Test
  @DisplayName("Should serialize White player, Ai variations, and parse empty moves")
  void testSerializerAndStateEdgeCases() throws IOException {
    GameConfig config = new GameConfig();
    config.setWhiteAi(true);
    config.setBlackAi(true);

    GameSaveData data =
        new GameSaveData(config, Color.WHITE, List.of(". X o ."), List.of("O a1 a2"));

    Path edgeCaseFile = tempDir.resolve("edge_case.asv");
    GameSaveSerializer serializer = new GameSaveSerializer();
    serializer.save(data, edgeCaseFile.toString());

    String content = Files.readString(edgeCaseFile);
    assertTrue(content.contains("O\n"), "Should write 'O' for White player");
    assertTrue(content.contains("ai_color = ALL\n"), "Should write 'ALL' for Ai color");
    assertTrue(content.endsWith("O a1 a2;\n"), "Should correctly terminate an odd number of moves");

    config.setBlackAi(false);
    serializer.save(data, edgeCaseFile.toString());
    assertTrue(Files.readString(edgeCaseFile).contains("ai_color = WHITE\n"));

    Path emptyMoveFile = tempDir.resolve("empty_move.asv");
    Files.writeString(emptyMoveFile, "[settings]\n[game]\nO\n. .\n[history]\nO a1 a2;  ; X c3 c5;\n");
    GameSaveParser parser = new GameSaveParser();
    GameSaveData loadedData = parser.parse(emptyMoveFile.toString());

    assertEquals(2, loadedData.getHistoryMoves().size(), "Should silently ignore empty moves");
  }

  @Test
  @DisplayName("GameState should throw IOException for invalid player character")
  void testGameStateInvalidPlayerCharacter() {
    GameState gameState = new GameState();
    GameSaveBuilder builder = new GameSaveBuilder();

    assertThrows(
        IOException.class,
        () -> {
          gameState.parseLine("Z", builder);
        },
        "Should throw an IOException for unknown player character like 'Z'");
  }

  @Test
  @DisplayName("Should reject save file where an unclosed comment swallows mandatory sections")
  void testUnclosedBlockCommentSwallowsSection() throws IOException {
    Path corruptFile = tempDir.resolve("corrupt_save.asv");

    String corruptContent =
            "[settings]\n"
                    + "timeout = 120\n"
                    + "[game]\n"
                    + "X\n"
                    + ". X o .\n"
                    + "{ Oops, I forgot to close this comment\n"
                    + "[history]\n"
                    + "O a1 a2;\n";

    Files.writeString(corruptFile, corruptContent);

    GameSaveParser parser = new GameSaveParser();
    GameSaveData data = parser.parse(corruptFile.toString());

    assertNull(data, "Parser should return null because the [history] section is missing/swallowed");
  }

  @Test
  @DisplayName("Builder should enforce the presence of all section flags")
  void testBuilderMissingSectionFlags() {
    GameSaveBuilder builder = new GameSaveBuilder();

    builder.markGameSection();
    builder.markHistorySection();

    builder.setCurrentPlayer(Color.WHITE);
    builder.addBoardLine(". . .");

    Exception exception = assertThrows(
            IOException.class,
            builder::build,
            "Builder should throw an exception if [settings] section flag is false"
    );

    assertTrue(
            exception.getMessage().contains("Settings"),
            "The error message should explicitly mention the missing [settings] section"
    );
  }
}
