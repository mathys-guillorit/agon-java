package fr.univ.bordeaux.technical.io;

import java.io.IOException;

/**
 * A generic interface for parsing files into objects.
 *
 * @param <T> The type of object to be created from the parsed file.
 */
public interface Parser<T> {

  /**
   * Parses the specified file and constructs an object of type T.
   *
   * @param filePath The path to the file to parse.
   * @return The constructed object containing the parsed data.
   * @throws IOException If an error occurs while reading the file.
   */
  T parse(String filePath) throws IOException;
}
