package fr.univ.bordeaux.application.network.client;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;
import fr.univ.bordeaux.application.network.client.runtime.ClientAsyncEventHandler;
import fr.univ.bordeaux.application.network.client.runtime.ClientKeepAliveService;
import fr.univ.bordeaux.application.network.client.runtime.ClientTransport;
import fr.univ.bordeaux.application.network.client.runtime.SocketClientTransport;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Provides the public client-side API. Used to communicate with the online game server. */
public class AgonClient {

  /** Local profile containing the player's identity and per-server identifiers. */
  private final LocalProfile profile;

  /** Low-level transport responsible for socket communication and response buffering. */
  private final ClientTransport transport;

  /** Component handling asynchronous protocol events coming from the server. */
  private final ClientAsyncEventHandler asyncEventHandler;

  /** Background service periodically sending keep-alive messages. */
  private final ClientKeepAliveService keepAliveService;

  /** Lock ensuring that only one synchronous request is active at a time. */
  private final Object commandLock = new Object();

  /** Indicates whether the client is currently connected to a server. */
  private final AtomicBoolean connected = new AtomicBoolean(false);

  /**
   * Creates a new online client bound to one local profile.
   *
   * @param profile the local player profile
   */
  public AgonClient(final LocalProfile profile) {
    this.profile = profile;
    this.transport = new SocketClientTransport();
    this.asyncEventHandler = new ClientAsyncEventHandler();
    this.keepAliveService = new ClientKeepAliveService();

    transport.setAsyncEventPredicate(asyncEventHandler::isAsyncEvent);
    transport.setAsyncEventConsumer(asyncEventHandler::handleAsyncEvent);
  }

  /**
   * Connects to a remote game server and logs in using the local profile.
   *
   * @param host the remote server host
   * @param port the remote server port
   * @return {@code true} if succeed {@code false} otherwise
   */
  public boolean connect(final String host, final int port) {
    boolean connectedSuccessfully = true;

    if (isConnected()) {
      connectedSuccessfully = true;
    } else {
      try {
        transport.connect(host, port);
        transport.startReader();

        synchronized (commandLock) {
          transport.sendLine(
              "LOGIN NAME=" + profile.getName() + " CLIENT_ID=" + profile.getClientId());

          final String response = transport.waitResponse(5000L);

          if (response == null || !response.startsWith("WELCOME")) {
            disconnectSilently();
            connectedSuccessfully = false;
          } else {
            registerServerPlayerId(host, port, response);
            connected.set(true);
            startKeepAlive();
            connectedSuccessfully = true;
          }
        }
      } catch (IOException e) {
        disconnectSilently();
        connectedSuccessfully = false;
      }
    }

    return connectedSuccessfully;
  }

  /**
   * Indicates whether the client is currently connected.
   *
   * @return {@code true} if the client is connected, {@code false} otherwise
   */
  public boolean isConnected() {
    if (!connected.get()) {
      return false;
    }

    if (!transport.isConnected()) {
      connected.set(false);
      return false;
    }

    return true;
  }

  /**
   * Requests the status of the connected remote server.
   *
   * @return the raw server response if successful, or {@code null} otherwise
   */
  public String requestServerStatus() {
    String response = null;

    if (isConnected()) {
      synchronized (commandLock) {
        response = performSingleLineRequest("STATUS", "STATUS_OK");
      }
    }

    return response;
  }

  /**
   * Requests the list of connected players from the server.
   *
   * @return the formatted players response, or {@code null} if the request fails
   */
  public String requestPlayers() {
    String response = null;

    if (isConnected()) {
      synchronized (commandLock) {
        response = performMultilineRequest("PLAYERS");
      }
    }

    return response;
  }

  /**
   * Requests the scoreboard from the server.
   *
   * @return the formatted scoreboard response, or {@code null} if the request fails
   */
  public String requestScoreboard() {
    String response = null;

    if (isConnected()) {
      synchronized (commandLock) {
        response = performMultilineRequest("SCOREBOARD");
      }
    }

    return response;
  }

  /**
   * Requests the details of one connected player.
   *
   * @param playerId the id of the player to query
   * @return the raw server response if successful, or {@code null} otherwise
   */
  public String requestPlayerDetails(final int playerId) {
    String response = null;

    if (isConnected()) {
      synchronized (commandLock) {
        response = performSingleLineRequest("PLAYERS " + playerId, null);
      }
    }

    return response;
  }

  /**
   * Sends a new game invitation request.
   *
   * @param targetPlayerId the id of the invited player
   * @return a local confirmation string, or {@code null} if sending fails
   */
  public String requestNewGame(final int targetPlayerId) {
    String response = null;

    if (isConnected()) {
      synchronized (commandLock) {
        response =
            sendFireAndForget("NEW PLAYER_ID=" + targetPlayerId, "[CLIENT] New game request sent.");
      }
    }

    return response;
  }

