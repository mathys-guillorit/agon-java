package fr.univ.bordeaux.application.commands;

/** Interface for commands (will be removed replaced by CmdAction). */
public interface Icmd {

  /**
   * Special case for quit command.
   *
   * @return boolean true if command is quit else false
   */
  boolean isQuit();

  /** Execute specific actions. */
  void execute();
}
