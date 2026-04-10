package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Displays the server scoreboard. Shows statistics of players (wins, losses, games). */
public class CmdScoreboard extends Cmd {

  /** Application context. */
  private final AppContext context;

  /**
   * Constructor.
   *
   * @param userInterface user interface
   * @param context application context
   */
  public CmdScoreboard(final GameUserInterface userInterface, final AppContext context) {
    super(
        userInterface,
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
   * @return a new CmdScoreboard command
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdScoreboard(getCtx(), context);
  }

  /**
   * Executes the scoreboard command.
   *
   * @param match current match manager (unused)
   * @return true if execution succeeds, false otherwise
   */
  @Override
  public boolean execute(final MatchManager match) {
    final AgonClient client = getClient();
    boolean result = true;

    if (client.isConnected()) {
      final String response = client.requestScoreboard();

      if (response != null) {
        getCtx().showMessage(response + "\n");
      } else {
        getCtx().showError("[CLIENT] Failed to retrieve scoreboard.");
        result = false;
      }
    } else {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      result = false;
    }

    return result;
  }

  /** Returns the client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }
}
