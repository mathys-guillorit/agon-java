package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdMove implements CmdAction {
  private Move move;
  public CmdMove(Move move) {
    this.move=move;
  }

  @Override
  public void execute(MatchManager match) {
     match.move(move);
  }

  @Override
  public CmdAction createNew(String[] args) {
    return null;
  }

  @Override
  public void showHelp() {

  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return null;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Options getOptions() {
    return null;
  }

}
