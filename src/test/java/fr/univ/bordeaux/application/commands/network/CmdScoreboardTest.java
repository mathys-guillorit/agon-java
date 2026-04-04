package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdScoreboardTest {

  @Test
  @DisplayName("scoreboard refuses when client is not connected")
  void scoreboard_refuses_when_client_is_not_connected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdScoreboard(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("scoreboard shows response when request succeeds")
  void scoreboard_shows_response_when_request_succeeds() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.scoreboardResponse = "Alice 3W 1L";

    boolean result = new CmdScoreboard(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Alice 3W 1L"));
  }

  @Test
  @DisplayName("scoreboard shows error when request fails")
  void scoreboard_shows_error_when_request_fails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.scoreboardResponse = null;

    boolean result = new CmdScoreboard(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to retrieve scoreboard"));
  }
}
