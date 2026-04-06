package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.TestUi;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdServerListTest {

  @Test
  @DisplayName("server_list shows warning when no server is found")
  void server_list_shows_warning_when_no_server_is_found() {
    TestUi ui = new TestUi();

    AppContext ctx =
        new AppContext(new LocalProfile("TestPlayer")) {
          @Override
          public void ensureDiscoveryStarted() {}

          @Override
          public ClientDiscovery getDiscovery() {
            return new ClientDiscovery() {
              @Override
              public List<ServerInfo> getServers() {
                return List.of();
              }
            };
          }
        };

    boolean result = new CmdServerList(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("No servers found"));
  }

  @Test
  @DisplayName("server_list shows all discovered servers")
  void server_list_shows_all_discovered_servers() {
    TestUi ui = new TestUi();

    AppContext ctx =
        new AppContext(new LocalProfile("TestPlayer")) {
          @Override
          public void ensureDiscoveryStarted() {}

          @Override
          public ClientDiscovery getDiscovery() {
            return new ClientDiscovery() {
              @Override
              public List<ServerInfo> getServers() {
                return List.of(
                    new ServerInfo("S1", "192.168.1.10", 1234),
                    new ServerInfo("S2", "192.168.1.11", 5678));
              }
            };
          }
        };

    boolean result = new CmdServerList(ui, ctx).createNew(new String[0]).execute(null);

    assertTrue(result);
    assertEquals(2, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("S1"));
    assertTrue(ui.messages.get(1).contains("S2"));
  }

  @Test
  @DisplayName("server_list shows error when discovery fails")
  void server_list_shows_error_when_discovery_fails() {
    TestUi ui = new TestUi();

    AppContext ctx =
        new AppContext(new LocalProfile("TestPlayer")) {
          @Override
          public void ensureDiscoveryStarted() {
            throw new RuntimeException("boom");
          }
        };

    boolean result = new CmdServerList(ui, ctx).createNew(new String[0]).execute(null);

    assertFalse(result);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Discovery error"));
    assertTrue(ui.errors.get(0).contains("boom"));
  }
}
