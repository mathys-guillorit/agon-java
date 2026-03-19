package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.reader.Completer;

public final class CmdSet extends Cmd {

  private final Options options;
  private GameConfig gameConfig;
  private String[] args;
  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdSet(GameUserInterface uictx, GameConfig gameConfig) {
    super(uictx);
    this.options = new Options();
    this.options.addOption("verbose", null, true, "increases the verbosity of the programme");
    this.options.addOption("debug", null, true, "displays the debug output");
    this.options.addOption("blitzmode", null, true, "sets if the game is a blitz");
    this.options.addOption("timeout", null, true, "set the timeout in milliseconds for blitzmode");
    this.options.addOption("aiActive", null, true, "enable aiPlayers for the game");
    this.options.addOption("aiMode", null, true, "set the algorithme to use for ai");
    this.options.addOption("aiDepth", null, true, "set the maximum depth for the AI algorithm");
    this.options.addOption("aiIterativeDeepening", null, true, "set IterativeDeepening for AI algorithm");
    this.options.addOption("aiTimeLimit", null, true, "set the response time for an AI");
    this.options.addOption("aiHeuristique", null, true, "set the heuristic use for AI algorithm");
    this.options.addOption(null, "whiteIsAI", true, "set if the white player is an AI");
    this.options.addOption("blackIsAI", null, true, "set if the black player an AI");
    this.gameConfig = gameConfig;
  }

  private CmdSet(GameUserInterface uictx, GameConfig gameConfig, String[] args) {
    this(uictx, gameConfig);
    this.args = args;
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return null;
  }

  @Override
  public String getName() {
    return "set";
  }

  @Override
  public Options getOptions() {
    return this.options;
  }

  @Override
  public String getDescription() {
    return "Usage: set PARAM=VALUE\n"+"Description: Changes the current game configuration dynamically during the session.\n";
  }

  public boolean execute(MatchManager match) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      StringBuilder feedback = new StringBuilder("Configuration mise à jour :\n");

      // --- Paramètres Système ---
      if (cmd.hasOption("verbose")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("verbose"));
        gameConfig.setVerbose(val);
        feedback.append("- Verbose : ").append(val).append("\n");
      }
      if (cmd.hasOption("debug")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("debug"));
        gameConfig.setDebug(val);
        feedback.append("- Debug : ").append(val).append("\n");
      }

      // --- Paramètres de Jeu ---
      if (cmd.hasOption("blitzmode")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("blitzmode"));
        gameConfig.setBlitzMode(val);
        feedback.append("- BlitzMode : ").append(val).append("\n");
      }
      if (cmd.hasOption("timeout")) {
        int val = Integer.parseInt(cmd.getOptionValue("timeout"));
        gameConfig.setTimeout(val);
        feedback.append("- Timeout : ").append(val).append("ms\n");
      }

      // --- Paramètres IA (Général) ---
      if (cmd.hasOption("aiActive")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("aiActive"));
        gameConfig.setAi(val);
        feedback.append("- AI Active : ").append(val).append("\n");
      }
      if (cmd.hasOption("aiMode")) {
        String val = cmd.getOptionValue("aiMode").toLowerCase().trim();
        gameConfig.setAiMode(val);
        feedback.append("- AI Mode : ").append(val).append("\n");
      }
      if (cmd.hasOption("aiDepth")) {
        int val = Integer.parseInt(cmd.getOptionValue("aiDepth"));
        gameConfig.setAiDepth(val);
        feedback.append("- AI Depth : ").append(val).append("\n");
      }
      if (cmd.hasOption("aiTimeLimit")) {
        int val = Integer.parseInt(cmd.getOptionValue("aiTimeLimit"));
        gameConfig.setAiTimeLimit(val);
        feedback.append("- AI Time Limit : ").append(val).append("s\n");
      }
      if (cmd.hasOption("aiIterativeDeepening")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("aiIterativeDeepening"));
        gameConfig.setAiIterativeDeepening(val);
        feedback.append("- Iterative Deepening : ").append(val).append("\n");
      }
      if (cmd.hasOption("aiHeuristique")) {
        String val = cmd.getOptionValue("aiHeuristique").toLowerCase().trim();
        gameConfig.setAiHeuristic(val);
        feedback.append("- Heuristique : ").append(val).append("\n");
      }

      // --- Attribution des Joueurs ---
      if (cmd.hasOption("whiteIsAI")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("whiteIsAI"));
        gameConfig.setWhiteAI(val);
        feedback.append("- White is AI : ").append(val).append("\n");
      }
      if (cmd.hasOption("blackIsAI")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("blackIsAI"));
        gameConfig.setBlackAI(val);
        feedback.append("- Black is AI : ").append(val).append("\n");
      }

      this.getCtx().showMessage(feedback.toString());

    } catch (ParseException e) {
      this.getCtx().showError("Erreur de syntaxe : " + e.getMessage());
      return false;
    } catch (NumberFormatException e) {
      this.getCtx().showError("Erreur : La valeur doit être un nombre.");
      return false;
    }

    return true;
  }

  public CmdAction createNew(String[] args) {
    return new CmdSet(super.getCtx(),this.gameConfig,args);
  }

}
