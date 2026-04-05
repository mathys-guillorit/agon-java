package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UiPromptParser;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The core engine of the Agon application. This class manages the main execution loop. It switches
 * between Out-of-Match and In-Match states.
 */
public class GameEngine {

  /** The UI context used for inputs and displays. */
  private final GameUserInterface ui;

  /** The current match manager. If null, the engine stays in the menu state. */
  private MatchManager matchManager;

  /** The registry containing all available CLI commands. */
  private final AgonRegister<CmdAction> cmds;

  /** Executor for running player actions (Human or AI) asynchronously. */
  private final ExecutorService playerExecutor =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("PlayerActionThread");
            return t;
          });

  /**
   * Constructs the game engine with the required UI and command registry.
   *
   * @param ui The interface for user interaction.
   * @param cmds The registry of available commands.
   */
  public GameEngine(GameUserInterface ui, AgonRegister<CmdAction> cmds) {
    this.ui = ui;
    this.cmds = cmds;
    GameLogger.info("GameEngine: initialized with UI and " + cmds.getKeys().size() + " commands.");
  }

  /**
   * Starts the main application loop.
   *
   * <p>The loop runs as long as the UI is active. It identifies the current player, requests an
   * action (from the user or the AI), executes it, and updates the display.
   */
  public void start() {
    GameLogger.info("Game Engine started.");
    while (ui.isRunning()) {
      CmdAction action = null;

      if (this.matchManager == null || this.matchManager.isMatchOver()) {
        GameLogger.debug("GameEngine: State = Out-of-Match. Waiting for menu command...");
        String input = ui.getUserInput();
        if (input == null) {
          continue;
        }

        action = UiPromptParser.parse(input, this.cmds, ui);
      } else {
        Player p = matchManager.getCurrentPlayer();
        GameLogger.info("GameEngine: Current turn -> " + p.getName() + " (" + p.getColor() + ")");
        matchManager.startTurn();

        Future<CmdAction> futureAction = playerExecutor.submit(() -> {
          try {
            return p.getAction(this.cmds);
          } catch (Exception e) {
            GameLogger.error("GameEngine: Error during player " + p.getName() + " action: " + e.getMessage());
            return null;
          }
        });

        try {
          while (!futureAction.isDone()) {
            if (matchManager.isMatchOver()) {
              GameLogger.debug("GameEngine: Match ended while waiting for action. Cancelling task.");
              futureAction.cancel(true);
              break;
            }
            Thread.sleep(50);
          }

          if (futureAction.isDone() && !futureAction.isCancelled()) {
            action = futureAction.get();
          }
        } catch (Exception e) {
          GameLogger.error("GameEngine: Exception during action wait loop: " + e.getMessage());
          futureAction.cancel(true);
        }
      }

      if (action != null) {
        GameLogger.info("GameEngine: Executing action [" + action.getClass().getSimpleName() + "]");
        try {
          action.execute(this.matchManager);
        } catch (Exception e) {
          GameLogger.error("GameEngine: Critical error during execution: " + e.getMessage());
          ui.showError("An internal error occurred while executing the action.");
        }
      }
    }
    GameLogger.info("Game Engine stopped.");
    playerExecutor.shutdownNow();
  }

  /**
   * Injects a new match manager into the engine.
   *
   * @param matchManager The new MatchManager instance.
   */
  public void setMatchManager(MatchManager matchManager) {
    if (matchManager != null) {
      GameLogger.info("GameEngine: New MatchManager set. Match starting.");
    } else {
      GameLogger.info("GameEngine: MatchManager cleared.");
    }
    this.matchManager = matchManager;
  }

  /**
   * Returns the current match manager.
   *
   * @return The MatchManager instance, or null if no match is active.
   */
  public MatchManager getMatchManager() {
    return this.matchManager;
  }
}