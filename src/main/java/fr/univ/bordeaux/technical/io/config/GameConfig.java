package fr.univ.bordeaux.technical.io.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the configuration settings for the Agon game. (like verbosity and debug modes), game
 * rules (like blitz mode and timeouts), and Artificial Intelligence settings (like algorithms,
 * depths, and heuristics).
 */
public class GameConfig {

  /** Verbose mode flag. */
  private boolean verbose;

  /** Debug mode flag. */
  private boolean debug;

  /** Blitz mode flag. */
  private boolean blitzMode;

  /** Manual placement flag. */
  private boolean manualPlacement;

  /** Timeout duration in seconds. */
  private int timeout = 30;

  /** Global AI activation flag. */
  private boolean aiActive;

  /** The AI algorithm mode. */
  private String aiMode = "minimax";

  /** Maximum depth for the AI search. */
  private int aiDepth = 4;

  /** Maximum computation time for AI in seconds. */
  private int aiTimeLimit = 5;

  /** Flag for AI iterative deepening. */
  private boolean iterDeepening = true;

  /** The heuristic strategy name. */
  private String aiHeuristic = "mixed";

  /** Flag indicating if White is AI. */
  private boolean whiteIsAi;

  /** Flag indicating if Black is AI. */
  private boolean blackIsAi;

  /** Map of keyboard shortcuts. */
  private Map<String, String> shortcuts = createDefaultShortcuts();;

  public GameConfig() {}

  /**
   * Sets whether the Black player is controlled by an Artificial Intelligence.
   *
   * @param blackIsAi {@code true} if Black is an AI, {@code false} if human.
   */
  public void setBlackAi(final boolean blackIsAi) {
    this.blackIsAi = blackIsAi;
  }

  /**
   * Sets the verbosity of the application output.
   *
   * @param verbose {@code true} to enable verbose output, {@code false} otherwise.
   */
  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Sets the debug mode for the application.
   *
   * @param debug {@code true} to enable debug logs and features, {@code false} otherwise.
   */
  public void setDebug(final boolean debug) {
    this.debug = debug;
  }

  /**
   * Sets whether the game should be played in blitz mode (timed).
   *
   * @param blitzMode {@code true} to enable blitz mode, {@code false} for untimed games.
   */
  public void setBlitzMode(final boolean blitzMode) {
    this.blitzMode = blitzMode;
  }

  /**
   * Sets the total timeout duration for blitz mode.
   *
   * @param timeout The timeout duration in seconds.
   */
  public void setTimeout(final int timeout) {
    this.timeout = timeout;
  }

  /**
   * Globally enables or disables the use of Artificial Intelligence in the game.
   *
   * @param isAiActive {@code true} to allow AI players, {@code false} to force human players.
   */
  public void setAi(final boolean isAiActive) {
    this.aiActive = isAiActive;
  }

  /**
   * Sets the algorithm mode used by the AI.
   *
   * @param mode The name of the AI algorithm (e.g., {@code "MINIMAX"}).
   */
  public void setAiMode(final String mode) {
    this.aiMode = mode;
  }

  /**
   * Sets the maximum search depth for the AI algorithm.
   *
   * @param depth The maximum number of turns ahead the AI should calculate.
   */
  public void setAiDepth(final int depth) {
    this.aiDepth = depth;
  }

  /**
   * Sets the maximum time allowed for the AI to compute a single move.
   *
   * @param timeLimit The calculation time limit in seconds.
   */
  public void setAiTimeLimit(final int timeLimit) {
    this.aiTimeLimit = timeLimit;
  }

  /**
   * Sets whether the AI should use the Iterative Deepening technique.
   *
   * @param itDeepening {@code true} to enable Iterative Deepening, {@code false} otherwise.
   */
  public void setAiIterativeDeepening(final boolean itDeepening) {
    this.iterDeepening = itDeepening;
  }

  /**
   * Sets the heuristic evaluation strategy to be used by the AI.
   *
   * @param heuristic The name of the heuristic strategy (e.g., {@code "MIXED"}).
   */
  public void setAiHeuristic(final String heuristic) {
    this.aiHeuristic = heuristic;
  }

  /**
   * Checks if verbose output is enabled.
   *
   * @return {@code true} if verbose output is enabled, {@code false} otherwise.
   */
  public boolean isVerbose() {
    return verbose;
  }

  /**
   * Checks if debug mode is enabled.
   *
   * @return {@code true} if debug mode is enabled, {@code false} otherwise.
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Checks if blitz mode is enabled.
   *
   * @return {@code true} if blitz mode is active, {@code false} otherwise.
   */
  public boolean isBlitzMode() {
    return blitzMode;
  }

  /**
   * Gets the timeout duration for blitz mode.
   *
   * @return The timeout duration in seconds.
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Checks if the Artificial Intelligence is globally active.
   *
   * @return {@code true} if AI features are enabled, {@code false} otherwise.
   */
  public boolean isAiActive() {
    return aiActive;
  }

  /**
   * Gets the algorithm mode currently configured for the AI.
   *
   * @return The name of the AI algorithm.
   */
  public String getAiMode() {
    return aiMode;
  }

  /**
   * Gets the maximum search depth configured for the AI.
   *
   * @return The AI search depth.
   */
  public int getAiDepth() {
    return aiDepth;
  }

