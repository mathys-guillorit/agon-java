package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdAcceptTest {

  @Test
  @DisplayName("accept refuses when client is not connected")
  void accept_refuses_when_client_is_not_connected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdAccept(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("accept sends request when connected")
  void accept_sends_request_when_connected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.acceptInvitationResult = true;

    boolean result = new CmdAccept(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertTrue(client.acceptInvitationCalled);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Accept request sent"));
  }

  @Test
  @DisplayName("accept shows error when request fails")
  void accept_shows_error_when_request_fails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.acceptInvitationResult = false;

    boolean result = new CmdAccept(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertTrue(client.acceptInvitationCalled);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to send accept request"));
  }
}
