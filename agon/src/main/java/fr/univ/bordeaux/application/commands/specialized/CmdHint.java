package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;

public final class CmdHint extends Cmd {

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdHint(GameUserInterface uictx) {
    super(uictx);
    this.setDesc("Description: show help from ai to the user");
    this.setName("hint");
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public boolean execute(MatchManager match) {
    Move hint = match.hint();
    super.getCtx().showMessage("Hint : From " + CoordinateMapper.toAbaPro(hint.getFrom()) + " To "
        + CoordinateMapper.toAbaPro(hint.getDestination()));
    return false;
  }

  @Override
  public CmdAction createNew(String[] args) {
    return new CmdHint(super.getCtx());
  }
}
