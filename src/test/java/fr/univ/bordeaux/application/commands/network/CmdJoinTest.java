package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdJoinTest {

  @Test
  @DisplayName("join without args uses default host and port when args is null")
  void joinWithoutArgsUsesDefaultHostAndPortWhenArgsIsNull() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = false;
    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(null).execute(null);

    assertTrue(result);
    assertEquals("127.0.0.1", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to 127.0.0.1:12345"));
  }

  @Test
  @DisplayName("join without args uses default host and port when args array is empty")
  void joinWithoutArgsUsesDefaultHostAndPortWhenArgsArrayIsEmpty() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals("127.0.0.1", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to 127.0.0.1:12345"));
  }

  @Test
  @DisplayName("join without args uses default host and port when first argument is blank")
  void joinWithoutArgsUsesDefaultHostAndPortWhenFirstArgumentIsBlank() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"   "}).execute(null);

    assertTrue(result);
    assertEquals("127.0.0.1", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to 127.0.0.1:12345"));
  }

  @Test
  @DisplayName("join with host only shows usage error")
  void joinWithHostOnlyUsesDefaultPort() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"192.168.1.10"}).execute(null);

    assertFalse(result);
    assertNull(client.lastHost);
    assertEquals(0, ui.messages.size());
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Usage: join IP:PORT"));
  }

  @Test
  @DisplayName("join with host and port uses provided values")
  void joinWithHostAndPortUsesProvidedValues() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"10.0.0.5:5555"}).execute(null);

    assertTrue(result);
    assertEquals("10.0.0.5", client.lastHost);
    assertEquals(5555, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to 10.0.0.5:5555"));
  }

  @Test
  @DisplayName("join with missing host shows invalid host error")
  void joinWithMissingHostKeepsDefaultHostAndUsesProvidedPort() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {":5555"}).execute(null);

    assertFalse(result);
    assertNull(client.lastHost);
    assertEquals(1, ui.errors.size());
  }

  @Test
  @DisplayName("join with missing port shows usage error")
  void joinWithMissingPortKeepsDefaultPortAndShowsWarning() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:"}).execute(null);

    assertFalse(result);
    assertNull(client.lastHost);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Usage: join IP:PORT"));
  }

  @Test
  @DisplayName("join with invalid port shows error")
  void joinWithInvalidPortKeepsDefaultPortAndShowsWarning() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:abc"}).execute(null);

    assertFalse(result);
    assertNull(client.lastHost);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Invalid port: abc"));
  }

  @Test
  @DisplayName("join trims host and port before connecting")
  void joinTrimsHostAndPortBeforeConnecting() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = true;

    boolean result =
        new CmdJoin(ui, ctx).createNew(new String[] {"  10.0.0.7 : 7777  "}).execute(null);

    assertTrue(result);
    assertEquals("10.0.0.7", client.lastHost);
    assertEquals(7777, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to 10.0.0.7:7777"));
  }

  @Test
  @DisplayName("join refuses when client is already connected and alive")
  void joinRefusesWhenClientIsAlreadyConnectedAndAlive() {
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
  void joinDisconnectsDeadConnectionBeforeReconnecting() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.alive = false;
    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(null).execute(null);

    assertTrue(result);
    assertTrue(client.disconnectSilentlyCalled);
    assertEquals("127.0.0.1", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to 127.0.0.1:12345"));
  }

  @Test
  @DisplayName("join shows error when connection fails")
  void joinShowsErrorWhenConnectionFails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = false;

    boolean result = new CmdJoin(ui, ctx).createNew(null).execute(null);

    assertFalse(result);
    assertEquals("127.0.0.1", client.lastHost);
    assertEquals(12345, client.lastPort);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Connection failed"));
  }

  @Test
  @DisplayName("join rejects port below 1024")
  void joinRejectsPortBelow1024() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:1023"}).execute(null);

    assertFalse(result);
    assertNull(client.lastHost);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Port must be between 1024 and 65535"));
  }

  @Test
  @DisplayName("join rejects port above 65535")
  void joinRejectsPortAbove65535() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:65536"}).execute(null);

    assertFalse(result);
    assertNull(client.lastHost);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Port must be between 1024 and 65535"));
  }

  @Test
  @DisplayName("join accepts boundary port 1024")
  void joinAcceptsBoundaryPort1024() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:1024"}).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(1024, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to localhost:1024"));
  }

  @Test
  @DisplayName("join accepts boundary port 65535")
  void joinAcceptsBoundaryPort65535() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connectResult = true;

    boolean result = new CmdJoin(ui, ctx).createNew(new String[] {"localhost:65535"}).execute(null);

    assertTrue(result);
    assertEquals("localhost", client.lastHost);
    assertEquals(65535, client.lastPort);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Connected to localhost:65535"));
  }
}
