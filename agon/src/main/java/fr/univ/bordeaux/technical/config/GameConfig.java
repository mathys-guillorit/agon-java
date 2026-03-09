package fr.univ.bordeaux.technical.config;

/**
 * Represents the configuration settings for the Agon game.
 *
 * <p>This class acts as a data container for all customizable parameters, including system options
 * (like verbosity and debug modes), game rules (like blitz mode and timeouts), and Artificial
 * Intelligence settings (like algorithms, depths, and heuristics).
 */
public class GameConfig {
  private boolean verbose = false;
  private boolean debug = false;
  private boolean blitzMode = true;
  private int timeout = 1800;
  private boolean aiActive = true;
  private String aiMode = "MINIMAX";
  private int aiDepth = 4;
  private int aiTimeLimit = 5;
  private boolean aiIterativeDeepening = true;
  private String aiHeuristic = "MIXED";
  private boolean whiteIsAI = false;
  private boolean blackIsAI = true;
  private long whiteInitialTimer = 30;
  private long blackInitialTimer = 30;

  /**
   * Sets whether the White player is controlled by an Artificial Intelligence.
   *
   * @param whiteIsAI {@code true} if White is an AI, {@code false} if human.
   */
  public void setWhiteAI(boolean whiteIsAI) {
    this.whiteIsAI = whiteIsAI;
  }

  /**
   * Sets whether the Black player is controlled by an Artificial Intelligence.
   *
   * @param blackIsAI {@code true} if Black is an AI, {@code false} if human.
   */
  public void setBlackAI(boolean blackIsAI) {
    this.blackIsAI = blackIsAI;
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
  public boolean isWhiteAI() {
    return whiteIsAI;
  }

  /**
   * Checks if the Black player is configured to be an AI.
   *
   * @return {@code true} if Black is an AI, {@code false} otherwise.
   */
  public boolean isBlackAI() {
    return blackIsAI;
  }

  public long getWhiteInitialTime() {
    return whiteInitialTimer;
  }

  public long getBlackInitialTime() {
    return blackInitialTimer;
  }
}
