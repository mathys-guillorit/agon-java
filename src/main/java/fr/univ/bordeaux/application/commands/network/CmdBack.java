package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command used to set the local player status back to idle on the server. */
public class CmdBack extends Cmd {

  /** Application context. */
  private final AppContext context;

  /**
   * Constructor.
   *
   * @param userInterface user interface
   * @param context application context
   */
  public CmdBack(final GameUserInterface userInterface, final AppContext context) {
    super(userInterface);
    this.context = context;

    this.setName("back");
    this.setDesc(
        "Usage: back\n"
            + "Description: sets your status back to idle on the server.\n"
            + "Requires an active connection.\n");
  }

  /**
   * Creates a new instance of the command.
   *
   * @param args command arguments
   * @return new CmdBack instance
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdBack(getCtx(), context);
  }

  /**
   * Executes the command.
   *
   * @param match current match manager (unused here)
   * @return true if execution succeeds
   */
  @Override
  public boolean execute(final MatchManager match) {
    final AgonClient client = getClient();
    final String response;
    boolean result = true;

    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      result = false;
    } else {
      response = client.requestBackStatus();

      if (response != null) {
        getCtx().showMessage(response + "\n");
      } else {
        getCtx().showError("[CLIENT] Failed to reset player status.");
      }
    }

    return result;
  }

  /** Returns the client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }
}
