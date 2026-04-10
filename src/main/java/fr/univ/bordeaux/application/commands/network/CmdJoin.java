package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Objects;

/**
 * Connects a client to a remote game server. Supported formats: {@code join} and {@code join
 * IP:PORT}.
 */
public class CmdJoin extends Cmd {

  /** Default host used if none is provided. */
  private static final String DEFAULT_HOST = "127.0.0.1";

  /** Default port used if none is provided. */
  private static final int DEFAULT_PORT = 12_345;

  /** Shared application context (client, server, etc.). */
  private final AppContext context;

  /** Command arguments (provided by parser). */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param userInterface user interface context
   * @param context application context
   */
  public CmdJoin(final GameUserInterface userInterface, final AppContext context) {
    super(
        userInterface,
        "join",
        "join [HOST[:PORT]]\n" + "Description: connects to a remote game server.\n");
    this.context = context;
  }

  /** Internal constructor used when the command is executed with arguments. */
  private CmdJoin(
      final GameUserInterface userInterface, final AppContext context, final String[] args) {
    this(userInterface, context);
    this.args = args;
  }

  /** Creates a new instance of the command with parsed arguments. */
  @Override
  public CmdAction createNew(final String[] args) {
    return new CmdJoin(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match not used (network command independent from game state)
   */
  @Override
  public boolean execute(final MatchManager match) {
    return run(args);
  }

  /**
   * Core logic of the join command.
   *
   * @param args command arguments
   * @return true if execution completed
   */
  private boolean run(final String[] args) {
    final AgonClient client = getClient();

    if (isAlreadyConnected(client)) {
      getCtx().showWarn("[CLIENT] Already connected.\n");
      return false;
    }

    disconnectIfDead(client);

    final ConnectionTarget connectionTarget = parseConnectionTarget(args);
    if (connectionTarget == null) {
      return false;
    }

    if (client.connect(connectionTarget.host(), connectionTarget.port())) {
      getCtx()
          .showMessage(
              "[CLIENT] Connected to "
                  + connectionTarget.host()
                  + ":"
                  + connectionTarget.port()
                  + "\n");
      return true;
    }

    getCtx().showError("[CLIENT] Connection failed.");
    return false;
  }

  /** Returns the client from the application context. */
  private AgonClient getClient() {
    return context.getClient();
  }

  /** Returns true when the client is already connected and alive. */
  private boolean isAlreadyConnected(final AgonClient client) {
    return client.isConnected() && client.isAlive();
  }

  /** Disconnects the client if a dead connection is still present. */
  private void disconnectIfDead(final AgonClient client) {
    if (client.isConnected() && !client.isAlive()) {
      client.disconnectSilently();
    }
  }

  /**
   * Parses the connection target from command arguments. Accepted formats: {@code join} and {@code
   * join IP:PORT}.
   *
   * @param args command arguments
   * @return parsed connection target, or {@code null} if invalid
   */
  private ConnectionTarget parseConnectionTarget(final String[] args) {
    if (!hasTargetArgument(args)) {
      return new ConnectionTarget(DEFAULT_HOST, DEFAULT_PORT);
    }

    final String rawArgument = args[0].trim();
    final int colonIndex = rawArgument.lastIndexOf(':');

    if (colonIndex <= 0 || colonIndex == rawArgument.length() - 1) {
      getCtx().showError("[CLIENT] Usage: join IP:PORT");
      return null;
    }

    final String host = rawArgument.substring(0, colonIndex).trim();
    final String portPart = rawArgument.substring(colonIndex + 1).trim();

    if (host.isEmpty()) {
      getCtx().showError("[CLIENT] Invalid host.");
      return null;
    }

    final Integer port = parsePort(portPart);
    if (port == null) {
      return null;
    }

    return new ConnectionTarget(host, port);
  }

  /** Returns true when a non-blank target argument is present. */
  private boolean hasTargetArgument(final String[] args) {
    return args != null && args.length > 0 && !args[0].isBlank();
  }

  /** Parses a port and validates its range. */
  private Integer parsePort(final String portPart) {
    try {
      final int parsedPort = Integer.parseInt(portPart);

      if (parsedPort < 1024 || parsedPort > 65535) {
        getCtx().showError("[CLIENT] Port must be between 1024 and 65535.");
        return null;
      }

      return parsedPort;
    } catch (NumberFormatException exception) {
      getCtx().showError("[CLIENT] Invalid port: " + portPart);
      return null;
    }
  }

  /** Immutable host/port pair for connection attempts. */
  private record ConnectionTarget(String host, int port) {
    private ConnectionTarget {
      host = Objects.requireNonNull(host);
    }
  }
}
