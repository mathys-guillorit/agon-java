package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command used to display the current status of the server. */
public class CmdServerStatus extends Cmd {

  /** Shared application context. */
  private final AppContext context;

  /**
   * Constructor.
   *
   * @param userInterface user interface
   * @param context application context
   */
  public CmdServerStatus(final GameUserInterface userInterface, final AppContext context) {
    super(
        userInterface,
        "server_status",
        "server_status\n"
            + "Description: displays the status of the local server,\n"
            + "or the connected remote server if no local server is running.\n");
    this.context = context;
  }

  /**
   * Creates a new command instance.
   *
   * @param args command arguments
   * @return a new CmdServerStatus instance
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdServerStatus(getCtx(), context);
  }

  /**
   * Executes the command.
   *
   * @param match current match manager (unused here)
   * @return true if execution completed
   */
  @Override
  public boolean execute(final MatchManager match) {
    boolean result = false;
    final AgonServer server = getServer();

    if (server != null && server.isRunning()) {
      showLocalServerStatus(server);
      result = true;
    } else {
      final AgonClient client = getClient();

      if (client != null && client.isConnected()) {
        final String response = client.requestServerStatus();

        if (response == null) {
          getCtx().showError("[CLIENT] Failed to retrieve server status.");
        } else {
          getCtx().showMessage(response + "\n");
          result = true;
        }
      } else {
        getCtx().showWarn("[SERVER] No server is currently running.");
      }
    }

    return result;
  }

  /** Returns the local server from the application context. */
  private AgonServer getServer() {
    return context.getServer();
  }

  /** Returns the client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }

  /** Displays the status of the local running server. */
  private void showLocalServerStatus(final AgonServer server) {
    getCtx().showMessage("=== SERVER STATUS ===\n");
    getCtx().showMessage("Port: " + server.getPort() + "\n");
    getCtx().showMessage("Connected Clients: " + server.getConnectedClientsCount() + "\n");
    getCtx().showMessage("Games In Progress: " + server.getActiveGameCount() + "\n");
    getCtx().showMessage("=====================\n");
  }
}
