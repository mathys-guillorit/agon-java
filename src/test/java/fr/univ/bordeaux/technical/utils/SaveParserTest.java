package fr.univ.bordeaux.technical.utils;

import static org.junit.jupiter.api.Assertions.*;
import fr.univ.bordeaux.agonCore.agonElements.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for the {@link SaveParser} class.
 */
public class SaveParserTest {

    @TempDir
    Path tempDir;

    /**
     * Tests the default constructor of SaveParser to achieve 100% line coverage.
     * Since it is a utility class with only static methods, the implicit default
     * constructor is never called in the production code.
     */
    @Test
    public void testSaveParserConstructor() {
        SaveParser parser = new SaveParser();
        assertNotNull(parser, "SaveParser instance should be successfully created.");
    }

    /**
     * Verifies that a standard, valid save file is correctly parsed.
     * Checks if the board is properly instantiated and the current player
     * color is accurately identified.
     */
    @Test
    public void testLoadValidGame() throws IOException {
        String content = "[game]\n" +
                "X\n" +
                "Q....\n" +
                ".....\n" +
                "[history]\n" +
                "move a1-b2";

        Path file = tempDir.resolve("test_save.txt");
        Files.writeString(file, content);

        SaveParser.GameState state = SaveParser.loadGame(file.toString());

        assertNotNull(state.board(), "Board should be instantiated");
        assertEquals(Color.BLACK, state.currentPlayer(), "Player should be BLACK based on 'X'");
    }

    /**
     * Ensures that comments within the save file (both curly braces and hashtags)
     * are correctly ignored and do not interfere with the parsing logic.
     */
    @Test
    public void testCommentsRemoval() throws IOException {
        String content = "{ This is a comment }\n" +
                "[game] # Another comment\n" +
                "O\n" +
                ".....";

        Path file = tempDir.resolve("test_comments.txt");
        Files.writeString(file, content);

        SaveParser.GameState state = SaveParser.loadGame(file.toString());
        assertEquals(Color.WHITE, state.currentPlayer());
    }

    /**
     * Validates that an {@link IllegalArgumentException} is thrown when
     * the mandatory '[game]' section is missing from the file.
     */
    @Test
    public void testMissingGameSection() throws IOException {
        String content = "Invalid content without game tag";
        Path file = tempDir.resolve("invalid.txt");
        Files.writeString(file, content);

        assertThrows(IllegalArgumentException.class, () -> {
            SaveParser.loadGame(file.toString());
        }, "Should throw exception if [game] section is missing");
    }

    /**
     * Tests variations in the [game] section, such as the absence of a history
     * section and case-insensitivity for player characters.
     */
    @Test
    public void testGameSectionParsingVariations() throws IOException {
        String contentNoHistory = "[game]\nX\n" + ".".repeat(126);
        Path file1 = tempDir.resolve("no_history.txt");
        Files.writeString(file1, contentNoHistory);
        assertDoesNotThrow(() -> SaveParser.loadGame(file1.toString()),
                "The parser should handle files without a [history] section.");
        String contentSmallX = "[game]\nx\n" + ".".repeat(126);
        Path file2 = tempDir.resolve("small_x.txt");
        Files.writeString(file2, contentSmallX);
        SaveParser.GameState state = SaveParser.loadGame(file2.toString());
        assertEquals(Color.BLACK, state.currentPlayer(), "Lowercase 'x' should be recognized as BLACK.");
    }

    /**
     * Verifies code coverage for the piece-type switch statement.
     * Ensures that all valid character representations (q, X, Q, O)
     * are correctly mapped to their respective pieces on the board.
     */
    @Test
    public void testPieceSwitchCoverage() throws IOException {
        String contentAllPieces = "[game]\nO\nqXQO" + ".".repeat(122);
        Path file = tempDir.resolve("all_pieces.txt");
        Files.writeString(file, contentAllPieces);
        SaveParser.GameState state = SaveParser.loadGame(file.toString());
        assertFalse(state.board().getBlackQueen().isEmpty(), "Character 'q' should be correctly mapped to a Black Queen.");
        assertFalse(state.board().getBlackPawns().isEmpty(), "Character 'X' should be correctly mapped to Black Pawns.");
        assertFalse(state.board().getWhiteQueen().isEmpty(), "Character 'Q' should be correctly mapped to a White Queen.");
        assertFalse(state.board().getWhitePawns().isEmpty(), "Character 'O' should be correctly mapped to White Pawns.");
    }

    /**
     * Tests the parsing logic when the [history] section is completely missing.
     * This specifically covers the FALSE branch of the ternary operator evaluating historyIdx.
     */
    @Test
    public void testLoadGameWithoutHistorySection() throws IOException {
        String content = "[game]\nO\n" + ".".repeat(126);
        Path file = tempDir.resolve("no_history_test.txt");
        Files.writeString(file, content);
        SaveParser.GameState state = SaveParser.loadGame(file.toString());
        assertNotNull(state.board(), "The board should be loaded even without a history section.");
        assertEquals(Color.WHITE, state.currentPlayer(), "The current player should be parsed as WHITE.");
    }

    /**
     * Tests the parsing logic when the [history] section appears BEFORE the [game] section.
     * This specifically covers the 'historyIdx > gameIdx' condition evaluating to FALSE
     * in the ternary operator.
     */
    @Test
    public void testLoadGameHistoryBeforeGame() throws IOException {
        String content = "[history]\nmove F6-E6\n[game]\nO\n" + ".".repeat(126);
        Path file = tempDir.resolve("history_before_game.txt");
        Files.writeString(file, content);
        SaveParser.GameState state = SaveParser.loadGame(file.toString());
        assertNotNull(state.board(), "The board should load correctly even if [history] is placed before [game].");
        assertEquals(Color.WHITE, state.currentPlayer(), "The player should be parsed correctly.");
    }
}