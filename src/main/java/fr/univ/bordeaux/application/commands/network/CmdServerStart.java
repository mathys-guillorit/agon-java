package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to start a local TCP server.
 *
 * <p>This command allows the user to launch a game server on a specified port. If no port is
 * provided, a default port (12345) is used.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>{@code server_start} → starts server on default port (12345)
 *   <li>{@code server_start 5555} → starts server on port 5555
 * </ul>
 *
 * <p>This command:
 *
 * <ul>
 *   <li>Checks if a server is already running
 *   <li>Parses the port argument safely
 *   <li>Starts the server
 *   <li>Stores it inside the application context
 * </ul>
 */
public class CmdServerStart extends Cmd {

  /** Default port used if no argument is provided. */
  private static final int DEFAULT_PORT = 12345;

  /** Shared application context (contains server, client, etc.). */
  private final AppContext context;

  /** Command arguments provided by the parser. */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param ui User interface context
   * @param context Application context
   */
  public CmdServerStart(GameUserInterface ui, AppContext context) {
    super(
        ui,
        "server_start",
        "server_start [PORT]\n"
            + "Description: starts the local TCP server on the given port.\n"
            + "If no port is provided, the default port 12345 is used.\n");
    this.context = context;
  }

  /**
   * Internal constructor used when the command is executed with arguments.
   *
   * @param ui User interface
   * @param context Application context
   * @param args Command arguments
   */
  private CmdServerStart(GameUserInterface ui, AppContext context, String[] args) {
    this(ui, context);
    this.args = args;
  }

  /**
   * Creates a new instance of the command with parsed arguments.
   *
   * @param args Arguments passed from the command line
   * @return A new CmdServerStart instance
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdServerStart(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match MatchManager (unused here)
   * @return true if execution completed
   */
  @Override
  public boolean execute(MatchManager match) {
    return run(args);
  }

  /**
   * Core logic of the server start command.
   *
   * @param args Command arguments
   * @return true if execution completed successfully
   */
  private boolean run(String[] args) {

    // 1. Check if a server is already running
    if (context.getServer() != null && context.getServer().isRunning()) {
      getCtx().showWarn("[SERVER] A server is already running.");
      return false;
    }

    int port = DEFAULT_PORT;

    // 2. Parse port argument if provided
    if (args != null && args.length > 0) {

      String raw = args[0].trim();

      if (!raw.isEmpty()) {
        try {
          port = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
          // Invalid port format
          getCtx().showError("[SERVER] Invalid port: " + raw);
          return false;
        }
      }
    }

    // 3. Create server instance
    String profileName = context.getProfile().getName();

    AgonServer server = new AgonServer(port, profileName);

    // 4. Attempt to start the server
    if (!server.start()) {
      getCtx().showError("[SERVER] Failed to start. Port " + port + " may already be in use.");
      return false;
    }

    // 5. Store server in context
    context.setServer(server);

    // 6. Inform the user
    getCtx().showMessage("[SERVER] Server started successfully on port " + port + "\n");

    return true;
  }
}
