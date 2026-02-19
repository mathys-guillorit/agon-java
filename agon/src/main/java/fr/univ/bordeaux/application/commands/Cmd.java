package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.cli.AgonShell;
import org.jline.reader.Completer;

import javax.annotation.Nonnull;

/**
 * represent the fixed code for all different Commands
 * @warning little changes require a lot refactor here
 * @apiNote each command knows his options only <br/>(to make easier auto-complete)
 */
public abstract class Cmd implements CmdAction {

  private AbstractGameUI ctx;
  // may require a GUI delegate here for later (example : IGUIDelegate)
  // mau require a delegate here for the NETwork for later (or
  // juste create one without args in the constructor) (example: INETDelegate)
  private static String prompt = null;
  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   */
  public Cmd(AbstractGameUI uictx) {
    this.ctx = uictx;
  }


  public AbstractGameUI getCtx() {
    return this.ctx;
  }


  /**
   * ask to {@link AgonShell} if the command require the user input
   * @implNote change const false to a variable that is toggleable
   * @return boolean false (by default)
   */
  public boolean requiresInput(){
    return false;
  }

  /**
   * override in sub commands
   * @return Completer for completing user writing with tab keycap
   */
  @Nonnull
  @Override
  public abstract Completer getAutoCompleter();



}
