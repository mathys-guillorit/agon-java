package fr.univ.bordeaux.application.commands;

/** Generic command to remove reundant code duplication into inherited sub commands. */
public abstract class Cmd implements Icmd {

  /**
   * Check if the command for later concretions is leaving or not for this specific special command
   * to allow leave the system and reuse components.
   *
   * @return boolean
   */
  public boolean isQuit() {
    return false;
  }
}
