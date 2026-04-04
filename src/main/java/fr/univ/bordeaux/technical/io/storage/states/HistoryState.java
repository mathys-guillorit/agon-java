package fr.univ.bordeaux.technical.io.storage.states;

import fr.univ.bordeaux.technical.io.storage.GameSaveBuilder;
import java.io.IOException;

/** Parses the move history section, extracting individual moves separated by semicolons. */
public class HistoryState implements SaveParserState {

  @Override
  public void parseLine(String line, GameSaveBuilder builder) throws IOException {
    String[] rawMoves = line.split(";");

    for (String move : rawMoves) {
      String cleanMove = move.trim();
      if (!cleanMove.isEmpty()) {
        builder.addHistoryMove(cleanMove);
      }
    }
  }
}
