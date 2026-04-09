package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger; // Import ajouté
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Arrays; // Import ajouté pour le debug des args
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Option;
import org.jline.reader.Completer;

/** Command responsible for dynamically updating the game configuration. */
public final class CmdSet extends Cmd {

  private GameConfig gameConfig;

  /** Types required to inform the user what to fill with option. */
  private Map<String, String> metaDataTypes = new HashMap<>();

  private String[] args;

  /**
   * Constructs the base Set command for registration.
   *
   * @param uictx The user interface context.
   * @param gameConfig The configuration object to be modified.
   */
  public CmdSet(GameUserInterface uictx, GameConfig gameConfig) {
    super(uictx);
    this.gameConfig = gameConfig;
    this.setName("set");
    this.addOption(new Option("verbose", true, "increase verbosity (true | false)"));
    this.addOption(new Option("debug", true, "to show more messages (true | false)"));
    this.addOption(new Option("blitzMode", true, "get a time limit"));
    this.addOption(new Option("timeout", true, "time limit for invites"));
    this.addOption(new Option("aiActive", true, "set if the ai is active"));
    this.addOption(new Option("aiMode", true, "algorithm specified"));
    this.addOption(new Option("aiDepth", true, "value of depth research algorithm"));
    this.addOption(new Option("aiTimeLimit", true, "set time limit to play for the ai"));
    this.addOption(new Option("aiIterativeDeepening", true, "use AI Iterative deepening"));
    this.addOption(new Option("aiHeuristic", true, "change ai heuristic"));
    this.addOption(new Option("whiteIsAi", true, "if the white player is an AI"));
    this.addOption(new Option("blackIsAi", true, "if the black player is an AI"));
    this.metaDataTypes.put("verbose", "true^false");
    this.metaDataTypes.put("debug", "true^false");
    this.metaDataTypes.put("blitzMode", "true^false");
    this.metaDataTypes.put("timeout", "0..." + Integer.MAX_VALUE);
    this.metaDataTypes.put("aiActive", "true^false");
    this.metaDataTypes.put("aiMode", "minimax^mcts^iterative");
    this.metaDataTypes.put("aiDepth", "0..." + Integer.MAX_VALUE);
    this.metaDataTypes.put("aiTimeLimit", "0..." + Integer.MAX_VALUE);
    this.metaDataTypes.put("aiIterativeDeepening", "true^false");
    this.metaDataTypes.put("aiHeuristic", "mixed^centrality^mobility^UCT^ML");
    this.metaDataTypes.put("whiteIsAi", "true^false");
    this.metaDataTypes.put("blackIsAi", "true^false");
  }

  /**
   * Internal constructor used to create an executable instance with arguments.
   *
   * @param uictx The user interface context.
   * @param gameConfig The configuration object.
   * @param args The CLI arguments to parse.
   */
  private CmdSet(GameUserInterface uictx, GameConfig gameConfig, String[] args) {
    this(uictx, gameConfig);
    this.args = args;
  }

