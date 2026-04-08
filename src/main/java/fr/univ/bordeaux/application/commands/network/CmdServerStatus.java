package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to display the current status of the server.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>{@code server_status}
 * </ul>
 *
 * <p>This command:
 *
 * <ul>
 *   <li>Displays local server information if a local server is running
 *   <li>Requests remote server status if connected as a client
 *   <li>Shows port, number of connected clients, connected players, and active games
 * </ul>
 */
public class CmdServerStatus extends Cmd {

  /** Shared application context. */
  private final AppContext context;

  /**
   * Constructor.
   *
   * @param ui user interface
   * @param context application context
   */
  public CmdServerStatus(GameUserInterface ui, AppContext context) {
    super(
        ui,
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
  public CmdAction createNew(String[] args) {
    return new CmdServerStatus(getCtx(), context);
  }

  /**
   * Executes the command.
   *
   * @param match current match manager (unused here)
   * @return true if execution completed
   */
  @Override
  public boolean execute(MatchManager match) {

    AgonServer server = context.getServer();

    // 1. Local server is running
    if (server != null && server.isRunning()) {
      getCtx().showMessage("=== SERVER STATUS ===\n");
      getCtx().showMessage("Port: " + server.getPort() + "\n");
      getCtx().showMessage("Connected Clients: " + server.getConnectedClientsCount() + "\n");
      getCtx().showMessage("Games In Progress: " + server.getActiveGameCount() + "\n");
      getCtx().showMessage("=====================\n");
      return true;
    }

    // 2. Remote server status through connected client
    AgonClient client = context.getClient();

    if (client != null && client.isConnected()) {
      String response = client.requestServerStatus();

      if (response == null) {
        getCtx().showError("[CLIENT] Failed to retrieve server status.");
        return false;
      }

      getCtx().showMessage(response + "\n");
      return true;
    }

    // 3. No local server and no remote connection
    getCtx().showWarn("[SERVER] No server is currently running.");
    return false;
  }
}
