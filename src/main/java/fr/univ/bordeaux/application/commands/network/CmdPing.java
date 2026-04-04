package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to measure the round-trip time (RTT) between the client and server.
 *
 * <p>This command sends a "PING" request and displays the response time.
 */
public class CmdPing extends Cmd {

  /** Application context. */
  private final AppContext context;

  /** Constructor. */
  public CmdPing(GameUserInterface ui, AppContext context) {
    super(ui);
    this.context = context;
    this.setName("ping");
    this.setDesc(
        "Usage: ping\n"
            + "Description: measures the latency (RTT) with the connected server.\n"
            + "Requires an active connection.\n");
  }

  /**
   * Creates a new instance of the ping command.
   *
   * @param args command arguments (unused)
   * @return a new {@code CmdPing} command
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdPing(getCtx(), context);
  }

  /**
   * Executes the ping command.
   *
   * @param match current match manager (unused)
   * @return true if the command was executed, false if the client is not connected
   */
  @Override
  public boolean execute(MatchManager match) {

    AgonClient client = context.getClient();

    // Ensure client is connected
    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      return false;
    }

    // Send ping request
    String response = client.pingRttMs();

    if (response != null) {
      getCtx().showMessage(response + "\n");
    } else {
      getCtx().showError("[CLIENT] Connection lost.");
    }

    return true;
  }
}
