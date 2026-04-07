package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandParser;
import fr.univ.bordeaux.application.network.protocol.CommandType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/** Handles the communication lifecycle for a single TCP client. */
public class ClientHandler implements Runnable {

  private final Socket socket;
  private final AgonServer server;
  private final CommandParser parser = new CommandParser();
  private volatile boolean running = true;

  /** Output stream used to send messages to the client. */
  private BufferedWriter out;

  /** Player associated with this connection. */
  private OnlinePlayer player;

  /**
   * Creates a new client handler for the given socket.
   *
   * @param socket the TCP socket associated with the connected client
   * @param server the server instance this handler belongs to
   */
  public ClientHandler(Socket socket, AgonServer server) {
    this.socket = socket;
    this.server = server;
  }

  /** Stops the client handler gracefully. */
  public void stop() {
    if (!running) {
      return;
    }

    running = false;

    try {
      if (out != null && socket != null && !socket.isClosed()) {
        send("BYE");
      }
    } catch (IOException ignored) {
      // Ignored
    }

    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      System.err.println("[SERVER] Error while closing client socket: " + e.getMessage());
    }
  }

  /** Main execution method of the client handler thread. */
  @Override
  public void run() {
    try {
      socket.setSoTimeout(60_000);

      BufferedReader in =
          new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
      this.out =
          new BufferedWriter(
              new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

      while (running && !socket.isClosed()) {
        String line;

        try {
          line = in.readLine();
        } catch (SocketTimeoutException e) {
          break;
        }

        if (line == null) {
          break;
        }

        Command cmd = parser.parse(line);

        if (cmd.getType() == CommandType.PING) {
          send("PONG TIME=0ms");

        } else if (cmd.getType() == CommandType.STATUS) {
          send(
              "STATUS_OK port="
                  + server.getPort()
                  + " clients="
                  + server.getConnectedClientsCount()
                  + " players="
                  + server.getPlayerCount()
                  + " games="
                  + server.getActiveGameCount());

        } else if (cmd.getType() == CommandType.LOGIN) {
          handleLogin(cmd);

        } else if (cmd.getType() == CommandType.PLAYERS) {
          handlePlayers(cmd);

        } else if (cmd.getType() == CommandType.SCOREBOARD) {
          send(server.getScoreboard());

        } else if (cmd.getType() == CommandType.NEW) {
          handleNew(cmd);

        } else if (cmd.getType() == CommandType.ACCEPT) {
          handleAccept();

        } else if (cmd.getType() == CommandType.DECLINE) {
          handleDecline();

        } else if (cmd.getType() == CommandType.CANCEL) {
          handleCancel();

        } else if (cmd.getType() == CommandType.MODE) {
          handleMode(cmd);

        } else if (cmd.getType() == CommandType.MOVE) {
          handleMove(cmd);

        } else if (cmd.getType() == CommandType.RESIGN) {
          handleResign();

        } else if (cmd.getType() == CommandType.AWAY) {
          handleAway();

        } else if (cmd.getType() == CommandType.BACK) {
          handleBack();

        } else if (cmd.getType() == CommandType.QUIT) {
          break;
        }
      }

    } catch (IOException e) {
      if (running) {
        System.err.println("[SERVER] ClientHandler error: " + e.getMessage());
      }
    } finally {
      if (player != null && server != null) {
        Integer gameId = server.getGameIdByPlayer(player.getId());

        if (gameId != null) {
          ServerGameSession session = server.getGameById(gameId);

          if (session != null) {
            OnlinePlayer opponent = session.getOpponent(player.getId());

            if (opponent != null) {
              server.finishGame(session, opponent.getId(), "OPPONENT_LEFT");
            }
          }
        }

        server.removePlayer(player);
      }

      if (server != null) {
        server.removeClient(this);
      }

      stop();
    }
  }

  /**
   * Sends a raw message to the client followed by a newline and flushes the buffer.
   *
   * @param msg the string message to send
   * @throws IOException if the message cannot be written to the socket
   */
  private synchronized void send(String msg) throws IOException {
    if (out != null) {
      out.write(msg);
      out.write('\n');
      out.flush();
    }
  }

  /**
   * Sends a server message to the connected client.
   *
   * @param msg the message to send
   * @throws IOException if the message cannot be written to the socket
   */
  public void sendFromServer(String msg) throws IOException {
    send(msg);
  }

  /**
   * Handles LOGIN command.
   *
   * @param cmd parsed LOGIN command
   * @throws IOException if sending a response fails
   */
  private void handleLogin(Command cmd) throws IOException {
    if (player != null) {
      send("ERROR ALREADY_LOGGED_IN");
      return;
    }

    String name = cmd.getArgs().get("NAME");
    String clientId = cmd.getArgs().get("CLIENT_ID");

    if (name == null || name.isBlank()) {
      send("ERROR MESSAGE=MISSING_NAME");
      return;
    }

    if (clientId == null || clientId.isBlank()) {
      send("ERROR MESSAGE=MISSING_CLIENT_ID");
      return;
    }

    player = server.registerPlayer(clientId, name, this);

    if (player == null) {
      send("ERROR MESSAGE=LOGIN_FAILED");
      return;
    }

    send(
        "WELCOME ID="
            + player.getId()
            + " NAME="
            + player.getName()
            + " STATUS="
            + player.getStatus().name().toLowerCase());
  }

  /**
   * Handles NEW command.
   *
   * @param cmd parsed NEW command
   * @throws IOException if sending a response fails
   */
  private void handleNew(Command cmd) throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    String targetValue = cmd.getArgs().get("PLAYER_ID");
    if (targetValue == null) {
      send("ERROR MESSAGE=MISSING_PLAYER_ID");
      return;
    }

    int targetId;
    try {
      targetId = Integer.parseInt(targetValue);
    } catch (NumberFormatException e) {
      send("ERROR MESSAGE=INVALID_PLAYER_ID");
      return;
    }

    OnlinePlayer target = server.getPlayerById(targetId);

    if (target == null) {
      send("ERROR MESSAGE=PLAYER_NOT_FOUND");
      return;
    }
    if (target.getId() == player.getId()) {
      send("ERROR MESSAGE=CANNOT_PLAY_SELF");
      return;
    }
    if (player.getStatus() != PlayerStatus.IDLE) {
      send("ERROR MESSAGE=YOU_ARE_BUSY");
      return;
    }
    if (target.getStatus() != PlayerStatus.IDLE) {
      send("ERROR MESSAGE=PLAYER_BUSY");
      return;
    }
    if (!server.createInvitation(player.getId(), targetId)) {
      send("ERROR MESSAGE=INVITATION_FAILED");
      return;
    }

    send("INVITATION_SENT PLAYER=" + target.getName() + " TIMEOUT=300s");

    if (target.getHandler() != null) {
      try {
        target.getHandler().send("INVITATION_RECEIVED FROM=" + player.getName() + " EXPIRES=300s");
      } catch (IOException ignored) {
        // Ignored
      }
    }
  }

  /**
   * Handles ACCEPT command.
   *
   * @throws IOException if sending a response fails
   */
  private void handleAccept() throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    Invitation invitation = server.getInvitationByInvited(player.getId());
    if (invitation == null) {
      send("ERROR MESSAGE=NO_PENDING_INVITATION");
      return;
    }

    GameLobby lobby = server.acceptInvitation(player.getId());
    if (lobby == null) {
      send("ERROR MESSAGE=INVITATION_ACCEPT_FAILED");
      return;
    }

    OnlinePlayer host = server.getPlayerById(lobby.getHostId());

    send("LOBBY_JOINED HOST=" + (host != null ? host.getName() : "UNKNOWN"));
    send("WAITING_MODE");

    if (host != null && host.getHandler() != null) {
      try {
        host.getHandler().send("INVITATION_ACCEPTED PLAYER=" + player.getName());
        host.getHandler().send("CHOOSE_MODE COMMAND=mode OPTIONS=normal|blitz");
      } catch (IOException ignored) {
        // Ignored
      }
    }
  }

  /**
   * Handles DECLINE command.
   *
   * @throws IOException if sending a response fails
   */
  private void handleDecline() throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    Invitation invitation = server.getInvitationByInvited(player.getId());
    if (invitation == null) {
      send("ERROR MESSAGE=NO_PENDING_INVITATION");
      return;
    }

    OnlinePlayer inviter = server.getPlayerById(invitation.getInviterId());
    server.removeInvitation(player.getId());

    send("DECLINE_OK");

    if (inviter != null && inviter.getHandler() != null) {
      try {
        inviter.getHandler().send("INVITATION_DECLINED PLAYER=" + player.getName());
      } catch (IOException ignored) {
        // Ignored
      }
    }
  }

  /**
   * Handles CANCEL command.
   *
   * @throws IOException if sending a response fails
   */
  private void handleCancel() throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    Invitation invitation = server.getInvitationByInviter(player.getId());
    if (invitation == null) {
      send("ERROR MESSAGE=NO_SENT_INVITATION");
      return;
    }

    OnlinePlayer invited = server.getPlayerById(invitation.getInvitedId());
    server.removeInvitation(player.getId());

    send("CANCEL_OK");

    if (invited != null && invited.getHandler() != null) {
      try {
        invited.getHandler().send("INVITATION_CANCELED PLAYER=" + player.getName());
      } catch (IOException ignored) {
        // Ignored
      }
    }
  }

  /**
   * Handles MODE command.
   *
   * @param cmd parsed MODE command
   * @throws IOException if sending a response fails
   */
  private void handleMode(Command cmd) throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    String rawMode = cmd.getRawArgument();
    if (rawMode == null || rawMode.isBlank()) {
      send("ERROR MESSAGE=MISSING_MODE");
      return;
    }

    GameMode mode;
    if ("normal".equalsIgnoreCase(rawMode)) {
      mode = GameMode.NORMAL;
    } else if ("blitz".equalsIgnoreCase(rawMode)) {
      mode = GameMode.BLITZ;
    } else {
      send("ERROR MESSAGE=INVALID_MODE");
      return;
    }

    GameLobby lobby = server.getLobbyByPlayer(player.getId());
    if (lobby == null) {
      send("ERROR MESSAGE=NOT_IN_LOBBY");
      return;
    }
    if (lobby.getHostId() != player.getId()) {
      send("ERROR MESSAGE=ONLY_HOST_CAN_CHOOSE_MODE");
      return;
    }

    ServerGameSession session = server.chooseMode(player.getId(), mode);
    if (session == null) {
      send("ERROR MESSAGE=GAME_CREATION_FAILED");
      return;
    }

    OnlinePlayer guest = server.getPlayerById(lobby.getGuestId());
    if (guest == null) {
      send("ERROR MESSAGE=OPPONENT_NOT_FOUND");
      return;
    }

    String roles = session.describeRoles();
    String requesterColor = session.getRoleLabel(player.getId());
    String targetColor = session.getRoleLabel(guest.getId());

    send(
        "GAME_STARTED GAME_ID="
            + session.getGameId()
            + " MODE="
            + mode.name()
            + " OPPONENT_ID="
            + guest.getId()
            + " OPPONENT_NAME="
            + guest.getName()
            + " COLOR="
            + requesterColor
            + " "
            + roles);

    if (guest.getHandler() != null) {
      try {
        guest
            .getHandler()
            .send(
                "GAME_STARTED GAME_ID="
                    + session.getGameId()
                    + " MODE="
                    + mode.name()
                    + " OPPONENT_ID="
                    + player.getId()
                    + " OPPONENT_NAME="
                    + player.getName()
                    + " COLOR="
                    + targetColor
                    + " "
                    + roles);
      } catch (IOException ignored) {
        // Ignored
      }
    }
  }

  /**
   * Handles MOVE command.
   *
   * @param cmd parsed MOVE command
   * @throws IOException if sending a response fails
   */
  private void handleMove(Command cmd) throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    Integer gameId = server.getGameIdByPlayer(player.getId());
    if (gameId == null) {
      send("ERROR MESSAGE=NOT_IN_GAME");
      return;
    }

    ServerGameSession session = server.getGameById(gameId);
    if (session == null) {
      send("ERROR MESSAGE=GAME_NOT_FOUND");
      return;
    }

    String rawMove = cmd.getRawArgument();
    if (rawMove == null || rawMove.isBlank()) {
      send("ERROR MESSAGE=MISSING_MOVE");
      return;
    }

    boolean ok = session.playMove(player.getId(), rawMove);

    if (!ok) {
      send(
          session.isPlayersTurn(player.getId())
              ? "ERROR MESSAGE=INVALID_MOVE"
              : "ERROR MESSAGE=NOT_YOUR_TURN");
      return;
    }

    send("MOVE_OK " + rawMove);

    OnlinePlayer opponent = session.getOpponent(player.getId());
    if (opponent != null && opponent.getHandler() != null) {
      try {
        opponent.getHandler().send("OPPONENT_MOVE " + rawMove);
      } catch (IOException ignored) {
        // Ignored
      }
    }

    if (session.isGameOver()) {
      server.finishGame(session, player.getId(), "NORMAL_END");
    }
  }

  /**
   * Handles RESIGN command.
   *
   * @throws IOException if sending a response fails
   */
  private void handleResign() throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    Integer gameId = server.getGameIdByPlayer(player.getId());
    if (gameId == null) {
      send("ERROR MESSAGE=NOT_IN_GAME");
      return;
    }

    ServerGameSession session = server.getGameById(gameId);
    if (session == null) {
      send("ERROR MESSAGE=GAME_NOT_FOUND");
      return;
    }

    OnlinePlayer opponent = session.getOpponent(player.getId());
    if (opponent == null) {
      send("ERROR MESSAGE=OPPONENT_NOT_FOUND");
      return;
    }

    server.finishGame(session, opponent.getId(), "OPPONENT_LEFT");
  }

  /**
   * Handles PLAYERS command.
   *
   * @param cmd parsed PLAYERS command
   * @throws IOException if sending a response fails
   */
  private void handlePlayers(Command cmd) throws IOException {
    String playerIdValue = cmd.getRawArgument();

    if (playerIdValue == null || playerIdValue.isBlank()) {
      send(server.getPlayersList());
      return;
    }

    int playerId;
    try {
      playerId = Integer.parseInt(playerIdValue);
    } catch (NumberFormatException e) {
      send("ERROR MESSAGE=INVALID_PLAYER_ID");
      return;
    }

    send(server.getPlayerDetails(playerId));
  }

  /**
   * Handles AWAY command.
   *
   * @throws IOException if sending a response fails
   */
  private void handleAway() throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    if (player.getStatus() != PlayerStatus.IDLE) {
      send("ERROR MESSAGE=CANNOT_SET_AWAY_NOW");
      return;
    }

    player.setStatus(PlayerStatus.AWAY);
    send("AWAY_OK STATUS=away");
  }

  /**
   * Handles BACK command.
   *
   * @throws IOException if sending a response fails
   */
  private void handleBack() throws IOException {
    if (player == null) {
      send("ERROR MESSAGE=NOT_LOGGED_IN");
      return;
    }

    if (player.getStatus() != PlayerStatus.AWAY) {
      send("ERROR MESSAGE=CANNOT_SET_BACK_NOW");
      return;
    }

    player.setStatus(PlayerStatus.IDLE);
    send("BACK_OK STATUS=idle");
  }
}
