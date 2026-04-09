package fr.univ.bordeaux.application.network.server.invitation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.application.network.server.player.PlayerService;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvitationManagerTest {

  private static class FakePlayerService implements PlayerService {
    private final Map<Integer, OnlinePlayer> players = new ConcurrentHashMap<>();

    void addPlayer(OnlinePlayer player) {
      players.put(player.getId(), player);
    }

    void removePlayer(int playerId) {
      players.remove(playerId);
    }

    @Override
    public OnlinePlayer registerPlayer(String clientId, String name, ClientHandler handler) {
      return null;
    }

    @Override
    public void detachPlayer(OnlinePlayer player) {}

    @Override
    public OnlinePlayer getPlayerById(int playerId) {
      return players.get(playerId);
    }

    @Override
    public Collection<OnlinePlayer> getActivePlayers() {
      return List.of();
    }

    @Override
    public int getPlayerCount() {
      return 0;
    }

    @Override
    public void clearActivePlayers() {}
  }

  private OnlinePlayer newPlayer(int id, String name, PlayerStatus status) {
    OnlinePlayer player = new OnlinePlayer(id, "client-" + id, name, PlayerStatus.IDLE, null);
    player.setStatus(status);
    return player;
  }

  private void expire(Invitation invitation) throws Exception {
    Field f = Invitation.class.getDeclaredField("expiresAt");
    f.setAccessible(true);
    f.setLong(invitation, System.currentTimeMillis() - 1);
  }

  @Test
  @DisplayName("constructeur + getters invitation null")
  void constructorAndEmptyLookupsTest() {
    FakePlayerService playerService = new FakePlayerService();
    InvitationManager manager = new InvitationManager(playerService);

    assertNotNull(manager);
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
  }

  @Test
  @DisplayName("createInvitation : succès")
  void createInvitationSuccessTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);

    assertTrue(manager.createInvitation(1, 2));

    Invitation byInviter = manager.getInvitationByInviter(1);
    Invitation byInvited = manager.getInvitationByInvited(2);

    assertNotNull(byInviter);
    assertNotNull(byInvited);
    assertEquals(byInviter, byInvited);
    assertEquals(PlayerStatus.WAITGAME, inviter.getStatus());
    assertEquals(PlayerStatus.WAITGAME, invited.getStatus());
  }

  @Test
  @DisplayName("createInvitation : inviter introuvable")
  void createInvitationMissingInviterTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);

    assertFalse(manager.createInvitation(1, 2));
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
  }

  @Test
  @DisplayName("createInvitation : invited introuvable")
  void createInvitationMissingInvitedTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    playerService.addPlayer(inviter);

    InvitationManager manager = new InvitationManager(playerService);

    assertFalse(manager.createInvitation(1, 2));
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
  }

  @Test
  @DisplayName("createInvitation : même joueur")
  void createInvitationSamePlayerTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    playerService.addPlayer(inviter);

    InvitationManager manager = new InvitationManager(playerService);

    assertFalse(manager.createInvitation(1, 1));
    assertNull(manager.getInvitationByInviter(1));
  }

  @Test
  @DisplayName("createInvitation : inviter pas IDLE")
  void createInvitationInviterNotIdleTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.AWAY);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);

    assertFalse(manager.createInvitation(1, 2));
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
  }

  @Test
  @DisplayName("createInvitation : invited pas IDLE")
  void createInvitationInvitedNotIdleTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.WAITGAME);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);

    assertFalse(manager.createInvitation(1, 2));
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
  }

  @Test
  @DisplayName("removeInvitation : par inviter")
  void removeInvitationByInviterTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    Invitation removed = manager.removeInvitation(1);

    assertNotNull(removed);
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.IDLE, inviter.getStatus());
    assertEquals(PlayerStatus.IDLE, invited.getStatus());
  }

  @Test
  @DisplayName("removeInvitation : par invited")
  void removeInvitationByInvitedTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    Invitation removed = manager.removeInvitation(2);

    assertNotNull(removed);
    assertEquals(1, removed.getInviterId());
    assertEquals(2, removed.getInvitedId());
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.IDLE, inviter.getStatus());
    assertEquals(PlayerStatus.IDLE, invited.getStatus());
  }

  @Test
  @DisplayName("removeInvitation : aucune invitation")
  void removeInvitationMissingTest() {
    FakePlayerService playerService = new FakePlayerService();
    InvitationManager manager = new InvitationManager(playerService);

    assertNull(manager.removeInvitation(99));
  }

  @Test
  @DisplayName("acceptInvitation : aucune invitation")
  void acceptInvitationMissingTest() {
    FakePlayerService playerService = new FakePlayerService();
    InvitationManager manager = new InvitationManager(playerService);

    assertNull(manager.acceptInvitation(2));
  }

  @Test
  @DisplayName("acceptInvitation : succès")
  void acceptInvitationSuccessTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    Invitation accepted = manager.acceptInvitation(2);

    assertNotNull(accepted);
    assertEquals(1, accepted.getInviterId());
    assertEquals(2, accepted.getInvitedId());
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.WAITGAME, inviter.getStatus());
    assertEquals(PlayerStatus.WAITGAME, invited.getStatus());
  }

  @Test
  @DisplayName("acceptInvitation : invitation expirée")
  void acceptInvitationExpiredTest() throws Exception {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    Invitation invitation = manager.getInvitationByInviter(1);
    assertNotNull(invitation);
    expire(invitation);

    Invitation accepted = manager.acceptInvitation(2);

    assertNull(accepted);
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.IDLE, inviter.getStatus());
    assertEquals(PlayerStatus.IDLE, invited.getStatus());
  }

  @Test
  @DisplayName("acceptInvitation : inviter supprimé")
  void acceptInvitationMissingInviterTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    playerService.removePlayer(1);

    Invitation accepted = manager.acceptInvitation(2);

    assertNull(accepted);
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.IDLE, invited.getStatus());
  }

  @Test
  @DisplayName("acceptInvitation : invited supprimé")
  void acceptInvitationMissingInvitedTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    playerService.removePlayer(2);

    Invitation accepted = manager.acceptInvitation(2);

    assertNull(accepted);
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.IDLE, inviter.getStatus());
  }

  @Test
  @DisplayName("clearExpiredInvitations : aucune invitation expirée")
  void clearExpiredInvitationsNoneExpiredTest() {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    manager.clearExpiredInvitations();

    assertNotNull(manager.getInvitationByInviter(1));
    assertNotNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.WAITGAME, inviter.getStatus());
    assertEquals(PlayerStatus.WAITGAME, invited.getStatus());
  }

  @Test
  @DisplayName("clearExpiredInvitations : une invitation expirée")
  void clearExpiredInvitationsExpiredTest() throws Exception {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    Invitation invitation = manager.getInvitationByInviter(1);
    assertNotNull(invitation);
    expire(invitation);

    manager.clearExpiredInvitations();

    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
    assertEquals(PlayerStatus.IDLE, inviter.getStatus());
    assertEquals(PlayerStatus.IDLE, invited.getStatus());
  }

  @Test
  @DisplayName("clearExpiredInvitations : resetWaitingPlayer ignore null")
  void clearExpiredInvitationsWithMissingPlayersTest() throws Exception {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    Invitation invitation = manager.getInvitationByInviter(1);
    assertNotNull(invitation);
    expire(invitation);

    playerService.removePlayer(1);
    playerService.removePlayer(2);

    assertDoesNotThrow(manager::clearExpiredInvitations);
    assertNull(manager.getInvitationByInviter(1));
    assertNull(manager.getInvitationByInvited(2));
  }

  @Test
  @DisplayName("removeInvitation : clearInvitation ignore invitation nulle")
  void removeInvitationStillNullWhenNothingExistsTest() {
    FakePlayerService playerService = new FakePlayerService();
    InvitationManager manager = new InvitationManager(playerService);

    assertDoesNotThrow(() -> manager.removeInvitation(1234));
    assertNull(manager.removeInvitation(1234));
  }

  @Test
  @DisplayName("clearExpiredInvitations : joueur non WAITGAME non modifié")
  void clearExpiredInvitationsResetWaitingPlayerBranchTest() throws Exception {
    FakePlayerService playerService = new FakePlayerService();
    OnlinePlayer inviter = newPlayer(1, "Alice", PlayerStatus.IDLE);
    OnlinePlayer invited = newPlayer(2, "Bob", PlayerStatus.IDLE);

    playerService.addPlayer(inviter);
    playerService.addPlayer(invited);

    InvitationManager manager = new InvitationManager(playerService);
    assertTrue(manager.createInvitation(1, 2));

    // On change artificiellement un statut pour couvrir le faux de isWaitingGame/resetWaitingPlayer
    invited.setStatus(PlayerStatus.AWAY);

    Invitation invitation = manager.getInvitationByInviter(1);
    assertNotNull(invitation);
    expire(invitation);

    manager.clearExpiredInvitations();

    assertEquals(PlayerStatus.IDLE, inviter.getStatus());
    assertEquals(PlayerStatus.AWAY, invited.getStatus());
  }
}
