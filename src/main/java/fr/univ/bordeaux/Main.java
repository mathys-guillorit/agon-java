package fr.univ.bordeaux;

import fr.univ.bordeaux.application.GameLauncher;
import fr.univ.bordeaux.application.network.server.AgonServer;

/** Entry point of the application. */
public class Main {

  private static final int DEFAULT_PORT = 12345;

  /**
   * Launches the application.
   *
   * @param args command-line arguments.
   */
  public static void main(String[] args) throws Exception {

    // =========================================================
    // CASE 1: Daemon mode (-d / --daemon)
    // Starts the server without launching the interactive shell.
    // =========================================================
    if (args != null && args.length > 0 && ("-d".equals(args[0]) || "--daemon".equals(args[0]))) {

      String owner;
      try {
        owner = java.net.InetAddress.getLocalHost().getHostName();
      } catch (Exception e) {
        owner = "Server";
      }

      AgonServer server = new AgonServer(DEFAULT_PORT, owner);

      if (server.start()) {
        System.out.println("[SERVER] Daemon mode enabled.");
        System.out.println("[SERVER] Running on port " + server.getPort() + " without interface.");

        while (server.isRunning()) {
          Thread.sleep(1000);
        }
      } else {
        System.err.println("[SERVER] Failed to start daemon mode.");
      }
      return;
    }

    // =========================================================
    // CASE 2: Server mode (-s [PORT] / --server [PORT])
    // Starts the server directly on the given port.
    // =========================================================
    if (args != null && args.length > 0 && ("-s".equals(args[0]) || "--server".equals(args[0]))) {

      int port = DEFAULT_PORT;

      if (args.length > 1) {
        try {
          port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
          System.out.println("[SERVER] Invalid port. Using default port " + DEFAULT_PORT + ".");
        }
      }

      String owner;
      try {
        owner = java.net.InetAddress.getLocalHost().getHostName();
      } catch (Exception e) {
        owner = "Server";
      }

      AgonServer server = new AgonServer(port, owner);

      if (server.start()) {
        System.out.println("[SERVER] Server mode enabled.");
        System.out.println("[SERVER] Running on port " + server.getPort() + ".");

        while (server.isRunning()) {
          Thread.sleep(1000);
        }
      } else {
        System.err.println("[SERVER] Failed to start server mode.");
      }

      return;
    }

    // =========================================================
    // CASE 3: Normal mode
    // Delegates startup to the GameLauncher.
    // =========================================================
    GameLauncher launcher = new GameLauncher();
    launcher.launch(args);
    System.exit(0);
  }
}
