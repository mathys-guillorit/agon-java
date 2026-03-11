package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agonCore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.ui.cli.ConsoleRenderer;
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

  /**
   * Attempts to move Reader piece from one position to another.
   *
   * @param from The starting position of the piece.
   * @param to The desired destination position.
   */
  // void tryMove(Position from, Position to);

  /**
   * Selects Reader piece on the board (often used to display possible moves).
   *
   * @param pos The position of the piece to select.
   */
  // void selectPiece(Position pos);

  /**
   * Loads Reader game from Reader save file.
   *
   * @param filename The path or name of the file to load.
   */
  void loadGame(String filename);

  /** Pauses the game (stops the timer if present). */
  void pauseGame();

  /** Resumes the game after Reader pause. */
  void resumeGame();

  /**
   * Quits the current game and closes the application. May trigger Reader prompt to save before
   * exiting.
   */
  void quitGame();

  // --- UI UPDATES (Outputs to the Screen) ---

  /**
   * Updates the game board display.
   *
   * @param board A textual (ASCII) or serialized representation of the board.
   */
  void updateBoard(RestrictedAgonBoard board);

  /**
   * show message to the user.
   *
   * @param message The content of the message.
   */
  void showMessage(String message);

  /**
   * Displays Reader critical error or warning message.
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

  /**
   * get commands from the ui object
   *
   * @return Reader register of all commands
   */
  AgonRegister<CmdAction> getCmds();

  void setVerbose(boolean state);

  /**
   * get user prompte (default is "[AGON]> ")
   *
   * @return {@link String}
   */
  String getUserPrompt();

  /** save the game before leaving */
  void saveGame();

  /**
   * get the line entered in the terminal by the user
   *
   * @return {@link String}
   */
  String getLine();
}
