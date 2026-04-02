package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** A builder class responsible for progressively assembling a {@link GameSaveData}. */
public class GameSaveBuilder {
  private GameConfig config = new GameConfig();
  private Color currentPlayer = null;
  private final List<String> boardLines = new ArrayList<>();
  private final List<String> historyMoves = new ArrayList<>();

  private boolean hasSettingsSection = false;
  private boolean hasGameSection = false;
  private boolean hasHistorySection = false;

  /**
   * Flags the {@code [settings]} section as present in the parsed save file.
   *
   * <p>This allows the builder to verify structural integrity, ensuring the section header exists
   * even if its contents are malformed or missing.
   */
  public void markSettingsSection() {
    this.hasSettingsSection = true;
  }

  /**
   * Flags the {@code [game]} section as present in the parsed save file.
   *
   * <p>This flag is checked during the {@link #build()} phase to prevent the creation of a game
   * state from a structurally corrupted or truncated file.
   */
  public void markGameSection() {
    this.hasGameSection = true;
  }

  /**
   * Flags the {@code [history]} section as present in the parsed save file.
   *
   * <p>This is crucial for distinguishing between a valid new game (where the section exists but
   * contains no played moves yet) and a corrupted file (where the section is missing entirely due
   * to an unclosed comment block or truncation).
   */
  public void markHistorySection() {
    this.hasHistorySection = true;
  }

  public void setConfig(GameConfig config) {
    this.config = config;
  }

  public GameConfig getConfig() {
    return config;
  }

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
    if (!this.hasSettingsSection) {
      throw new IOException("Settings section not set");
    }
    if (!this.hasGameSection) {
      throw new IOException("Game section not set");
    }
    if (!this.hasHistorySection) {
      throw new IOException("History section not set");
    }
    if (currentPlayer == null) {
      throw new IOException("Invalid save file: Missing current player in [game] section.");
    }
    if (boardLines.isEmpty()) {
      throw new IOException("Invalid save file: Board representation is missing.");
    }
    return new GameSaveData(config, currentPlayer, boardLines, historyMoves);
  }
}
