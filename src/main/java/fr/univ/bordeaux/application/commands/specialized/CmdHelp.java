package fr.univ.bordeaux.application.commands.specialized;

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

/** Command responsible for displaying help information to the user. */
public final class CmdHelp extends Cmd {

  private String commandToHelp = null;

  private AgonRegister<CmdAction> agonRegister;

  /**
   * Constructs the base Help command and populates CLI options based on currently registered
   * commands.
   *
   * @param uictx The user interface context for displaying help messages.
   * @param cmds The registry of commands used to generate the help list.
   */
  public CmdHelp(GameUserInterface uictx, AgonRegister<CmdAction> cmds) {
    super(uictx);
    this.agonRegister = cmds;

    cmds.getKeys()
        .forEach(
            cmdName -> {
              this.addOption(Option.builder(cmdName).get());
            });

    this.setDesc("Description: display help, show this help with \"help help\"");
    this.setName("help");
  }

  /**
   * Internal constructor used to create an executable instance with a specific target.
   *
   * @param uictx The user interface context.
   * @param cmds The command registry.
   * @param commandToHelp The specific command name to provide help for.
   */
  private CmdHelp(GameUserInterface uictx, AgonRegister<CmdAction> cmds, String commandToHelp) {
    this(uictx, cmds);
    this.commandToHelp = commandToHelp;
  }

  /**
   * Provides a custom Completer for the help command.
   *
   * @return A non-null JLine Completer.
   */
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return (reader, line, candidates) -> {
      String buffer = line.word();
      this.agonRegister.getKeys().stream()
          .filter(name -> name.startsWith(buffer))
          .forEach(name -> candidates.add(new Candidate(name)));
    };
  }

  /**
   * Executes the help logic.
   *
   * @param match The current match manager (unused by the help command).
   * @return Always true, as help display is always considered successful.
   */
  @Override
  public boolean execute(MatchManager match) {
    GameUserInterface ctx = this.getCtx();

    if (commandToHelp != null) {
      Optional<CmdAction> targetCmd = this.agonRegister.get(commandToHelp);

      if (targetCmd.isPresent()) {
        ctx.showMessage("======= HELP: " + commandToHelp.toUpperCase() + " =======\n");
        ctx.showMessage(targetCmd.get().getDescription());
        return true;
      } else {
        ctx.showMessage("Unknown command: " + commandToHelp + "\n");
      }
    }

    ctx.showMessage("======= AVAILABLE COMMANDS =======\n");
    for (String name : this.agonRegister.getKeys()) {
      this.agonRegister
          .get(name)
          .ifPresent(
              cmd -> {
                ctx.showMessage(String.format("  %-8s : %s\n", name, cmd.getDescription()));
              });
    }
    ctx.showMessage("\nType 'help [command]' for detailed instructions (e.g., 'help show').\n");
    return true;
  }

  /**
   * Provides the short description for the help command itself.
   *
   * @return An empty string.
   */
  @Override
  public String getDescription() {
    return "Usage: help\n"
        + "Description: display all commands available and their usage or for a specific command.\n"
        + "Example: help new";
  }

  /**
   * Factory method to create a new instance of CmdHelp based on user input.
   *
   * @param args The arguments passed after the 'help' keyword.
   * @return A new CmdAction targeting either a specific command or global help.
   */
  @Override
  public CmdAction createNew(String[] args) {
    if (args.length == 0) {
      return new CmdHelp(this.getCtx(), this.agonRegister, null);
    }
    return new CmdHelp(this.getCtx(), this.agonRegister, args[0].toLowerCase());
  }
}
