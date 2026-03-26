package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.List;

/** A data container holding all the extracted information from a save file. */
public class GameSaveData {
  private final GameConfig config;
  private final Color currentPlayer;
  private final List<String> boardLines;
  private final List<String> historyMoves;

  public GameSaveData(
      GameConfig config, Color currentPlayer, List<String> boardLines, List<String> historyMoves) {
    this.config = config;
    this.currentPlayer = currentPlayer;
    this.boardLines = boardLines;
    this.historyMoves = historyMoves;
  }

  public GameConfig getConfig() {
    return config;
  }

  public Color getCurrentPlayer() {
    return currentPlayer;
  }

  public List<String> getBoardLines() {
    return boardLines;
  }

  public List<String> getHistoryMoves() {
    return historyMoves;
  }
}
