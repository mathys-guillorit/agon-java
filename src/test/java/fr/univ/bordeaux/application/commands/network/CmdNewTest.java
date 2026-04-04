package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.AppMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdNewTest {

  @Test
  @DisplayName("new in online mode refuses when client is not connected")
  void new_in_online_mode_refuses_when_client_is_not_connected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    ctx.setMode(AppMode.ONLINE);

    boolean result = new CmdNew(ui, ctx, null, null).createNew(new String[] {"7"}).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("new in online mode refuses when player id is missing")
  void new_in_online_mode_refuses_when_player_id_is_missing() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    ctx.setMode(AppMode.ONLINE);

    client.connected = true;

    boolean result = new CmdNew(ui, ctx, null, null).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Usage: new PLAYER_ID"));
  }

  @Test
  @DisplayName("new in online mode sends request when player id is valid")
  void new_in_online_mode_sends_request_when_player_id_is_valid() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    ctx.setMode(AppMode.ONLINE);

    client.connected = true;
    client.newGameResponse = "Game request sent";

    boolean result = new CmdNew(ui, ctx, null, null).createNew(new String[] {"7"}).execute(null);

    assertTrue(result);
    assertEquals(7, client.requestedNewGamePlayerId);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Game request sent"));
  }

  @Test
  @DisplayName("new in online mode shows error when server rejects request")
  void new_in_online_mode_shows_error_when_server_rejects_request() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    ctx.setMode(AppMode.ONLINE);

    client.connected = true;
    client.newGameResponse = null;

    boolean result = new CmdNew(ui, ctx, null, null).createNew(new String[] {"7"}).execute(null);

    assertFalse(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to start online game"));
  }

  @Test
  @DisplayName("new in online mode with invalid player id exposes current args index bug")
  void new_in_online_mode_with_invalid_player_id_exposes_current_args_index_bug() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    ctx.setMode(AppMode.ONLINE);

    client.connected = true;

    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> new CmdNew(ui, ctx, null, null).createNew(new String[] {"abc"}).execute(null));
  }
}
