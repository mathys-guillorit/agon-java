package fr.univ.bordeaux.application.network.server.player;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerRegistryTest {

  @Test
  @DisplayName("constructeur + état initial")
  void constructorAndInitialStateTest() {
    PlayerRegistry registry = new PlayerRegistry();

    assertNotNull(registry);
    assertEquals(0, registry.getPlayerCount());
    assertTrue(registry.getActivePlayers().isEmpty());
    assertNull(registry.getPlayerById(1));
  }

  @Test
  @DisplayName("registerPlayer : succès avec trim du nom")
  void registerPlayerSuccessTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer player = registry.registerPlayer("client-1", "  Alice  ", null);

    assertNotNull(player);
    assertEquals(1, player.getId());
    assertEquals("client-1", player.getClientId());
    assertEquals("Alice", player.getName());
    assertEquals(PlayerStatus.IDLE, player.getStatus());
    assertNull(player.getHandler());

    assertEquals(1, registry.getPlayerCount());
    assertEquals(player, registry.getPlayerById(player.getId()));
    assertTrue(registry.getActivePlayers().contains(player));
  }

  @Test
  @DisplayName("registerPlayer : clientId null")
  void registerPlayerClientIdNullTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer player = registry.registerPlayer(null, "Alice", null);

    assertNull(player);
    assertEquals(0, registry.getPlayerCount());
  }

  @Test
  @DisplayName("registerPlayer : clientId vide")
  void registerPlayerClientIdBlankTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer player = registry.registerPlayer("   ", "Alice", null);

    assertNull(player);
    assertEquals(0, registry.getPlayerCount());
  }

  @Test
  @DisplayName("registerPlayer : nom null")
  void registerPlayerNameNullTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer player = registry.registerPlayer("client-1", null, null);

    assertNull(player);
    assertEquals(0, registry.getPlayerCount());
  }

  @Test
  @DisplayName("registerPlayer : nom vide")
  void registerPlayerNameBlankTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer player = registry.registerPlayer("client-1", "   ", null);

    assertNull(player);
    assertEquals(0, registry.getPlayerCount());
  }

  @Test
  @DisplayName("registerPlayer : reconnexion d'un joueur existant")
  void registerPlayerReconnectTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer first = registry.registerPlayer("client-1", "Alice", null);
    assertNotNull(first);

    first.setStatus(PlayerStatus.AWAY);
    registry.detachPlayer(first);

    assertEquals(0, registry.getPlayerCount());
    assertNull(registry.getPlayerById(first.getId()));

    OnlinePlayer reconnected = registry.registerPlayer("client-1", "AliceChanged", null);

    assertNotNull(reconnected);
    assertSame(first, reconnected);
    assertEquals(first.getId(), reconnected.getId());
    assertEquals("Alice", reconnected.getName());
    assertEquals("client-1", reconnected.getClientId());
    assertEquals(PlayerStatus.IDLE, reconnected.getStatus());

    assertEquals(1, registry.getPlayerCount());
    assertEquals(reconnected, registry.getPlayerById(reconnected.getId()));
  }

  @Test
  @DisplayName("registerPlayer : trim du clientId pour reconnexion")
  void registerPlayerReconnectWithTrimmedClientIdTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer first = registry.registerPlayer("client-1", "Alice", null);
    assertNotNull(first);

    registry.detachPlayer(first);

    OnlinePlayer reconnected = registry.registerPlayer("  client-1  ", "AutreNom", null);

    assertSame(first, reconnected);
    assertEquals(first.getId(), reconnected.getId());
    assertEquals("Alice", reconnected.getName());
    assertEquals(PlayerStatus.IDLE, reconnected.getStatus());
    assertEquals(1, registry.getPlayerCount());
  }

  @Test
  @DisplayName("detachPlayer : null")
  void detachPlayerNullTest() {
    PlayerRegistry registry = new PlayerRegistry();

    assertDoesNotThrow(() -> registry.detachPlayer(null));
    assertEquals(0, registry.getPlayerCount());
  }

  @Test
  @DisplayName("detachPlayer : retire le joueur actif et remet IDLE")
  void detachPlayerSuccessTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer player = registry.registerPlayer("client-1", "Alice", null);
    assertNotNull(player);

    player.setStatus(PlayerStatus.WAITGAME);

    registry.detachPlayer(player);

    assertNull(player.getHandler());
    assertEquals(PlayerStatus.IDLE, player.getStatus());
    assertNull(registry.getPlayerById(player.getId()));
    assertEquals(0, registry.getPlayerCount());
    assertFalse(registry.getActivePlayers().contains(player));
  }

  @Test
  @DisplayName("clearActivePlayers : vide les joueurs actifs")
  void clearActivePlayersTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer p1 = registry.registerPlayer("client-1", "Alice", null);
    OnlinePlayer p2 = registry.registerPlayer("client-2", "Bob", null);

    assertNotNull(p1);
    assertNotNull(p2);
    assertEquals(2, registry.getPlayerCount());

    registry.clearActivePlayers();

    assertEquals(0, registry.getPlayerCount());
    assertTrue(registry.getActivePlayers().isEmpty());
    assertNull(registry.getPlayerById(p1.getId()));
    assertNull(registry.getPlayerById(p2.getId()));
  }

  @Test
  @DisplayName("ids générés sont uniques")
  void generatedIdsAreUniqueTest() {
    PlayerRegistry registry = new PlayerRegistry();

    OnlinePlayer p1 = registry.registerPlayer("client-1", "Alice", null);
    OnlinePlayer p2 = registry.registerPlayer("client-2", "Bob", null);

    assertNotNull(p1);
    assertNotNull(p2);
    assertNotEquals(p1.getId(), p2.getId());
    assertEquals(1, p1.getId());
    assertEquals(2, p2.getId());
  }
}
