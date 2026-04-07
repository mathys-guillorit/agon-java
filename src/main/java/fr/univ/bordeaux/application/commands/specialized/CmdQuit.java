package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command responsible for handling exit behavior in the application.
 *
 * <p>This command supports three hierarchical exit levels:
 *
 * <ul>
 *   <li><b>Match level:</b> If a game is currently running, it will be terminated.
 *   <li><b>Network level:</b> If connected to a remote server, the client will disconnect.
 *   <li><b>Application level:</b> If no match and no connection exist, the application will close.
 * </ul>
 *
 * <p>This ensures a consistent and intuitive user experience across local and network modes.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>{@code quit}
 * </ul>
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>The command does not require any arguments.
 *   <li>The behavior adapts dynamically depending on the current state.
 * </ul>
 */
public class CmdQuit extends Cmd {

  /** Shared application context (used for network state access). */
  private final AppContext context;

  /**
   * Constructs a new Quit command.
   *
   * @param uictx The user interface context
   * @param context The application context (network + global state)
   */
  public CmdQuit(GameUserInterface uictx, AppContext context) {
    super(uictx);
    this.context = context;
    this.setName("quit");
  }

  /**
   * Provides the usage and description for the quit command.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Usage: quit (or Ctrl+C)\n");
    sb.append("Description: Exits the application.");
    sb.append("If connected to a server: disconnects from it.");
    sb.append(" You will be prompted to save your current ");
    sb.append("progress before leaving.\n");
    return sb.toString();
  }

  /**
   * Executes the shutdown sequence.
   *
   * <ul>
   *   <li>if connected → disconnect from server
   *   <li>Else → exit application
   * </ul>
   *
   * @param match The current match manager (may be null)
   * @return true if the command executed successfully
   */
  @Override
  public boolean execute(MatchManager match) {

    AgonClient client = context.getClient();

    // ===== CASE 1: ONLINE MATCH RUNNING =====
    if (context.isOnlineGameActive() && context.getCurrentOnlineMatch() != null) {
      client.resignGame();
      return true;
    }

    // ===== CASE 2: LOCAL MATCH RUNNING =====
    if (match != null && !match.isMatchOver() && !match.isSaved()) {
      boolean resolved = false;

      while (!resolved) {
        this.getCtx().showMessage("Save the game before quitting? [y/N] \n");
        String response = this.getCtx().getUserInput();
        GameLogger.debug("User response for save before quit: " + response);
        if (response != null && (response.equalsIgnoreCase("y"))) {
          this.getCtx().showMessage("Enter filename: \n");
          String filename = this.getCtx().getUserInput();

          if (filename == null || filename.trim().isEmpty()) {
            filename = "default_save";
          }

          CmdSave saveCmd = new CmdSave(this.getCtx());
          saveCmd.createNew(new String[] {filename}).execute(match);

          if (match.isSaved()) {
            resolved = true;
          } else {
            this.getCtx().showMessage("Save failed. Try again.\n");
          }
        } else {
          resolved = true;
        }
      }

      match.quit();
      this.getCtx().quit();
      return true;
    }

    // ===== CASE 3: CLIENT CONNECTED TO SERVER =====
    if (client != null && client.isConnected()) {
      client.quit();
      this.getCtx().showMessage("[CLIENT] Disconnected from server.\n");
      return true;
    }

    // ===== CASE 4: EXIT APPLICATION =====
    this.getCtx().showMessage("[APP] Exiting application.\n");
    this.getCtx().quit();
    return true;
  }

  /**
   * Factory method to create a new executable instance of the command.
   *
   * @param args CLI arguments (ignored)
   * @return A new CmdQuit instance
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdQuit(super.getCtx(), context);
  }
}
