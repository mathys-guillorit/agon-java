package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdRegister;
import fr.univ.bordeaux.application.commands.ICmd;
import fr.univ.bordeaux.application.commands.ICmdCtx;
import fr.univ.bordeaux.ui.cli.AgonShell;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Optional;

public class CmdQuit extends Cmd {

  private boolean waitingConfirmation = false;

  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   *
   * @param cmdCtx cmdCtx the responsibility to use resources in commands
   */
  public CmdQuit(ICmdCtx cmdCtx) {
    super(cmdCtx);
  }

  @Override
  public void execute() {
    this.cliWln("Save the game before quitting ? [y/n]");
    this.waitingConfirmation = true;
  }

  /**
   * recover input from {@link AgonShell}
   * @param input text from the user in cli
   */
  @Override
  public void handleInput(String input) {
    if (input.equalsIgnoreCase("y")) {
      /// TODO: must replace here with a real save
      this.cliWln("saving...");
      this.cliWln("saved !");
    }
    this.waitingConfirmation = false;
    this.setRunning(false);
  }

  @Override
  public void showHelp() {

  }

  @Override
  public boolean requiresInput() {
    return waitingConfirmation;
  }


}
