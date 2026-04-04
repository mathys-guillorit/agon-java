package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdPingTest {

  @Test
  @DisplayName("ping refuses when client is not connected")
  void ping_refuses_when_client_is_not_connected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = false;

    boolean result = new CmdPing(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("ping shows server response when connected")
  void ping_shows_server_response_when_connected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.pingResponse = "RTT = 12 ms";

    boolean result = new CmdPing(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("RTT = 12 ms"));
  }

  @Test
  @DisplayName("ping shows error when response is null")
  void ping_shows_error_when_response_is_null() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.pingResponse = null;

    boolean result = new CmdPing(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Connection lost"));
  }
}
