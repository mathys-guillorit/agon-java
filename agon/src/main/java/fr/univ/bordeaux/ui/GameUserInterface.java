package fr.univ.bordeaux.ui;

public interface GameUserInterface {
    void startNewGame(String[] args);
    //void tryMove(Position from, Position to); 
    //void selectPiece(Position pos);
    void undo();
    void redo();
    void saveGame(String filename);
    void loadGame(String filename);
    void pauseGame();
    void resumeGame();
    void quitGame();
    void requestHint();
    void updateBoard(String boardRepresentation);
    void showMessage(String message);
    void showError(String error);
    boolean getUserConfirmation(String question);
}