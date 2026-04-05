package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgonServerTest {

  private Object invoke(Object o, String name) throws Exception {
    Method m = o.getClass().getDeclaredMethod(name);
    m.setAccessible(true);
    return m.invoke(o);
  }

  private void expire(Invitation i) throws Exception {
    Field f = Invitation.class.getDeclaredField("expiresAt");
    f.setAccessible(true);
    f.setLong(i, System.currentTimeMillis() - 1);
  }

  @Test
  @DisplayName("invitation + accept + chooseMode full flow")
  void full_flow() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertTrue(s.createInvitation(p1.getId(), p2.getId()));
    assertNotNull(s.getInvitationByInviter(p1.getId()));

    assertNotNull(s.acceptInvitation(p2.getId()));

    assertNotNull(s.chooseMode(p1.getId(), GameMode.NORMAL));
    assertEquals(PlayerStatus.INGAME, p1.getStatus());
  }

  @Test
  @DisplayName("invitation invalid cases")
  void invitation_invalid() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);

    assertFalse(s.createInvitation(1, 2));
    assertFalse(s.createInvitation(p1.getId(), p1.getId()));

    p1.setStatus(PlayerStatus.AWAY);
    assertFalse(s.createInvitation(p1.getId(), p1.getId()));
  }

  @Test
  @DisplayName("accept invitation null + expired")
  void accept_invitation_edge() throws Exception {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertNull(s.acceptInvitation(1));

    s.createInvitation(p1.getId(), p2.getId());
    Invitation inv = s.getInvitationByInvited(p2.getId());
    expire(inv);

    assertNull(s.acceptInvitation(p2.getId()));
  }

  @Test
  @DisplayName("chooseMode edge cases")
  void choose_mode_edge() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    assertNull(s.chooseMode(p1.getId(), null));
    assertNull(s.chooseMode(p1.getId(), GameMode.NORMAL));

    s.createInvitation(p1.getId(), p2.getId());
    s.acceptInvitation(p2.getId());

    assertNull(s.chooseMode(p2.getId(), GameMode.NORMAL)); // pas host
  }

  @Test
  @DisplayName("removePlayer with lobby")
  void remove_player_lobby() {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    s.createInvitation(p1.getId(), p2.getId());
    s.acceptInvitation(p2.getId());

    s.removePlayer(p1);

    assertEquals(PlayerStatus.IDLE, p1.getStatus());
    assertEquals(PlayerStatus.IDLE, p2.getStatus());
  }

  @Test
  @DisplayName("cleanExpiredInvitations")
  void clean_expired() throws Exception {
    AgonServer s = new AgonServer(12345, "test");
    OnlinePlayer p1 = s.registerPlayer("c1", "A", null);
    OnlinePlayer p2 = s.registerPlayer("c2", "B", null);

    s.createInvitation(p1.getId(), p2.getId());
    Invitation inv = s.getInvitationByInviter(p1.getId());
    expire(inv);

    invoke(s, "cleanExpiredInvitations");

    assertNull(s.getInvitationByInviter(p1.getId()));
    assertEquals(PlayerStatus.IDLE, p1.getStatus());
  }
}
