package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdJoinTest {

  @Test
  @DisplayName("join without args uses default host and port when args is null")
  void join_without_args_uses_default_host_and_port_when_args_is_null() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = false;
    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(null).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to localhost:12345"));
  }

  @Test
  @DisplayName("join without args uses default host and port when args array is empty")
  void join_without_args_uses_default_host_and_port_when_args_array_is_empty() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(12345, client.lastPort);
  }

  @Test
  @DisplayName("join without args uses default host and port when first argument is blank")
  void join_without_args_uses_default_host_and_port_when_first_argument_is_blank() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"   "}).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(12345, client.lastPort);
  }

  @Test
  @DisplayName("join with host only uses default port")
  void join_with_host_only_uses_default_port() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"192.168.1.10"}).execute(null);

    assertTrue(result);
    assertEquals("192.168.1.10", client.lastHost);
    assertEquals(12345, client.lastPort);
  }

  @Test
  @DisplayName("join with host and port uses provided values")
  void join_with_host_and_port_uses_provided_values() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"10.0.0.5:5555"}).execute(null);

    assertTrue(result);
    assertEquals("10.0.0.5", client.lastHost);
    assertEquals(5555, client.lastPort);
  }

  @Test
  @DisplayName("join with missing host keeps default host and uses provided port")
  void join_with_missing_host_keeps_default_host_and_uses_provided_port() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {":5555"}).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(5555, client.lastPort);
  }

  @Test
  @DisplayName("join with missing port keeps default port and shows warning")
  void join_with_missing_port_keeps_default_port_and_shows_warning() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:"}).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Invalid port"));
  }

  @Test
  @DisplayName("join with invalid port keeps default port and shows warning")
  void join_with_invalid_port_keeps_default_port_and_shows_warning() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:abc"}).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Invalid port"));
  }

  @Test
  @DisplayName("join trims host and port before connecting")
  void join_trims_host_and_port_before_connecting() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result =
        new CmdJoin(ui, ctx).createNew(new String[] {"  10.0.0.7 : 7777  "}).execute(null);

    assertTrue(result);
    assertEquals("10.0.0.7", client.lastHost);
    assertEquals(7777, client.lastPort);
  }

  @Test
  @DisplayName("join refuses when client is already connected and alive")
  void join_refuses_when_client_is_already_connected_and_alive() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.alive = true;

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:12345"}).execute(null);

    assertFalse(result);
    assertNull(client.lastHost);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Already connected"));
  }

  @Test
  @DisplayName("join disconnects dead connection before reconnecting")
  void join_disconnects_dead_connection_before_reconnecting() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.alive = false;
    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(null).execute(null);

    assertTrue(result);
    assertTrue(client.disconnectSilentlyCalled);
    assertEquals("localhost", client.lastHost);
    assertEquals(12345, client.lastPort);
  }

  @Test
  @DisplayName("join shows error when connection fails")
  void join_shows_error_when_connection_fails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = false;

    boolean result = new CmdJoin(ui, ctx).createNew(null).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Connection failed"));
  }
}
