package fr.univ.bordeaux.technical.io.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration settings for the Agon game. (like verbosity and debug modes), game
 * rules (like blitz mode and timeouts), and Artificial Intelligence settings (like algorithms,
 * depths, and heuristics).
 */
public class GameConfig {

  private boolean verbose = false;
  private boolean debug = false;
  private boolean blitzMode = false;
  private boolean manualPlacement = false;
  private int timeout = 30;
  private boolean aiActive = false;
  private String aiMode = "minimax";
  private int aiDepth = 4;
  private int aiTimeLimit = 5;
  private boolean aiIterativeDeepening = true;
  private String aiHeuristic = "mixed";
  private boolean whiteIsAi = false;
  private boolean blackIsAi = false;
  private Map<String, String> shortcuts = createDefaultShortcuts();

  /**
   * Sets whether the Black player is controlled by an Artificial Intelligence.
   *
   * @param blackIsAi {@code true} if Black is an AI, {@code false} if human.
   */
  public void setBlackAi(boolean blackIsAi) {
    this.blackIsAi = blackIsAi;
  }

  /**
   * Sets the verbosity of the application output.
   *
   * @param verbose {@code true} to enable verbose output, {@code false} otherwise.
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Sets the debug mode for the application.
   *
   * @param debug {@code true} to enable debug logs and features, {@code false} otherwise.
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Sets whether the game should be played in blitz mode (timed).
   *
   * @param blitzMode {@code true} to enable blitz mode, {@code false} for untimed games.
   */
  public void setBlitzMode(boolean blitzMode) {
    this.blitzMode = blitzMode;
  }

  /**
   * Sets the total timeout duration for blitz mode.
   *
   * @param timeout The timeout duration in seconds.
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /**
   * Globally enables or disables the use of Artificial Intelligence in the game.
   *
   * @param ai {@code true} to allow AI players, {@code false} to force human-only players.
   */
  public void setAi(boolean ai) {
    this.aiActive = ai;
  }

  /**
   * Sets the algorithm mode used by the AI.
   *
   * @param mode The name of the AI algorithm (e.g., {@code "MINIMAX"}).
   */
  public void setAiMode(String mode) {
    this.aiMode = mode;
  }

  /**
   * Sets the maximum search depth for the AI algorithm.
   *
   * @param depth The maximum number of turns ahead the AI should calculate.
   */
  public void setAiDepth(int depth) {
    this.aiDepth = depth;
  }

  /**
   * Sets the maximum time allowed for the AI to compute a single move.
   *
   * @param timeLimit The calculation time limit in seconds.
   */
  public void setAiTimeLimit(int timeLimit) {
    this.aiTimeLimit = timeLimit;
  }

  /**
   * Sets whether the AI should use the Iterative Deepening technique.
   *
   * @param iterativeDeepening {@code true} to enable Iterative Deepening, {@code false} otherwise.
   */
  public void setAiIterativeDeepening(boolean iterativeDeepening) {
    this.aiIterativeDeepening = iterativeDeepening;
  }

  /**
   * Sets the heuristic evaluation strategy to be used by the AI.
   *
   * @param heuristic The name of the heuristic strategy (e.g., {@code "MIXED"}).
   */
  public void setAiHeuristic(String heuristic) {
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
    return aiIterativeDeepening;
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
   * @return A map where the key is the shortcut identifier (e.g., "shortcut_new") and the value is
   *     the assigned key combination (e.g., "ctrl+n").
   */
  public Map<String, String> getShortcuts() {
    return shortcuts;
  }

  /**
   * Sets whether the White player is controlled by an Artificial Intelligence.
   *
   * @param whiteIsAi {@code true} if White is an AI, {@code false} if human.
   */
  public void setWhiteAi(boolean whiteIsAi) {
    this.whiteIsAi = whiteIsAi;
  }

  /**
   * Enables or disables the manual placement of Pawns and Queens.
   *
   * @param manualPlacement {@code true} to allow the players to manually choose the initial
   *     placement of their pawns, {@code false} to make it automatic.
   */
  public void setManualPlacement(boolean manualPlacement) {
    this.manualPlacement = manualPlacement;
  }

  /**
   * Sets the entire map of keyboard shortcuts.
   *
   * @param shortcuts A map containing the shortcut identifiers and their corresponding key
   *     combinations.
   */
  public void setShortcuts(Map<String, String> shortcuts) {
    this.shortcuts = shortcuts;
  }

  /**
   * Adds or updates a single keyboard shortcut in the configuration.
   *
   * @param key The shortcut identifier (must start with "shortcut_").
   * @param value The key combination assigned to this shortcut.
   */
  public void addShortcut(String key, String value) {
    shortcuts.put(key, value);
  }

  /**
   * String representation of the object.
   *
   * @see Object .toString() method for more infos
   * @return {@link String}
   */
  public String toString() {
    StringBuilder string = new StringBuilder();
    string.append("[verbose]=").append(verbose).append("\n");
    string.append("[debug]=").append(debug).append("\n");
    string.append("[blitzMode]=").append(blitzMode).append("\n");
    string.append("[timeout]=").append(timeout).append("\n");
    string.append("[aiActive]=").append(aiActive).append("\n");
    string.append("[aiMode]=").append(aiMode).append("\n");
    string.append("[aiDepth]=").append(aiDepth).append("\n");
    string.append("[aiIterativeDeepening]=").append(aiIterativeDeepening).append("\n");
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
    GameConfig clone = new GameConfig();

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
    clone.setAiIterativeDeepening(this.aiIterativeDeepening);
    clone.setAiHeuristic(this.aiHeuristic);

    clone.setShortcuts(new HashMap<>(this.shortcuts));

    return clone;
  }

  private Map<String, String> createDefaultShortcuts() {
    Map<String, String> shortcuts = new HashMap<>();
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
