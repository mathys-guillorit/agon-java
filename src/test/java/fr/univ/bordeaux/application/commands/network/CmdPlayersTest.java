package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdPlayersTest {

  @Test
  @DisplayName("players refuses when client is not connected")
  void playersRefusesWhenClientIsNotConnected() {
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
  void playersShowsResponseWhenRequestSucceeds() {
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
  void playersShowsErrorWhenRequestFails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playersResponse = null;

    boolean result = new CmdPlayers(ui, ctx, new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to retrieve players"));
  }

  @Test
  @DisplayName("players with id shows detailed player response")
  void playersWithIdShowsDetailedPlayerResponse() {
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
  @DisplayName("players with invalid id shows both validation and request errors")
  void playersWithInvalidIdShowsError() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;

    boolean result = new CmdPlayers(ui, ctx, new String[] {"abc"}).execute(null);

    assertFalse(result);
    assertEquals(2, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Invalid player id"));
    assertTrue(ui.errors.get(1).contains("Failed to retrieve players"));
  }

  @Test
  @DisplayName("players with id shows error when request fails")
  void playersWithIdShowsErrorWhenRequestFails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playerDetailsResponse = null;

    boolean result = new CmdPlayers(ui, ctx, new String[] {"2"}).execute(null);

    assertFalse(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to retrieve players"));
  }

  @Test
  @DisplayName("players with null args shows players list")
  void playersWithNullArgsShowsPlayersList() {
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

  @Test
  @DisplayName("players with negative id requests player details")
  void playersWithNegativeIdRequestsPlayerDetails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.playerDetailsResponse = "PLAYER ID=-1 NAME=Ghost";

    boolean result = new CmdPlayers(ui, ctx, new String[] {"-1"}).execute(null);

    assertTrue(result);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("PLAYER ID=-1"));
  }

  @Test
  @DisplayName("players with blank id shows validation and request errors")
  void playersWithBlankIdShowsErrors() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;

    boolean result = new CmdPlayers(ui, ctx, new String[] {"   "}).execute(null);

    assertFalse(result);
    assertEquals(2, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Invalid player id"));
    assertTrue(ui.errors.get(1).contains("Failed to retrieve players"));
  }
}