  /**
   * Gets the maximum computation time allowed for the AI per move.
   *
   * @return The AI time limit in seconds.
   */
  public int getAiTimeLimit() {
    return aiTimeLimit;
  }

  /**
   * Checks if the Iterative Deepening feature is enabled for the AI.
   *
   * @return {@code true} if Iterative Deepening is active, {@code false} otherwise.
   */
  public boolean isAiIterativeDeepening() {
    return iterDeepening;
  }

  /**
   * Gets the heuristic evaluation strategy configured for the AI.
   *
   * @return The name of the heuristic strategy.
   */
  public String getAiHeuristic() {
    return aiHeuristic;
  }

  /**
   * Checks if the White player is configured to be an AI.
   *
   * @return {@code true} if White is an AI, {@code false} otherwise.
   */
  public boolean isWhiteAi() {
    return whiteIsAi;
  }

  /**
   * Checks if the Black player is configured to be an AI.
   *
   * @return {@code true} if Black is an AI, {@code false} otherwise.
   */
  public boolean isBlackAi() {
    return blackIsAi;
  }

  /**
   * Checks if the placement is configured to be manual or automatic.
   *
   * @return {@code true} if placement is manual, {@code false} otherwise.
   */
  public boolean isManualPlacement() {
    return manualPlacement;
  }

  /**
   * Retrieves the map containing all configured keyboard shortcuts.
   *
   * @return A map where the key is the shortcut identifier and the value is the assigned key combination.
   */
  public Map<String, String> getShortcuts() {
    return shortcuts;
  }

  /**
   * Sets whether the White player is controlled by an Artificial Intelligence.
   *
   * @param whiteIsAi {@code true} if White is an AI, {@code false} if human.
   */
  public void setWhiteAi(final boolean whiteIsAi) {
    this.whiteIsAi = whiteIsAi;
  }

  /**
   * Enables or disables the manual placement of Pawns and Queens.
   *
   * @param manualPlacement {@code true} to allow the players to manually choose the initial placement of their pawns, {@code false} to make it automatic.
   */
  public void setManualPlacement(final boolean manualPlacement) {
    this.manualPlacement = manualPlacement;
  }

  /**
   * Sets the entire map of keyboard shortcuts.
   *
   * @param shortcuts A map containing the shortcut identifiers and their corresponding key combinations.
   */
  public void setShortcuts(final Map<String, String> shortcuts) {
    this.shortcuts = shortcuts;
  }

  /**
   * Adds or updates a single keyboard shortcut in the configuration.
   *
   * @param key   The shortcut identifier (must start with "shortcut_").
   * @param value The key combination assigned to this shortcut.
   */
  public void addShortcut(final String key, final String value) {
    shortcuts.put(key, value);
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder(256);
    string.append("[verbose]=").append(verbose).append("\n");
    string.append("[debug]=").append(debug).append("\n");
    string.append("[blitzMode]=").append(blitzMode).append("\n");
    string.append("[timeout]=").append(timeout).append("\n");
    string.append("[aiActive]=").append(aiActive).append("\n");
    string.append("[aiMode]=").append(aiMode).append("\n");
    string.append("[aiDepth]=").append(aiDepth).append("\n");
    string.append("[aiIterativeDeepening]=").append(iterDeepening).append("\n");
    string.append("[aiTimeLimit]=").append(aiTimeLimit).append("\n");
    string.append("[aiHeuristique]=").append(aiHeuristic).append("\n");
    string.append("[whiteIsAI]=").append(whiteIsAi).append("\n");
    string.append("[blackIsAI]=").append(blackIsAi).append("\n");
    return string.toString();
  }

  /**
   * Creates a deep copy of the current configuration.
   *
   * @return A new GameConfig instance with the same settings.
   */
  public GameConfig copy() {
    final GameConfig clone = new GameConfig();

    clone.setVerbose(this.verbose);
    clone.setDebug(this.debug);
    clone.setManualPlacement(this.manualPlacement);

    clone.setBlitzMode(this.blitzMode);
    clone.setTimeout(this.timeout);

    clone.setAi(this.aiActive);
    clone.setWhiteAi(this.whiteIsAi);
    clone.setBlackAi(this.blackIsAi);

    clone.setAiMode(this.aiMode);
    clone.setAiDepth(this.aiDepth);
    clone.setAiTimeLimit(this.aiTimeLimit);
    clone.setAiIterativeDeepening(this.iterDeepening);
    clone.setAiHeuristic(this.aiHeuristic);

    clone.setShortcuts(new ConcurrentHashMap<>(this.shortcuts));

    return clone;
  }

  private Map<String, String> createDefaultShortcuts() {
    final Map<String, String> shortcuts = new ConcurrentHashMap<>();
    shortcuts.put("shortcut_new", "Ctrl+N");
    shortcuts.put("shortcut_load", "Ctrl+L");
    shortcuts.put("shortcut_save", "Ctrl+S");
    shortcuts.put("shortcut_config", "Ctrl+,");
    shortcuts.put("shortcut_info", "Ctrl+I");
    shortcuts.put("shortcut_quit", "Ctrl+Q");
    shortcuts.put("shortcut_undo", "Ctrl+U");
    shortcuts.put("shortcut_redo", "Ctrl+R");
    shortcuts.put("shortcut_pause", "Ctrl+P");
    shortcuts.put("shortcut_hint", "Ctrl+H");
    return shortcuts;
  }
}