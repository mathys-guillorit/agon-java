package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

/**
 * Create a new game. Command representation in cli : "new [ARGS]"
 */
public class CmdCreate extends Cmd {

  private GameConfig gameConfig;
  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param ui context
   */
  public CmdCreate(GameUserInterface ui, GameConfig gameConfig) {
    super(ui);
    this.gameConfig = gameConfig;
    this.opts = new Options();
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return null;
  }

  @Override
  public String getName() {
    return "new";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  @Override
  public boolean execute(MatchManager unused) {
    Match match = MatchFactory.createMatch(gameConfig, this.getCtx());
    match.setObserver((MatchObserver) super.getCtx());
    super.getCtx().setMatchManager((MatchManager) match);
    //match.startGame();
    return true;
  }

  @Override
  public CmdAction createNew(String[] args) {
    return new CmdCreate(super.getCtx(), this.gameConfig);
  }

  public String getDescription() {
    return "Usage: new\n"+"Description: Starts a new Agon game session. This will reset the board and timers.\n";
    /*this.getCtx().showMessage();
    this.getCtx()
        .showMessage(
            "Description: Starts a new Agon game session. This will reset the board and timers.\n");*/
  }
}
