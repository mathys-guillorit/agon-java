package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;

public class CmdLoad extends Cmd {

  private final String desc;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdLoad(AbstractGameUI uictx) {
    super(uictx);
    this.desc = "load a file from disk";
  }

  @Override
  public String getName() {
    return "load";
  }

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }

  @Override
  public void execute() {}
}
