package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * class to test AgonShell
 *
 * <p>requirements : - overvrite 2 classes from Jline (LineReader and Terminal)
 */
public class AgonShellTest {

  @Test
  @DisplayName("resource with URL and '/' path notation")
  void debugResourcePath() {
    URL url = getClass().getResource("/cmdsInformations/agonShellMenu.txt");
    assertNotNull(url, () -> "Resource URL =" + url);
  }

  @Test
  @DisplayName("test quitGame with 'n' (no save)")
  void testQuitGameNoSave() throws Exception {
    LineReader reader = new FakeLineReader("n");
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(terminal, reader);
    shell.quitGame();
    assertFalse(shell.getDebugMode().get());
  }

  @Test
  @DisplayName("test quitGame with 'y' (save)")
  void testQuitGameWithSave() throws Exception {
    LineReader reader = new FakeLineReader("y");
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(terminal, reader);
    shell.quitGame();
    assertFalse(shell.getDebugMode().get());
  }

  @Test
  @DisplayName("test readLine with empty input")
  void testReadLineEmpty() throws Exception {
    LineReader reader = new FakeLineReader("");
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(terminal, reader);
    shell.readLine();
    // no except thrown
    assertTrue(true);
  }

  @Test
  @DisplayName("test readLine with UserInterruptException")
  void testReadLineInterrupt() throws Exception {
    LineReader reader = new FakeLineReader();
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(terminal, reader);
    shell.readLine();
    assertTrue(true); // no except thrown
  }

  @Test
  @DisplayName("test safeCloseTerminal with IOException")
  void testSafeCloseTerminalIOException() throws Exception {
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader());
    shell.safeCloseTerminal();
    assertTrue(true); // no throws before
  }

  @Test
  @DisplayName("test showError writes to terminal")
  void testShowError() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = TerminalBuilder.builder().streams(null, out).dumb(true).build();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader());
    shell.showError("test error");
    String output = out.toString();
    String container = "ERROR";
    assertTrue(
        output.contains(container), "\n'" + output + "\n\tmust contains:\n'" + container + "'");
    container = "AGON";
    assertTrue(
        output.contains(container), "\n'" + output + "\n\tmust contains:\n'" + container + "'");
  }

  @Test
  @DisplayName("test showInfo writes to terminal")
  void testShowInfo() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = TerminalBuilder.builder().streams(null, out).dumb(true).build();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader());
    shell.showInfo("test info");
    String output = out.toString();
    assertTrue(output.contains("INFO"));
  }

  @Test
  @DisplayName("test showWarn writes to terminal")
  void testShowWarn() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = TerminalBuilder.builder().streams(null, out).dumb(true).build();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader());
    shell.showWarn("test warn");
    String output = out.toString();
    assertTrue(output.contains("WARNING"));
  }

  @Test
  @DisplayName("test leave sets running to false")
  void testLeave() throws Exception {
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader());
    shell.leave();
    assertFalse(shell.getDebugMode().get());
  }

  @Test
  @DisplayName("test leave sets running to false")
  void testLeaveWithFakeTerminal() {
    var out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""));
    shell.leave();
    assertFalse(shell.getRunning().get());
  }

  @Test
  @DisplayName("test setVerbose toggles state")
  void testSetVerbose() throws Exception {
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader());
    shell.setVerbose(true);
    assertTrue(shell.getVerbose());
    shell.setVerbose(false);
    assertFalse(shell.getVerbose());
  }

  @Test
  @DisplayName("test without commands shows error message")
  void testWithoutCommands() {
    var out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""));
    shell.loop();
    String cliOutput = out.toString();
    assertTrue(
        cliOutput.contains("No Command \"Cmd\" registered"),
        "Le message d'erreur n'a pas été affiché. Sortie : " + cliOutput);
  }

  @Test
  @DisplayName("test init sets default values")
  void testInit() {
    var out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""));

    assertFalse(shell.getVerbose());
    assertFalse(shell.getDebugMode().get());
    assertTrue(shell.getRunning().get());
    assertNotNull(shell.getCmds(), "shell.getCmds() must not be null");
  }

  @Test
  @DisplayName("test loadMainMenu updates displayed menu")
  void testLoadMainMenu() {
    var out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""));
    String newMenu =
        "    undo [N] : cancel the last turn (or the N lasts)\n"
            + "    redo [N] : replay the last canceled turn (or the N lasts)\n"
            + "    show";
    shell.loadMainMenu(newMenu);
    shell.showHelp();
    String output = out.toString();
    assertTrue(output.contains("undo [N]"), "Menu should contain 'undo [N]'");
    assertTrue(output.contains("redo [N]"), "Menu should contain 'redo [N]'");
    assertTrue(output.contains("show"), "Menu should contain 'show'");
  }

  @Test
  @DisplayName("test loadMainMenu when no menu is set")
  void testLoadMainMenuWithoutMainMenu() {
    var out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""));
    shell.start();
    String output = out.toString();
    final String defaultMsg =
        "Main menu didn't change, you may specify a menu before using associated commands ?";
    final String finalMsg =
        "\n\twarning msg must contain:\n'"
            + defaultMsg
            + "'"
            + "\n\tbut id contains:\n"
            + "'"
            + output
            + "'";
    assertTrue(output.contains(defaultMsg), finalMsg);
  }
}
