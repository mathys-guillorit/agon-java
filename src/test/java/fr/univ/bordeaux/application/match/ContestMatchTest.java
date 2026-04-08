package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ContestMatchTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("Verify ContestMatch constructor")
  public void testContestMatchConstructor() {
    ContestMatch match = new ContestMatch();
    assertNotNull(match, "ContestMatch instance should be successfully created.");
  }

  @Test
  @DisplayName("Verify successful move calculation integration")
  public void testExecuteContestIntegration() throws Exception {
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

    StringBuilder content = new StringBuilder();
    content.append("[settings]\ntimeout = 300\n[game]\nO\n");
    for (String line : boardLines) {
      content.append(line).append("\n");
    }
    content.append("[history]\nX c3 c5;\n");

    Path file = tempDir.resolve("contest_test.asv");
    Files.writeString(file, content.toString());

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      ContestMatch.executeContest(file.toString());
      String output = outContent.toString().trim();
      assertFalse(output.isEmpty(), "The AI should have returned a move.");
      boolean isValidFormat = output.matches("^[OX] ([a-z][0-9]{1,2} [a-z][0-9]{1,2}|reloc [A-G][0-9]{1,2})$");

      assertTrue(isValidFormat, "Invalid move format received: " + output);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  @DisplayName("Verify behavior when no moves are possible")
  public void testExecuteContestNoMoveBranch() throws Exception {
    Path emptyFile = tempDir.resolve("no_move.asv");

    // A completely blocked board where no moves can be made
    List<String> blockedBoard =
        Arrays.asList(
            "      O X O X O X",
            "     X O X O X O X",
            "    O X O X O X O X",
            "   X O X O X O X O X",
            "  O X O X O X O X O X",
            " X O X O X Q O X O X O",
            "  O X O X O X O X O X",
            "   X O X O X O X O X",
            "    O X O X O X O X",
            "     X O X O X O X",
            "      O X O X O X");

    StringBuilder sb = new StringBuilder();
    sb.append("[settings]\n[game]\nX\n");
    for (String line : blockedBoard) {
      sb.append(line).append("\n");
    }
    sb.append("[history]\n");
    Files.writeString(emptyFile, sb.toString());

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      ContestMatch.executeContest(emptyFile.toString());
      String stdOutput = outContent.toString().trim();
      // If the AI finds no move, stdout should be empty as the error goes to Logger/Stderr
      assertTrue(stdOutput.isEmpty(), "Output should be empty when no move is found.");
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  @DisplayName("Verify failure when parsing a corrupted file")
  public void testExecuteContestParseFailure() throws Exception {
    Path corruptFile = tempDir.resolve("corrupted.asv");
    Files.writeString(corruptFile, "[unknown_section]\nrandom nonsense\n");

    Exception exception =
        assertThrows(
            Exception.class,
            () -> ContestMatch.executeContest(corruptFile.toString()),
            "Should throw an Exception when parser returns null.");
    assertEquals("Failed to parse save data.", exception.getMessage());
  }
}