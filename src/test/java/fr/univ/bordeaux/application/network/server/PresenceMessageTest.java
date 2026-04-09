package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PresenceMessageTest {

  @Test
  @DisplayName("Constructor and getters store values correctly")
  void constructorAndGetters() {
    PresenceMessage pm = new PresenceMessage("S", 12345);

    assertEquals("S", pm.getServerName());
    assertEquals(12345, pm.getTcpPort());
  }

  @Test
  @DisplayName("ToBytes encodes expected message format")
  void toBytes() {
    PresenceMessage pm = new PresenceMessage("S", 12345);

    byte[] data = pm.toBytes();
    String text = new String(data, StandardCharsets.US_ASCII);

    assertEquals("name=S;tcp=12345", text);
  }

  @Test
  @DisplayName("Parse decodes valid presence message")
  void presenceEncodeParseOk() {
    PresenceMessage pm = new PresenceMessage("S", 12345);
    byte[] data = pm.toBytes();

    PresenceMessage parsed = PresenceMessage.parse(data, data.length);

    assertNotNull(parsed);
    assertEquals("S", parsed.getServerName());
    assertEquals(12345, parsed.getTcpPort());
  }

  @Test
  @DisplayName("Parse returns null when tcp field is missing")
  void parseMissingTcpReturnsNull() {
    byte[] bad = "name=S".getBytes(StandardCharsets.US_ASCII);

    assertNull(PresenceMessage.parse(bad, bad.length));
  }

  @Test
  @DisplayName("Parse returns null when name field is missing")
  void parseMissingNameReturnsNull() {
    byte[] bad = "tcp=12345".getBytes(StandardCharsets.US_ASCII);

    assertNull(PresenceMessage.parse(bad, bad.length));
  }

  @Test
  @DisplayName("Parse supports reversed field order")
  void parseReversedOrder() {
    byte[] data = "tcp=12345;name=S".getBytes(StandardCharsets.US_ASCII);

    PresenceMessage parsed = PresenceMessage.parse(data, data.length);

    assertNotNull(parsed);
    assertEquals("S", parsed.getServerName());
    assertEquals(12345, parsed.getTcpPort());
  }

  @Test
  @DisplayName("Parse throws NumberFormatException when tcp value is invalid")
  void parseInvalidTcpValue() {
    byte[] bad = "name=S;tcp=abc".getBytes(StandardCharsets.US_ASCII);

    assertThrows(NumberFormatException.class, () -> PresenceMessage.parse(bad, bad.length));
  }
}
