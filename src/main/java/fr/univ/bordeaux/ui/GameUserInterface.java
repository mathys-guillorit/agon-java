package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.match.MoveDtO;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// spotless:off
/**
 * Unified interface defining possible interactions in the Agon game.
 *
 * <p>This interface aggregates both:
 * <ul>
 *   <li>Actions the player can perform (e.g., {@code tryMove}, {@code undo}).
 *   <li>Methods for updating the display (e.g., {@code updateBoard}).
 * </ul>
 * </p>
 * It serves as the contract between the Presentation layer (UI) and the Application layer (Engine).
 */
// spotless:on
public interface GameUserInterface {

  /**
   * to check if the UI is running and if the Match is running to close the entire application.
   *
   * @return truee | false
   */
  boolean isRunning();

  /**
   * Quit the current game and closes the application. May trigger Reader prompt to save before
   * exiting.
   */
  void quit();

  /**
   * Updates the game board display.
   *
   * @param agonBoard A textual (ASCII) or serialized representation of the board.
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

  /** Display the help menu into terminal. */
  void showHelp();

  /**
   * Show warning messages into the sub UI object.
   *
   * @param msg {@link String}
   */
  void showWarn(String msg);

  /**
   * Show information into the sub UI object.
   *
   * @param msg {@link String}
   */
  void showInfo(String msg);

  /** Explicit. */
  AtomicBoolean getDebugMode();

  /** Explicit. */
  void setVerbose(boolean state);

  /** Save the game before leaving. */
  void saveGame();

  /**
   * Retrieve from ui/CLi some text.
   *
   * @return {@link String}
   */
  String getUserInput();

  /**
   * To display Moves history.
   *
   * @param moves {@link List}
   */
  void displayHistory(List<MoveDtO> moves);
}
