package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;

public class CmdPause extends Cmd {
  private final String desc;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdPause(AbstractGameUI uictx) {
    super(uictx);
    // may add resume command so ? (if we can pause in logic we can resume too)
    // or it's not only "pause" but it toggles pause
    this.desc = "pause the game";
  }

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }

  @Override
  public String getName() {
    return "pause";
  }

  @Override
  public void execute() {}
}
