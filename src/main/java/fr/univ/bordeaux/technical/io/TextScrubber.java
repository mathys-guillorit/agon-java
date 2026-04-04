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
public class TextScrubber {

  private TextScrubber() {}

  /**
   * Cleans a list of raw string lines according to the game's file format specifications.
   *
   * @param rawLines The original lines read from the file.
   * @return A list of clean, meaningful lines ready for parsing.
   */
  public static List<String> clean(List<String> rawLines) throws IOException {
    List<String> cleanLines = new ArrayList<>();
    boolean inBlockComment = false;

    int lineNumber = 0;
    int errorLine = -1;

    for (String line : rawLines) {
      lineNumber++;
      StringBuilder cleanedLine = new StringBuilder();

      for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);

        if (!inBlockComment) {
          if (c == '{') {
            inBlockComment = true;
            errorLine = lineNumber;
          } else if (c == '#') {
            break;
          } else {
            cleanedLine.append(c);
          }
        } else {
          if (c == '}') {
            inBlockComment = false;
          }
        }
      }

      String trimmed = cleanedLine.toString().trim();
      if (!trimmed.isEmpty()) {
        cleanLines.add(trimmed);
      }
    }

    if (inBlockComment) {
      final String msgp = "Corrupted file : Block comment '{' was opened on line";
      throw new IOException(msgp + " " + errorLine + " and was never closed.");
    }

    return cleanLines;
  }
}
