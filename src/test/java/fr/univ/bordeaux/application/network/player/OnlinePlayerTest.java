package fr.univ.bordeaux.application.network.player;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.server.ClientHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OnlinePlayerTest {

  @Test
  @DisplayName("Constructor initializes all fields correctly")
  void constructorTest() {
    ClientHandler handler = null;

    OnlinePlayer player = new OnlinePlayer(1, "client123", "Alice", PlayerStatus.IDLE, handler);

    assertEquals(1, player.getId());
    assertEquals("client123", player.getClientId());
    assertEquals("Alice", player.getName());
    assertEquals(PlayerStatus.IDLE, player.getStatus());
    assertNull(player.getHandler());
  }

  @Test
  @DisplayName("Getters return correct values")
  void gettersTest() {
    OnlinePlayer player = new OnlinePlayer(2, "client456", "Bob", PlayerStatus.INGAME, null);

    assertEquals(2, player.getId());
    assertEquals("client456", player.getClientId());
    assertEquals("Bob", player.getName());
    assertEquals(PlayerStatus.INGAME, player.getStatus());
    assertNull(player.getHandler());
  }

  @Test
  @DisplayName("setStatus updates player status correctly")
  void setStatusTest() {
    OnlinePlayer player = new OnlinePlayer(3, "client789", "Charlie", PlayerStatus.IDLE, null);

    player.setStatus(PlayerStatus.INGAME);

    assertEquals(PlayerStatus.INGAME, player.getStatus());
  }

  @Test
  @DisplayName("setHandler updates handler correctly")
  void setHandlerTest() {
    OnlinePlayer player = new OnlinePlayer(4, "client000", "Dave", PlayerStatus.IDLE, null);

    ClientHandler handler = null; // on reste simple (pas besoin de mock ici)
    player.setHandler(handler);

    assertNull(player.getHandler());
  }

  @Test
  @DisplayName("Handler can be updated from null to non-null")
  void handlerUpdateTest() {
    OnlinePlayer player = new OnlinePlayer(5, "client999", "Eve", PlayerStatus.IDLE, null);

    ClientHandler fakeHandler = new ClientHandler(null, null);
    player.setHandler(fakeHandler);

    assertEquals(fakeHandler, player.getHandler());
  }

  @Test
  @DisplayName("isAvailable returns true only when status is IDLE")
  void isAvailableTest() {
    OnlinePlayer player = new OnlinePlayer(6, "client111", "Frank", PlayerStatus.IDLE, null);

    assertTrue(player.isAvailable());

    player.setStatus(PlayerStatus.INGAME);
    assertFalse(player.isAvailable());

    player.setStatus(PlayerStatus.AWAY);
    assertFalse(player.isAvailable());
  }

  @Test
  @DisplayName("isAway returns true only when status is AWAY")
  void isAwayTest() {
    OnlinePlayer player = new OnlinePlayer(7, "client222", "Grace", PlayerStatus.IDLE, null);

    assertFalse(player.isAway());

    player.setStatus(PlayerStatus.AWAY);
    assertTrue(player.isAway());

    player.setStatus(PlayerStatus.INGAME);
    assertFalse(player.isAway());
  }
}
