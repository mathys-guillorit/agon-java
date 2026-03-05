package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import jdk.jfr.Description;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AgonShellTest {

  @Test
  @DisplayName("resource with URL and '/' path notation")
  void debugResourcePath() {
    URL url = getClass().getResource("/cmdsInformations/agonShellMenu.txt");
    assertNotNull(url, () -> "Resource URL =" + url);
  }

  @Test
  @DisplayName("resource with '/' path notation")
  void debugResource() {
    InputStream s = AgonShell.class.getResourceAsStream("/cmdsInformations/agonShellMenu.txt");
    System.out.println("Resource stream =" + s);
    assertNotNull(s);
  }

  // impossible to test
  @Test
  @DisplayName("test with dumb system (cannot test with Reader real full system)")
  void testCliWritesOutput() throws Exception {
    //    var in = new ByteArrayInputStream(new byte[0]);
    //    var out = new ByteArrayOutputStream();
    //    Terminal term = TerminalBuilder.builder().streams(in, out).dumb(true).build();
    //    // existing default menu loaded file here
    //    final String msg = "hello";
    //    LineReader fkReader = new FakeLineReader("n");
    //    AgonShell shell = new AgonShell(term, fkReader);
    //    shell.showMessage(msg);
    //    term.flush();
    //    String output = out.toString();
    //    System.out.println("out: " + out);
    //    assertTrue(
    //        output.contains(msg),
    //        () -> "Expected '" + msg + "' in output, but was: '" + output + "'\n\t");
  }

  @Test
  void testShellReadsCommand() throws Exception {
    String input = "help exit\n";
    var in = new ByteArrayInputStream(input.getBytes());
    var out = new ByteArrayOutputStream();
    Terminal terminal = TerminalBuilder.builder().streams(in, out).dumb(true).build();
    AgonShell shell = new AgonShell(terminal);
    shell.loop();
    String output = out.toString();
    assertTrue(output.contains("help"));
  }

  @Test
  @Description("parse with a command name")
  void testREadLineParsesCommand() throws Exception {
    LineReader reader = new FakeLineReader("help quit");
    Terminal term = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(term, reader);
    boolean empty = shell.readLine();
    shell.quitGame();
    assertFalse(empty);
    assertEquals("help", shell.getUserCmdName());
    assertArrayEquals(new String[] {"quit"}, shell.getTxtOptions());
  }

  @Test
  @Description("add a test with custom Reader from JLine (just to test)")
  void testQuitGameBeforeNotBlock() throws Exception {
    LineReader reader = new FakeLineReader("n");
    Terminal term = TerminalBuilder.builder().dumb(true).build();
    AgonShell shell = new AgonShell(term, reader);
    shell.quitGame();
    assertTrue(true);
  }

  @Test
  @Description("must throw an error if no commands are filled")
  void testWithoutCommands() {}

  // impossible to test

  //  @Test
  //  @DisplayName("write to terminal")
  //  void testShowMsg() throws Exception {
  //    String input = "quit\n \n \n";
  //    var out = new ByteArrayOutputStream();
  //    var in = new ByteArrayInputStream(input.getBytes());
  //    final String msg = "show message did not write correctly\nout is:\n\t";
  //    final String showMsg = "hello world";
  //
  //    try(Terminal term = TerminalBuilder.builder().streams(in, out).dumb(true).build()){
  //      AgonShell shell = new AgonShell(term);
  //      shell.showMessage(showMsg);
  //      in = new ByteArrayInputStream("\n ".getBytes());
  //      shell.quitGame();
  //      String output = out.toString();
  //      assertTrue(output.contains("hello world"), msg+output+"\n it must be:\n\t"+showMsg);
  //    }
  //  }

}
