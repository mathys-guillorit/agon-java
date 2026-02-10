package fr.univ.bordeaux.application.commands;

public interface ICmd {

  /**
   * special case for quit command
   * @return boolean true if command is quit else false
   */
  public boolean isQuit();

}
