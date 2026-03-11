package fr.univ.bordeaux.ui;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractGameUI implements GameUserInterface,MatchObserver {

  private GameUserInterface gameEngine;

  public void setGameEngine(GameUserInterface gameEngine) {
    this.gameEngine = gameEngine;
  }

  protected GameUserInterface getGameEngine() {
    if (this.gameEngine == null) {
      throw new IllegalStateException("The game engine is disconnected");
    }
    return this.gameEngine;
  }

  public abstract void start();

  /*

      @Override public void tryMove(Position from, Position to) {
          getGameEngine().tryMove(from, to);
      }

      @Override public void selectPiece(Position pos) {
          getGameEngine().selectPiece(pos);
      }

  */

  /**
   * save the game configuration into the local machine from the user using current path
   *
   * @apiNote save the file into the /Downloads repertory with current date (computer's date)
   */
  @Override
  public void saveGame() {
    // same method here for all interfaces

    // get download path to save file there :
    final String userHome = System.getProperty("user.home");
    if (userHome == null || userHome.isEmpty())
      throw new IllegalStateException("Cannot determine user home directory");
    final Path downDir = Paths.get(userHome, "Downloads"); // save file into /Download directory
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    String currentTime = LocalDateTime.now().format(formatter);
    Path filepath =
        downDir.resolve("AgonSave_" + currentTime + ".agon"); // where to save the file in
    ///  TODO: saving here the real file name
    showInfo("saving...");
    // code here (with try-catch if necessary)
    // if(....noProblemsEncountered()){
    showInfo("saved at : " + filepath);
    // return;
    // }
    // // problem encountered show them :
    // this.showError("cannot safe file for reason : ");
    // this.showError(...);
  }

  @Override
  public void loadGame(String f) {
    getGameEngine().loadGame(f);
  }

  @Override
  public void pauseGame() {
    getGameEngine().pauseGame();
  }

  @Override
  public void resumeGame() {
    getGameEngine().resumeGame();
  }

  @Override
  public void quitGame() {
    getGameEngine().quitGame();
  }
}
