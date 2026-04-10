package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.history.History;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.io.storage.GameSaveData;
import fr.univ.bordeaux.technical.io.storage.GameSaveParser;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import fr.univ.bordeaux.ui.ObservableMatch;
import org.apache.commons.cli.Options;

/** Command responsible for loading a saved game state from a file. */
public final class CmdLoad extends Cmd {

  private Options opts;

  private String filename;

  private GameEngine gameEngine;

  /**
   * Constructs the base Load command for registration.
   *
   * @param uictx The user interface context for interaction.
   * @param gameEngine The engine that will run the match.
   */
  public CmdLoad(GameUserInterface uictx, GameEngine gameEngine) {
    super(
        uictx,
        "load",
        "load [filename]\n"
            + "Description: Loads a previously saved game from the specified file.\n"
            + "Example: load my_save.txt\n");
    this.opts = new Options();
    this.gameEngine = gameEngine;
  }

  /**
   * Internal constructor used to create an executable instance with a specific filename.
   *
   * @param uictx The user interface context.
   * @param gameEngine The engine that will run the match.
   * @param filename The name of the file to load.
   */
  private CmdLoad(GameUserInterface uictx, GameEngine gameEngine, String filename) {
    this(uictx, gameEngine);
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
   * Executes the loading logic.
   *
   * @param match The current match manager.
   * @return true if the game was successfully loaded, false otherwise.
   */
  @Override
  public boolean execute(MatchManager match) {
    GameLogger.info("Executing 'load' command for file: " + this.filename);
    GameSaveParser parser = new GameSaveParser();
    try {
      GameSaveData saveData = parser.parse(this.filename);

      GameConfig loadedConfig = saveData.getConfig();
      Color playerTurn = saveData.getCurrentPlayer();

      History loadedHistory = new History(saveData.getHistoryMoves());
      AgonBoard loadedBoard = new AgonBoardImpl(saveData.getBoardLines(), loadedHistory);

      Match newMatch =
          MatchFactory.createMatch(loadedConfig, super.getCtx(), loadedBoard, playerTurn);
      if (newMatch instanceof ObservableMatch obsMatch) {
        obsMatch.setObserver((MatchObserver) super.getCtx());
      }
      if (newMatch instanceof ReadOnlyMatch roMatch) {
        ((MatchObserver) super.getCtx()).onMatchUpdate(roMatch);
      }
      gameEngine.setMatchManager(newMatch);
      super.getCtx().showMessage("Game successfully loaded from: " + this.filename + "\n");
      return true;
    } catch (Exception e) {
      super.getCtx().showError("Failed to load game: " + e.getMessage() + "\n");
      return false;
    }
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
    return new CmdLoad(this.getCtx(), this.gameEngine, fileToLoad);
  }
}
