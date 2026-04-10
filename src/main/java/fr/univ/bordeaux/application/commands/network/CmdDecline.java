package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command used to decline a pending invitation. */
public class CmdDecline extends Cmd {

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
  public CmdDecline(final GameUserInterface userInterface, final AppContext context) {
    super(userInterface, "decline", "decline\nDescription: declines the current invitation.\n");
    this.context = context;
  }

  /** Internal constructor used when the command is executed with arguments. */
  private CmdDecline(
      final GameUserInterface userInterface, final AppContext context, final String[] args) {
    this(userInterface, context);
    this.args = args;
  }

  /** Creates a new instance of the command with parsed arguments. */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdDecline(getCtx(), context, args);
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
   * Core logic of the decline command.
   *
   * @param args Command arguments
   * @return true if execution completed
   */
  @SuppressWarnings("PMD.UnusedFormalParameter")
  private boolean run(final String[] args) {
    final AgonClient client = getClient();
    boolean result = true;

    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected.\n");
      result = false;
    } else if (client.declineInvitation()) {
      getCtx().showMessage("[CLIENT] Decline request sent.\n");
    } else {
      getCtx().showError("[CLIENT] Failed to send decline request.");
    }

    return result;
  }

  /** Returns the network client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }
}
