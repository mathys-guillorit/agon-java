package fr.univ.bordeaux.application.commands;

public interface ICmd {

  /**
   * special case for quit command
   *
   * @return boolean true if command is quit else false
   */
  boolean isQuit();

  /** execute specific actions */
  void execute();
}
