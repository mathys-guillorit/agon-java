package fr.univ.bordeaux.technical.io.storage.states;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.io.storage.GameSaveBuilder;
import java.io.IOException;

/** Parses the game state section, extracting the current player and the board layout. */
public class GameState implements SaveParserState {

  @Override
  public void parseLine(String line, GameSaveBuilder builder) throws IOException {
    if (line.length() == 1) {
      if (line.equalsIgnoreCase("X")) {
        builder.setCurrentPlayer(Color.BLACK);
      } else if (line.equalsIgnoreCase("O")) {
        builder.setCurrentPlayer(Color.WHITE);
      } else {
        throw new IOException("Unknown player character: " + line);
      }
    } else {
      builder.addBoardLine(line);
    }
  }
}
