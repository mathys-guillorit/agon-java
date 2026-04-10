package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdDeclineTest {

  @Test
  @DisplayName("decline refuses when client is not connected")
  void declineRefusesWhenClientIsNotConnected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdDecline(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("decline sends request when connected")
  void declineSendsRequestWhenConnected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.declineInvitationResult = true;

    boolean result = new CmdDecline(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertTrue(client.declineInvitationCalled);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Decline request sent"));
  }

  @Test
  @DisplayName("decline shows error when request fails")
  void declineShowsErrorWhenRequestFails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.declineInvitationResult = false;

    boolean result = new CmdDecline(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertTrue(client.declineInvitationCalled);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to send decline request"));
  }
}
