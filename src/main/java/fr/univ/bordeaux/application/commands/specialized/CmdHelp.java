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

/**
 * Command responsible for displaying help information to the user. This command can operate in two
 * modes:
 *
 * <ul>
 *   <li><b>Global Help:</b> Lists all available commands registered in the system.
 *   <li><b>Targeted Help:</b> Provides the specific description and usage of a given command (e.g.,
 *       {@code help load}).
 * </ul>
 */
public final class CmdHelp extends Cmd {

  /** The specific command name for which the user is seeking help (optional). */
  private String commandToHelp = null;

  /** The registry containing all commands available in the application. */
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

    // Dynamically add all registered command names as valid options for the help command
    cmds.getKeys()
        .forEach(
            cmdName -> {
              this.addOption(Option.builder(cmdName).get());
            });

    this.setDesc("Description: display help, show this help with \"help help\"");
    this.setName("help");
  }

  /**
   * Internal constructor used by {@link #createNew(String[])} to create an executable instance with
   * a specific target.
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
   * Provides a custom {@link Completer} for the help command.
   *
   * <p>This completer suggests names of other registered commands to assist the user in typing
   * {@code help [command]}.
   *
   * @return A non-null JLine {@link Completer}.
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
   * <p>If {@code commandToHelp} is set, it attempts to find and display that specific command's
   * description. Otherwise, it iterates through the registry to list all available commands.
   *
   * @param match The current match manager (unused by the help command).
   * @return Always true, as help display is always considered successful.
   */
  @Override
  public boolean execute(MatchManager match) {
    GameUserInterface ctx = this.getCtx();

    // CASE 1: Targeted Help (e.g., help show)
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

    // CASE 2: Global Help
    ctx.showMessage("======= AVAILABLE COMMANDS =======\n");
    for (String name : this.agonRegister.getKeys()) {
      this.agonRegister
          .get(name)
          .ifPresent(
              cmd -> {
                ctx.showMessage(String.format("  %-12s : %s\n", name, cmd.getDescription()));
              });
    }
    ctx.showMessage("\nType 'help [command]' for detailed instructions (e.g., 'help show').\n");
    return true;
  }

  /**
   * Provides the short description for the help command itself.
   *
   * @return An empty string (description is managed via {@code setDesc} in constructor).
   */
  @Override
  public String getDescription() {
    return "";
  }

  /**
   * Factory method to create a new instance of {@code CmdHelp} based on user input.
   *
   * @param args The arguments passed after the 'help' keyword.
   * @return A new {@link CmdAction} targeting either a specific command or global help.
   */
  @Override
  public CmdAction createNew(String[] args) {
    if (args.length == 0) {
      return new CmdHelp(this.getCtx(), this.agonRegister, null);
    }
    return new CmdHelp(this.getCtx(), this.agonRegister, args[0].toLowerCase());
  }
}
