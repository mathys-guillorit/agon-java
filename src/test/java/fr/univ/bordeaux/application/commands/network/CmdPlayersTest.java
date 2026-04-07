package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdPlayersTest {

  @Test
  @DisplayName("players refuses when client is not connected")
  void players_refuses_when_client_is_not_connected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdPlayers(ui, ctx, new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("players shows response when request succeeds")
  void players_shows_response_when_request_succeeds() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playersResponse = "1 - Alice\n2 - Bob";

    boolean result = new CmdPlayers(ui, ctx, new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Alice"));
    assertTrue(ui.messages.get(0).contains("Bob"));
  }

  @Test
  @DisplayName("players shows error when request fails")
  void players_shows_error_when_request_fails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playersResponse = null;

    boolean result = new CmdPlayers(ui, ctx, new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to retrieve players"));
  }

  @Test
  @DisplayName("players with id shows detailed player response")
  void players_with_id_shows_detailed_player_response() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playerDetailsResponse =
        "PLAYER ID=2 NAME=Bob CLIENT_ID=abc STATUS=away WINS=3 LOSSES=1 GAMES=4";

    boolean result = new CmdPlayers(ui, ctx, new String[] {"2"}).execute(null);

    assertTrue(result);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("PLAYER ID=2"));
    assertTrue(ui.messages.get(0).contains("NAME=Bob"));
    assertTrue(ui.messages.get(0).contains("STATUS=away"));
    assertTrue(ui.messages.get(0).contains("WINS=3"));
    assertTrue(ui.messages.get(0).contains("LOSSES=1"));
    assertTrue(ui.messages.get(0).contains("GAMES=4"));
  }

  @Test
  @DisplayName("players with invalid id shows error")
  void players_with_invalid_id_shows_error() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;

    boolean result = new CmdPlayers(ui, ctx, new String[] {"abc"}).execute(null);

    assertFalse(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Invalid player id"));
  }

  @Test
  @DisplayName("players with id shows error when request fails")
  void players_with_id_shows_error_when_request_fails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playerDetailsResponse = null;

    boolean result = new CmdPlayers(ui, ctx, new String[] {"2"}).execute(null);

    assertTrue(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to retrieve players"));
  }

  @Test
  @DisplayName("players with null args shows players list")
  void players_with_null_args_shows_players_list() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playersResponse = "1 - Alice\n2 - Bob";

    boolean result = new CmdPlayers(ui, ctx, null).execute(null);

    assertTrue(result);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Alice"));
    assertTrue(ui.messages.get(0).contains("Bob"));
  }
}
