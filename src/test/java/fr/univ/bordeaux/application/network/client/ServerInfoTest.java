package fr.univ.bordeaux.application.network.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerInfoTest {

  @Test
  @DisplayName("Server key is correctly formatted")
  void keyTest() {
    ServerInfo s = new ServerInfo("S", "127.0.0.1", 1234);

    String key = s.key();

    assertTrue(key.contains("S"));
    assertTrue(key.contains("127.0.0.1"));
    assertTrue(key.contains("1234"));
  }
}
