package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command used to stop the local TCP server. */
public class CmdServerStop extends Cmd {

  /** Application context used to manage the local server. */
  private final AppContext context;

  /**
   * Constructs a new command to stop the local TCP server.
   *
   * @param userInterface the user interface associated with this command.
   * @param context the application context used to manage the server state.
   */
  public CmdServerStop(final GameUserInterface userInterface, final AppContext context) {
    super(
        userInterface,
        "server_stop",
        "server stop\n" + "Description: stops the local TCP server if running.\n");
    this.context = context;
  }

  /**
   * Creates a new instance of the server stop command.
   *
   * @param args command arguments (unused)
   * @return a new {@code CmdServerStop} command
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdServerStop(getCtx(), context);
  }

  /**
   * Executes the server stop command.
   *
   * @param match current match manager (unused)
   * @return true if the server was stopped, false if no server was running
   */
  @Override
  public boolean execute(final MatchManager match) {
    final AgonServer server = getServer();
    boolean result = true;

    if (server == null || !server.isRunning()) {
      getCtx().showWarn("[SERVER] No server is currently running.");
      result = false;
    } else {
      server.stop();
      context.setServer(null);
      getCtx().showMessage("[SERVER] Server stopped successfully.\n");
    }

    return result;
  }

  /** Returns the local server from the application context. */
  private AgonServer getServer() {
    return context.getServer();
  }
}
