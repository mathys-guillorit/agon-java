package fr.univ.bordeaux.ui;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractGameUI implements GameUserInterface, MatchObserver {


  /**
   * save the game configuration into the local machine from the user using current path
   *  save the file into the /Downloads repertory with current date (computer's date)
   */
  @Override
  public void saveGame() {
    // same method here for all interfaces

    // get download path to save file there :
    final String userHome = System.getProperty("user.home");
    if (userHome == null || userHome.isEmpty()) {
      throw new IllegalStateException("Cannot determine user home directory");
    }
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



  public abstract void quit();
}
