package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unified interface defining possible interactions in the Agon game.
 *
 * <p>This interface aggregates both:
 *
 * <ul>
 *   <li>Actions the player can perform (e.g., {@code tryMove}, {@code undo}).
 *   <li>Methods for updating the display (e.g., {@code updateBoard}).
 * </ul>
 *
 * It serves as the contract between the Presentation layer (UI) and the Application layer (Engine).
 */
public interface GameUserInterface {

  boolean isRunning();

  /**
   * Quits the current game and closes the application. May trigger Reader prompt to save before
   * exiting.
   */
  void quit();

  // --- UI UPDATES (Outputs to the Screen) ---

  /*/**
   * Updates the game board display.
   *
   * @param boardRepresentation A textual (ASCII) or serialized representation of the board.
   */
  // void updateBoard(String boardRepresentation);
  void updateBoard(RestrictedAgonBoard agonBoard);

  /**
   * Displays an informational message to the user.
   *
   * @param message The content of the message.
   */
  void showMessage(String message);

  /**
   * Displays a critical error or warning message.
   *
   * @param error The content of the error.
   */
  void showError(String error);

  /** display the help menu into terminal */
  void showHelp();

  /**
   * show warning messages into the sub UI object
   *
   * @param msg
   */
  void showWarn(String msg);

  /**
   * show information into the sub UI object
   *
   * @param msg
   */
  void showInfo(String msg);

  AtomicBoolean getDebugMode();

  void setVerbose(boolean state);

  /** save the game before leaving */
  void saveGame();

  String getUserInput();
  /**
   * Asks the user for confirmation (e.g., "Do you really want to quit?").
   *
   * @param question The question to ask.
   * @return {@code true} if the user accepts, {@code false} otherwise.
   */
  // boolean getUserConfirmation(String question);
}
