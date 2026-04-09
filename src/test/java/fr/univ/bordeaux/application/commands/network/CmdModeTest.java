package fr.univ.bordeaux.application.commands.network;

import static fr.univ.bordeaux.application.commands.network.NetworkCommandTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CmdModeTest {

  @Test
  @DisplayName("mode refuses when client is not connected")
  void modeRefusesWhenClientIsNotConnected() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);

    boolean result = new CmdMode(ui, ctx).createNew(new String[] {"normal"}).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Not connected"));
  }

  @Test
  @DisplayName("mode refuses when argument is missing")
  void modeRefusesWhenArgumentIsMissing() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;

    boolean result1 = new CmdMode(ui, ctx).createNew(null).execute(null);
    boolean result2 = new CmdMode(ui, ctx).createNew(new String[0]).execute(null);
    boolean result3 = new CmdMode(ui, ctx).createNew(new String[] {""}).execute(null);

    assertFalse(result1);
    assertFalse(result2);
    assertFalse(result3);
    assertEquals(3, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Missing mode"));
  }

  @Test
  @DisplayName("mode refuses invalid value")
  void modeRefusesInvalidValue() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;

    boolean result = new CmdMode(ui, ctx).createNew(new String[] {"ranked"}).execute(null);

    assertFalse(result);
    assertEquals(1, ui.warnings.size());
    assertTrue(ui.warnings.get(0).contains("Invalid mode"));
  }

  @Test
  @DisplayName("mode sends request when value is normal")
  void modeSendsRequestWhenValueIsNormal() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.chooseModeResult = true;

    boolean result = new CmdMode(ui, ctx).createNew(new String[] {"normal"}).execute(null);

    assertTrue(result);
    assertEquals("normal", client.chosenMode);
    assertEquals(1, ui.messages.size());
    assertTrue(ui.messages.get(0).contains("Mode request sent"));
  }

  @Test
  @DisplayName("mode trims and lowercases blitz")
  void modeTrimsAndLowercasesBlitz() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.chooseModeResult = true;

    boolean result = new CmdMode(ui, ctx).createNew(new String[] {"  BLITZ  "}).execute(null);

    assertTrue(result);
    assertEquals("blitz", client.chosenMode);
    assertEquals(1, ui.messages.size());
  }

  @Test
  @DisplayName("mode shows error when request fails")
  void modeShowsErrorWhenRequestFails() {
    TestUi ui = new TestUi();
    FakeAgonClient client = new FakeAgonClient();
    AppContext ctx = contextWithClient(client);
    client.connected = true;
    client.chooseModeResult = false;

    boolean result = new CmdMode(ui, ctx).createNew(new String[] {"normal"}).execute(null);

    assertTrue(result);
    assertEquals("normal", client.chosenMode);
    assertEquals(1, ui.errors.size());
    assertTrue(ui.errors.get(0).contains("Failed to send mode request"));
  }
}
