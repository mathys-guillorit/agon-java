package fr.univ.bordeaux.application.commands.specialized; // Adapte le package si besoin

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.cli.AgonShell;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdHelp extends Cmd {
  private Options opts;

  public CmdHelp(AbstractGameUI uictx) {
    super(uictx);
    this.opts = new Options();
  }

  @Override
  public void execute() {
    if (!(this.getCtx() instanceof AgonShell shell)) return;
    String[] args = shell.getTxtOptions();
    if (args != null && args.length > 0) {
      String targetCmd = args[0].toLowerCase();
      Optional<CmdAction> cmd = shell.getCmds().get(targetCmd);

      if (cmd.isPresent()) {
        shell.showMessage("--- Help for: " + targetCmd + " ---\n");
        cmd.get().showHelp();
      } else {
        shell.showError("Unknown command: " + targetCmd + "\n");
      }
    } else {
      shell.showMessage("======= AVAILABLE COMMANDS =======\n");
      for (String name : shell.getCmds().getKeys()) {
        shell.showMessage("  - " + name + "\n");
      }
      shell.showMessage("\nType 'help [command]' for detailed instructions.\n");
    }
  }

  @Override
  public void showHelp() {
    this.getCtx().showMessage("Usage: help\n");
    this.getCtx()
        .showMessage(
            "Description: Displays the general game menu and lists all available commands.\n");
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return null;
  }

  @Override
  public String getName() {
    return "help";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }
}
