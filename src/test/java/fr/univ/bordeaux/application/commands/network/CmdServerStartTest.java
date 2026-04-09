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
  void serverStartStartsServerOnValidPort() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();
    int port = freePort();

    boolean result =
        new CmdServerStart(ui, ctx).createNew(new String[] {String.valueOf(port)}).execute(null);

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Server started on"));
    assertTrue(ui.messages.get(0).contains(":" + port));
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start refuses invalid port")
  void serverStartRefusesInvalidPort() {
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
  void serverStartUsesDefaultPortWhenArgsIsNull() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(null).execute(null);

    if (!result && ui.errors.stream().anyMatch(e -> e.contains("may already be in use"))) {
      assertTrue(true);
      return;
    }

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(12345, ctx.getServer().getPort());
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains(":12345"));
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start uses default port when args array is empty")
  void serverStartUsesDefaultPortWhenArgsArrayIsEmpty() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[0]).execute(null);

    if (!result && ui.errors.stream().anyMatch(e -> e.contains("may already be in use"))) {
      assertTrue(true);
      return;
    }

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(12345, ctx.getServer().getPort());
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains(":12345"));
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start uses default port when first argument is blank")
  void serverStartUsesDefaultPortWhenFirstArgumentIsBlank() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[] {""}).execute(null);

    if (!result && ui.errors.stream().anyMatch(e -> e.contains("may already be in use"))) {
      assertTrue(true);
      return;
    }

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(12345, ctx.getServer().getPort());
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains(":12345"));
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start trims port argument before starting")
  void serverStartTrimsPortArgumentBeforeStarting() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();
    int port = freePort();

    boolean result =
        new CmdServerStart(ui, ctx).createNew(new String[] {"  " + port + "  "}).execute(null);

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(port, ctx.getServer().getPort());
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains(":" + port));
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start refuses when a server is already running")
  void serverStartRefusesWhenAServerIsAlreadyRunning() throws Exception {
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
  void serverStartFailsWhenPortIsAlreadyUsed() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();
    int port = freePort();

    try (ServerSocket lock = new ServerSocket(port)) {
      boolean result =
          new CmdServerStart(ui, ctx).createNew(new String[] {String.valueOf(port)}).execute(null);

      assertFalse(result);
      assertNull(ctx.getServer());
      assertEquals(1, ui.errors.size());
      assertTrue(ui.errors.get(0).contains("Failed to start on port " + port));
    }
  }

  @Test
  @DisplayName("server_start refuses port below 1024")
  void serverStartRefusesPortBelow1024() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[] {"1023"}).execute(null);

    assertFalse(result);
    assertNull(ctx.getServer());
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Port must be between 1024 and 65535"));
  }

  @Test
  @DisplayName("server_start refuses port above 65535")
  void serverStartRefusesPortAbove65535() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[] {"65536"}).execute(null);

    assertFalse(result);
    assertNull(ctx.getServer());
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Port must be between 1024 and 65535"));
  }

  @Test
  @DisplayName("server_start accepts boundary port 1024")
  void serverStartAcceptsBoundaryPort1024() throws Exception {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();
    int port = 1024;

    try (ServerSocket ignored = isPortAvailable(port) ? null : new ServerSocket(freePort())) {
      if (!isPortAvailable(port)) {
        return;
      }
    }

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[] {"1024"}).execute(null);

    assertTrue(result);
    assertNotNull(ctx.getServer());
    assertTrue(ctx.getServer().isRunning());
    assertEquals(1024, ctx.getServer().getPort());
    server = ctx.getServer();
  }

  @Test
  @DisplayName("server_start accepts boundary port 65535")
  void serverStartAcceptsBoundaryPort65535() {
    TestUi ui = new TestUi();
    AppContext ctx = newContext();

    boolean result = new CmdServerStart(ui, ctx).createNew(new String[] {"65535"}).execute(null);

    if (result) {
      assertNotNull(ctx.getServer());
      assertTrue(ctx.getServer().isRunning());
      assertEquals(65535, ctx.getServer().getPort());
      server = ctx.getServer();
    } else {
      assertEquals(1, ui.errors.size());
      assertTrue(ui.errors.get(0).contains("Failed to start on port 65535"));
    }
  }

  private boolean isPortAvailable(int port) {
    try (ServerSocket ignored = new ServerSocket(port)) {
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
