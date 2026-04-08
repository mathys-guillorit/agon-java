package fr.univ.bordeaux.technical.io.storage.states;

import fr.univ.bordeaux.technical.io.storage.GameSaveBuilder;
import java.io.IOException;

/** Parses the move history section, extracting individual moves separated by semicolons. */
public class HistoryState implements SaveParserState {

  @Override
  public void parseLine(final String line, final GameSaveBuilder builder) throws IOException {
    final String[] rawMoves = line.split(";");

    for (final String move : rawMoves) {
      final String cleanMove = move.trim();
      if (!cleanMove.isEmpty()) {
        builder.addHistoryMove(cleanMove);
      }
    }
  }
}
