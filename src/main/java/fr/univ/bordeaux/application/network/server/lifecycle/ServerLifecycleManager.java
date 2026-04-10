package fr.univ.bordeaux.application.network.server.lifecycle;

import fr.univ.bordeaux.application.network.server.ServerDiscovery;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;

/** Handles server socket lifecycle, client accept loop, and discovery broadcasting. */
public class ServerLifecycleManager {


  /** TCP port listened to by the server socket. */
  private final int port;

  /** Logical server name used for discovery. */
  private final String name;

  /** Listening server socket. */
  private ServerSocket serverSocket;

  /** Discovery broadcaster associated with this server. */
  private ServerDiscovery discovery;

  /** Indicates whether the lifecycle is currently active. */
  private final AtomicBoolean running = new AtomicBoolean(false);

  /** Dedicated executor used for the accept loop. */
  private final ExecutorService acceptExecutor =
      Executors.newSingleThreadExecutor(
          runnable -> {
            final Thread thread = new Thread(runnable, "accept-client-loop");
            thread.setDaemon(true);
            return thread;
          });

  /** Executor used to run client handler tasks. */
  private final ExecutorService clientExecutor =
      Executors.newCachedThreadPool(
          runnable -> {
            final Thread thread = new Thread(runnable, "client-handler");
            thread.setDaemon(true);
            return thread;
          });

  /**
   * Creates a new ServerLifecycleManager.
   *
   * @param port the TCP port listened to by the server
   * @param name the discovery name of the server
   */
  public ServerLifecycleManager(final int port, final String name) {
    this.port = port;
    this.name = name;
  }

  /**
   * Starts the socket lifecycle and discovery service.
   *
   * @param socketConsumer callback executed for each accepted client socket
   * @return {@code true} if startup succeeded, {@code false} otherwise
   */
  public boolean start(final Consumer<Socket> socketConsumer) {
    if (running.get()) {
      return true;
    }

    try {
      serverSocket = new ServerSocket(port);
      discovery = new ServerDiscovery(name, port);
      discovery.start();

      running.set(true);
      acceptExecutor.submit(() -> acceptClientLoop(socketConsumer));
      return true;
    } catch (IOException e) {
        GameLogger.error("[SERVER] Failed to start on port " + port);
      return false;
    }
  }

  /**
   * Stops the socket lifecycle, discovery service, and executors.
   *
   * @return {@code true} if shutdown completed
   */
  public boolean stop() {
    running.set(false);
    stopDiscovery();
    closeServerSocket();
    acceptExecutor.shutdownNow();
    clientExecutor.shutdownNow();
    return true;
  }

  /**
   * Returns whether the server lifecycle is currently active.
   *
   * @return {@code true} if the lifecycle is running, {@code false} otherwise
   */
  public boolean isRunning() {
    return running.get();
  }

  /**
   * Submits a client handler task to the client executor.
   *
   * @param handler the client task to execute
   */
  public void submitClientHandler(final Runnable handler) {
    clientExecutor.submit(handler);
  }

  /**
   * Executes the accept loop and forwards accepted sockets to the provided callback.
   *
   * @param socketConsumer callback executed for each accepted socket
   */
  @SuppressWarnings("PMD.CloseResource")
  private void acceptClientLoop(final Consumer<Socket> socketConsumer) {
    while (running.get()) {
      try {
        final Socket clientSocket = serverSocket.accept();
        socketConsumer.accept(clientSocket);
      } catch (IOException e) {
        if (running.get()) {
          GameLogger.warn("[SERVER] Error while accepting a client");
        }
        break;
      }
    }
  }

  /** Stops the discovery broadcaster if it exists. */
  private void stopDiscovery() {
    if (discovery != null) {
      discovery.stop();
    }
  }

  /** Closes the server socket if it exists. */
  private void closeServerSocket() {
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        GameLogger.error("[SERVER] Error while closing server socket");
      }
    }
  }
}
