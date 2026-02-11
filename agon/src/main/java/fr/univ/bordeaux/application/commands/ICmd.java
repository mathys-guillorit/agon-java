package fr.univ.bordeaux.application.commands;

public interface ICmd {

  /**
   * special case for quit command
   * @return boolean true if command is quit else false
   */

  /**
   * execute specific actions
   */
  void execute(String[] args);

}