  /**
   * Sends ACCEPT to the server.
   *
   * @return {@code true} if the command succeed, {@code false} otherwise
   */
  public boolean acceptInvitation() {
    boolean accepted = false;

    if (isConnected()) {
      synchronized (commandLock) {
        accepted = sendBooleanCommand("ACCEPT");
      }
    }

    return accepted;
  }

  /**
   * Sends DECLINE to the server.
   *
   * @return {@code true} if the command succeed, {@code false} otherwise
   */
  public boolean declineInvitation() {
    boolean declined = false;

    if (isConnected()) {
      synchronized (commandLock) {
        declined = sendBooleanCommand("DECLINE");
      }
    }

    return declined;
  }

  /**
   * Sends CANCEL to the server.
   *
   * @return {@code true} if the command succeed, {@code false} otherwise
   */
  public boolean cancelInvitation() {
    boolean canceled = false;

    if (isConnected()) {
      synchronized (commandLock) {
        canceled = sendBooleanCommand("CANCEL");
      }
    }

    return canceled;
  }

  /**
   * Sends MODE to the server.
   *
   * @param mode the selected mode
   * @return {@code true} if the command succeed, {@code false} otherwise
   */
  public boolean chooseMode(final String mode) {
    boolean modeChosen = false;

    if (isConnected() && mode != null && !mode.isBlank()) {
      synchronized (commandLock) {
        modeChosen = sendBooleanCommand("MODE " + mode.trim());
      }
    }

    return modeChosen;
  }

  /**
   * Checks whether the connection is alive by sending a PING request.
   *
   * @return {@code true} if the server => PONG, {@code false} otherwise
   */
  public boolean isAlive() {
    boolean alive = false;

    if (isConnected()) {
      synchronized (commandLock) {
        final String response = performSingleLineRequest("PING", "PONG");
        alive = response != null;
      }
    }

    return alive;
  }

  /**
   * Sends PING to the server and measures round-trip time.
   *
   * @return a formatted RTT string if successful, or {@code null} otherwise
   */
  public String pingRttMs() {
    String roundTripTime = null;

    if (isConnected()) {
      synchronized (commandLock) {
        final long startTimeMs = System.currentTimeMillis();
        final String response = performSingleLineRequest("PING", "PONG");

        if (response != null) {
          final long elapsedTimeMs = System.currentTimeMillis() - startTimeMs;
          roundTripTime = "[SERVER] PONG TIME=" + elapsedTimeMs + "ms";
        }
      }
    }

    return roundTripTime;
  }

  /** Sends QUIT to the server, then closes the connection locally. */
  public void quit() {
    if (isConnected()) {
      synchronized (commandLock) {
        try {
          transport.sendLine("QUIT");
          transport.waitResponse(2000L);
        } catch (IOException ignored) {
          // Ignored
        } finally {
          disconnectSilently();
        }
      }
    } else {
      disconnectSilently();
    }
  }

  /** Closes all resources without throwing any exception to the caller. */
  public void disconnectSilently() {
    connected.set(false);
    keepAliveService.stop();
    transport.stop();
  }

  /**
   * Sends a full move to the server using board indices.
   *
   * @param fromIndex source board index
   * @param toIndex destination board index
   * @return {@code true} if the move was sent successfully, {@code false} otherwise
   */
  public boolean sendMove(final int fromIndex, final int toIndex) {
    boolean moveSent = false;

    if (isConnected()) {
      try {
        final String moveText =
            CoordinateMapper.toAbaPro(fromIndex).toLowerCase(java.util.Locale.ROOT)
                + CoordinateMapper.toAbaPro(toIndex).toLowerCase(java.util.Locale.ROOT);

        synchronized (commandLock) {
          transport.sendLine("MOVE " + moveText);
        }

        moveSent = true;
      } catch (IllegalArgumentException | IOException e) {
        disconnectSilently();
        moveSent = false;
      }
    }

    return moveSent;
  }

  /**
   * Sends a raw move string directly to the server.
   *
   * @param rawMove move text to send
   * @return {@code true} if the move was sent successfully, {@code false} otherwise
   */
  public boolean sendRawMove(final String rawMove) {
    boolean moveSent = false;

    if (isConnected() && rawMove != null && !rawMove.isBlank()) {
      synchronized (commandLock) {
        moveSent = sendBooleanCommand("MOVE " + rawMove.trim().toUpperCase(java.util.Locale.ROOT));
      }
    }

    return moveSent;
  }

  /** Sends a resignation request to the server for the current online game. */
  public void resignGame() {
    if (isConnected()) {
      try {
        transport.sendLine("RESIGN");
      } catch (IOException e) {
        GameLogger.error("[CLIENT] Failed to resign from online match: " + e.getMessage());
      }
    }
  }

  /**
   * Requests the server to set the local player status to away.
   *
   * @return the raw server response if successful, or {@code null} otherwise
   */
  public String requestAwayStatus() {
    String response = null;

    if (isConnected()) {
      synchronized (commandLock) {
        response = performSingleLineRequest("AWAY", null);
      }
    }

    return response;
  }