  /**
   * Returns the usage and description for the set command.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    return "Usage: set PARAM=VALUE\n"
        + "Description: Changes the current game configuration "
        + "dynamically, if you use this command in game the "
        + "change will be effective in the real configuration "
        + "but not in the match configuration.\n"
        + "Example: set aiDepth=5 verbose=true\n";
  }

  /**
   * Executes the configuration update logic.
   *
   * @param match The current match manager (unused during config update).
   * @return true if the configuration was successfully parsed and updated.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (args == null || args.length == 0) {
      GameLogger.error("CmdSet: Execution failed - No arguments provided.");
      this.getCtx().showError("Error: No parameters provided. Usage: set PARAM=VALUE");
      return false;
    }

    GameLogger.info("CmdSet: Attempting to update configuration with: " + Arrays.toString(args));
    StringBuilder feedback = new StringBuilder("Configuration updated:\n");

    try {
      for (String arg : args) {
        if (!arg.contains("=")) {
          GameLogger.error("CmdSet: Invalid argument format -> " + arg);
          this.getCtx().showError("Invalid format for: " + arg + ". Expected PARAM=VALUE");
          continue;
        }

        String[] parts = arg.split("=", 2);
        String param = parts[0].trim();
        String value = parts[1].trim();
        GameLogger.debug("CmdSet: Processing parameter [" + param + "] with value [" + value + "]");

        switch (param) {
          case "verbose" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setVerbose(val);
            feedback.append("  - Verbose: ").append(val).append("\n");
          }
          case "debug" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setDebug(val);
            GameLogger.getInstance().setDebugMode(val);
            feedback.append("  - Debug: ").append(val).append("\n");
          }
          case "blitzmode" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setBlitzMode(val);
            feedback.append("  - BlitzMode: ").append(val).append("\n");
          }
          case "timeout" -> {
            int val = Integer.parseInt(value);
            if (gameConfig.setTimeout(val)) {
              feedback.append("  - Timeout: ").append(val).append("ms\n");
            } else {
              getCtx().showWarn("invalid timeout. It must be greater than 0.\n");
              return false;
            }
          }
          case "aiActive" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setAi(val);
            feedback.append("  - AI Active: ").append(val).append("\n");
          }
          case "aiMode" -> {
            if (gameConfig.setAiMode(value.toLowerCase())) {
              feedback.append("  - AI Mode: ").append(value.toLowerCase()).append("\n");
            } else {
              getCtx()
                  .showWarn("invalid aiMode. It must be mcts,minimax or iterative.\n");
              return false;
            }
          }
          case "aiDepth" -> {
            int val = Integer.parseInt(value);
            if (gameConfig.setAiDepth(val)) {
              feedback.append("  - AI Depth: ").append(val).append("\n");
            } else {
              getCtx().showWarn("invalid aiDepth. It must be greater than 0.\n");
              return false;
            }
          }
          case "aiTimeLimit" -> {
            int val = Integer.parseInt(value);
            if (gameConfig.setAiTimeLimit(val)) {
              feedback.append("  - AI Time Limit: ").append(val).append("s\n");
            } else {
              getCtx().showWarn("invalid aiDepth. It must be greater than 0.\n");
              return false;
            }
          }
          case "aiIterativeDeepening" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setAiIterativeDeepening(val);
            feedback.append("  - Iterative Deepening: ").append(val).append("\n");
          }
          case "aiHeuristic" -> {
            if (gameConfig.setAiHeuristic(value.toLowerCase())) {
              feedback.append("  - Heuristic: ").append(value.toLowerCase()).append("\n");
            } else {
              getCtx()
                  .showWarn("invalid aiMode. It must be centrality,mixed,ML,UCT or mobility.\n");
              return false;
            }
          }
          case "whiteIsAi" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setWhiteAi(val);
            gameConfig.setAi(true);
            feedback.append("  - White is Ai: ").append(val).append("\n");
          }
          case "blackIsAi" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setBlackAi(val);
            gameConfig.setAi(true);
            feedback.append("  - Black is Ai: ").append(val).append("\n");
          }
          default -> {
            GameLogger.error("CmdSet: Unknown parameter attempted -> " + param);
            this.getCtx().showError("Unknown parameter: " + param);
            this.getCtx().showInfo(this.getDescription());
            return false;
          }
        }
      }

      GameLogger.info("CmdSet: Configuration update successful.");
      this.getCtx().showInfo(feedback.toString());
      return true;

    } catch (NumberFormatException e) {
      GameLogger.error("CmdSet: Numerical parsing error -> " + e.getMessage());
      this.getCtx().showError("Error: Numeric value expected.");
      return false;
    }
  }

  /**
   * Factory method to create an instance of the set command with provided arguments.
   *
   * @param args CLI arguments for configuration.
   * @return A new CmdSet instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdSet(super.getCtx(), this.gameConfig, args);
  }

  /**
   * AutoCompletion for the command.
   *
   * @return {@link Completer}
   */
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return new SetCompleter(this.getOptions());
  }

  /**
   * Show a Specific help form {@link CmdSet} command ("=" required).
   *
   * @return {@link String} help message about how to use the command specifically
   */
  @Override
  public String getHelp() {
    var sb = new StringBuilder();
    sb.append("usage: ").append(this.getName());
    // add opts with format [optName=] (first line "usage:")
    for (Option opt : this.getOptions().getOptions()) {
      sb.append(" [").append(opt.getOpt()).append("=");
      if (opt.hasArg()) {
        sb.append(this.metaDataTypes.get(opt.getOpt()));
      }
      sb.append("]");
    }
    sb.append("\n\n");
    sb.append(String.format("%-30s %s%n", "Options", "Description"));
    sb.append(String.format("%-30s %s%n", "-------", "-----------"));
    // compute max width of the first column
    // (option that has the biggest length for next lines alignment)
    int maxFirstColumnWidth = 0; // formatting
    StringBuilder firstColumn = new StringBuilder();
    for (Option opt : this.getOptions().getOptions()) {
      firstColumn.append(opt.getOpt()).append("=");
      firstColumn.append(this.metaDataTypes.get(opt.getOpt()));
      if (firstColumn.length() > maxFirstColumnWidth) {
        maxFirstColumnWidth = firstColumn.length();
      }
      firstColumn = new StringBuilder();
    }
    maxFirstColumnWidth += 3; // add a bit more space before description
    // display each lines
    for (Option opt : this.getOptions().getOptions()) {
      firstColumn.append(opt.getOpt()).append("=");
      firstColumn.append(this.metaDataTypes.get(opt.getOpt()));
      sb.append(
          String.format(" %-" + maxFirstColumnWidth + "s %s%n", firstColumn, opt.getDescription()));
      firstColumn = new StringBuilder();
    }
    return sb.toString();
  }
}
