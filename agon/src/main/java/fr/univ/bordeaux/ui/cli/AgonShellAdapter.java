package fr.univ.bordeaux.ui.cli;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

/** terminal for production */
public class AgonShellAdapter {

  private Terminal terminal;

  public AgonShellAdapter(Terminal terminal) {
    this.terminal = terminal;
  }

  public void showMessage(String msg) {
    terminal.writer().println(msg);
    terminal.flush();
  }

  public LineReader getLineReader() {
    return LineReaderBuilder.builder().terminal(terminal).build();
  }
}
