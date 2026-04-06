package fr.univ.bordeaux.ui.cli.tools;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.ui.cli.AgonShell;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

public class FakeShell extends AgonShell {

  public FakeShell(Terminal term, LineReader reader, AgonRegister<CmdAction> cmds) {
    super(term, reader, cmds);
  }

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public void quit() {}

  @Override
  public void showMessage(String message) {}

  @Override
  public void showError(String error) {}

  @Override
  public void showHelp() {}

  @Override
  public void showWarn(String msg) {}

  @Override
  public void showInfo(String msg) {}

  @Override
  public String getUserInput() {
    return "";
  }

  @Override
  public void displayHistory(List<MoveDtO> moves) {}
}
