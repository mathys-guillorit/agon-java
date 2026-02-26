package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
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

  @Test
  @DisplayName("test with dumb system (cannot test with a real full system)")
  void testCliWritesOutput() throws Exception {
//    var in = new ByteArrayInputStream(new byte[0]);
//    var out = new ByteArrayOutputStream();
//    Terminal term = TerminalBuilder.builder().streams(in, out).dumb(true).build();
//    // existing default menu loaded file here
//    final String msg = "hello";
//    AgonShell shell = new AgonShell(term, false);
//    shell.showMessage(msg);
//    term.flush();
//    String output = out.toString();
//    System.out.println("out: " + out);
//    assertTrue(
//        output.contains(msg),
//        () -> "Expected '" + msg + "' in output, but was: '" + output + "'\n\t");
//  }
  }
}
