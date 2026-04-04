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
 * Command used to start a new game.
 *
 * <p>This command behaves differently depending on the current application mode:
 *
 * <ul>
 *   <li>LOCAL mode → starts a local game using the existing {@link CmdCreate} logic
 *   <li>ONLINE mode → sends a request to the server to start a game with another player
 * </ul>
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>{@code new [LOCAL_OPTIONS]} → starts a local game
 *   <li>{@code new PLAYER_ID} → starts an online game with the specified player
 * </ul>
 *
 * <p>This command:
 *
 * <ul>
 *   <li>Checks the current application mode
 *   <li>Delegates to {@link CmdCreate} in local mode
 *   <li>Validates the target player ID in online mode
 *   <li>Sends a NEW request to the connected server
 * </ul>
 */
public class CmdNew extends Cmd {

  /** Shared application context (contains mode, client, server, etc.). */
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
   * @param ui user interface context
   * @param context application context
   * @param gameConfig game configuration
   * @param gameEngine game engine
   */
  public CmdNew(
      GameUserInterface ui, AppContext context, GameConfig gameConfig, GameEngine gameEngine) {
    super(ui);
    this.context = context;
    this.gameConfig = gameConfig;
    this.gameEngine = gameEngine;

    this.setName("new");
    this.setDesc(
        "Usage: new [LOCAL_OPTIONS] | new PLAYER_ID\n"
            + "Description: starts a local game in LOCAL mode,\n"
            + "or requests an online game against the specified player in ONLINE mode.\n");
  }

  /**
   * Internal constructor used when the command is executed with arguments.
   *
   * @param ui user interface context
   * @param context application context
   * @param gameConfig game configuration
   * @param gameEngine game engine
   * @param args command arguments
   */
  private CmdNew(
      GameUserInterface ui,
      AppContext context,
      GameConfig gameConfig,
      GameEngine gameEngine,
      String[] args) {
    this(ui, context, gameConfig, gameEngine);
    this.args = args;
  }

  /**
   * Creates a new instance of the command with parsed arguments.
   *
   * @param args arguments passed from the command line
   * @return a new CmdNew instance
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdNew(getCtx(), context, gameConfig, gameEngine, args);
  }

  /**
   * Executes the command.
   *
   * @param match current match manager
   * @return true if execution completed successfully
   */
  @Override
  public boolean execute(MatchManager match) {
    return run(args, match);
  }

  /**
   * Core logic of the new command.
   *
   * <p>In LOCAL mode, this delegates to {@link CmdCreate}. In ONLINE mode, this validates the
   * arguments and sends a request to the server.
   *
   * @param args command arguments
   * @param match current match manager
   * @return true if execution completed successfully
   */
  private boolean run(String[] args, MatchManager match) {

    // 1. LOCAL MODE
    if (context.getMode() == AppMode.LOCAL) {
      CmdCreate localCmd = new CmdCreate(getCtx(), gameConfig, gameEngine, args);
      return localCmd.execute(match);
    }

    // 2. ONLINE MODE → client must be connected
    AgonClient client = context.getClient();

    if (client == null || !client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected. Use join first.");
      return false;
    }

    if (args == null || args.length < 1) {
      getCtx().showError("[CLIENT] Usage: new PLAYER_ID");
      return false;
    }

    int targetPlayerId;
    try {
      targetPlayerId = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      getCtx().showError("[CLIENT] Invalid player ID: " + args[1]);
      return false;
    }

    String response = client.requestNewGame(targetPlayerId);

    if (response == null) {
      getCtx().showError("[CLIENT] Failed to start online game.");
      return false;
    }

    getCtx().showMessage(response + "\n");
    return true;
  }
}
