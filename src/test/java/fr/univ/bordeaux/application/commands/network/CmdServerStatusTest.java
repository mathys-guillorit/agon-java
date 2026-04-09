package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.network.server.AgonServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdServerStatusTest {

  private AgonServer server;

  @AfterEach
  void cleanup() {
    try {
      if (server != null) {
        server.stop();
      }
    } catch (Exception ignored) {
    }
    server = null;
  }

  @Test
  @DisplayName("server_status shows local server status when local server is running")
  void serverStatusShowsLocalServerStatusWhenLocalServerIsRunning() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    server = new AgonServer(freePort(), "TestServer");
    assertTrue(server.start());
    ctx.setServer(server);

    boolean result = new CmdServerStatus(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertFalse(ui.messages.isEmpty());
    assertTrue(ui.messages.get(0).contains("SERVER STATUS"));
  }

  @Test
  @DisplayName("server_status shows remote server status when client is connected")
  void serverStatusShowsRemoteServerStatusWhenClientIsConnected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.serverStatusResponse = "Remote server OK";

    boolean result = new CmdServerStatus(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Remote server OK"));
  }

  @Test
  @DisplayName("server_status shows error when remote server request fails")
  void serverStatusShowsErrorWhenRemoteServerRequestFails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = true;
    client.serverStatusResponse = null;

    boolean result = new CmdServerStatus(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to retrieve server status"));
  }

  @Test
  @DisplayName("server_status shows warning when neither local server nor remote connection exists")
  void serverStatusShowsWarningWhenNeitherLocalServerNorRemoteConnectionExists() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    client.connected = false;

    boolean result = new CmdServerStatus(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("No server is currently running"));
  }
}
