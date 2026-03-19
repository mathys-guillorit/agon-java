package fr.univ.bordeaux.technical.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * An abstract base class for file parsers using the <b>Template Method</b> design pattern.
 *
 * <p>This class handles the common boilerplate of opening a file, reading its lines, and stripping
 * out comments/whitespace via the {@link TextScrubber}. Subclasses only need to implement the
 * specific logic for interpreting the clean data.
 *
 * @param <T> The type of object to be returned by the parser.
 */
public abstract class AbstractFileParser<T> implements Parser<T> {

  /**
   * The template method that dictates the standard parsing workflow.
   *
   * @param filePath The path of the file to read.
   * @return The fully constructed object of type T.
   * @throws IOException If the file cannot be read.
   */
  @Override
  public final T parse(String filePath) throws IOException {

    Path path = Paths.get(filePath);

    if (!Files.exists(path)) {
      throw new IOException("Config file does not exist : " + filePath);
    }

    List<String> rawLines = Files.readAllLines(path);

    List<String> cleanLines = TextScrubber.clean(rawLines);

    return processCleanLines(cleanLines);
  }

  /**
   * Processes the cleaned lines to construct the target object.
   *
   * <p>Subclasses must implement this method to provide the specific mapping logic (e.g., mapping
   * "key=value" to a configuration object or reading a game board).
   *
   * @param cleanLines A list of strings guaranteed to be free of comments and empty lines.
   * @return The constructed object of type T.
   */
  protected abstract T processCleanLines(List<String> cleanLines) throws IOException;
}
