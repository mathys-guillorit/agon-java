package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContestMatchTest {

    @TempDir
    Path tempDir;

    /**
     * Tests the default constructor of ContestMatch to ensure 100% line coverage.
     * Similar to SaveParser, this class acts as a static utility and its
     * default constructor requires an explicit call in tests.
     */
    @Test
    public void testContestMatchConstructor() {
        ContestMatch match = new ContestMatch();
        assertNotNull(match, "ContestMatch instance should be successfully created.");
    }

    /**
     * Verifies that the contest mode correctly executes an integration flow.
     * It simulates a valid game file, checks that the AI successfully
     * produces a move, and validates the output format.
     */
    @Test
    public void testExecuteContestIntegration() throws Exception {
        String content = "[game]\nO\nQ" + ".".repeat(125);
        Path file = tempDir.resolve("contest_test.txt");
        Files.writeString(file, content);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            ContestMatch.executeContest(file.toString());
            String output = outContent.toString().trim();
            assertFalse(output.isEmpty(), "The AI should have returned a move.");
            assertTrue(output.length() >= 5, "Invalid move format.");
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Tests the scenario where the AI is unable to find a valid move.
     * Ensures that the system correctly logs a warning or error message
     * to the standard error stream when the game state provides no options.
     */
    @Test
    public void testExecuteContestNoMoveBranch() throws Exception {
        Path emptyFile = tempDir.resolve("no_move.txt");
        Files.writeString(emptyFile, "[game]\nO\n" + ".".repeat(126));
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        ContestMatch.executeContest(emptyFile.toString());
        assertTrue(errContent.toString().contains("The AI could not find any valid move."));
    }
}