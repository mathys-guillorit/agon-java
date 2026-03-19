package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdMove extends Cmd {
  private GameUserInterface ui;
  private Move move;
  private int from;
  private int destination;

  public CmdMove(Move move,GameUserInterface ui) {
    super(ui);
    System.out.println("j ai crée un move");
    this.move = move;
  }

  public CmdMove(int from, int to, GameUserInterface ui) {
    super(ui);
    this.from = from;
    this.destination = to;
  }

  @Override
  public boolean execute(MatchManager match) {
    if (match == null) {
      return false;
    }
    if (this.move == null) {
      return match.move(new Move(this.from, this.destination, match.getCurrentPlayer().getColor()));
    }else{
      boolean res=match.move(move);
      if (res==false && this.from==-1){

      }
      System.out.println("apply move renvoie : "+res);
      return res;
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
