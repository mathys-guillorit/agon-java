package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

/** Create a new game. Command representation in cli : "new [ARGS]" */
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
  public String getDescription() {
    return "";
  }

  @Override
  public void execute(MatchManager matchManager) {
    super.getCtx().showMessage("j exec la commande \n");
    Match match=MatchFactory.createMatch(gameConfig,this.getCtx());
    match.setObserver((MatchObserver) super.getCtx());
    match.startGame();
  }

  @Override
  public CmdAction createNew(String[] args) {
    return new CmdCreate(super.getCtx(), this.gameConfig);
  }

  @Override
  public void showHelp() {
    this.getCtx().showMessage("Usage: new\n");
    this.getCtx()
        .showMessage(
            "Description: Starts a new Agon game session. This will reset the board and timers.\n");
  }
}
