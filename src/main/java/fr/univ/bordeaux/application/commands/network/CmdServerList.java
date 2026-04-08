package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.List;

/**
 * Command used to list available servers on the local network.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>{@code server_list}
 * </ul>
 *
 * <p>This command:
 *
 * <ul>
 *   <li>Ensures discovery is running
 *   <li>Retrieves detected servers
 *   <li>Displays them to the user
 * </ul>
 */
public class CmdServerList extends Cmd {

  private final AppContext context;

  /**
   * Constructs a command to list available servers on the local network.
   *
   * @param ui the user interface instance to display the results.
   * @param context the application context used for UDP discovery and networking.
   */
  public CmdServerList(GameUserInterface ui, AppContext context) {
    super(
        ui,
        "server_list",
        "server list\n"
            + "Description: displays available servers on the local network.\n"
            + "Uses UDP discovery to detect active servers.\n");
    this.context = context;
  }

  /**
   * Creates a new instance of the server list command.
   *
   * @param args command arguments (unused)
   * @return a new {@code CmdServerList} command
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdServerList(getCtx(), context);
  }

  /**
   * Executes the server list command.
   *
   * <p>This method ensures that the discovery service is running, retrieves the list of available
   * servers on the local network, and displays them to the user.
   *
   * @param match current match manager (unused)
   * @return true if execution completes, false if discovery fails
   */
  @Override
  public boolean execute(MatchManager match) {

    // Ensure discovery process is active
    try {
      context.ensureDiscoveryStarted();
    } catch (Exception e) {
      getCtx().showError("[SERVER] Discovery error: " + e.getMessage());
      return false;
    }

    List<ServerInfo> servers = context.getDiscovery().getServers();

    // Display results
    if (servers.isEmpty()) {
      getCtx().showWarn("[SERVER] No servers found on the network.");
    } else {
      for (ServerInfo s : servers) {
        getCtx().showMessage(s.name + " @ " + s.ip + ":" + s.tcpPort + "\n");
      }
    }

    return true;
  }
}
