package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.config.GameConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** A builder class responsible for progressively assembling a {@link GameSaveData}. */
public class GameSaveBuilder {
  private GameConfig config = new GameConfig(); // Default config just in case
  private Color currentPlayer = null;
  private final List<String> boardLines = new ArrayList<>();
  private final List<String> historyMoves = new ArrayList<>();

  public void setConfig(GameConfig config) {
    this.config = config;
  }

  public GameConfig getConfig() {
    return config;
  } // Useful to reuse ConfigParser logic

  public void setCurrentPlayer(Color player) {
    this.currentPlayer = player;
  }

  public void addBoardLine(String line) {
    this.boardLines.add(line);
  }

  public void addHistoryMove(String move) {
    this.historyMoves.add(move);
  }

  /** Validates and builds the final GameSaveData object. */
  public GameSaveData build() throws IOException {
    if (currentPlayer == null) {
      throw new IOException("Invalid save file: Missing current player in [game] section.");
    }
    if (boardLines.isEmpty()) {
      throw new IOException("Invalid save file: Board representation is missing.");
    }
    return new GameSaveData(config, currentPlayer, boardLines, historyMoves);
  }
}
