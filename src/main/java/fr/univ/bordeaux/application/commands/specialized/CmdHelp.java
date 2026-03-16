package fr.univ.bordeaux.application.commands.specialized; // Adapte le package si besoin

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Optional;
import org.apache.commons.cli.Option;

public final class CmdHelp extends Cmd {
  private String commandToHelp = null;

  public CmdHelp(GameUserInterface uictx) {
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
  private CmdHelp(GameUserInterface uictx, String commandToHelp) {
    this(uictx);
    this.commandToHelp = commandToHelp;
  }

  @Override
  public boolean execute(MatchManager match) {
    GameUserInterface ctx = this.getCtx();

    // CAS 1 : Aide spécifique (ex: help show)
    if (commandToHelp != null) {
      Optional<CmdAction> targetCmd = ctx.getCmds().get(commandToHelp);

      if (targetCmd.isPresent()) {
        ctx.showMessage("======= HELP: " + commandToHelp.toUpperCase() + " =======\n");
        // On appelle la méthode showHelp() de la commande cible
        ctx.showMessage(targetCmd.get().getDescription());
        return true;
      } else {
        ctx.showMessage("Unknown command: " + commandToHelp + "\n");
        // On retombe sur l'aide globale si la commande n'existe pas
      }
    }

    // CAS 2 : Aide globale (si pas d'argument ou commande inconnue)
    ctx.showMessage("======= AVAILABLE COMMANDS =======\n");
    for (String name : ctx.getCmds().getKeys()) {
      ctx.getCmds().get(name).ifPresent(cmd -> {
        ctx.showMessage(String.format("  %-12s : %s\n", name, cmd.getDescription()));
      });
    }
    ctx.showMessage("\nType 'help [command]' for detailed instructions (e.g., 'help show').\n");
    return true;
  }

  /*public boolean execute(MatchManager match) {
    GameUserInterface ctx = this.getCtx();
    // No line parsing needed here, CmdHelp already knows its options if it was created via createNew
    // But since execute() is called directly, we might need to know which command we are helping for.
    // However, the previous implementation was trying to parse ctx.getUserPrompt() which is wrong.
    
    // In the new architecture, the options are passed to createNew()
    // Let's assume for now we list all commands if no specific one is requested.
    ctx.showMessage("======= AVAILABLE COMMANDS =======\n");
    for (String name : ctx.getCmds().getKeys()) {
      ctx.showMessage("  - " + name + "\n");
    }
    ctx.showMessage("\nType 'help [command]' for detailed instructions.\n");
    ctx.showMessage("use \"ctrl+r\" to show history");
    return true;
  }*/

  @Override
  public String getDescription() {
    return "";
  }
@Override
  public CmdAction createNew(String[] args) {
    // Si l'utilisateur tape juste "help"
    if (args.length == 0) {
      return new CmdHelp(super.getCtx(), null);
    }

    // Si l'utilisateur tape "help undo" (args[0] est "undo")
    if (args.length == 1) {
      return new CmdHelp(super.getCtx(), args[0].toLowerCase());
    }

    super.getCtx().showMessage("Usage: help [command_name]");
    return null;
  }

}



