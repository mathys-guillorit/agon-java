package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command providing strategic suggestions to the player. */
public final class CmdHint extends Cmd {

  /**
   * Constructs the Hint command.
   *
   * @param uictx The user interface context for displaying the suggestion.
   */
  public CmdHint(GameUserInterface uictx) {
    super(uictx);
    this.setDesc("Description: Request a strategic suggestion from the AI.");
    this.setName("hint");
  }

  /**
   * Provides the help description for the hint command.
   *
   * @return A formatted string describing the command.
   */
  @Override
  public String getDescription() {
    return "Usage: hint\n Description: show to the user the best move to play\n";
  }

  /**
   * Executes the hint logic.
   *
   * @param match The current match manager providing the game state and AI logic.
   * @return Always false as this command does not modify the game state.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (match == null) {
      getCtx().showMessage("you must create a match to use this command\n");
      return false;
    }
    Move hint = match.hint();
    if (hint != null) {
      super.getCtx()
          .showMessage(
              "Hint: From "
                  + CoordinateMapper.toAbaPro(hint.getFrom())
                  + " To "
                  + CoordinateMapper.toAbaPro(hint.getDestination())
                  + "\n");
    } else {
      super.getCtx().showError("No hint available for the current state.\n");
    }
    return false;
  }

  /**
   * Factory method to create an executable instance of the hint command.
   *
   * @param args The arguments passed (ignored for this command).
   * @return A new CmdHint instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdHint(super.getCtx());
  }
}
