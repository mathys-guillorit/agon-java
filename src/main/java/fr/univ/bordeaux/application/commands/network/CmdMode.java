package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Locale;

/**
 * Command used to choose the game mode in a lobby. Supported modes: {@code mode normal} and {@code
 * mode blitz}.
 */
public class CmdMode extends Cmd {

  /** Shared application context (client, server, etc.). */
  private final AppContext context;

  /** Command arguments (provided by parser). */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param userInterface User interface context
   * @param context Application context
   */
  public CmdMode(final GameUserInterface userInterface, final AppContext context) {
    super(
        userInterface,
        "mode",
        "mode <normal|blitz>\n" + "Description: chooses the game mode in the current lobby.\n");
    this.context = context;
  }

  /** Internal constructor used when the command is executed with arguments. */
  private CmdMode(
      final GameUserInterface userInterface, final AppContext context, final String[] args) {
    this(userInterface, context);
    this.args = args;
  }

  /** Creates a new instance of the command with parsed arguments. */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdMode(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match Not used (network command independent from game state)
   */
  @Override
  public boolean execute(final MatchManager match) {
    return run(args);
  }

  /**
   * Core logic of the mode command.
   *
   * @param args Command arguments
   * @return true if execution completed
   */
  private boolean run(final String[] args) {
    final AgonClient client = getClient();
    boolean result = true;

    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected.\n");
      result = false;
    } else if (args == null || args.length == 0 || args[0].isBlank()) {
      getCtx().showWarn("[CLIENT] Missing mode. Use: mode normal | mode blitz\n");
      result = false;
    } else {
      final String mode = args[0].trim().toLowerCase(Locale.ROOT);

      if (!"normal".equals(mode) && !"blitz".equals(mode)) {
        getCtx().showWarn("[CLIENT] Invalid mode. Use: mode normal | mode blitz\n");
        result = false;
      } else if (client.chooseMode(mode)) {
        getCtx().showMessage("[CLIENT] Mode request sent: " + mode + "\n");
      } else {
        getCtx().showError("[CLIENT] Failed to send mode request.");
      }
    }

    return result;
  }

  /** Returns the network client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }
}
