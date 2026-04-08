package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to display the scoreboard of the server.
 *
 * <p>This command retrieves and displays the statistics of all players who have played on the
 * server (wins, losses, games).
 */
public class CmdScoreboard extends Cmd {

  /** Application context. */
  private final AppContext context;

  /**
   * Constructor.
   *
   * @param ui user interface
   * @param context application context
   */
  public CmdScoreboard(GameUserInterface ui, AppContext context) {
    super(
        ui,
        "scoreboard",
        "scoreboard\n"
            + "Description: displays the scoreboard of the server.\n"
            + "Requires an active connection.\n");
    this.context = context;
  }

  /**
   * Creates a new instance of the scoreboard command.
   *
   * @param args command arguments (unused)
   * @return a new {@code CmdScoreboard} command
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdScoreboard(getCtx(), context);
  }

  /**
   * Executes the scoreboard command.
   *
   * <p>This method checks whether the client is connected, sends a request to retrieve the server
   * scoreboard, and displays the result to the user.
   *
   * @param match current match manager (unused)
   * @return true if the command executed, false if the client is not connected
   */
  @Override
  public boolean execute(MatchManager match) {

    AgonClient client = context.getClient();

    // Check connection
    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      return false;
    }

    // Request scoreboard
    String response = client.requestScoreboard();

    if (response != null) {
      getCtx().showMessage(response + "\n");
    } else {
      getCtx().showError("[CLIENT] Failed to retrieve scoreboard.");
    }

    return true;
  }
}
