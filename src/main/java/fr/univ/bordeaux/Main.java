package fr.univ.bordeaux;

import fr.univ.bordeaux.application.GameLauncher;
import fr.univ.bordeaux.application.network.server.AgonServer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Entry point of the application. */
public final class Main {

  /** Application logger. */
  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  /** Default TCP port used by the server. */
  private static final int DEFAULT_PORT = 12_345;

  /** Command-line option enabling daemon mode. */
  private static final String OPT_DAEMON = "-d";

  /** Long command-line option enabling daemon mode. */
  private static final String OPT_DAEMON_LONG = "--daemon";

  /** Command-line option enabling server mode. */
  private static final String OPT_SERVER = "-s";

  /** Long command-line option enabling server mode. */
  private static final String OPT_SERVER_LONG = "--server";

  /** Default owner name used when the host name cannot be resolved. */
  private static final String DEFAULT_OWNER = "Server";

  /** Index of the first command-line argument. */
  private static final int ARG0 = 0;

  /** Index of the optional port argument. */
  private static final int PORT_ARG = 1;

  /** Sleep duration in milliseconds while waiting for the server to stop. */
  private static final long POLL_DELAY_MS = 1_000L;

  /** Utility class constructor. */
  private Main() {}

  /**
   * Launches the application.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    launchFromArgs(args);
  }

  /**
   * Launches the mode choose for app.
   *
   * @param args command-line arguments
   */
  static void launchFromArgs(final String[] args) {
    if (isDaemonMode(args)) {
      runDaemonMode();
    } else if (isServerMode(args)) {
      runServerMode(args);
    } else {
      runNormalMode(args);
    }
  }

  /**
   * Checks whether the application must start in daemon mode.
   *
   * @param args command-line arguments
   * @return true if daemon mode is requested, false otherwise
   */
  private static boolean isDaemonMode(final String[] args) {
    return hasArguments(args)
        && (OPT_DAEMON.equals(args[ARG0]) || OPT_DAEMON_LONG.equals(args[ARG0]));
  }

  /**
   * Checks whether the application must start in server mode.
   *
   * @param args command-line arguments
   * @return true if server mode is requested, false otherwise
   */
  private static boolean isServerMode(final String[] args) {
    return hasArguments(args)
        && (OPT_SERVER.equals(args[ARG0]) || OPT_SERVER_LONG.equals(args[ARG0]));
  }

  /**
   * Checks whether at least one command-line argument is present.
   *
   * @param args command-line arguments
   * @return true if at least one argument is present, false otherwise
   */
  private static boolean hasArguments(final String[] args) {
    return args != null && args.length > 0;
  }

  /** Starts the application in daemon mode. */
  private static void runDaemonMode() {
    final String owner = resolveOwnerName();
    final AgonServer server = new AgonServer(DEFAULT_PORT, owner);

    if (server.start()) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("[SERVER] Daemon mode enabled.");
        LOGGER.info("[SERVER] Running on port " + server.getPort() + " without interface.");
      }
      waitWhileRunning(server);
    } else {
      LOGGER.severe("[SERVER] Failed to start daemon mode.");
    }
  }

  /**
   * Starts the application in server mode.
   *
   * @param args command-line arguments
   */
  private static void runServerMode(final String[] args) {
    final int port = resolvePort(args);
    final String owner = resolveOwnerName();
    final AgonServer server = new AgonServer(port, owner);

    if (server.start()) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("[SERVER] Server mode enabled.");
        LOGGER.info("[SERVER] Running on port " + server.getPort() + ".");
      }
      waitWhileRunning(server);
    } else {
      LOGGER.severe("[SERVER] Failed to start server mode.");
    }
  }

  /**
   * Starts the application in normal mode.
   *
   * @param args command-line arguments
   */
  private static void runNormalMode(final String[] args) {
    final GameLauncher launcher = new GameLauncher();
    launcher.launch(args);
  }

  /**
   * Resolves the server port from command-line arguments.
   *
   * @param args command-line arguments
   * @return the port to use
   */
  private static int resolvePort(final String[] args) {
    int port = DEFAULT_PORT;

    if (args.length > PORT_ARG) {
      try {
        port = Integer.parseInt(args[PORT_ARG]);
      } catch (NumberFormatException exception) {
        LOGGER.warning("[SERVER] Invalid port. Using default port " + DEFAULT_PORT + ".");
      }
    }

    return port;
  }

  /**
   * Resolves the owner name from the local host.
   *
   * @return the host name, or a default owner name if resolution fails
   */
  private static String resolveOwnerName() {
    String owner = DEFAULT_OWNER;

    try {
      owner = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException exception) {
      LOGGER.log(
          Level.WARNING, "Unable to resolve local host name. Using default owner.", exception);
    }

    return owner;
  }

  /**
   * Waits while the server is still running.
   *
   * @param server server instance to monitor
   */
  private static void waitWhileRunning(final AgonServer server) {
    while (server.isRunning()) {
      sleepBeforeNextPoll();
    }
  }

  /** Sleeps before the next server state poll. */
  private static void sleepBeforeNextPoll() {
    try {
      Thread.sleep(POLL_DELAY_MS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }
}
