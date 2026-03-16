package fr.univ.bordeaux.technical.utils;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link SaveParser} class.
 * Ensures the parser correctly reads valid files, handles missing sections,
 * and appropriately ignores single-line and multi-line comments.
 */
public class SaveParserTest {

    /**
     * A temporary directory managed by JUnit.
     * Created before tests and deleted automatically after execution.
     */
    @TempDir
    Path tempDir;

    /**
     * Tests the constructor of {@link SaveParser} to ensure the instance
     * is correctly initialized.
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
     * * @throws IOException If file writing or parsing fails.
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

        SaveParser parser = new SaveParser();
        SaveParser.GameState state = parser.parse(file.toString());

        assertNotNull(state.board(), "Board should be instantiated");
        assertEquals(Color.BLACK, state.currentPlayer(), "Player should be BLACK based on 'X'");
    }

    /**
     * Ensures that comments within the save file (both curly braces and hashtags)
     * are correctly ignored and do not interfere with the parsing logic.
     * * @throws IOException If file writing or parsing fails.
     */
    @Test
    public void testCommentsRemoval() throws IOException {
        String content = "{ This is a comment }\n" +
                "[game] # Another comment\n" +
                "O\n" +
                ".....";

        Path file = tempDir.resolve("test_comments.txt");
        Files.writeString(file, content);

        SaveParser parser = new SaveParser();
        SaveParser.GameState state = parser.parse(file.toString());
        assertEquals(Color.WHITE, state.currentPlayer());
    }

    /**
     * Validates that an {@link IllegalArgumentException} is thrown when
     * the mandatory '[game]' section is missing from the file.
     * * @throws IOException If file writing fails.
     */
    @Test
    public void testMissingGameSection() throws IOException {
        String content = "Invalid content without game tag";
        Path file = tempDir.resolve("invalid.txt");
        Files.writeString(file, content);

        SaveParser parser = new SaveParser();
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse(file.toString());
        }, "Should throw exception if [game] section is missing");
    }

    /**
     * Tests variations in the [game] section, such as the absence of a history
     * section and case-insensitivity for player characters.
     * * @throws IOException If file writing or parsing fails.
     */
    @Test
    public void testGameSectionParsingVariations() throws IOException {
        String contentNoHistory = "[game]\nX\n" + ".".repeat(126);
        Path file1 = tempDir.resolve("no_history.txt");
        Files.writeString(file1, contentNoHistory);

        SaveParser parser = new SaveParser();
        assertDoesNotThrow(() -> parser.parse(file1.toString()),
                "The parser should handle files without a [history] section.");

        String contentSmallX = "[game]\nx\n" + ".".repeat(126);
        Path file2 = tempDir.resolve("small_x.txt");
        Files.writeString(file2, contentSmallX);

        SaveParser.GameState state = parser.parse(file2.toString());
        assertEquals(Color.BLACK, state.currentPlayer(), "Lowercase 'x' should be recognized as BLACK.");
    }

    /**
     * Verifies code coverage for the piece-type switch statement.
     * Ensures that all valid character representations (q, X, Q, O)
     * are correctly mapped to their respective pieces on the board.
     * * @throws IOException If file writing or parsing fails.
     */
    @Test
    public void testPieceSwitchCoverage() throws IOException {
        String contentAllPieces = "[game]\nO\nqXQO" + ".".repeat(122);
        Path file = tempDir.resolve("all_pieces.txt");
        Files.writeString(file, contentAllPieces);

        SaveParser parser = new SaveParser();
        SaveParser.GameState state = parser.parse(file.toString());

        assertFalse(state.board().getBlackQueen().isEmpty(), "Character 'q' should be correctly mapped to a Black Queen.");
        assertFalse(state.board().getBlackPawns().isEmpty(), "Character 'X' should be correctly mapped to Black Pawns.");
        assertFalse(state.board().getWhiteQueen().isEmpty(), "Character 'Q' should be correctly mapped to a White Queen.");
        assertFalse(state.board().getWhitePawns().isEmpty(), "Character 'O' should be correctly mapped to White Pawns.");
    }

    /**
     * Tests the parsing logic when the [history] section is completely missing.
     * Verifies the parser stops collecting data correctly or doesn't crash when it hits EOF.
     * * @throws IOException If file writing or parsing fails.
     */
    @Test
    public void testLoadGameWithoutHistorySection() throws IOException {
        String content = "[game]\nO\n" + ".".repeat(126);
        Path file = tempDir.resolve("no_history_test.txt");
        Files.writeString(file, content);

        SaveParser parser = new SaveParser();
        SaveParser.GameState state = parser.parse(file.toString());

        assertNotNull(state.board(), "The board should be loaded even without a history section.");
        assertEquals(Color.WHITE, state.currentPlayer(), "The current player should be parsed as WHITE.");
    }

    /**
     * Tests the parsing logic when the [history] section appears BEFORE the [game] section.
     * Verifies the parser correctly ignores the [history] section and still captures the [game] section.
     * * @throws IOException If file writing or parsing fails.
     */
    @Test
    public void testLoadGameHistoryBeforeGame() throws IOException {
        String content = "[history]\nmove F6-E6\n[game]\nO\n" + ".".repeat(126);
        Path file = tempDir.resolve("history_before_game.txt");
        Files.writeString(file, content);

        SaveParser parser = new SaveParser();
        SaveParser.GameState state = parser.parse(file.toString());

        assertNotNull(state.board(), "The board should load correctly even if [history] is placed before [game].");
        assertEquals(Color.WHITE, state.currentPlayer(), "The player should be parsed correctly.");
    }
}