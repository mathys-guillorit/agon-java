package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdMove implements CmdAction {

  private Move move;
  private int from;
  private int destination;

  public CmdMove(Move move) {
    System.out.println("j ai crée un move");
    this.move = move;
  }

  public CmdMove(int from, int to) {
    this.from = from;
    this.destination = to;
  }

  @Override
  public boolean execute(MatchManager match) {
    if (this.move == null) {
      return match.move(new Move(this.from, this.destination, match.getCurrentPlayer().getColor()));
    }else{
      return match.move(move);
    }
  }

  @Override
  public CmdAction createNew(String[] args) {
    return null;
  }

  @Override
  public String getDescription() {
    return "Move\n";
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
