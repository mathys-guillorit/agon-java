package fr.univ.bordeaux.application.network.player;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatusTest {

  @Test
  @DisplayName("Enum contains IDLE and INGAME")
  void enum_values_test() {
    assertNotNull(PlayerStatus.IDLE);
    assertNotNull(PlayerStatus.INGAME);
  }

  @Test
  @DisplayName("Enum valueOf works correctly")
  void enum_valueof_test() {
    assertEquals(PlayerStatus.IDLE, PlayerStatus.valueOf("IDLE"));
    assertEquals(PlayerStatus.INGAME, PlayerStatus.valueOf("INGAME"));
  }

  @Test
  @DisplayName("Enum toString returns correct names")
  void enum_to_string_test() {
    assertEquals("IDLE", PlayerStatus.IDLE.toString());
    assertEquals("INGAME", PlayerStatus.INGAME.toString());
  }
}
