package fr.univ.bordeaux.application.commands.specialized; // Adapte le package si besoin

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;
import java.util.Optional;
import org.apache.commons.cli.Option;

public class CmdHelp extends Cmd {

  public CmdHelp(AbstractGameUI uictx) {
    super(uictx);
    // all cmd name as option
    uictx
        .getCmds()
        .getKeys()
        .forEach(
            cmdName -> {
              this.addOption(Option.builder(cmdName).get());
            });
    this.setDesc("Description: display help, show this help with \"help help\"");
    this.setName("Help");
  }

  public void execute() {
    GameUserInterface ctx = this.getCtx();
    var tmp = new UIPromptParser(ctx.getUserPrompt());
    String[] args = tmp.getTxtOptions();
    if (args != null && args.length > 0) {
      String targetCmd = args[0].toLowerCase();
      Optional<CmdAction> cmd = ctx.getCmds().get(targetCmd);
      if (cmd.isPresent()) {
        ctx.showMessage("--- Help for: " + targetCmd + " ---\n");
        cmd.get().showHelp();
      } else {
        ctx.showError("Unknown command: " + targetCmd + "\n");
      }
    } else {
      ctx.showMessage("======= AVAILABLE COMMANDS =======\n");
      for (String name : ctx.getCmds().getKeys()) {
        ctx.showMessage("  - " + name + "\n");
      }
      ctx.showMessage("\nType 'help [command]' for detailed instructions.\n");
    }
    ctx.showMessage("use \"ctrl+r\" to show history");
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public void execute(MatchManager match) {

  }

  @Override
  public CmdAction createNew(String[] args) {
    return null;
  }
}
