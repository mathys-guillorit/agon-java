package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import javax.annotation.Nonnull;
import org.jline.reader.Completer;

/**
 * Command responsible for executing a player's move on the board.
 *
 * <p>This command bridges the UI input (coordinates or Move object) with the {@link MatchManager}
 * to update the game state.
 */
public class CmdMove extends Cmd {

  /** The move object to execute (optional if coordinates are provided). */
  private Move move;

  /** The starting index/coordinate. */
  private int from;

  /** The target index/coordinate. */
  private int destination;

  /**
   * Constructs a move command using a pre-built {@link Move} object.
   *
   * @param move The move to be applied.
   * @param ui The user interface context.
   */
  public CmdMove(Move move, GameUserInterface ui) {
    super(ui);
    this.move = move;
    this.setName("move");
  }

  /**
   * Constructs a move command using raw coordinates.
   *
   * @param from The origin position.
   * @param to The destination position.
   * @param ui The user interface context.
   */
  public CmdMove(int from, int to, GameUserInterface ui) {
    super(ui);
    this.from = from;
    this.destination = to;
    this.setName("move");
  }

  /**
   * Executes the move on the match manager.
   *
   * <p>If a {@link Move} object was provided, it is used directly. Otherwise, a new Move is created
   * using the current player's color.
   *
   * @param match The manager responsible for game rules and board updates.
   * @return true if the move was valid and successfully applied, false otherwise.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (match == null) {
      this.getCtx().showError("No active match to execute move.");
      return false;
    }

    boolean result;
    if (this.move != null) {
      result = match.move(this.move);
    } else {
      // Fallback to coordinates if Move object is null
      Move newMove = new Move(this.from, this.destination, match.getCurrentPlayer().getColor());
      result = match.move(newMove);
    }

    if (!result) {
      this.getCtx().showWarn("Invalid move attempt.");
    }

    return result;
  }

  /**
   * Factory method to create a new move action from CLI arguments. * @param args Arguments provided
   * (e.g., from and destination).
   *
   * @return A new {@link CmdMove} or null if arguments are not handled here.
   */
  @Override
  public CmdAction createNew(String[] args) {
    // Note: Implementation depends on how you parse "move A1 B2"
    return null;
  }

  /**
   * Returns the description of the move command.
   *
   * @return String description.
   */
  @Override
  public String getDescription() {
    return "Usage: move <from> <to>\nDescription: Moves a piece from one coordinate to another.\n";
  }

  /**
   * Provides the autocompleter for moves.
   *
   * @return null (Could be implemented for coordinate completion).
   */
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return super.getAutoCompleter();
  }
}
