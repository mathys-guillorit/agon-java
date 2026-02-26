package fr.univ.bordeaux.ui.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * use the load local files (example: cli required to load local Menu for display into cli it's load
 * by this class) - UTF-8 only (ASCII)
 *
 * @apiNote file format transformation must not be here
 */
public class LoadLocalFile {

  private String content;

  /**
   * Load a local file in "resources" directory
   *
   * @param filepath path from resource directory where to find a file
   * @throws IOException Exception if file not present or invalid path or other problems
   * @throws NullPointerException Exception if file not present
   */
  public LoadLocalFile(String filepath) throws IOException, NullPointerException {
    // DP Command here
    final String defaultTxt = "";
    InputStream stream = getClass().getResourceAsStream(filepath);
    if (stream == null) throw new IOException("resource not found: " + filepath);
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      var lines = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) lines.append(line).append("\n");
      this.content = lines.toString();
    }
  }

  public String getContent() {
    return content;
  }
}
