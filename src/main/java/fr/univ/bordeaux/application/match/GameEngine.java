package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;

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
        if (input == null) {
          continue;
        }

        action = UIPromptParser.parse(input, this.cmds, ui);
      } else {
        Player p = matchManager.getCurrentPlayer();
        ui.showMessage("\n>> Current Player: " + p.getName() + " (" + p.getColor() + ")\n");

        action = p.getAction(this.cmds);
      }

      if (action != null) {
        action.execute(this.matchManager);

        if (matchManager != null) {
          ui.updateBoard(matchManager.getAgonBoard());
        }
      } else {
        ui.showError("Unknown command. Type 'help' to see available commands.\n");
      }
    }
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
