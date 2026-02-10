package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;

public class CmdQuit extends Cmd {

  @Override
  public boolean isQuit() {
    return true;
  }


}
