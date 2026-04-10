package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.AppMode;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdCreate;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Starts a new game. In LOCAL mode, delegates to {@link CmdCreate}. In ONLINE mode, requests a game
 * against a target player.
 */
public class CmdNew extends Cmd {

  /** Shared application context (mode, client, server, etc.). */
  private final AppContext context;

  /** Game configuration used in local mode. */
  private final GameConfig gameConfig;

  /** Game engine updated in local mode. */
  private final GameEngine gameEngine;

  /** Command arguments provided by the parser. */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param userInterface user interface context
   * @param context application context
   * @param gameConfig game configuration
   * @param gameEngine game engine
   */
  public CmdNew(
      final GameUserInterface userInterface,
      final AppContext context,
      final GameConfig gameConfig,
      final GameEngine gameEngine) {
    super(
        userInterface,
        "new",
        "new [LOCAL_OPTIONS] | new PLAYER_ID\n"
            + "Description: starts a local game in LOCAL mode,\n"
            + "or requests an online game against the specified player in ONLINE mode.\n");
    this.context = context;
    this.gameConfig = gameConfig;
    this.gameEngine = gameEngine;
  }

  /**
   * Internal constructor used when the command is executed with arguments.
   *
   * @param userInterface user interface context
   * @param context application context
   * @param gameConfig game configuration
   * @param gameEngine game engine
   * @param args command arguments
   */
  private CmdNew(
      final GameUserInterface userInterface,
      final AppContext context,
      final GameConfig gameConfig,
      final GameEngine gameEngine,
      final String[] args) {
    this(userInterface, context, gameConfig, gameEngine);
    this.args = args;
  }

  /**
   * Creates a new instance of the command with parsed arguments.
   *
   * @param args arguments passed from the command line
   * @return a new CmdNew instance
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdNew(getCtx(), context, gameConfig, gameEngine, args);
  }

  /**
   * Executes the command.
   *
   * @param match current match manager
   * @return true if execution completed successfully
   */
  @Override
  public boolean execute(final MatchManager match) {
    return run(args, match);
  }

  /**
   * Core logic of the new command.
   *
   * @param args command arguments
   * @param match current match manager
   * @return true if execution completed successfully
   */
  private boolean run(final String[] args, final MatchManager match) {
    boolean result;

    if (getMode() == AppMode.LOCAL) {
      result = runLocalMode(args, match);
    } else {
      result = runOnlineMode(args);
    }

    return result;
  }

  /**
   * Runs the command in local mode.
   *
   * @param args command arguments
   * @param match current match manager
   * @return true if execution completed successfully
   */
  private boolean runLocalMode(final String[] args, final MatchManager match) {
    final CmdCreate localCmd = new CmdCreate(getCtx(), gameConfig, gameEngine, args);
    return localCmd.execute(match);
  }

  /**
   * Runs the command in online mode.
   *
   * @param args command arguments
   * @return true if execution completed successfully
   */
  private boolean runOnlineMode(final String[] args) {
    boolean result = true;
    final AgonClient client = getClient();

    if (client == null || !client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      result = false;
    } else if (args == null || args.length < 1) {
      getCtx().showError("[CLIENT] Usage: new PLAYER_ID");
      result = false;
    } else {
      final Integer targetPlayerId = parseTargetPlayerId(args[0]);

      if (targetPlayerId == null) {
        getCtx().showError("[CLIENT] Invalid player ID: " + args[0]);
        result = false;
      } else {
        result = requestOnlineGame(client, targetPlayerId);
      }
    }

    return result;
  }

  /**
   * Sends the online new-game request.
   *
   * @param client connected client
   * @param targetPlayerId target player identifier
   * @return true if the request succeeds
   */
  private boolean requestOnlineGame(final AgonClient client, final int targetPlayerId) {
    boolean result = true;
    final String response = client.requestNewGame(targetPlayerId);

    if (response == null) {
      getCtx().showError("[CLIENT] Failed to start online game.");
      result = false;
    } else {
      getCtx().showMessage(response + "\n");
    }

    return result;
  }

  /** Returns the current application mode. */
  private AppMode getMode() {
    return context.getMode();
  }

  /** Returns the network client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }

  /** Parses the target player id, or returns null if invalid. */
  private Integer parseTargetPlayerId(final String rawPlayerId) {
    try {
      return Integer.parseInt(rawPlayerId);
    } catch (NumberFormatException exception) {
      return null;
    }
  }
}
