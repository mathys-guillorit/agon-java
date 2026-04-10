package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Arrays;

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
   * @param userInterface user interface
   * @param context application context
   * @param args command arguments
   */
  public CmdPlayers(
      final GameUserInterface userInterface, final AppContext context, final String[] args) {
    super(
        userInterface,
        "players",
        "players [PLAYER_ID]\n"
            + "Description: displays the list of connected players "
            + "or the details of a specific player.\n"
            + "Requires an active connection.\n");
    this.context = context;
    this.args = args == null ? new String[0] : Arrays.copyOf(args, args.length);
  }

  /**
   * Creates a new instance of the command.
   *
   * @param args command arguments
   * @return new CmdPlayers instance
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdPlayers(getCtx(), context, args);
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
    boolean result = true;

    if (client.isConnected()) {
      final String response = getPlayersResponse(client);

      if (response != null) {
        getCtx().showMessage(response + "\n");
      } else {
        getCtx().showError("[CLIENT] Failed to retrieve players.");
        result = false;
      }
    } else {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      result = false;
    }

    return result;
  }

  /** Returns the network client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }

  /** Returns the appropriate server response for the players request. */
  private String getPlayersResponse(final AgonClient client) {
    String response = null;

    if (args.length == 0) {
      response = client.requestPlayers();
    } else {
      final Integer playerId = parsePlayerId(args[0]);

      if (playerId == null) {
        getCtx().showError("[CLIENT] Invalid player id.");
      } else {
        response = client.requestPlayerDetails(playerId);
      }
    }

    return response;
  }

  /** Parses a player id or returns null if invalid. */
  private Integer parsePlayerId(final String rawPlayerId) {
    try {
      return Integer.parseInt(rawPlayerId);
    } catch (NumberFormatException exception) {
      return null;
    }
  }
}
