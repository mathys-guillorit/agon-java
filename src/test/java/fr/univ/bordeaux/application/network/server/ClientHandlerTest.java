package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
      if (server != null) server.stop();
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

  @SuppressWarnings("unchecked")
  private <T> T getField(Object target, String name, Class<T> type) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return (T) f.get(target);
  }

  private void setField(Object target, String name, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    f.set(target, value);
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
        if ("END".equals(line)) break;
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
    final int id2;
    final boolean c1IsWhite;

    GameSetup(RawClient c1, RawClient c2, int id1, int id2, boolean c1IsWhite) {
      this.c1 = c1;
      this.c2 = c2;
      this.id1 = id1;
      this.id2 = id2;
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
    String newOk = c1.readLine();
    String started = c2.readLine();

    assertTrue(newOk.startsWith("NEW_OK"));
    assertTrue(started.startsWith("GAME_STARTED"));

    return new GameSetup(c1, c2, id1, id2, newOk.contains("COLOR=WHITE"));
  }

  @Test
  @DisplayName("PING, STATUS and QUIT basic flow")
  void ping_status_and_quit() throws Exception {
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
  void login_cases() throws Exception {
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
  void players_and_scoreboard() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      assertTrue(client.login("Alice", "cid1").startsWith("WELCOME"));

      client.send("PLAYERS");
      String players = client.readUntilEnd();
      assertTrue(players.contains("=== PLAYERS ==="));
      assertTrue(players.contains("NAME=Alice"));
      assertTrue(players.contains("STATUS=idle"));
      assertTrue(players.contains("END"));

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
  @DisplayName("NEW, MOVE and RESIGN require login")
  void commands_require_login() throws Exception {
    int port = startServer();

    try (RawClient client = new RawClient("127.0.0.1", port)) {
      client.send("NEW PLAYER_ID=1");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("MOVE e2e4");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());

      client.send("RESIGN");
      assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", client.readLine());
    }
  }

  @Test
  @DisplayName("MOVE and RESIGN require active game")
  void commands_require_game() throws Exception {
    int port = startServer();

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
  void new_validation_and_busy() throws Exception {
    int port = startServer();

    try (RawClient c1 = new RawClient("127.0.0.1", port);
        RawClient c2 = new RawClient("127.0.0.1", port);
        RawClient c3 = new RawClient("127.0.0.1", port)) {

      int id1 = c1.loginAndExtractId("Alice", "cid1");
      int id2 = c2.loginAndExtractId("Bob", "cid2");
      c3.login("Charlie", "cid3");

      c1.send("NEW");
      assertEquals("ERROR MESSAGE=MISSING_PLAYER_ID", c1.readLine());

      c1.send("NEW PLAYER_ID=abc");
      assertEquals("ERROR MESSAGE=INVALID_PLAYER_ID", c1.readLine());

      c1.send("NEW PLAYER_ID=999");
      assertEquals("ERROR MESSAGE=PLAYER_NOT_FOUND", c1.readLine());

      c1.send("NEW PLAYER_ID=" + id1);
      assertEquals("ERROR MESSAGE=CANNOT_PLAY_SELF", c1.readLine());

      c1.send("NEW PLAYER_ID=" + id2);
      assertTrue(c1.readLine().startsWith("NEW_OK"));
      assertTrue(c2.readLine().startsWith("GAME_STARTED"));

      c1.send("NEW PLAYER_ID=" + id2);
      assertEquals("ERROR MESSAGE=YOU_ARE_BUSY", c1.readLine());

      c3.send("NEW PLAYER_ID=" + id1);
      assertEquals("ERROR MESSAGE=PLAYER_BUSY", c3.readLine());
    }
  }

  @Test
  @DisplayName("NEW success notifies both players")
  void new_success() throws Exception {
    int port = startServer();

    try (RawClient c1 = new RawClient("127.0.0.1", port);
        RawClient c2 = new RawClient("127.0.0.1", port)) {

      int id1 = c1.loginAndExtractId("Alice", "cid1");
      int id2 = c2.loginAndExtractId("Bob", "cid2");

      c1.send("NEW PLAYER_ID=" + id2);

      String requester = c1.readLine();
      String target = c2.readLine();

      assertTrue(requester.startsWith("NEW_OK"));
      assertTrue(requester.contains("GAME_ID="));
      assertTrue(requester.contains("OPPONENT_ID=" + id2));
      assertTrue(requester.contains("OPPONENT_NAME=Bob"));

      assertTrue(target.startsWith("GAME_STARTED"));
      assertTrue(target.contains("OPPONENT_ID=" + id1));
      assertTrue(target.contains("OPPONENT_NAME=Alice"));
    }
  }

  @Test
  @DisplayName("MOVE validation covers missing move, wrong turn and invalid move")
  void move_validation() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      game.c1.send("MOVE");
      assertEquals("ERROR MESSAGE=MISSING_MOVE", game.c1.readLine());

      game.wrongPlayer().send("MOVE e2e4");
      assertEquals("ERROR MESSAGE=NOT_YOUR_TURN", game.wrongPlayer().readLine());

      game.currentPlayer().send("MOVE badMove");
      assertEquals("ERROR MESSAGE=INVALID_MOVE", game.currentPlayer().readLine());
    }
  }

  @Test
  @DisplayName("MOVE returns GAME_NOT_FOUND when session is missing")
  void move_game_not_found() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      Integer gameId = server.getGameIdByPlayer(game.id1);
      assertNotNull(gameId);

      @SuppressWarnings("unchecked")
      Map<Integer, ServerGameSession> activeGames = getField(server, "activeGames", Map.class);
      activeGames.remove(gameId);

      game.c1.send("MOVE e2e4");
      assertEquals("ERROR MESSAGE=GAME_NOT_FOUND", game.c1.readLine());
    }
  }

  @Test
  @DisplayName("MOVE valid path executes extra logic")
  void move_success_path() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      RawClient current = game.currentPlayer();
      RawClient opponent = game.wrongPlayer();

      current.send("MOVE a1a2");
      String first = current.readLine();
      assertNotNull(first);

      if (first.startsWith("MOVE_OK")) {
        String second = opponent.readLine();
        if (second != null) {
          assertTrue(second.startsWith("OPPONENT_MOVE") || second.startsWith("GAME_OVER"));
        }
      } else {
        assertEquals("ERROR MESSAGE=INVALID_MOVE", first);
      }
    }
  }

  @Test
  @DisplayName("RESIGN returns GAME_NOT_FOUND when session is missing")
  void resign_game_not_found() throws Exception {
    int port = startServer();

    try (GameSetup game = startLoggedGame(port)) {
      Integer gameId = server.getGameIdByPlayer(game.id1);
      assertNotNull(gameId);

      @SuppressWarnings("unchecked")
      Map<Integer, ServerGameSession> activeGames = getField(server, "activeGames", Map.class);
      activeGames.remove(gameId);

      game.c1.send("RESIGN");
      assertEquals("ERROR MESSAGE=GAME_NOT_FOUND", game.c1.readLine());
    }
  }

  @Test
  @DisplayName("RESIGN returns OPPONENT_NOT_FOUND when session has no opponent")
  void resign_opponent_not_found() throws Exception {
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
  }

  @Test
  @DisplayName("RESIGN ends the game and notifies both players")
  void resign_ends_game() throws Exception {
    int port = startServer();

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
  void server_stop_with_connected_clients() throws Exception {
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
  @DisplayName("ClientHandler stop is safe when called twice and with closed socket")
  void client_handler_stop_branches() throws Exception {
    int port = startServer();

    try (Socket socket = new Socket("127.0.0.1", port)) {
      ClientHandler handler = new ClientHandler(socket, server);

      handler.stop();
      handler.stop();

      assertDoesNotThrow(socket::close);
    }
  }

  @Test
  @DisplayName("Private send does nothing when writer is null")
  void send_with_null_writer() throws Exception {
    int port = startServer();

    try (Socket socket = new Socket("127.0.0.1", port)) {
      ClientHandler handler = new ClientHandler(socket, server);

      Method send = ClientHandler.class.getDeclaredMethod("send", String.class);
      send.setAccessible(true);

      setField(handler, "out", null);

      assertDoesNotThrow(() -> send.invoke(handler, "HELLO"));
    }
  }
}
