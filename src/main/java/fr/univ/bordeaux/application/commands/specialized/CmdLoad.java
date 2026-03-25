package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.history.History;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.io.storage.GameSaveData;
import fr.univ.bordeaux.technical.io.storage.GameSaveParser;
import fr.univ.bordeaux.ui.GameUserInterface;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

/**
 * Command responsible for loading a saved game state from a file.
 *
 * <p>This command uses a specific filename provided as an argument to restore a previous session's
 * match data.
 */
public final class CmdLoad extends Cmd {

  /** CLI options associated with the load command. */
  private Options opts;

  /** The target filename to be loaded during execution. */
  private String filename;

  /**
   * Constructs the base Load command used for registration in the command set. Initializes the
   * command name to "load".
   *
   * @param uictx The user interface context for interaction.
   */
  public CmdLoad(GameUserInterface uictx) {
    super(uictx);
    this.opts = new Options();
    this.setName("load");
  }

  /**
   * Internal constructor used to create an executable instance of the command with a specific
   * filename.
   *
   * @param uictx The user interface context.
   * @param filename The name of the file to load.
   */
  private CmdLoad(GameUserInterface uictx, String filename) {
    this(uictx);
    this.filename = filename;
  }

  /**
   * Provides the autocompleter for this command. * @return null (Default behavior, could be
   * replaced by a file completer).
   */
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return super.getAutoCompleter();
  }

  /**
   * Returns the command trigger name.
   *
   * @return "load".
   */
  @Override
  public String getName() {
    return "load";
  }

  /**
   * Returns the CLI options for the load command.
   *
   * @return An {@link Options} object.
   */
  @Override
  public Options getOptions() {
    return this.opts;
  }

  /**
   * Returns the help description and usage examples for the load command.
   *
   * @return A formatted string describing the command.
   */
  @Override
  public String getDescription() {
    return "Usage: load [filename]\n"
        + "Description: Loads a previously saved game from the specified file.\n"
        + "Example: load my_save.txt\n";
  }

  /**
   * Executes the loading logic.
   *
   * <p>Currently, this method is a placeholder. Future implementations should use the {@code
   * filename} attribute to restore the match state.
   *
   * @param match The current match manager.
   * @return false (Implementation pending).
   */
  @Override
  public boolean execute(MatchManager match) {
    //---------------------------------------

    GameSaveParser parser = new GameSaveParser();
    try {
      GameSaveData saveData = parser.parse("save.asv");

      GameConfig loadedConfig = saveData.getConfig();
      Color playerTurn = saveData.getCurrentPlayer();

      AgonBoard loadedBoard = new AgonBoardImpl(saveData.getBoardLines());
      History loadedHistory = new History(saveData.getHistoryMoves());
    }catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    //------------------------

    this.getCtx()
        .showError("Load command recognized but not yet implemented for: " + this.filename + "\n");
    return false;
  }

  /**
   * Factory method to create a new {@code CmdLoad} instance with the filename argument provided by
   * the user.
   *
   * @param args Array of arguments where the first element is the filename.
   * @return A new {@link CmdAction} ready for execution, or null if arguments are missing.
   */
  @Override
  public CmdAction createNew(String[] args) {
    if (args.length == 0) {
      this.getCtx().showError("Error: Please provide a filename.\n" + getDescription());
      return null;
    }

    // Capture the filename from arguments
    String fileToLoad = args[0];

    // Return an instance specialized for this file
    return new CmdLoad(this.getCtx(), fileToLoad);
  }
}
