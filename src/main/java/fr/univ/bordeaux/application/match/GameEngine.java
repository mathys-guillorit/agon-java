package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.player.Player;
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
  }

  /**
   * Starts the main application loop.
   *
   * <p>The loop runs as long as the UI is active. It identifies the current player, requests an
   * action (from the user or the AI), executes it, and updates the display.
   */
  public void start() {
    while (ui.isRunning()) {
      CmdAction action = null;

      if (this.matchManager == null || this.matchManager.isMatchOver()) {
        String input = ui.getUserInput();
        if (input == null) {
          continue;
        }

        action = UiPromptParser.parse(input, this.cmds, ui);
      } else {
        Player p = matchManager.getCurrentPlayer();
        // ui.showMessage("\n>> Current Player: " + p.getName() + " (" + p.getColor() + ")\n");
        matchManager.startTurn();

        Future<CmdAction> futureAction = playerExecutor.submit(() -> p.getAction(this.cmds));

        try {
          while (!futureAction.isDone()) {
            if (matchManager.isMatchOver()) {
              futureAction.cancel(true);
              break;
            }
            Thread.sleep(50);
          }

          if (futureAction.isDone() && !futureAction.isCancelled()) {
            action = futureAction.get();
          }
        } catch (Exception e) {
          futureAction.cancel(true);
        }
      }

      if (action != null) {
        action.execute(this.matchManager);
      } else {
        if (this.matchManager == null || !this.matchManager.isMatchOver()) {
          ui.showError("Unknown command. Type 'help' to see available commands.\n");
        }
      }
    }
    playerExecutor.shutdownNow();
  }

  /**
   * Injects a new match manager into the engine.
   *
   * @param matchManager The new MatchManager instance.
   */
  public void setMatchManager(MatchManager matchManager) {
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
