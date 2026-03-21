package fr.univ.bordeaux.technical.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class responsible for loading the content of local resource files. This class is
 * primarily used to read text files embedded within the application's resources (for example, a CLI
 * menu layout to be displayed in the terminal). It reads the file strictly using UTF-8 encoding. If
 * the specified file cannot be found, the content automatically defaults to an empty string to
 * prevent crashes.
 */
public class LoadLocalFile {

  private final String content;

  /**
   * Constructs a new LoadLocalFile instance and immediately attempts to load the content of the
   * specified resource file.
   *
   * @param filepath The relative path to the resource file to be loaded (e.g., "cli_menu.txt").
   * @throws IOException If an I/O error occurs while reading the file stream.
   * @throws NullPointerException If an unexpected null reference is encountered during processing.
   */
  public LoadLocalFile(String filepath) throws IOException, NullPointerException {
    // DP Command here
    final String defaultTxt = "";
    final String resourcePath = "/" + filepath;
    InputStream stream = getClass().getResourceAsStream(resourcePath);
    if (stream == null) {
      this.content = defaultTxt;
    } else {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      var lines = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        lines.append(line).append("\n");
      }
      this.content = lines.toString();
    }
  }

  /**
   * Retrieves the text content that was successfully loaded from the file.
   *
   * @return A string containing the entire text of the file, or an empty string if the file was not
   *     found.
   */
  public String getContent() {
    return content;
  }
}
