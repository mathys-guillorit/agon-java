package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** A builder class responsible for progressively assembling a {@link GameSaveData}. */
public class GameSaveBuilder {

  /** Configuration object representing the settings. */
  private GameConfig config = new GameConfig();

  /** The current player whose turn it is to play. */
  private Color currentPlayer;

  /** List of strings representing the board's visual state. */
  private final List<String> boardLines = new ArrayList<>();

  /** List of strings representing the move history. */
  private final List<String> historyMoves = new ArrayList<>();

  /** Flag indicating if the settings section was found. */
  private boolean hasSettings;

  /** Flag indicating if the game section was found. */
  private boolean hasGame;

  /** Flag indicating if the history section was found. */
  private boolean hasHistory;

  /** Constructs a new empty builder for game saves. */
  public GameSaveBuilder() {}

  /**
   * Flags the {@code [settings]} section as present in the parsed save file.
   *
   * <p>This allows the builder to verify structural integrity, ensuring the section header exists
   * even if its contents are malformed or missing.
   */
  public void markSettingsSection() {
    this.hasSettings = true;
  }

  /**
   * Flags the {@code [game]} section as present in the parsed save file.
   *
   * <p>This flag is checked during the {@link #build()} phase to prevent the creation of a game
   * state from a structurally corrupted or truncated file.
   */
  public void markGameSection() {
    this.hasGame = true;
  }

  /**
   * Flags the {@code [history]} section as present in the parsed save file.
   *
   * <p>This is crucial for distinguishing between a valid new game (where the section exists but
   * contains no played moves yet) and a corrupted file (where the section is missing entirely due
   * to an unclosed comment block or truncation).
   */
  public void markHistorySection() {
    this.hasHistory = true;
  }

  /**
   * Sets the game configuration parsed from the save file.
   *
   * @param config The extracted {@link GameConfig} object.
   */
  public void setConfig(final GameConfig config) {
    this.config = config;
  }

  /**
   * Retrieves the current game configuration held by the builder.
   *
   * @return The current {@link GameConfig}.
   */
  public GameConfig getConfig() {
    return config;
  }

  /**
   * Sets the player whose turn it is to play next.
   *
   * @param player The {@link Color} representing the current player.
   */
  public void setCurrentPlayer(final Color player) {
    this.currentPlayer = player;
  }

  /**
   * Adds a single line to the board representation.
   *
   * @param line The string representing a line on the board.
   */
  public void addBoardLine(final String line) {
    this.boardLines.add(line);
  }

  /**
   * Adds a move notation string to the history.
   *
   * @param move The standard string notation of the move.
   */
  public void addHistoryMove(final String move) {
    this.historyMoves.add(move);
  }

  /**
   * Validates and builds the final GameSaveData object.
   *
   * @return A fully constructed and validated {@link GameSaveData}.
   * @throws IOException If the parsed data is incomplete or corrupted.
   */
  public GameSaveData build() throws IOException {
    validateSections();
    validateData();
    return new GameSaveData(config, currentPlayer, boardLines, historyMoves);
  }

  /**
   * Checks that all required section headers were found in the file.
   *
   * @throws IOException If a structural section is missing.
   */
  private void validateSections() throws IOException {
    if (!this.hasSettings) {
      throw new IOException("Settings section not set");
    }
    if (!this.hasGame) {
      throw new IOException("Game section not set");
    }
    if (!this.hasHistory) {
      throw new IOException("History section not set");
    }
  }

  /**
   * Checks that the core game data is present and well-formed.
   *
   * @throws IOException If essential data points are missing.
   */
  private void validateData() throws IOException {
    if (this.currentPlayer == null) {
      throw new IOException("Invalid save file: Missing current player in [game] section.");
    }
    if (this.boardLines.isEmpty()) {
      throw new IOException("Invalid save file: Board representation is missing.");
    }
  }
}
