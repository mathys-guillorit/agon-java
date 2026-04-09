package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.io.IOException;
import java.util.List;

/**
 * Lists available servers on the local network. Uses UDP discovery and displays detected servers.
 */
public class CmdServerList extends Cmd {

  /** Application context used for discovery and networking. */
  private final AppContext context;

  /**
   * Constructs the server list command.
   *
   * @param userInterface user interface used to display results
   * @param context application context used for discovery and networking
   */
  public CmdServerList(final GameUserInterface userInterface, final AppContext context) {
    super(userInterface);
    this.context = context;
    this.setName("server_list");
    this.setDesc(
        "Usage: server list\n"
            + "Description: displays available servers on the local network.\n"
            + "Uses UDP discovery to detect active servers.\n");
  }

  /**
   * Creates a new instance of the server list command.
   *
   * @param args command arguments (unused)
   * @return a new {@code CmdServerList} command
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdServerList(getCtx(), context);
  }

  /**
   * Executes the server list command.
   *
   * @param match current match manager (unused)
   * @return true if execution completes, false otherwise
   */
  @Override
  public boolean execute(final MatchManager match) {
    boolean result = true;

    if (ensureDiscoveryStarted()) {
      final List<ServerInfo> servers = getServers();

      if (servers.isEmpty()) {
        getCtx().showWarn("[SERVER] No servers found on the network.");
      } else {
        for (final ServerInfo serverInfo : servers) {
          getCtx()
              .showMessage(
                  serverInfo.name + " @ " + serverInfo.serverIp + ":" + serverInfo.tcpPort + "\n");
        }
      }
    } else {
      result = false;
    }

    return result;
  }

  /**
   * Ensures the discovery service is started.
   *
   * @return true if discovery is available, false otherwise
   */
  private boolean ensureDiscoveryStarted() {
    boolean result = true;

    try {
      context.ensureDiscoveryStarted();
    } catch (IllegalStateException exception) {
      getCtx().showError("[SERVER] Discovery error: " + exception.getMessage());
      result = false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return result;
  }

  /** Returns the discovered servers list. */
  private List<ServerInfo> getServers() {
    final var discovery = context.getDiscovery();
    return discovery.getServers();
  }
}
