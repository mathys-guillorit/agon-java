package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

/** Command responsible for loading a saved game state from a file. */
public final class CmdLoad extends Cmd {

  private Options opts;

  private String filename;

  /**
   * Constructs the base Load command for registration.
   *
   * @param uictx The user interface context for interaction.
   */
  public CmdLoad(GameUserInterface uictx) {
    super(uictx);
    this.opts = new Options();
    this.setName("load");
  }

  /**
   * Internal constructor used to create an executable instance with a specific filename.
   *
   * @param uictx The user interface context.
   * @param filename The name of the file to load.
   */
  private CmdLoad(GameUserInterface uictx, String filename) {
    this(uictx);
    this.filename = filename;
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
   * @return An Options object.
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
   * @param match The current match manager.
   * @return false (Implementation pending).
   */
  @Override
  public boolean execute(MatchManager match) {
    this.getCtx()
        .showError("Load command recognized but not yet implemented for: " + this.filename + "\n");
    return false;
  }

  /**
   * Factory method to create a new CmdLoad instance with the filename argument.
   *
   * @param args Array of arguments where the first element is the filename.
   * @return A new CmdAction ready for execution, or null if arguments are missing.
   */
  @Override
  public CmdAction createNew(String[] args) {
    if (args.length == 0) {
      this.getCtx().showError("Error: Please provide a filename.\n" + getDescription());
      return null;
    }

    String fileToLoad = args[0];
    return new CmdLoad(this.getCtx(), fileToLoad);
  }
}
