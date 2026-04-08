package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to display the list of connected players on the server or the details of a specific
 * player.
 */
public class CmdPlayers extends Cmd {

  /** Application context. */
  private final AppContext context;

  /** Command arguments. */
  private final String[] args;

  /**
   * Constructor.
   *
   * @param ui user interface
   * @param context application context
   * @param args command arguments
   */
  public CmdPlayers(GameUserInterface ui, AppContext context, String[] args) {
    super(
        ui,
        "players",
        "players [PLAYER_ID]\n"
            + "Description: displays the list of connected players "
            + "or the details of a specific player.\n"
            + "Requires an active connection.\n");
    this.context = context;
    this.args = args;
  }

  /**
   * Creates a new instance of the command.
   *
   * @param args command arguments
   * @return new CmdPlayers instance
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdPlayers(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match current match manager (unused here)
   * @return true if execution succeeds
   */
  @Override
  public boolean execute(MatchManager match) {
    AgonClient client = context.getClient();

    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      return false;
    }

    String response;

    if (args == null || args.length == 0) {
      response = client.requestPlayers();
    } else {
      int playerId;

      try {
        playerId = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        getCtx().showError("[CLIENT] Invalid player id.");
        return false;
      }

      response = client.requestPlayerDetails(playerId);
    }

    if (response != null) {
      getCtx().showMessage(response + "\n");
    } else {
      getCtx().showError("[CLIENT] Failed to retrieve players.");
    }

    return true;
  }
}
