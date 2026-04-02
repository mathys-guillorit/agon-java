package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UiPromptParser;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The core engine of the Agon application. This class manages the main execution loop.
 * It switches between Out-of-Match and In-Match states.
 */
public class GameEngine {

  /** The UI context used for inputs and displays. */
  private final GameUserInterface ui;

  /** The current match manager. If null, the engine stays in the menu state. */
  private MatchManager matchManager;

  /** The registry containing all available CLI commands. */
  private final AgonRegister<CmdAction> cmds;

  /** Shared application context, used to detect online game state. */
  private AppContext appContext;

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

        if (appContext != null
                && appContext.isOnlineGameActive()
                && action instanceof CmdMove onlineMove) {

          if (!appContext.isMyOnlineTurn()) {
            ui.showWarn("[ONLINE] It is not your turn.\n");
            refreshOnlineBoard();
            continue;
          }

          boolean sent;

          if (onlineMove.getFrom() == -1) {
            String rawMove =
                    CoordinateMapper.toAbaPro(onlineMove.getDestination()).toLowerCase();
            sent = appContext.getClient().sendRawMove(rawMove);
          } else {
            sent = appContext.getClient().sendMove(
                    onlineMove.getFrom(),
                    onlineMove.getDestination());
          }

          if (!sent) {
            ui.showError("[ONLINE] Failed to send move.\n");
            refreshOnlineBoard();
          }

        } else {

          if (appContext != null
                  && appContext.isOnlineGameActive()
                  && isForbiddenOnlineCommand(action)) {
            ui.showWarn("[ONLINE] This command is disabled during an online match.\n");
            refreshOnlineBoard();
            continue;
          }

          action.execute(this.matchManager);
          if (appContext != null
                  && appContext.isOnlineGameActive()
                  && appContext.getCurrentOnlineMatch() != null) {
            refreshOnlineBoard();
          }
        }

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

  /**
   * Displays the board of a match manager without injecting it into the main engine loop.
   *
   * @param matchManager the match whose board should be displayed
   */
  public void previewMatch(MatchManager matchManager) {
    if (matchManager == null) {
      return;
    }

    if (ui instanceof fr.univ.bordeaux.ui.cli.AgonShell shell) {
      if (appContext != null && appContext.isOnlineGameActive()) {
        shell.setBoardFooter(
                appContext.isMyOnlineTurn()
                        ? "[ONLINE] Your turn"
                        : "[ONLINE] Opponent turn");
      } else {
        shell.setBoardFooter("");
      }
    }

    ui.onMatchUpdate((ReadOnlyMatch) matchManager);

  }

  /**
   * Registers the shared application context used by this engine.
   *
   * @param appContext the application context
   */
  public void setAppContext(AppContext appContext) {
    this.appContext = appContext;
  }

  public void clearBoardPreview() {
    if (ui instanceof fr.univ.bordeaux.ui.cli.AgonShell shell) {
      shell.setBoardFooter("");
      shell.clearBoardDisplay();
    }
  }

  private boolean isForbiddenOnlineCommand(CmdAction action) {
    if (action == null) {
      return false;
    }

    String name = action.getName();
    if (name == null) {
      return false;
    }

    return name.equalsIgnoreCase("undo")
            || name.equalsIgnoreCase("redo")
            || name.equalsIgnoreCase("pause")
            || name.equalsIgnoreCase("save")
            || name.equalsIgnoreCase("load");
  }

  private void refreshOnlineBoard() {
    if (appContext != null && appContext.getCurrentOnlineMatch() != null) {
      previewMatch(appContext.getCurrentOnlineMatch());
    }
  }

  private void refreshCurrentBoard() {
    if (appContext != null
            && appContext.isOnlineGameActive()
            && appContext.getCurrentOnlineMatch() != null) {
      previewMatch(appContext.getCurrentOnlineMatch());
    } else if (matchManager != null) {
      previewMatch(matchManager);
    }
  }
}