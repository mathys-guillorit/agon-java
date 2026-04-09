package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandParser;
import fr.univ.bordeaux.application.network.server.clientcommand.ClientCommandProcessor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Handles the communication lifecycle for a single TCP client. */
public class ClientHandler implements Runnable {

  /** Logger used for client handler runtime errors. */
  private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

  /** TCP socket associated with the connected client. */
  final Socket socket;

  /** Server facade used to access the network game services. */
  final AgonServer server;

  /** Command processor responsible for handling parsed client commands. */
  private final ClientCommandProcessor commandProcessor;

  /** Indicates whether the handler is still active. */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /** Lock used to serialize writes to the client output stream. */
  private final Object sendLock = new Object();

  /** Output stream used to send messages to the client. */
  private BufferedWriter out;

  /** Player associated with this connection. */
  OnlinePlayer player;

  /**
   * Creates a new client handler for the given socket.
   *
   * @param socket the TCP socket associated with the connected client
   * @param server the server instance this handler belongs to
   */
  public ClientHandler(final Socket socket, final AgonServer server) {
    this.socket = socket;
    this.server = server;
    this.commandProcessor = new ClientCommandProcessor();
  }

  /** Stops the client handler gracefully. */
  public void stop() {
    if (running.compareAndSet(true, false)) {
      sendByeIfPossible();
      closeSocketQuietly();
    }
  }

  /** Main execution method of the client handler thread. */
  @Override
  public void run() {
    try {
      socket.setSoTimeout(60_000);

      try (BufferedReader reader =
              new BufferedReader(
                  new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
          BufferedWriter writer =
              new BufferedWriter(
                  new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))) {

        out = writer;
        processClientLoop(reader);
      }
    } catch (IOException e) {
      logRuntimeFailure(e);
    } finally {
      out = null;
      commandProcessor.handleDisconnect(this);
      server.removeClient(this);
      stop();
    }
  }

  /**
   * Sends a server message to the connected client.
   *
   * @param msg the message to send
   * @throws IOException if the message cannot be written to the socket
   */
  public void sendFromServer(final String msg) throws IOException {
    synchronized (sendLock) {
      if (out != null) {
        out.write(msg);
        out.write('\n');
        out.flush();
      }
    }
  }

  /**
   * Processes the main client read loop.
   *
   * @param reader the reader bound to the current client socket
   * @throws IOException if an I/O error occurs while handling the socket
   */
  private void processClientLoop(final BufferedReader reader) throws IOException {
    while (running.get() && !socket.isClosed()) {
      final String receivedLine = readNextLine(reader);

      if (receivedLine == null) {
        running.set(false);
      } else {
        final Command command = CommandParser.parse(receivedLine);
        final boolean shouldQuit = commandProcessor.handleCommand(command, this);

        if (shouldQuit) {
          running.set(false);
        }
      }
    }
  }

  /**
   * Reads the next line from the client connection.
   *
   * @param reader the reader bound to the socket
   * @return the received line, or {@code null} if client disconnected
   * @throws IOException if an unexpected I/O error occurs
   */
  private String readNextLine(final BufferedReader reader) throws IOException {
    String receivedLine = null;

    try {
      receivedLine = reader.readLine();
    } catch (SocketTimeoutException e) {
      receivedLine = null;
    }

    return receivedLine;
  }

  /** Sends a termination message to the client when the socket is still open. */
  private void sendByeIfPossible() {
    try {
      if (out != null && !socket.isClosed()) {
        sendFromServer("BYE");
      }
    } catch (IOException ignored) {
      // Ignored
    }
  }

  /** Closes the client socket while ignoring secondary shutdown failures. */
  private void closeSocketQuietly() {
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "[SERVER] Error while closing client socket", e);
    }
  }

  /**
   * Logs a runtime communication failure if the handler was still active.
   *
   * @param exception the exception raised during execution
   */
  private void logRuntimeFailure(final IOException exception) {
    if (running.get()) {
      LOGGER.log(Level.WARNING, "[SERVER] ClientHandler error", exception);
    }
  }

  /**
   * Returns the server facade associated with this handler.
   *
   * @return the server facade
   */
  public AgonServer getServer() {
    return server;
  }

  /**
   * Returns the player bound to this handler.
   *
   * @return the bound player, or {@code null} if the client is not logged in
   */
  public OnlinePlayer getPlayer() {
    return player;
  }

  /**
   * Updates the player bound to this handler.
   *
   * @param player the player to bind to this handler
   */
  public void setPlayer(final OnlinePlayer player) {
    this.player = player;
  }
}
