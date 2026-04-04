package fr.univ.bordeaux.technical.io.storage.states;

import fr.univ.bordeaux.technical.io.storage.GameSaveBuilder;
import java.io.IOException;

/** Represents a specific state in the save file parsing process. */
public interface SaveParserState {

  /**
   * Parses a single clean line and feeds the extracted data into the builder.
   *
   * @param line The clean line from the save file.
   * @param builder The builder accumulating the game save data.
   * @throws IOException If the line format is invalid for this specific state.
   */
  void parseLine(String line, GameSaveBuilder builder) throws IOException;
}
