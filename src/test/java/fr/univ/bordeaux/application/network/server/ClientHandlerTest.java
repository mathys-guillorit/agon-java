package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.server.game.GameManager;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientHandlerTest {

  private AgonServer server;

  @AfterEach
  void cleanup() {
    try {
      if (server != null) {
        server.stop();
      }
    } catch (Exception ignored) {
    }
    server = null;
  }

  private int freePort() throws Exception {
    try (ServerSocket tmp = new ServerSocket(0)) {
      return tmp.getLocalPort();
    }
  }

  private int startServer() throws Exception {
    int port = freePort();
    server = new AgonServer(port, "TestServer");
    assertTrue(server.start());
    return port;
  }

  private void setField(Object target, String name, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    f.set(target, value);
  }

  @SuppressWarnings("unchecked")
  private Map<Integer, ServerGameSession> activeGamesOf(AgonServer server) throws Exception {
    Field gameServiceField = AgonServer.class.getDeclaredField("gameService");
    gameServiceField.setAccessible(true);
    Object gameService = gameServiceField.get(server);

    Field activeGamesField = GameManager.class.getDeclaredField("activeGames");
    activeGamesField.setAccessible(true);
    return (Map<Integer, ServerGameSession>) activeGamesField.get(gameService);
  }

  private static class RawClient implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    RawClient(String host, int port) throws Exception {
      socket = new Socket(host, port);
      socket.setSoTimeout(3000);
      in =
          new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
      out =
          new BufferedWriter(
              new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
    }

    void send(String line) throws Exception {
      out.write(line);
      out.write('\n');
      out.flush();
    }

    String readLine() throws Exception {
      return in.readLine();
    }

    String readUntilEnd() throws Exception {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = in.readLine()) != null) {
        sb.append(line).append('\n');
        if ("END".equals(line)) {
          break;
        }
      }
      return sb.toString();
    }

    String login(String name, String clientId) throws Exception {
      send("LOGIN NAME=" + name + " CLIENT_ID=" + clientId);
      return readLine();
    }

    int loginAndExtractId(String name, String clientId) throws Exception {
      return extractId(login(name, clientId));
    }

    @Override
    public void close() throws Exception {
      socket.close();
    }
  }

  private static int extractId(String welcomeLine) {
    for (String part : welcomeLine.split("\\s+")) {
      if (part.startsWith("ID=")) {
        return Integer.parseInt(part.substring(3));
      }
    }
    throw new IllegalArgumentException("No ID found in line: " + welcomeLine);
  }

  private static class GameSetup implements AutoCloseable {
    final RawClient c1;
    final RawClient c2;
    final int id1;
    final boolean c1IsWhite;

    GameSetup(RawClient c1, RawClient c2, int id1, boolean c1IsWhite) {
      this.c1 = c1;
      this.c2 = c2;
      this.id1 = id1;
      this.c1IsWhite = c1IsWhite;
    }

    RawClient currentPlayer() {
      return c1IsWhite ? c1 : c2;
    }

    RawClient wrongPlayer() {
      return c1IsWhite ? c2 : c1;
    }

    @Override
    public void close() throws Exception {
      c1.close();
      c2.close();
    }
  }

  private GameSetup startLoggedGame(int port) throws Exception {
    RawClient c1 = new RawClient("127.0.0.1", port);
    RawClient c2 = new RawClient("127.0.0.1", port);

    int id1 = c1.loginAndExtractId("Alice", "cid1");
    int id2 = c2.loginAndExtractId("Bob", "cid2");

    c1.send("NEW PLAYER_ID=" + id2);
    assertTrue(c1.readLine().startsWith("INVITATION_SENT"));
    assertTrue(c2.readLine().startsWith("INVITATION_RECEIVED"));

    c2.send("ACCEPT");
    assertTrue(c2.readLine().startsWith("LOBBY_JOINED"));
    assertTrue(c2.readLine().startsWith("WAITING_MODE"));
    assertTrue(c1.readLine().startsWith("INVITATION_ACCEPTED"));
    assertTrue(c1.readLine().startsWith("CHOOSE_MODE"));

    c1.send("MODE normal");
    String started1 = c1.readLine();
    String started2 = c2.readLine();

    assertNotNull(started1);
    assertNotNull(started2);
    assertTrue(started1.startsWith("GAME_STARTED"));
    assertTrue(started2.startsWith("GAME_STARTED"));

    return new GameSetup(c1, c2, id1, started1.contains("COLOR=WHITE"));
  }

  @Test
  @DisplayName("PING, STATUS and QUIT basic flow")
  void pingStatusAndQuit() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      client.send("PING");
      assertTrue(client.readLine().startsWith("PONG"));

      client.send("STATUS");
      String status = client.readLine();
      assertNotNull(status);
      assertTrue(status.startsWith("STATUS_OK"));
      assertTrue(status.contains("port=" + port));

      client.send("QUIT");
      String maybeBye = client.readLine();
      if (maybeBye != null) {
        assertEquals("BYE", maybeBye);
      }
    }
  }

  @Test
  @DisplayName("LOGIN success and validation errors")
  void loginCases() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      String ok = client.login("Alice", "cid1");
      assertTrue(ok.startsWith("WELCOME"));
      assertTrue(ok.contains("NAME=Alice"));
      assertTrue(ok.contains("STATUS=idle"));

      client.send("LOGIN NAME=Alice CLIENT_ID=cid1");
      assertEquals("ERROR ALREADY_LOGGED_IN", client.readLine());
    }

    try (RawClient c1 = new RawClient("127.0.0.1", port)) {
      c1.send("LOGIN CLIENT_ID=cid1");
      assertEquals("ERROR MESSAGE=MISSING_NAME", c1.readLine());
    }

    try (RawClient c2 = new RawClient("127.0.0.1", port)) {
      c2.send("LOGIN NAME=Alice");
      assertEquals("ERROR MESSAGE=MISSING_CLIENT_ID", c2.readLine());
    }
  }

  @Test
  @DisplayName("PLAYERS and SCOREBOARD after login")
  void playersAndScoreboard() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      int id = client.loginAndExtractId("Alice", "cid1");

      client.send("PLAYERS");
      String players = client.readUntilEnd();
      assertTrue(players.contains("=== PLAYERS ==="));
      assertTrue(players.contains("NAME=Alice"));
      assertTrue(players.contains("STATUS=idle"));
      assertTrue(players.contains("END"));

      client.send("PLAYERS " + id);
      String details = client.readLine();
      assertNotNull(details);
      assertTrue(details.contains("PLAYER ID=" + id));
      assertTrue(details.contains("NAME=Alice"));
      assertTrue(details.contains("WINS=0"));
      assertTrue(details.contains("LOSSES=0"));
      assertTrue(details.contains("GAMES=0"));

      client.send("SCOREBOARD");
      String scoreboard = client.readUntilEnd();
      assertTrue(scoreboard.contains("=== SCOREBOARD ==="));
      assertTrue(scoreboard.contains("NAME=Alice"));
      assertTrue(scoreboard.contains("WINS=0"));
      assertTrue(scoreboard.contains("LOSSES=0"));
      assertTrue(scoreboard.contains("GAMES=0"));
      assertTrue(scoreboard.contains("END"));
    }
  }

  @Test
  @DisplayName("PLAYERS with invalid id and valid player information")
  void playersCommandVariants() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      int id = client.loginAndExtractId("Alice", "cid1");

      client.send("PLAYERS abc");
      assertEquals("ERROR MESSAGE=INVALID_PLAYER_ID", client.readLine());

      client.send("PLAYERS " + id);
      String valid = client.readLine();
      assertNotNull(valid);
      assertTrue(valid.contains("PLAYER ID=" + id));
      assertTrue(valid.contains("NAME=Alice"));
    }
  }

  @Test
  @DisplayName("NEW, MOVE and RESIGN require login, then game")
  void commandsRequireLoginAndGame() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      client.send("NEW PLAYER_ID=1");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("MOVE e2e4");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("RESIGN");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());
    }

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      assertTrue(client.login("Alice", "cid1").startsWith("WELCOME"));

      client.send("MOVE e2e4");
      assertEquals("ERROR MESSAGE=NOT_IN_GAME", client.readLine());

      client.send("RESIGN");
      assertEquals("ERROR MESSAGE=NOT_IN_GAME", client.readLine());
    }
  }

  @Test
  @DisplayName("NEW validates id, self-play and busy cases")
  void newValidationAndBusy() throws Exception {
    int port = startServer();

    try (RawClient c1 = new RawClient("127.0.0.1", port);
        RawClient c2 = new RawClient("127.0.0.1", port);
        RawClient c3 = new RawClient("127.0.0.1", port)) {

      int id1 = c1.loginAndExtractId("Alice", "cid1");
      int id2 = c2.loginAndExtractId("Bob", "cid2");
      c3.login("Charlie", "cid3");

      c1.send("NEW");
      assertEquals("ERROR MESSAGE=INVALID_PLAYER_ID", c1.readLine());

      c1.send("NEW PLAYER_ID=abc");
      assertEquals("ERROR MESSAGE=INVALID_PLAYER_ID", c1.readLine());

      c1.send("NEW PLAYER_ID=999");
      assertEquals("ERROR MESSAGE=PLAYER_NOT_FOUND", c1.readLine());

      c1.send("NEW PLAYER_ID=" + id1);
      assertEquals("ERROR MESSAGE=CANNOT_PLAY_SELF", c1.readLine());

      c1.send("NEW PLAYER_ID=" + id2);
      assertTrue(c1.readLine().startsWith("INVITATION_SENT"));
      assertTrue(c2.readLine().startsWith("INVITATION_RECEIVED"));

      c1.send("NEW PLAYER_ID=" + id2);
      assertEquals("ERROR MESSAGE=YOU_ARE_BUSY", c1.readLine());

      c3.send("NEW PLAYER_ID=" + id1);
      assertEquals("ERROR MESSAGE=PLAYER_BUSY", c3.readLine());
    }
  }

  @Test
  @DisplayName("Invitation flow covers NEW ACCEPT DECLINE CANCEL MODE")
  void invitationAndModeFlow() throws Exception {
    int port = startServer();

    try (RawClient c1 = new RawClient("127.0.0.1", port);
        RawClient c2 = new RawClient("127.0.0.1", port)) {

      c1.loginAndExtractId("Alice", "cid1");
      int id2 = c2.loginAndExtractId("Bob", "cid2");

      c2.send("ACCEPT");
      assertEquals("ERROR MESSAGE=NO_PENDING_INVITATION", c2.readLine());

      c2.send("DECLINE");
      assertEquals("ERROR MESSAGE=NO_PENDING_INVITATION", c2.readLine());

      c1.send("CANCEL");
      assertEquals("ERROR MESSAGE=NO_SENT_INVITATION", c1.readLine());

      c1.send("MODE normal");
      assertEquals("ERROR MESSAGE=NOT_IN_LOBBY", c1.readLine());

      c1.send("NEW PLAYER_ID=" + id2);
      String requester = c1.readLine();
      String target = c2.readLine();

      assertTrue(requester.startsWith("INVITATION_SENT"));
      assertTrue(target.startsWith("INVITATION_RECEIVED"));
      assertTrue(requester.contains("PLAYER=Bob"));
      assertTrue(target.contains("FROM=Alice"));

      c1.send("CANCEL");
      assertEquals("CANCEL_OK", c1.readLine());
      assertTrue(c2.readLine().startsWith("INVITATION_CANCELED"));

      c1.send("NEW PLAYER_ID=" + id2);
      assertTrue(c1.readLine().startsWith("INVITATION_SENT"));
      assertTrue(c2.readLine().startsWith("INVITATION_RECEIVED"));

      c2.send("DECLINE");
      assertEquals("DECLINE_OK", c2.readLine());
      assertTrue(c1.readLine().startsWith("INVITATION_DECLINED"));

      c1.send("NEW PLAYER_ID=" + id2);
      assertTrue(c1.readLine().startsWith("INVITATION_SENT"));
      assertTrue(c2.readLine().startsWith("INVITATION_RECEIVED"));

      c2.send("ACCEPT");
      assertTrue(c2.readLine().startsWith("LOBBY_JOINED"));
      assertTrue(c2.readLine().startsWith("WAITING_MODE"));
      assertTrue(c1.readLine().startsWith("INVITATION_ACCEPTED"));
      assertTrue(c1.readLine().startsWith("CHOOSE_MODE"));

      c2.send("MODE normal");
      assertEquals("ERROR MESSAGE=ONLY_HOST_CAN_CHOOSE_MODE", c2.readLine());

      c1.send("MODE");
      assertEquals("ERROR MESSAGE=INVALID_MODE", c1.readLine());

      c1.send("MODE invalid");
      assertEquals("ERROR MESSAGE=INVALID_MODE", c1.readLine());
    }
  }

  @Test
  @DisplayName("AWAY BACK ACCEPT DECLINE CANCEL require login")
  void statusAndInvitationCommandsRequireLogin() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      client.send("AWAY");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("BACK");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("ACCEPT");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("DECLINE");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("CANCEL");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());
    }
  }

  @Test
  @DisplayName("AWAY and BACK refuse when player is in game")
  void statusIngameRefused() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      game.c1.send("AWAY");
      assertEquals("ERROR MESSAGE=CANNOT_SET_AWAY_NOW", game.c1.readLine());

      game.c1.send("BACK");
      assertEquals("ERROR MESSAGE=CANNOT_SET_BACK_NOW", game.c1.readLine());
    }
  }

  @Test
  @DisplayName("MOVE validation and success path")
  void moveValidationAndSuccessPath() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      game.c1.send("MOVE");
      assertEquals("ERROR MESSAGE=MISSING_MOVE", game.c1.readLine());

      game.wrongPlayer().send("MOVE e2e4");
      assertEquals("ERROR MESSAGE=NOT_YOUR_TURN", game.wrongPlayer().readLine());

      game.currentPlayer().send("MOVE badMove");
      assertEquals("ERROR MESSAGE=INVALID_MOVE", game.currentPlayer().readLine());

      game.currentPlayer().send("MOVE a1a2");
      String first = game.currentPlayer().readLine();
      assertNotNull(first);

      if (first.startsWith("MOVE_OK")) {
        String second = game.wrongPlayer().readLine();
        if (second != null) {
          assertTrue(second.startsWith("OPPONENT_MOVE") || second.startsWith("GAME_OVER"));
        }
      } else {
        assertEquals("ERROR MESSAGE=INVALID_MOVE", first);
      }
    }
  }

  @Test
  @DisplayName("MOVE and RESIGN return GAME_NOT_FOUND when session is missing")
  void commandsGameNotFound() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      Integer gameId = server.getGameIdByPlayer(game.id1);
      assertNotNull(gameId);

      activeGamesOf(server).remove(gameId);

      game.c1.send("MOVE e2e4");
      assertEquals("ERROR MESSAGE=GAME_NOT_FOUND", game.c1.readLine());
    }

    try (GameSetup game = startLoggedGame(port)) {
      Integer gameId = server.getGameIdByPlayer(game.id1);
      assertNotNull(gameId);

      activeGamesOf(server).remove(gameId);

      game.c1.send("RESIGN");
      assertEquals("ERROR MESSAGE=GAME_NOT_FOUND", game.c1.readLine());
    }
  }

  @Test
  @DisplayName("RESIGN handles missing opponent or ends game")
  void resignCases() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      Integer gameId = server.getGameIdByPlayer(game.id1);
      assertNotNull(gameId);

      ServerGameSession session = server.getGameById(gameId);
      assertNotNull(session);

      OnlinePlayer player = server.getPlayerById(game.id1);
      assertNotNull(player);

      OnlinePlayer opponent = session.getOpponent(player.getId());
      assertNotNull(opponent);

      opponent.setHandler(null);

      game.c1.send("RESIGN");
      String response = game.c1.readLine();
      assertNotNull(response);
      assertTrue(
          response.equals("ERROR MESSAGE=OPPONENT_NOT_FOUND") || response.startsWith("GAME_OVER"));
    }

    try (GameSetup game = startLoggedGame(port)) {
      game.c1.send("RESIGN");

      String msg1 = game.c1.readLine();
      String msg2 = game.c2.readLine();

      assertTrue(msg1.startsWith("GAME_OVER"));
      assertTrue(msg2.startsWith("GAME_OVER"));
      assertTrue(msg1.contains("RESULT="));
      assertTrue(msg2.contains("RESULT="));
    }
  }

  @Test
  @DisplayName("Server stop is safe when clients are connected")
  void serverStopWithConnectedClients() throws Exception {
    int port = startServer();

    try (RawClient c1 = new RawClient("127.0.0.1", port);
        RawClient c2 = new RawClient("127.0.0.1", port)) {
      c1.login("Alice", "cid1");
      c2.login("Bob", "cid2");

      assertDoesNotThrow(() -> server.stop());
      assertFalse(server.isRunning());
    }
  }

  @Test
  @DisplayName(
      "ClientHandler stop is safe when called twice and sendFromServer does nothing when writer is null")
  void clientHandlerInternalSafety() throws Exception {
    int port = startServer();

    try (Socket socket = new Socket("127.0.0.1", port)) {
      ClientHandler handler = new ClientHandler(socket, server);

      handler.stop();
      handler.stop();

      setField(handler, "out", null);

      assertDoesNotThrow(() -> handler.sendFromServer("HELLO"));
      assertDoesNotThrow(socket::close);
    }
  }
}
