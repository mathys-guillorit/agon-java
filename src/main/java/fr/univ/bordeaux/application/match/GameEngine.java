package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;

/**
 * The core engine of the Agon application. This class manages the main execution loop. It switches
 * between two states:
 *
 * <ul>
 *   <li><b>Out-of-Match:</b> Where the user interacts with the system via the shell (e.g., help,
 *       load).
 *   <li><b>In-Match:</b> Where the current player (Human or AI) provides moves to progress the
 *       game.
 * </ul>
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
      CmdAction action;

      if (this.matchManager == null || this.matchManager.isMatchOver()) {
        String input = ui.getUserInput();
        if (input == null) continue;

        action = UIPromptParser.parse(input, this.cmds, ui);
      } else {

        Player p = matchManager.getCurrentPlayer();
        ui.showMessage("\n>> Current Player: " + p.getName() + " (" + p.getColor() + ")\n");

        action = p.getAction(this.cmds);
      }

      if (action != null) {

        if (appContext != null
                && appContext.isOnlineGameActive()
                && action instanceof CmdMove onlineMove) {

          if (!appContext.isMyOnlineTurn()) {
            ui.showWarn("[ONLINE] It is not your turn.\n");
            if (appContext.getCurrentOnlineMatch() != null) {
              ui.updateBoard(appContext.getCurrentOnlineMatch().getAgonBoard());
            }
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
                    onlineMove.getDestination()
            );
          }

          if (!sent) {
            ui.showError("[ONLINE] Failed to send move.\n");
            if (appContext.getCurrentOnlineMatch() != null) {
              ui.updateBoard(appContext.getCurrentOnlineMatch().getAgonBoard());
            }
          }

        } else {
          if (appContext != null
                  && appContext.isOnlineGameActive()
                  && isForbiddenOnlineCommand(action)) {
            ui.showWarn("[ONLINE] This command is disabled during an online match.\n");
            continue;
          }

          action.execute(this.matchManager);

          if (appContext != null
                  && appContext.isOnlineGameActive()
                  && appContext.getCurrentOnlineMatch() != null) {
            ui.updateBoard(appContext.getCurrentOnlineMatch().getAgonBoard());
          } else if (matchManager != null) {
            ui.updateBoard(matchManager.getAgonBoard());
          }
        }

      } else {
        ui.showError("Unknown command. Type 'help' to see available commands.\n");
      }

    }
  }

  /**
   * Injects a new match manager into the engine.
   *
   * <p>This is typically called by a "New Game" or "Load" command to transition the engine into the
   * In-Match state.
   *
   * @param matchManager The new {@link MatchManager} instance.
   */
  public void setMatchManager(MatchManager matchManager) {
    this.matchManager = matchManager;
  }

  /**
   * Displays the board of a match manager without injecting it into the main engine loop.
   *
   * <p>This is useful for online games during the initialization phase.
   * @param matchManager the match whose board should be displayed
   */
  public void previewMatch(MatchManager matchManager) {
    if (matchManager != null) {
      ui.updateBoard(matchManager.getAgonBoard());
    }
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
}
