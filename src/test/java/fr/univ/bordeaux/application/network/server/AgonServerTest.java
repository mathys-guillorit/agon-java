package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgonServerTest {

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

  @SuppressWarnings("unchecked")
  private <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
    Field f = target.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    return (T) f.get(target);
  }

  private ServerGameSession start2PlayersGame(AgonServer s) {
    OnlinePlayer p1 = s.registerPlayer("client-1", "Alice", null);
    OnlinePlayer p2 = s.registerPlayer("client-2", "Bob", null);

    ServerGameSession session = s.startNewGame(p1.getId(), p2.getId());
    assertNotNull(session);
    assertEquals(1, s.getActiveGameCount());
    assertTrue(s.isPlayerInGame(p1.getId()));
    assertTrue(s.isPlayerInGame(p2.getId()));

    return session;
  }

  @Test
  @DisplayName("Constructors and default values")
  void constructors_and_defaults() {
    AgonServer s1 = new AgonServer("TestServer");
    assertEquals(12345, s1.getPort());
    assertNotNull(s1.getName());
    assertTrue(s1.getName().contains("TestServer"));
    assertFalse(s1.isRunning());

    AgonServer s2 = new AgonServer(33342, "OtherServer");
    assertEquals(33342, s2.getPort());
    assertTrue(s2.getName().contains("OtherServer"));
    assertFalse(s2.isRunning());

    assertEquals(0, s2.getConnectedClientsCount());
    assertEquals(0, s2.getActiveGameCount());
    assertFalse(s2.isPlayerInGame(1));
    assertNull(s2.getGameIdByPlayer(1));
    assertNull(s2.getGameById(1));
    assertNull(s2.getPlayerById(1));
  }

  @Test
  @DisplayName("Start and stop basic lifecycle")
  void server_lifecycle() throws Exception {
    int port = freePort();
    server = new AgonServer(port, "TestServer");

    assertTrue(server.start());
    assertTrue(server.isRunning());

    assertTrue(server.start());
    assertTrue(server.isRunning());

    assertTrue(server.stop());
    assertFalse(server.isRunning());

    assertTrue(server.stop());
    assertFalse(server.isRunning());
  }

  @Test
  @DisplayName("Start fails when port is already used")
  void server_start_port_used() throws Exception {
    int port = freePort();

    try (ServerSocket lock = new ServerSocket(port)) {
      AgonServer s = new AgonServer(port, "TestServer");

      assertFalse(s.start());
      assertFalse(s.isRunning());
    }
  }

  @Test
  @DisplayName("RegisterPlayer validates inputs and creates player")
  void register_player_validation_and_creation() {
    AgonServer s = new AgonServer(33334, "TestServer");

    assertNull(s.registerPlayer(null, "Alice", null));
    assertNull(s.registerPlayer("id1", null, null));
    assertNull(s.registerPlayer("   ", "Alice", null));
    assertNull(s.registerPlayer("id1", "   ", null));

    OnlinePlayer player = s.registerPlayer("client-1", "Alice", null);
    assertNotNull(player);
    assertEquals(1, player.getId());
    assertEquals("client-1", player.getClientId());
    assertEquals("Alice", player.getName());
    assertEquals(PlayerStatus.IDLE, player.getStatus());
    assertEquals(1, s.getPlayerCount());
    assertEquals(player, s.getPlayerById(player.getId()));
  }

  @Test
  @DisplayName("RegisterPlayer reconnects existing player with same client ID")
  void register_player_reconnects_existing_player() {
    AgonServer s = new AgonServer(33336, "TestServer");

    OnlinePlayer p1 = s.registerPlayer("client-1", "Alice", null);
    p1.setStatus(PlayerStatus.INGAME);

    OnlinePlayer p2 = s.registerPlayer("client-1", "AliceAgain", null);

    assertNotNull(p2);
    assertSame(p1, p2);
    assertEquals(PlayerStatus.IDLE, p2.getStatus());
    assertEquals(1, s.getPlayerCount());
  }

  @Test
  @DisplayName("removePlayer handles null and existing player")
  void remove_player() {
    AgonServer s = new AgonServer(33337, "TestServer");

    assertDoesNotThrow(() -> s.removePlayer(null));

    OnlinePlayer player = s.registerPlayer("client-1", "Alice", null);
    assertEquals(1, s.getPlayerCount());

    s.removePlayer(player);

    assertEquals(0, s.getPlayerCount());
    assertNull(player.getHandler());
    assertEquals(PlayerStatus.IDLE, player.getStatus());
  }

  @Test
  @DisplayName("Players list and scoreboard formatting")
  void players_and_scoreboard() {
    AgonServer s = new AgonServer(33339, "TestServer");

    assertEquals("PLAYERS_EMPTY\nEND", s.getPlayersList());
    assertEquals("SCOREBOARD_EMPTY\nEND", s.getScoreboard());

    OnlinePlayer p1 = s.registerPlayer("client-1", "Alice", null);
    OnlinePlayer p2 = s.registerPlayer("client-2", "Bob", null);
    p2.setStatus(PlayerStatus.INGAME);

    String players = s.getPlayersList();
    assertTrue(players.contains("=== PLAYERS ==="));
    assertTrue(players.contains("ID=" + p1.getId()));
    assertTrue(players.contains("NAME=Alice"));
    assertTrue(players.contains("NAME=Bob"));
    assertTrue(players.contains("STATUS=idle"));
    assertTrue(players.contains("STATUS=ingame"));
    assertTrue(players.endsWith("END"));

    String scoreboard = s.getScoreboard();
    assertTrue(scoreboard.contains("=== SCOREBOARD ==="));
    assertTrue(scoreboard.contains("NAME=Alice"));
    assertTrue(scoreboard.contains("NAME=Bob"));
    assertTrue(scoreboard.contains("WINS=0"));
    assertTrue(scoreboard.contains("LOSSES=0"));
    assertTrue(scoreboard.contains("GAMES=0"));
    assertTrue(scoreboard.endsWith("END"));
  }

  @Test
  @DisplayName("startNewGame returns null for invalid players")
  void start_new_game_invalid_players() {
    AgonServer s = new AgonServer(33343, "TestServer");

    assertNull(s.startNewGame(1, 2));

    OnlinePlayer p1 = s.registerPlayer("client-1", "Alice", null);
    assertNull(s.startNewGame(p1.getId(), 999));
    assertNull(s.startNewGame(999, p1.getId()));
  }

  @Test
  @DisplayName("startNewGame starts a valid game")
  void start_new_game_success() {
    AgonServer s = new AgonServer(33344, "TestServer");

    ServerGameSession session = start2PlayersGame(s);
    assertNotNull(session);

    Integer gameId = s.getGameIdByPlayer(session.getwhitePlayer().getId());
    assertNotNull(gameId);
    assertEquals(session, s.getGameById(gameId));
  }

  @Test
  @DisplayName("finishGame does nothing when session is null or already removed")
  void finish_game_null_or_already_removed() throws Exception {
    AgonServer s = new AgonServer(33345, "TestServer");

    assertDoesNotThrow(() -> s.finishGame(null, 1, "NORMAL_END"));

    ServerGameSession session = start2PlayersGame(s);
    int gameId = session.getGameId();

    @SuppressWarnings("unchecked")
    Map<Integer, ServerGameSession> activeGames = getField(s, "activeGames", Map.class);
    activeGames.remove(gameId);

    assertDoesNotThrow(() -> s.finishGame(session, session.getwhitePlayer().getId(), "NORMAL_END"));
  }

  @Test
  @DisplayName("finishGame handles white winner and updates state")
  void finish_game_white_winner() {
    AgonServer s = new AgonServer(33346, "TestServer");

    ServerGameSession session = start2PlayersGame(s);
    OnlinePlayer white = session.getwhitePlayer();
    OnlinePlayer black = session.getblackPlayer();

    s.finishGame(session, white.getId(), "NORMAL_END");

    assertEquals(0, s.getActiveGameCount());
    assertFalse(s.isPlayerInGame(white.getId()));
    assertFalse(s.isPlayerInGame(black.getId()));
    assertEquals(PlayerStatus.IDLE, white.getStatus());
    assertEquals(PlayerStatus.IDLE, black.getStatus());

    String scoreboard = s.getScoreboard();
    assertTrue(scoreboard.contains("NAME=" + white.getName()));
    assertTrue(scoreboard.contains("NAME=" + black.getName()));
    assertTrue(scoreboard.contains("WINS=1"));
    assertTrue(scoreboard.contains("LOSSES=1"));
    assertTrue(scoreboard.contains("GAMES=1"));
  }

  @Test
  @DisplayName("finishGame handles black winner and updates state")
  void finish_game_black_winner() {
    AgonServer s = new AgonServer(33347, "TestServer");

    ServerGameSession session = start2PlayersGame(s);
    OnlinePlayer white = session.getwhitePlayer();
    OnlinePlayer black = session.getblackPlayer();

    s.finishGame(session, black.getId(), "NORMAL_END");

    assertEquals(0, s.getActiveGameCount());
    assertEquals(PlayerStatus.IDLE, white.getStatus());
    assertEquals(PlayerStatus.IDLE, black.getStatus());

    String scoreboard = s.getScoreboard();
    assertTrue(scoreboard.contains("NAME=" + white.getName()));
    assertTrue(scoreboard.contains("NAME=" + black.getName()));
    assertTrue(scoreboard.contains("WINS=1"));
    assertTrue(scoreboard.contains("LOSSES=1"));
    assertTrue(scoreboard.contains("GAMES=1"));
  }

  @Test
  @DisplayName("finishGame with unknown winner does not break cleanup")
  void finish_game_unknown_winner() {
    AgonServer s = new AgonServer(33348, "TestServer");

    ServerGameSession session = start2PlayersGame(s);
    OnlinePlayer white = session.getwhitePlayer();
    OnlinePlayer black = session.getblackPlayer();

    s.finishGame(session, -1, "ABORTED");

    assertEquals(0, s.getActiveGameCount());
    assertFalse(s.isPlayerInGame(white.getId()));
    assertFalse(s.isPlayerInGame(black.getId()));
    assertEquals(PlayerStatus.IDLE, white.getStatus());
    assertEquals(PlayerStatus.IDLE, black.getStatus());

    String scoreboard = s.getScoreboard();
    assertTrue(scoreboard.contains("NAME=" + white.getName()));
    assertTrue(scoreboard.contains("NAME=" + black.getName()));
    assertTrue(scoreboard.contains("WINS=0"));
    assertTrue(scoreboard.contains("LOSSES=0"));
  }

  @Test
  @DisplayName("getPlayerDetails returns error when player is not found")
  void get_player_details_player_not_found() {
    AgonServer s = new AgonServer(33349, "TestServer");

    String result = s.getPlayerDetails(999);

    assertEquals("ERROR MESSAGE=PLAYER_NOT_FOUND", result);
  }

  @Test
  @DisplayName("getPlayerDetails returns formatted details for an existing player")
  void get_player_details_existing_player() {
    AgonServer s = new AgonServer(33350, "TestServer");

    OnlinePlayer player = s.registerPlayer("client-1", "Alice", null);
    assertNotNull(player);

    player.setStatus(PlayerStatus.AWAY);

    String result = s.getPlayerDetails(player.getId());

    assertTrue(result.contains("PLAYER ID=" + player.getId()));
    assertTrue(result.contains("NAME=Alice"));
    assertTrue(result.contains("STATUS=away"));
    assertTrue(result.contains("WINS=0"));
    assertTrue(result.contains("LOSSES=0"));
    assertTrue(result.contains("GAMES=0"));
  }
}
