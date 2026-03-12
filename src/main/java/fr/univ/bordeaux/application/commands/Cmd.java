package fr.univ.bordeaux.application.commands;

public abstract class Cmd implements ICmd {

  /**
   * check if the command for later concretions is leaving or not for this specific special command
   * to allow leave the system and reuse components
   *
   * @return boolean
   */
  public boolean isQuit() {
    return false;
  }
}
