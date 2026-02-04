package fr.univ.bordeaux.ui;

public interface IGameUserInterface {
    void updateBoard(String board);
    void showMessage(String message);
    void showError(String error);
    boolean getUserConfirmation(String prompt);
}
