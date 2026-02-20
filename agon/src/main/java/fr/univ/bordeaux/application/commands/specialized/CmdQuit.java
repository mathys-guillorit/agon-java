package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;

public class CmdQuit extends Cmd {
  private final String desc;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx display context
   */
  public CmdQuit(AbstractGameUI uictx) {
    super(uictx);
    this.desc = "leaving the program with this command or \"ctrl-c\"";
  }

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }

  @Override
  public String getName() {
    return "quit";
  }

  /** to be run with higher levels in code */
  public void execute() {
    this.getCtx().quitGame();
  }
}
