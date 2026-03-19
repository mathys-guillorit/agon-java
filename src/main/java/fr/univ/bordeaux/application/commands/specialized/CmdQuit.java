package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;

/** Quit command. */
public class CmdQuit extends Cmd {

  @Override
  public boolean isQuit() {
    return true;
  }

  @Override
  public void execute() {}
}
