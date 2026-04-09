package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.game.GameLobby;
import fr.univ.bordeaux.application.network.server.game.GameMode;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import fr.univ.bordeaux.application.network.server.invitation.Invitation;
import fr.univ.bordeaux.application.network.server.invitation.InvitationService;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgonServerTest {

  private void expire(Invitation invitation) throws Exception {
    Field f = Invitation.class.getDeclaredField("expiresAt");
    f.setAccessible(true);
    f.setLong(invitation, System.currentTimeMillis() - 1);
  }

  private InvitationService invitationServiceOf(AgonServer server) throws Exception {
    Field f = AgonServer.class.getDeclaredField("invitationService");
    f.setAccessible(true);
    return (InvitationService) f.get(server);
  }

  @Test
  @DisplayName("invitation + accept + chooseMode full flow")
  void fullFlow() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertTrue(s.createInvitation(p1.getId(), p2.getId()));
    assertNotNull(s.getInvitationByInviter(p1.getId()));

    GameLobby lobby = s.acceptInvitation(p2.getId());
    assertNotNull(lobby);
    assertEquals(p1.getId(), lobby.getHostId());
    assertEquals(p2.getId(), lobby.getGuestId());

    ServerGameSession session = s.chooseMode(p1.getId(), GameMode.NORMAL);
    assertNotNull(session);
    assertEquals(PlayerStatus.INGAME, p1.getStatus());
    assertEquals(PlayerStatus.INGAME, p2.getStatus());
  }

  @Test
  @DisplayName("invitation invalid cases")
  void invitationInvalid() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);

    assertFalse(s.createInvitation(1, 2));
    assertFalse(s.createInvitation(p1.getId(), p1.getId()));

    p1.setStatus(PlayerStatus.AWAY);
    assertFalse(s.createInvitation(p1.getId(), p1.getId()));
  }

  @Test
  @DisplayName("accept invitation null + expired")
  void acceptInvitationEdge() throws Exception {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertNull(s.acceptInvitation(1));

    assertTrue(s.createInvitation(p1.getId(), p2.getId()));
    Invitation inv = s.getInvitationByInvited(p2.getId());
    assertNotNull(inv);
    expire(inv);

    assertNull(s.acceptInvitation(p2.getId()));
    assertNull(s.getInvitationByInviter(p1.getId()));
    assertNull(s.getInvitationByInvited(p2.getId()));
  }

  @Test
  @DisplayName("chooseMode edge cases")
  void chooseModeEdge() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertNull(s.chooseMode(p1.getId(), null));
    assertNull(s.chooseMode(p1.getId(), GameMode.NORMAL));

    assertTrue(s.createInvitation(p1.getId(), p2.getId()));
    assertNotNull(s.acceptInvitation(p2.getId()));

    assertNull(s.chooseMode(p2.getId(), GameMode.NORMAL));
  }

  @Test
  @DisplayName("removePlayer with lobby")
  void removePlayerLobby() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertTrue(s.createInvitation(p1.getId(), p2.getId()));
    assertNotNull(s.acceptInvitation(p2.getId()));

    s.removePlayer(p1);

    assertEquals(PlayerStatus.IDLE, p1.getStatus());
    assertEquals(PlayerStatus.IDLE, p2.getStatus());
    assertNull(s.getLobbyByPlayer(p1.getId()));
    assertNull(s.getLobbyByPlayer(p2.getId()));
  }

  @Test
  @DisplayName("clearExpiredInvitations through invitation service")
  void cleanExpired() throws Exception {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertTrue(s.createInvitation(p1.getId(), p2.getId()));
    Invitation inv = s.getInvitationByInviter(p1.getId());
    assertNotNull(inv);
    expire(inv);

    invitationServiceOf(s).clearExpiredInvitations();

    assertNull(s.getInvitationByInviter(p1.getId()));
    assertNull(s.getInvitationByInvited(p2.getId()));
    assertEquals(PlayerStatus.IDLE, p1.getStatus());
    assertEquals(PlayerStatus.IDLE, p2.getStatus());
  }

  @Test
  @DisplayName("player list details and scoreboard basics")
  void viewsAndCounts() {
    AgonServer s = new AgonServer(12345, "test");

    OnlinePlayer p1 = s.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "Bob", null);

    assertNotNull(p1);
    assertNotNull(p2);
    assertEquals(2, s.getPlayerCount());

    String players = s.getPlayersList();
    assertTrue(players.contains("=== PLAYERS ==="));
    assertTrue(players.contains("NAME=Alice"));
    assertTrue(players.contains("NAME=Bob"));

    String details = s.getPlayerDetails(p1.getId());
    assertTrue(details.contains("PLAYER ID=" + p1.getId()));
    assertTrue(details.contains("NAME=Alice"));
    assertTrue(details.contains("WINS=0"));
    assertTrue(details.contains("LOSSES=0"));
    assertTrue(details.contains("GAMES=0"));

    String missing = s.getPlayerDetails(9999);
    assertEquals("ERROR MESSAGE=PLAYER_NOT_FOUND", missing);

    String scoreboard = s.getScoreboard();
    assertTrue(scoreboard.contains("=== SCOREBOARD ==="));
    assertTrue(scoreboard.contains("NAME=Alice"));
    assertTrue(scoreboard.contains("NAME=Bob"));
  }

  @Test
  @DisplayName("remove player also clears pending invitation")
  void removePlayerClearsInvitation() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertTrue(s.createInvitation(p1.getId(), p2.getId()));
    assertNotNull(s.getInvitationByInviter(p1.getId()));

    s.removePlayer(p1);

    assertNull(s.getInvitationByInviter(p1.getId()));
    assertNull(s.getInvitationByInvited(p2.getId()));
    assertNull(s.getPlayerById(p1.getId()));
  }

  @Test
  @DisplayName("server lifecycle basic start stop")
  void lifecycle() {
    AgonServer s = new AgonServer(12345, "test");
    assertDoesNotThrow(s::stop);
    assertFalse(s.isRunning());
  }
}
