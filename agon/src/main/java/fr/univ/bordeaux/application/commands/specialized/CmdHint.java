package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;

public class CmdHint extends Cmd {

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdHint(AbstractGameUI uictx) {
    super(uictx);
    this.setDesc("Description: show help from ai to the user");
    this.setName("hint");
  }

  @Override
  public void execute() {}
}
