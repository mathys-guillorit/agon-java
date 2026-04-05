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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ContestMatchTest {

  @TempDir Path tempDir;

  @Test
  public void testContestMatchConstructor() {
    ContestMatch match = new ContestMatch();
    assertNotNull(match, "ContestMatch instance should be successfully created.");
  }

  @Test
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
    content.append("[settings]\n");
    content.append("timeout = 300\n");
    content.append("[game]\n");
    content.append("O\n");
    for (String line : boardLines) {
      content.append(line).append("\n");
    }
    content.append("[history]\n");
    content.append("X c3 c5;\n");

    Path file = tempDir.resolve("contest_test.asv");
    Files.writeString(file, content.toString());

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

  /*@Test
  public void testExecuteContestNoMoveBranch() throws Exception {
    Path emptyFile = tempDir.resolve("no_move.asv");

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
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    PrintStream originalOut = System.out;
    System.setErr(new PrintStream(errContent));
    System.setOut(new PrintStream(outContent));

    try {
      ContestMatch.executeContest(emptyFile.toString());
      String errorOutput = errContent.toString();
      String stdOutput = outContent.toString().trim();
      assertTrue(
          errorOutput.contains("The AI could not find any valid move."),
          "Expected AI to fail to find a move. Instead it returned: '" + stdOutput + "'");
    } finally {
      System.setErr(originalErr);
      System.setOut(originalOut);
    }
  }*/

  /**
   * Tests the scenario where the save file is corrupted or invalid, causing the parser to return
   * null and throwing an Exception.
   */
  @Test
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
