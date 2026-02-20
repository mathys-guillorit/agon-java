package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;

public class CmdRedo extends Cmd {

  private final String desc;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdRedo(AbstractGameUI uictx) {
    super(uictx);
    this.desc = "redo a turn if there a turn to redo if not it will not work";
  }

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }

  @Override
  public String getName() {
    return "redo";
  }

  @Override
  public void execute() {}
}
