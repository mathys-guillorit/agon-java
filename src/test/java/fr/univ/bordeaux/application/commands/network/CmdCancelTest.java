package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdCancelTest {

  @Test
  @DisplayName("cancel refuses when client is not connected")
  void cancelRefusesWhenClientIsNotConnected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdCancel(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("cancel sends request when connected")
  void cancelSendsRequestWhenConnected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.cancelInvitationResult = true;

    boolean result = new CmdCancel(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertTrue(client.cancelInvitationCalled);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Cancel request sent"));
  }

  @Test
  @DisplayName("cancel shows error when request fails")
  void cancelShowsErrorWhenRequestFails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.cancelInvitationResult = false;

    boolean result = new CmdCancel(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertTrue(client.cancelInvitationCalled);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to send cancel request"));
  }
}
