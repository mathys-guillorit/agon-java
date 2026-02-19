package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.ui.cli.ConsoleRenderer;

public interface GameUserInterface {
  void startNewGame(String[] args);

  // void tryMove(Position from, Position to);
  // void selectPiece(Position pos);
  boolean undo();

  void redo();

  void saveGame(String filename);

  void loadGame(String filename);

  void pauseGame();

  void resumeGame();

  void quitGame();

  void requestHint();

  /**
   * update board representation with ui
   * @param boardRepresentation object up-to-date to display the new state
   */
  void updateBoard(ConsoleRenderer boardRepresentation);

  void showMessage(String message);

  void showError(String error);

  boolean getUserConfirmation(String question);

  void showHelp();

}
