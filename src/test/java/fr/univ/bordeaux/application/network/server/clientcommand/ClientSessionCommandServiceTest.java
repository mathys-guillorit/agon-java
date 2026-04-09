package fr.univ.bordeaux.application.network.server.clientcommand;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientSessionCommandServiceTest {

  private final ClientSessionCommandService service = new ClientSessionCommandService();

  private Field findField(Class<?> type, String fieldName) throws Exception {
    Class<?> current = type;
    while (current != null) {
      try {
        Field f = current.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f;
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field f = findField(target.getClass(), fieldName);
    f.set(target, value);
  }

  private void trySetField(Object target, String fieldName, Object value) {
    try {
      setField(target, fieldName, value);
    } catch (Exception ignored) {
    }
  }

  private static class HandlerBox {
    final ClientHandler handler;
    final StringWriter buffer;

    HandlerBox(ClientHandler handler, StringWriter buffer) {
      this.handler = handler;
      this.buffer = buffer;
    }

    String sentText() {
      return buffer.toString();
    }
  }

  private HandlerBox buildHandler(AgonServer server, OnlinePlayer player) throws Exception {
    ClientHandler handler = new ClientHandler(new Socket(), server);
    StringWriter sw = new StringWriter();
    BufferedWriter bw = new BufferedWriter(sw);

    setField(handler, "out", bw);
    setField(handler, "player", player);

    return new HandlerBox(handler, sw);
  }

  private Command buildCommand(String rawArgument, Map<String, String> args) {
    return new Command(null, args, rawArgument);
  }

  private Command buildLoginCommand(String clientId, String name) throws Exception {
    Map<String, String> args = new HashMap<>();
    args.put("CLIENT_ID", clientId);
    args.put("NAME", name);
    return buildCommand(null, args);
  }

  private Command buildPlayersCommand(String rawArgument) throws Exception {
    return buildCommand(rawArgument, new HashMap<>());
  }

  @Test
  @DisplayName("constructeur")
  void constructorTest() {
    assertNotNull(new ClientSessionCommandService());
  }

  @Test
  @DisplayName("handleStatus : réponse STATUS_OK")
  void handleStatusTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    server.registerPlayer("c1", "Alice", null);
    server.registerPlayer("c2", "Bob", null);

    HandlerBox hb = buildHandler(server, null);

    assertDoesNotThrow(() -> service.handleStatus(hb.handler));

    String sent = hb.sentText();
    assertTrue(sent.contains("STATUS_OK"));
    assertTrue(sent.contains("port=12345"));
    assertTrue(sent.contains("players=2"));
  }

  @Test
  @DisplayName("handleLogin : déjà connecté")
  void handleLoginAlreadyLoggedTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);

    HandlerBox hb = buildHandler(server, player);
    Command cmd = buildLoginCommand("otherClient", "OtherName");

    assertDoesNotThrow(() -> service.handleLogin(cmd, hb.handler));
    assertTrue(hb.sentText().contains("ERROR ALREADY_LOGGED_IN"));
  }

  @Test
  @DisplayName("handleLogin : nom manquant")
  void handleLoginMissingNameTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildLoginCommand("c1", "   ");

    assertDoesNotThrow(() -> service.handleLogin(cmd, hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=MISSING_NAME"));
  }

  @Test
  @DisplayName("handleLogin : client id manquant")
  void handleLoginMissingClientIdTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildLoginCommand("   ", "Alice");

    assertDoesNotThrow(() -> service.handleLogin(cmd, hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=MISSING_CLIENT_ID"));
  }

  static class NullRegisterServer extends AgonServer {
    NullRegisterServer() {
      super(12345, "test");
    }

    @Override
    public OnlinePlayer registerPlayer(String clientId, String name, ClientHandler handler) {
      return null;
    }
  }

  @Test
  @DisplayName("handleLogin : échec d'enregistrement")
  void handleLoginFailedRegistrationTest() throws Exception {
    AgonServer server = new NullRegisterServer();

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildLoginCommand("c1", "Alice");

    assertDoesNotThrow(() -> service.handleLogin(cmd, hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=LOGIN_FAILED"));
  }

  @Test
  @DisplayName("handleLogin : succès")
  void handleLoginSuccessTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildLoginCommand("c1", "Alice");

    assertDoesNotThrow(() -> service.handleLogin(cmd, hb.handler));

    String sent = hb.sentText();
    assertTrue(sent.contains("WELCOME ID="));
    assertTrue(sent.contains("NAME=Alice"));
    assertTrue(sent.contains("STATUS=idle"));
    assertNotNull(hb.handler.getPlayer());
  }

  @Test
  @DisplayName("handlePlayers : sans argument -> liste")
  void handlePlayersListTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    server.registerPlayer("c1", "Alice", null);
    server.registerPlayer("c2", "Bob", null);

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildPlayersCommand(null);

    assertDoesNotThrow(() -> service.handlePlayers(cmd, hb.handler));

    String sent = hb.sentText();
    assertTrue(sent.contains("=== PLAYERS ==="));
    assertTrue(sent.contains("NAME=Alice"));
    assertTrue(sent.contains("NAME=Bob"));
  }

  @Test
  @DisplayName("handlePlayers : argument vide -> liste")
  void handlePlayersBlankArgumentTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    server.registerPlayer("c1", "Alice", null);

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildPlayersCommand("   ");

    assertDoesNotThrow(() -> service.handlePlayers(cmd, hb.handler));
    assertTrue(hb.sentText().contains("=== PLAYERS ==="));
  }

  @Test
  @DisplayName("handlePlayers : id invalide")
  void handlePlayersInvalidIdTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildPlayersCommand("abc");

    assertDoesNotThrow(() -> service.handlePlayers(cmd, hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=INVALID_PLAYER_ID"));
  }

  @Test
  @DisplayName("handlePlayers : détails d'un joueur")
  void handlePlayersDetailsTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);

    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildPlayersCommand(String.valueOf(player.getId()));

    assertDoesNotThrow(() -> service.handlePlayers(cmd, hb.handler));

    String sent = hb.sentText();
    assertTrue(sent.contains("PLAYER ID=" + player.getId()));
    assertTrue(sent.contains("NAME=Alice"));
  }

  @Test
  @DisplayName("handleAway : pas connecté")
  void handleAwayNotLoggedTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    HandlerBox hb = buildHandler(server, null);

    assertDoesNotThrow(() -> service.handleAway(hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=NOT_LOGGED_IN"));
  }

  @Test
  @DisplayName("handleAway : joueur non idle")
  void handleAwayCannotSetAwayNowTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);
    player.setStatus(PlayerStatus.AWAY);

    HandlerBox hb = buildHandler(server, player);

    assertDoesNotThrow(() -> service.handleAway(hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=CANNOT_SET_AWAY_NOW"));
  }

  @Test
  @DisplayName("handleAway : succès")
  void handleAwaySuccessTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);
    player.setStatus(PlayerStatus.IDLE);

    HandlerBox hb = buildHandler(server, player);

    assertDoesNotThrow(() -> service.handleAway(hb.handler));

    assertTrue(hb.sentText().contains("AWAY_OK STATUS=away"));
    assertEquals(PlayerStatus.AWAY, player.getStatus());
  }

  @Test
  @DisplayName("handleBack : pas connecté")
  void handleBackNotLoggedTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    HandlerBox hb = buildHandler(server, null);

    assertDoesNotThrow(() -> service.handleBack(hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=NOT_LOGGED_IN"));
  }

  @Test
  @DisplayName("handleBack : joueur pas away")
  void handleBackCannotSetBackNowTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);
    player.setStatus(PlayerStatus.IDLE);

    HandlerBox hb = buildHandler(server, player);

    assertDoesNotThrow(() -> service.handleBack(hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=CANNOT_SET_BACK_NOW"));
  }

  @Test
  @DisplayName("handleBack : succès")
  void handleBackSuccessTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);
    player.setStatus(PlayerStatus.AWAY);

    HandlerBox hb = buildHandler(server, player);

    assertDoesNotThrow(() -> service.handleBack(hb.handler));

    assertTrue(hb.sentText().contains("BACK_OK STATUS=idle"));
    assertEquals(PlayerStatus.IDLE, player.getStatus());
  }

  @Test
  @DisplayName("LOGIN puis STATUS puis PLAYERS")
  void integrationLikeFlowTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    HandlerBox hb = buildHandler(server, null);

    Command login = buildLoginCommand("c1", "Alice");
    assertDoesNotThrow(() -> service.handleLogin(login, hb.handler));
    assertTrue(hb.sentText().contains("WELCOME ID="));

    HandlerBox hb2 = buildHandler(server, null);
    assertDoesNotThrow(() -> service.handleStatus(hb2.handler));
    assertTrue(hb2.sentText().contains("players=1"));

    HandlerBox hb3 = buildHandler(server, null);
    Command players = buildPlayersCommand(null);
    assertDoesNotThrow(() -> service.handlePlayers(players, hb3.handler));
    assertTrue(hb3.sentText().contains("NAME=Alice"));
  }
}
