package fr.univ.bordeaux.technical.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class responsible for cleaning raw text lines.
 *
 * <p>It removes inline comments starting with '#' and block comments enclosed in '{' and '}'. It
 * also trims whitespace and ignores empty lines.
 */
public final class TextScrubber {

  /** Character marking the start of a block comment. */
  private static final char BLOCK_START = '{';

  /** Character marking the end of a block comment. */
  private static final char BLOCK_END = '}';

  /** Character marking an inline comment. */
  private static final char INLINE_COMMENT = '#';

  private TextScrubber() {}

  /**
   * Cleans a list of raw string lines according to the game's file format specifications.
   *
   * @param rawLines The original lines read from the file.
   * @return A list of clean, meaningful lines ready for parsing.
   */
  public static List<String> clean(final List<String> rawLines) throws IOException {
    final List<String> cleanLines = new ArrayList<>();
    final StringBuilder lineBuilder = new StringBuilder();
    final ScrubberState state = new ScrubberState();
    int lineNumber = 0;

    for (final String line : rawLines) {
      lineNumber++;

      final String trimmed = processLine(line, lineNumber, state, lineBuilder).trim();

      if (!trimmed.isEmpty()) {
        cleanLines.add(trimmed);
      }
    }

    if (state.isInsideBlock) {
      final String errorMsg = "Corrupted file : Block comment '{' was opened on line";
      throw new IOException(errorMsg + " " + state.errorLineNumber + " and was never closed.");
    }

    return cleanLines;
  }

  /** Processes a single line character by character, managing block comment states. */
  private static String processLine(
      final String line,
      final int lineNumber,
      final ScrubberState state,
      final StringBuilder lineBuilder) {

    lineBuilder.setLength(0);

    for (int i = 0; i < line.length(); i++) {
      final char currentChar = line.charAt(i);

      if (state.isInsideBlock) {
        if (currentChar == BLOCK_END) {
          state.isInsideBlock = false;
        }
      } else {
        if (currentChar == BLOCK_START) {
          state.isInsideBlock = true;
          state.errorLineNumber = lineNumber;
        } else if (currentChar == INLINE_COMMENT) {
          break;
        } else {
          lineBuilder.append(currentChar);
        }
      }
    }

    return lineBuilder.toString();
  }

  /** Mutable state object to track multi-line block comments across iterations. */
  private static final class ScrubberState {
    /** Indicates if the parser is currently inside a block comment. */
    boolean isInsideBlock;

    /** Records the line number where the block comment started to report errors. */
    int errorLineNumber = -1;
  }
}
