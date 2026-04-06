package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to choose the game mode in a lobby.
 *
 * <p>Supported modes:
 *
 * <ul>
 *   <li>{@code mode normal}
 *   <li>{@code mode blitz}
 * </ul>
 */
public class CmdMode extends Cmd {

  /** Shared application context (client, server, etc.). */
  private final AppContext context;

  /** Command arguments (provided by parser). */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param ui User interface context
   * @param context Application context
   */
  public CmdMode(GameUserInterface ui, AppContext context) {
    super(ui);
    this.context = context;
    this.setName("mode");
    this.setDesc(
        "Usage: mode <normal|blitz>\n"
            + "Description: chooses the game mode in the current lobby.\n");
  }

  /** Internal constructor used when the command is executed with arguments. */
  private CmdMode(GameUserInterface ui, AppContext context, String[] args) {
    this(ui, context);
    this.args = args;
  }

  /** Creates a new instance of the command with parsed arguments. */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdMode(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match Not used (network command independent from game state)
   */
  @Override
  public boolean execute(MatchManager match) {
    return run(args);
  }

  /**
   * Core logic of the mode command.
   *
   * @param args Command arguments
   * @return true if execution completed
   */
  private boolean run(String[] args) {
    AgonClient client = context.getClient();

    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected.\n");
      return false;
    }

    if (args == null || args.length == 0 || args[0].isBlank()) {
      getCtx().showWarn("[CLIENT] Missing mode. Use: mode normal | mode blitz\n");
      return false;
    }

    String mode = args[0].trim().toLowerCase();

    if (!"normal".equals(mode) && !"blitz".equals(mode)) {
      getCtx().showWarn("[CLIENT] Invalid mode. Use: mode normal | mode blitz\n");
      return false;
    }

    if (client.chooseMode(mode)) {
      getCtx().showMessage("[CLIENT] Mode request sent: " + mode + "\n");
    } else {
      getCtx().showError("[CLIENT] Failed to send mode request.");
    }

    return true;
  }
}
