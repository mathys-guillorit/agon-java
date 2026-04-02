package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.storage.GameSaveData;
import fr.univ.bordeaux.technical.io.storage.GameSaveSerializer;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.io.IOException;
import java.util.List;

/**
 * Command responsible for saving the current game state to a file.
 *
 * <p>This command captures the board state, player information, and move history to allow future
 * restoration via the 'load' command.
 */
public final class CmdSave extends Cmd {

  private String filename;

  /**
   * Constructs the base Save command for registration.
   *
   * @param uictx The user interface context.
   */
  public CmdSave(GameUserInterface uictx) {
    super(uictx);
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
        + "Description: Saves the current game state to the specified file, if there is no filename save by default in default_save.\n"
        + "Example: save myparty.txt\n";
  }

  /**
   * Executes the save logic.
   *
   * @param match The manager handling the current match data.
   * @return true if the command was recognized, false if the operation failed.
   */
  @Override
  public boolean execute(MatchManager match) {
    AgonBoard board = match.getAgonBoard();
    List<String> boardText = board.toTextList();
    List<String> historyText = board.getHistoryAsText();
    GameSaveData saveData =
        new GameSaveData(
            match.getGameConfig(), match.getCurrentPlayer().getColor(), boardText, historyText);

    GameSaveSerializer serializer = new GameSaveSerializer();
    try {
      serializer.save(saveData, filename);
      match.setIsSaved(true);
    } catch (IOException e) {
      super.getCtx().showError("Something went wrong while saving the game please try again.\n");
      return false;
    }
    return false;
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
      return new CmdSave(super.getCtx(), "default_save");
    }
    return new CmdSave(super.getCtx(), args[0]);
  }
}
