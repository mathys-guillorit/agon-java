package fr.univ.bordeaux;

import fr.univ.bordeaux.application.GameLauncher;

public class Main {

  public static void main(String[] arg) throws Exception {
    GameLauncher launcher = new GameLauncher();
    launcher.launch(arg);
  }
}
