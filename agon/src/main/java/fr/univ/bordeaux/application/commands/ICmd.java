package fr.univ.bordeaux.application.commands;

public interface ICmd {

  /** execute specific actions */
  void execute();

  /* show help for a specific command */
  void showHelp();

}
