package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** Starts a local TCP server. Uses the given port or the default port if none is provided. */
public class CmdServerStart extends Cmd {

  /** Default port used if no argument is provided. */
  private static final int DEFAULT_PORT = 12_345;

  /** Shared application context (server, client, profile, etc.). */
  private final AppContext context;

  /** Command arguments provided by the parser. */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param userInterface user interface context
   * @param context application context
   */
  public CmdServerStart(final GameUserInterface userInterface, final AppContext context) {
    super(userInterface);
    this.context = context;
    this.setName("server_start");
    this.setDesc(
        "Usage: server_start [PORT]\n"
            + "Description: starts the local TCP server on the given port.\n"
            + "If no port is provided, the default port 12345 is used.\n");
  }

  /**
   * Internal constructor used when the command is executed with arguments.
   *
   * @param userInterface user interface
   * @param context application context
   * @param args command arguments
   */
  private CmdServerStart(
      final GameUserInterface userInterface, final AppContext context, final String[] args) {
    this(userInterface, context);
    this.args = args;
  }

  /**
   * Creates a new instance of the command with parsed arguments.
   *
   * @param args arguments passed from the command line
   * @return a new CmdServerStart instance
   */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdServerStart(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match match manager (unused here)
   * @return true if execution completed
   */
  @Override
  public boolean execute(final MatchManager match) {
    return run(args);
  }

  /**
   * Core logic of the server start command.
   *
   * @param args command arguments
   * @return true if execution completed successfully
   */
  private boolean run(final String[] args) {
    boolean result = false;

    if (isServerRunning()) {
      getCtx().showWarn("[SERVER] A server is already running.");
    } else {
      final Integer parsedPort = parsePort(args);

      if (parsedPort != null) {
        final String profileName = getProfileName();
        final AgonServer server = new AgonServer(parsedPort, profileName);

        if (server.start()) {
          context.setServer(server);
          getCtx()
              .showMessage(
                  "[SERVER] Server started on " + getMachineAddress() + ":" + parsedPort + "\n");
          result = true;
        } else {
          getCtx().showError("[SERVER] Failed to start on port " + parsedPort + ".");
        }
      }
    }

    return result;
  }

  /** Returns true if a server is already running. */
  private boolean isServerRunning() {
    return getServer() != null && getServer().isRunning();
  }

  /** Returns the current server from the application context. */
  private AgonServer getServer() {
    return context.getServer();
  }

  /** Returns the current profile name. */
  private String getProfileName() {
    final var profile = context.getProfile();
    return profile.getName();
  }

  /**
   * Parses the port from arguments.
   *
   * @param args command arguments
   * @return parsed port, or the default port, or {@code null} if invalid
   */
  private Integer parsePort(final String[] args) {
    Integer port = DEFAULT_PORT;
    boolean valid = true;

    if (args != null && args.length > 0) {
      final String raw = args[0].trim();

      if (!raw.isEmpty()) {
        try {
          port = Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
          getCtx().showError("[SERVER] Invalid port: " + raw);
          valid = false;
        }
      }
    }

    if (valid && (port < 1024 || port > 65_535)) {
      getCtx().showError("[SERVER] Port must be between 1024 and 65535.");
      valid = false;
    }

    if (!valid) {
      port = null;
    }

    return port;
  }

  /** Returns the machine IP address, or the loopback address if unavailable. */
  private String getMachineAddress() {
    String address;

    try {
      address = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException exception) {
      address = InetAddress.getLoopbackAddress().getHostAddress();
    }

    return address;
  }
}
