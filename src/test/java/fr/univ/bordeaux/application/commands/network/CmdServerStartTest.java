package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.network.server.AgonServer;
import java.net.ServerSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdServerStartTest {

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
  @DisplayName("server_start starts server on valid port")
  void server_start_starts_server_on_valid_port() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();
    int port = freePort();

    boolean result =
        new CmdServerStart(ui, ctx).createNew(new String[] {String.valueOf(port)}).execute(null);

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Server started successfully"));
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start refuses invalid port")
  void server_start_refuses_invalid_port() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[] {"abc"}).execute(null);

    assertFalse(result);
    assertNull(ctx.getServer());
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Invalid port"));
  }

  @Test
  @DisplayName("server_start uses default port when args is null")
  void server_start_uses_default_port_when_args_is_null() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(null).execute(null);

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(12345, ctx.getServer().getPort());
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start uses default port when args array is empty")
  void server_start_uses_default_port_when_args_array_is_empty() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(12345, ctx.getServer().getPort());
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start uses default port when first argument is blank")
  void server_start_uses_default_port_when_first_argument_is_blank() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[] {""}).execute(null);

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(12345, ctx.getServer().getPort());
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start refuses when a server is already running")
  void server_start_refuses_when_a_server_is_already_running() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    server = new AgonServer(freePort(), "TestServer");
    assertTrue(server.start());
    ctx.setServer(server);

    boolean result =
        new CmdServerStart(ui, ctx)
            .createNew(new String[] {String.valueOf(freePort())})
            .execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("already running"));
  }

  @Test
  @DisplayName("server_start fails when port is already used")
  void server_start_fails_when_port_is_already_used() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();
    int port = freePort();

    try (ServerSocket lock = new ServerSocket(port)) {
      boolean result =
          new CmdServerStart(ui, ctx).createNew(new String[] {String.valueOf(port)}).execute(null);

      assertFalse(result);
      assertNull(ctx.getServer());
      assertEquals(1, ui.errors.size());
      assertTrue(ui.errors.get(0).contains("may already be in use"));
    }
  }
}