  /**
   * Requests the server to set the local player status back to idle.
   *
   * @return the raw server response if successful, or {@code null} otherwise
   */
  public String requestBackStatus() {
    String response = null;

    if (isConnected()) {
      synchronized (commandLock) {
        response = performSingleLineRequest("BACK", null);
      }
    }

    return response;
  }

  /**
   * Registers a listener notified when an online game starts or evolves.
   *
   * @param listener the listener to register
   */
  public void setOnlineGameStartListener(final OnlineGameStartListener listener) {
    asyncEventHandler.setGameStartListener(listener);
  }

  /** Starts the periodic keep-alive mechanism. */
  private void startKeepAlive() {
    keepAliveService.start(
        () -> {
          if (isConnected()) {
            synchronized (commandLock) {
              final String response = performSingleLineRequest("PING", "PONG");

              if (response == null) {
                disconnectSilently();
              }
            }
          }
        });
  }

  /**
   * Registers the local player id associated with one connected server.
   *
   * @param host the connected server host
   * @param port the connected server port
   * @param response the welcome response returned by the server
   */
  private void registerServerPlayerId(final String host, final int port, final String response) {
    final Integer playerId = extractId(response);

    if (playerId != null) {
      final String serverKey = host + ":" + port;
      profile.setIdForServer(serverKey, playerId);
    }
  }

  /**
   * Performs one request expecting one response line.
   *
   * @param requestLine the protocol line to send
   * @param expectedPrefix optional expected prefix
   * @return the response line if valid, or {@code null} otherwise
   */
  private String performSingleLineRequest(final String requestLine, final String expectedPrefix) {
    String responseLine = null;

    try {
      transport.sendLine(requestLine);
      responseLine = transport.waitResponse(5000L);

      if (expectedPrefix != null
          && (responseLine == null || !responseLine.startsWith(expectedPrefix))) {
        responseLine = null;
      }
    } catch (IOException e) {
      disconnectSilently();
      responseLine = null;
    }

    return responseLine;
  }

  /**
   * Performs one request expecting a multiline response terminated by END.
   *
   * @param requestLine the protocol line to send
   * @return the formatted multiline response, or {@code null} otherwise
   */
  private String performMultilineRequest(final String requestLine) {
    String multilineResponse = null;

    try {
      transport.sendLine(requestLine);

      final StringBuilder responseBuilder = new StringBuilder();
      boolean finished = false;

      while (!finished) {
        final String responseLine = transport.waitResponse(5000L);

        if (responseLine == null) {
          responseBuilder.setLength(0);
          finished = true;
        } else if ("END".equals(responseLine)) {
          finished = true;
        } else {
          responseBuilder.append(responseLine).append('\n');
        }
      }

      if (responseBuilder.length() > 0) {
        multilineResponse = responseBuilder.toString().trim();
      }
    } catch (IOException e) {
      disconnectSilently();
      multilineResponse = null;
    }

    return multilineResponse;
  }

  /**
   * Sends one command that doesn't require an synchronous protocol response.
   *
   * @param requestLine the protocol line to send
   * @param successValue the value returned when sending succeeds
   * @return the provided success value, or {@code null} on failure
   */
  private String sendFireAndForget(final String requestLine, final String successValue) {
    String result = null;

    try {
      transport.sendLine(requestLine);
      result = successValue;
    } catch (IOException e) {
      disconnectSilently();
      result = null;
    }

    return result;
  }

  /**
   * Sends one command that only needs a local success/failure outcome.
   *
   * @param requestLine the protocol line to send
   * @return {@code true} if sending succeeds, {@code false} otherwise
   */
  private boolean sendBooleanCommand(final String requestLine) {
    boolean sentSuccessfully = false;

    try {
      transport.sendLine(requestLine);
      sentSuccessfully = true;
    } catch (IOException e) {
      disconnectSilently();
      sentSuccessfully = false;
    }

    return sentSuccessfully;
  }

  /**
   * Extracts the player id from a WELCOME response.
   *
   * @param response the raw server response
   * @return the extracted id, or {@code null} if none can be parsed
   */
  private Integer extractId(final String response) {
    Integer extractedId = null;

    if (response != null) {
      final String[] tokens = response.split("\\s+");

      for (String token : tokens) {
        if (token.startsWith("ID=")) {
          try {
            extractedId = Integer.parseInt(token.substring(3));
            break;
          } catch (NumberFormatException ignored) {
            extractedId = null;
          }
        }
      }
    }

    return extractedId;
  }

  /**
   * Returns the local profile associated with this client.
   *
   * @return the local profile
   */
  public LocalProfile getProfile() {
    return profile;
  }

  /**
   * Indicates whether another object is the same client instance reference.
   *
   * @param obj the compared object
   * @return {@code true} if both references are equal, {@code false} otherwise
   */
  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  /**
   * Returns the identity hash code of this object.
   *
   * @return the object hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode());
  }
}
