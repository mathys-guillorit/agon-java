package fr.univ.bordeaux.application.commands.specialized; // Adapte le package si besoin

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Option;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;

public final class CmdHelp extends Cmd {
  private String commandToHelp = null;
  private AgonRegister<CmdAction> agonRegister;

  public CmdHelp(GameUserInterface uictx, AgonRegister<CmdAction> cmds) {
    super(uictx);
    this.agonRegister = cmds;
    // all cmd name as option

        cmds.getKeys()
        .forEach(
            cmdName -> {
              this.addOption(Option.builder(cmdName).get());
            });
    this.setDesc("Description: display help, show this help with \"help help\"");
    this.setName("help");
  }
  private CmdHelp(GameUserInterface uictx, AgonRegister<CmdAction> cmds, String commandToHelp) {
    this(uictx, cmds);
    this.commandToHelp = commandToHelp;
  }

  public String getName(){
    return "help";
  }
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
      return (reader, line, candidates) -> {
        String buffer = line.word(); // ex: "n"
        this.agonRegister.getKeys().stream()
            .filter(name -> name.startsWith(buffer))
            .forEach(name -> candidates.add(new Candidate(name)));
      };
  }
  @Override
  public boolean execute(MatchManager match) {
    GameUserInterface ctx = this.getCtx();

    // CAS 1 : Aide spécifique (ex: help show)
    if (commandToHelp != null) {
      Optional<CmdAction> targetCmd = this.agonRegister.get(commandToHelp);

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
    for (String name : this.agonRegister.getKeys()) {
      this.agonRegister.get(name).ifPresent(cmd -> {
        ctx.showMessage(String.format("  %-12s : %s\n", name, cmd.getDescription()));
      });
    }
    ctx.showMessage("\nType 'help [command]' for detailed instructions (e.g., 'help show').\n");
    return true;
  }

  @Override
  public String getDescription() {
    return "";
  }
  @Override
  public CmdAction createNew(String[] args) {
    if (args.length == 0) {
      return new CmdHelp(this.getCtx(), this.agonRegister, null);
    }
    return new CmdHelp(this.getCtx(), this.agonRegister, args[0].toLowerCase());
  }

}



