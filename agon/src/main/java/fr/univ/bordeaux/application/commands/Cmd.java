package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.cli.AgonShell;
import javax.annotation.Nonnull;
import org.jline.reader.Completer;


public abstract class Cmd implements CmdAction {

  private AbstractGameUI ctx;
  // may require a GUI delegate here for later (example : IGUIDelegate)
  // mau require a delegate here for the NETwork for later (or
  // juste create one without args in the constructor) (example: INETDelegate)
  private static String prompt = null;


  public Cmd(AbstractGameUI uictx) {
    this.ctx = uictx;
  }

  public AbstractGameUI getCtx() {
    return this.ctx;
  }


  public boolean requiresInput() {
    return false;
  }

  /**
   * override in sub commands
   *
   * @return Completer for completing user writing with tab keycap
   */
  @Nonnull
  @Override
  public abstract Completer getAutoCompleter();
}
