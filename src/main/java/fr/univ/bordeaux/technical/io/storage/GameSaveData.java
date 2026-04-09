package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.List;

/** A data container holding all the extracted information from a save file. */
public class GameSaveData {

  /** The game configuration extracted from the save. */
  private final GameConfig config;

  /** The color of the player whose turn it is. */
  private final Color currentPlayer;

  /** The string representation of the board state. */
  private final List<String> boardLines;

  /** The sequential list of moves played in the game. */
  private final List<String> historyMoves;

  /**
   * Datastructures for saving information about the game.
   *
   * @param config {@link GameConfig}
   * @param currentPlayer {@link Color}
   * @param boardLines {@link List}
   * @param historyMoves {@link List}
   */
  public GameSaveData(
          final GameConfig config, final Color currentPlayer, final List<String> boardLines, final List<String> historyMoves) {
    this.config = config;
    this.currentPlayer = currentPlayer;
    this.boardLines = boardLines;
    this.historyMoves = historyMoves;
  }

  /**
   * Retrieves the game configuration.
   *
   * @return The game configuration object.
   */
  public GameConfig getConfig() {
    return config;
  }

  /**
   * Retrieves the current player.
   *
   * @return The color of the current player.
   */
  public Color getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Retrieves the board representation.
   *
   * @return A list of strings representing the board lines.
   */
  public List<String> getBoardLines() {
    return boardLines;
  }

  /**
   * Retrieves the history of moves.
   *
   * @return A list of strings representing the recorded moves.
   */
  public List<String> getHistoryMoves() {
    return historyMoves;
  }
}