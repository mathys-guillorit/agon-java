package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command providing strategic suggestions to the player.
 *
 * <p>This command requests the best possible move from the current {@link MatchManager} (often
 * calculated via an AI) and displays it in the standard Aba-Pro notation.
 */
public final class CmdHint extends Cmd {

  /**
   * Constructs the Hint command. Initializes the name to "hint" and sets a default description.
   *
   * @param uictx The user interface context for displaying the suggestion.
   */
  public CmdHint(GameUserInterface uictx) {
    super(uictx);
    this.setDesc("Description: Request a strategic suggestion from the AI.");
    this.setName("hint");
  }

  /**
   * Provides the short description for the hint command.
   *
   * @return An empty string (description is pre-set in the constructor via {@code setDesc}).
   */
  @Override
  public String getDescription() {
    return "Usage: hint\n Description: show to the user the best move to play\n";
  }

  /**
   * Executes the hint logic.
   *
   * <p>Calls {@code match.hint()} to obtain a recommended {@link Move}, then converts the move's
   * coordinates into human-readable Aba-Pro notation using {@link CoordinateMapper}.
   *
   * @param match The current match manager providing the game state and AI logic.
   * @return Always {@code false} as this command does not modify the game state (it only provides
   *     information).
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
   * @return A new {@link CmdHint} instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdHint(super.getCtx());
  }
}
