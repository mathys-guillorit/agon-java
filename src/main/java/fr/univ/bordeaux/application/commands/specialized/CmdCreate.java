package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.reader.Completer;

/**
 * Create a new game. Command representation in cli : "new [ARGS]"
 */
public class CmdCreate extends Cmd {

  private GameConfig gameConfig;
  private GameEngine gameEngine;
  private Options options;
  private String[] args;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param ui context
   */
  public CmdCreate(GameUserInterface ui, GameConfig gameConfig,GameEngine gameEngine) {
    this(ui, gameConfig,gameEngine ,new String[0]);

  }

  public CmdCreate(GameUserInterface ui, GameConfig gameConfig,GameEngine gameEngine, String[] args) {
    super(ui);
    this.gameConfig = gameConfig;
    this.gameEngine = gameEngine;
    this.args = args;
    this.options = new Options();
    this.options.addOption("p1Ia", "player1IsAi", true, "Define if the player 1 is an AI (true/false)");
    this.options.addOption("p2Ia", "player2IsAi", true, "Define if the player 2 is an AI (true/false)");
    this.options.addOption("p1Color", "player1Color", true, "Define the color for the player 1 (white/black)");
    this.options.addOption("p2Color", "player2Color", true, "Define the color for the player 2 (white/black)");
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return super.getAutoCompleter();
  }

  @Override
  public String getName() {
    return "new";
  }

  @Override
  public Options getOptions() {
    return this.options;
  }

  @Override
  public boolean execute(MatchManager unused) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      Color p1Color = Color.WHITE;
      if (cmd.hasOption("p1Color")) {
        p1Color = Color.valueOf(cmd.getOptionValue("p1Color").toUpperCase());
      } else if (cmd.hasOption("p2Color")) {
        Color p2Color = Color.valueOf(cmd.getOptionValue("p2Color").toUpperCase());
        p1Color = (p2Color == Color.WHITE) ? Color.BLACK : Color.WHITE;
      }

      boolean p1IsAi = false;
      if (cmd.hasOption("p1Ia")) {
        System.out.println("oui ya l'option p1");
        p1IsAi = Boolean.parseBoolean(cmd.getOptionValue("p1Ia"));
      }

      boolean p2IsAi = true;
      if (cmd.hasOption("p2Ia")) {
        System.out.println("oui ya l'option p2");
        p2IsAi = Boolean.parseBoolean(cmd.getOptionValue("p2Ia"));
        System.out.println("is ia "+ p2IsAi);
      }

      if (p1Color == Color.WHITE) {
        gameConfig.setWhiteAI(p1IsAi);
        gameConfig.setBlackAI(p2IsAi);
      } else {
        gameConfig.setWhiteAI(p2IsAi);
        gameConfig.setBlackAI(p1IsAi);
      }

    } catch (ParseException | IllegalArgumentException e) {
      this.getCtx().showError("Invalid options for command 'new': " + e.getMessage());
      return false;
    }
    System.out.println("le noir est ia " +gameConfig.isBlackAI());
    Match match = MatchFactory.createMatch(gameConfig, this.getCtx());
    //match.setObserver((MatchObserver) super.getCtx());
    gameEngine.setMatchManager((MatchManager) match);
    System.out.println("je vais return ");
    return true;
  }

  @Override
  public CmdAction createNew(String[] args) {
    return new CmdCreate(super.getCtx(), this.gameConfig,this.gameEngine, args);
  }

  public String getDescription() {
    return "Usage: new\n"+"Description: Starts a new Agon game session. This will reset the board and timers.\n";
    /*this.getCtx().showMessage();
    this.getCtx()
        .showMessage(
            "Description: Starts a new Agon game session. This will reset the board and timers.\n");*/
  }
}
