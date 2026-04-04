package fr.univ.bordeaux.application.network.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocalProfileTest {

  @Test
  @DisplayName("Profile name getter/setter works")
  void name_test() {
    LocalProfile p = new LocalProfile("Alice");

    assertEquals("Alice", p.getName());

    p.setName("Bob");
    assertEquals("Bob", p.getName());
  }

  @Test
  @DisplayName("ClientId is unique and not null")
  void client_id_test() {
    LocalProfile p = new LocalProfile("Alice");

    assertNotNull(p.getClientId());
    assertFalse(p.getClientId().isBlank());
  }

  @Test
  @DisplayName("Server ID mapping works correctly")
  void server_id_mapping() {
    LocalProfile p = new LocalProfile("Alice");

    p.setIdForServer("srv", 42);

    assertEquals(42, p.getIdForServer("srv"));
    assertTrue(p.getServerIds().containsKey("srv"));
  }
}
