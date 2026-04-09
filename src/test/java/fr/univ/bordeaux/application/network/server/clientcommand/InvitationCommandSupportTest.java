package fr.univ.bordeaux.application.network.server.clientcommand;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.application.network.server.game.GameLobby;
import fr.univ.bordeaux.application.network.server.game.GameMode;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import fr.univ.bordeaux.application.network.server.invitation.Invitation;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvitationCommandSupportTest {

  private final InvitationCommandSupport support = new InvitationCommandSupport();

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

  private ClientHandler buildHandler(AgonServer server, OnlinePlayer player) throws Exception {
    ClientHandler handler = new ClientHandler(new Socket(), server);
    StringWriter sw = new StringWriter();
    BufferedWriter bw = new BufferedWriter(sw);

    setField(handler, "out", bw);
    setField(handler, "player", player);

    return handler;
  }

  private void setPlayerHandler(OnlinePlayer player, ClientHandler handler) throws Exception {
    setField(player, "handler", handler);
  }

  @Test
  @DisplayName("parsePlayerId : valeurs valides et invalides")
  void parsePlayerIdTest() {
    assertEquals(12, support.parsePlayerId("12"));
    assertEquals(0, support.parsePlayerId("0"));
    assertEquals(-4, support.parsePlayerId("-4"));

    assertNull(support.parsePlayerId(null));
    assertNull(support.parsePlayerId("abc"));
    assertNull(support.parsePlayerId("12a"));
    assertNull(support.parsePlayerId(" "));
  }

  @Test
  @DisplayName("parseMode : normal, blitz et cas invalides")
  void parseModeTest() {
    assertEquals(GameMode.NORMAL, support.parseMode("normal"));
    assertEquals(GameMode.NORMAL, support.parseMode("NORMAL"));
    assertEquals(GameMode.NORMAL, support.parseMode("NoRmAl"));

    assertEquals(GameMode.BLITZ, support.parseMode("blitz"));
    assertEquals(GameMode.BLITZ, support.parseMode("BLITZ"));
    assertEquals(GameMode.BLITZ, support.parseMode("BlItZ"));

    assertNull(support.parseMode(null));
    assertNull(support.parseMode(""));
    assertNull(support.parseMode("   "));
    assertNull(support.parseMode("ranked"));
  }

  @Test
  @DisplayName("isIdle : vrai si IDLE, faux sinon")
  void isIdleTest() {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);

    assertTrue(support.isIdle(player));

    player.setStatus(PlayerStatus.AWAY);
    assertFalse(support.isIdle(player));
  }

  @Test
  @DisplayName("getPlayerNameOrUnknown : nom ou UNKNOWN")
  void getPlayerNameOrUnknownTest() {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);

    assertEquals("Alice", support.getPlayerNameOrUnknown(player));
    assertEquals("UNKNOWN", support.getPlayerNameOrUnknown(null));
  }

  @Test
  @DisplayName("getCurrentPlayer : retourne le joueur associé au handler")
  void getCurrentPlayerTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);
    ClientHandler handler = buildHandler(server, player);

    assertEquals(player, support.getCurrentPlayer(handler));
  }

  @Test
  @DisplayName("getPlayerById : retourne le joueur trouvé ou null")
  void getPlayerByIdTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    server.registerPlayer("c2", "Bob", null);

    ClientHandler handler = buildHandler(server, p1);

    assertEquals(p1, support.getPlayerById(handler, p1.getId()));
    assertNull(support.getPlayerById(handler, 99999));
  }

  @Test
  @DisplayName("createInvitation + getInvitationByInvited + getInvitationByInviter")
  void invitationLookupTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    ClientHandler handler = buildHandler(server, p1);

    assertTrue(support.createInvitation(handler, p1.getId(), p2.getId()));

    Invitation byInvited = support.getInvitationByInvited(handler, p2.getId());
    Invitation byInviter = support.getInvitationByInviter(handler, p1.getId());

    assertNotNull(byInvited);
    assertNotNull(byInviter);
    assertEquals(byInvited, byInviter);
  }

  @Test
  @DisplayName("acceptInvitation + getLobbyByPlayer")
  void acceptInvitationAndLobbyLookupTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    ClientHandler handler = buildHandler(server, p1);

    assertTrue(support.createInvitation(handler, p1.getId(), p2.getId()));

    GameLobby lobby = support.acceptInvitation(handler, p2.getId());

    assertNotNull(lobby);
    assertEquals(lobby, support.getLobbyByPlayer(handler, p1.getId()));
    assertEquals(lobby, support.getLobbyByPlayer(handler, p2.getId()));
    assertNull(support.getLobbyByPlayer(handler, 99999));
  }

  @Test
  @DisplayName("removeInvitation : supprime l'invitation")
  void removeInvitationTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    ClientHandler handler = buildHandler(server, p1);

    assertTrue(support.createInvitation(handler, p1.getId(), p2.getId()));
    assertNotNull(support.getInvitationByInviter(handler, p1.getId()));

    support.removeInvitation(handler, p1.getId());

    assertNull(support.getInvitationByInviter(handler, p1.getId()));
    assertNull(support.getInvitationByInvited(handler, p2.getId()));
  }

  @Test
  @DisplayName("chooseMode : crée une session quand l'hôte choisit un mode valide")
  void chooseModeTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    ClientHandler handler = buildHandler(server, p1);

    assertTrue(support.createInvitation(handler, p1.getId(), p2.getId()));
    assertNotNull(support.acceptInvitation(handler, p2.getId()));

    ServerGameSession session = support.chooseMode(handler, p1.getId(), GameMode.NORMAL);

    assertNotNull(session);
    assertEquals(PlayerStatus.INGAME, p1.getStatus());
    assertEquals(PlayerStatus.INGAME, p2.getStatus());
  }

  @Test
  @DisplayName("sendToPlayer : ne fait rien si player est null")
  void sendToPlayerNullPlayerTest() {
    assertDoesNotThrow(() -> support.sendToPlayer(null, "hello"));
  }

  @Test
  @DisplayName("sendToPlayer : ne fait rien si handler du joueur est null")
  void sendToPlayerNullHandlerTest() {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);

    assertNull(player.getHandler());
    assertDoesNotThrow(() -> support.sendToPlayer(player, "hello"));
  }

  @Test
  @DisplayName("sendToPlayer : appel avec handler injecté")
  void sendToPlayerWithInjectedHandlerTest() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer player = server.registerPlayer("c1", "Alice", null);
    ClientHandler handler = buildHandler(server, player);

    setPlayerHandler(player, handler);

    assertDoesNotThrow(() -> support.sendToPlayer(player, "hello"));
  }

  @Test
  @DisplayName("constantes protocolaires")
  void constantsTest() {
    assertEquals("ERROR MESSAGE=NOT_LOGGED_IN", InvitationCommandSupport.ERROR_NOT_LOGGED);
    assertEquals("normal", InvitationCommandSupport.MODE_NORMAL);
    assertEquals("blitz", InvitationCommandSupport.MODE_BLITZ);
  }
}
