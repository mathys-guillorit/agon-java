package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

/**
 * Command responsible for saving the current game state to a file.
 *
 * <p>This command captures the board state, player information, and move history to allow future
 * restoration via the 'load' command.
 */
public final class CmdSave extends Cmd {

  /** CLI options for the save command. */
  private Options opts;

  /** The target filename for the save operation. */
  private String filename;

  /**
   * Constructs the base Save command for registration. Initializes the name to "save" and sets the
   * default description.
   *
   * @param uictx The user interface context.
   */
  public CmdSave(GameUserInterface uictx) {
    super(uictx);
    this.opts = new Options();
    this.setDesc("Description: Saves the current game state and history to the specified file.");
    this.setName("save");
  }

  /**
   * Internal constructor used to create an executable instance with a filename.
   *
   * @param uictx The user interface context.
   * @param filename The name of the file to create or overwrite.
   */
  private CmdSave(GameUserInterface uictx, String filename) {
    this(uictx);
    this.filename = filename;
  }

  /**
   * Returns the help description and usage for the save command.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    return "Usage: save [filename]\n"
        + "Description: Saves the current game state to the specified file.\n"
        + "Example: save party1.txt\n";
  }

  /**
   * Executes the save logic.
   *
   * <p>Currently a placeholder. It should interface with a storage service to write the {@link
   * MatchManager} state into {@code filename}.
   *
   * @param match The manager handling the current match data.
   * @return true if the command was recognized, false if the operation failed.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (this.filename == null) {
      this.getCtx().showError("No filename provided for saving.\n");
      return false;
    }

    // Logique de sauvegarde à implémenter ici
    this.getCtx()
        .showInfo(
            "Save command recognized for file: " + this.filename + " (Implementation pending).\n");
    return true;
  }

  /**
   * Factory method to create a new {@code CmdSave} instance with the filename provided by the user.
   *
   * @param args Array of arguments where the first element is the target filename.
   * @return A new {@link CmdSave} instance, or the base instance if no filename is provided.
   */
  @Override
  public CmdAction createNew(String[] args) {
    if (args.length == 0) {
      this.getCtx().showWarn("Warning: Saving without a filename might use a default slot.\n");
      return new CmdSave(super.getCtx(), "default_save.txt");
    }

    return new CmdSave(super.getCtx(), args[0]);
  }

  /**
   * Returns the CLI options for this command.
   *
   * @return An {@link Options} object.
   */
  @Override
  public Options getOptions() {
    return this.opts;
  }
}
