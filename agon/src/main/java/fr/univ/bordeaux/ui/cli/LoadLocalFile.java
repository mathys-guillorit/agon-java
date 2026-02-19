package fr.univ.bordeaux.ui.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * use the load local files (example: cli required to load local Menu for display into cli it's load
 * by this class) - UTF-8 only (ASCII)
 */
public class LoadLocalFile {

  private String content;

  public LoadLocalFile(String filepath) throws IOException, NullPointerException {
    // DP Command here
    final String defaultTxt = "";
    final String resourcePath = "/" + filepath;
    InputStream stream = getClass().getResourceAsStream(resourcePath);
    if (stream == null) this.content = defaultTxt;
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    var lines = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      lines.append(line).append("\n");
    }
    this.content = lines.toString();
  }

  public String getContent() {
    return content;
  }
}
