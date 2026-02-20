package fr.univ.bordeaux.ui;

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
  // --- GAME ACTIONS (Commands to the Engine) ---

  /**
   * Starts a new game with the specified options.
   *
   * @param args Configuration arguments (e.g., player names, variants).
   */
  void startNewGame(String[] args);

  /**
   * Attempts to move a piece from one position to another.
   *
   * @param from The starting position of the piece.
   * @param to The desired destination position.
   */
  // void tryMove(Position from, Position to);

  /**
   * Selects a piece on the board (often used to display possible moves).
   *
   * @param pos The position of the piece to select.
   */
  // void selectPiece(Position pos);

  /** Undoes the last move played (if history allows). */
  void undo();

  /** Redoes the last undone move (if history allows). */
  void redo();

  /**
   * Saves the current game state to a file.
   *
   * @param filename The path or name of the save file.
   */
  void saveGame(String filename);

  /**
   * Loads a game from a save file.
   *
   * @param filename The path or name of the file to load.
   */
  void loadGame(String filename);

  /** Pauses the game (stops the timer if present). */
  void pauseGame();

  /** Resumes the game after a pause. */
  void resumeGame();

  /**
   * Quits the current game and closes the application. May trigger a prompt to save before exiting.
   */
  void quitGame();

  /** Requests a hint or advice from the game engine (AI). */
  void requestHint();

  // --- UI UPDATES (Outputs to the Screen) ---

  /**
   * Updates the game board display.
   *
   * @param boardRepresentation A textual (ASCII) or serialized representation of the board.
   */
  void updateBoard(ConsoleRenderer boardRepresentation);

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

  /**
   * Asks the user for confirmation (e.g., "Do you really want to quit?").
   *
   * @param question The question to ask.
   * @return {@code true} if the user accepts, {@code false} otherwise.
   */
  boolean getUserConfirmation(String question);

  void showHelp();

  /**
   * get options name written by the user
   *
   * @return "args" from app EntryPoint
   */
  public String[] getTxtOptions();

  public AtomicBoolean getDebugMode();

  /**
   * get commands from the ui object
   *
   * @return a register of all commands
   */
  public AgonRegister<CmdAction> getCmds();
}
