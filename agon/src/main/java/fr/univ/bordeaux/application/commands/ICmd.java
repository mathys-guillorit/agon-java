package fr.univ.bordeaux.application.commands;

public interface ICmd {

  /** execute specific actions */
  void shellExecute();

  /* show help for a specific command */
  void shellShowHelp();

  void guiExecute();
  void guiShowHelp();

  // cannot know exact methods
  // void netConnect();
  // void netDisconnect();

}
