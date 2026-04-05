package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.network.server.AgonServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdServerStopTest {

  @Test
  @DisplayName("server_stop stops running server and clears context")
  void server_stop_stops_running_server_and_clears_context() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    AgonServer server = new AgonServer(freePort(), "TestServer");
    assertTrue(server.start());
    ctx.setServer(server);

    boolean result = new CmdServerStop(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertNull(ctx.getServer());
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Server stopped successfully"));
  }

  @Test
  @DisplayName("server_stop refuses when no server exists")
  void server_stop_refuses_when_no_server_exists() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStop(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("No server is currently running"));
  }

  @Test
  @DisplayName("server_stop refuses when server exists but is not running")
  void server_stop_refuses_when_server_exists_but_is_not_running() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    AgonServer server = new AgonServer(freePort(), "TestServer");
    ctx.setServer(server);

    boolean result = new CmdServerStop(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("No server is currently running"));
  }
}
