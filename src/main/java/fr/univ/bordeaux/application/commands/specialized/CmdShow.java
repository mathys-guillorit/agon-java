package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class CmdShow extends Cmd {
  private final Options options;
  private String target;
  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdShow(GameUserInterface uictx) {
    super(uictx);
    this.setDesc("Description: Displays specific information about the current game state.");
    this.setName("Show");
    this.options = new Options();
    this.options.addOption("board", null, false, "Display the Board");
    this.options.addOption("history", null, false, "Display the History");
    this.options.addOption("time", null, false, "Display the Time of both players");
    this.options.addOption("configuration", null, false, "Display the Configuration");
  }

  @Override
  public String getDescription() {
    return "Usage: show [target]\n"+"Description: Displays specific information about the current game state.\n"+
        "Available targets:\\n\");\n"+
        "  - board         : Shows the current hexagonal board state.\\n\");\n"+
        "  - history       : Shows the history of all played turns.\\n\");\n"+
        "  - time          : Shows the remaining time for each player.\n"+
        "  - configuration : Shows the current game settings.\n";
  }

  private CmdShow(GameUserInterface uictx, String target) {
    this(uictx);
    this.target = target;
  }

  @Override
  public boolean execute(MatchManager match) {
    if (match == null) {
      super.getCtx().showMessage("No active match. Create one with 'new'.");
      return false;
    }

    // On aiguille selon la cible stockée dans l'instance
    return switch (target) {
      case "board" ->showBoard(match);
      case "history" -> showHistory(match);
      case "time" -> showTime(match);
      case "configuration" -> showConfiguration(match);
      default -> false;
    };
  }

  @Override
  public CmdAction createNew(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(this.options, args);

      // Vérification : pas plus d'une option
      if (line.getOptions().length > 1) {
        super.getCtx().showMessage("Error: Please specify only one target (e.g., -board or -history).");
        return null;
      }

      // On détermine la cible
      String selectedTarget="";
      if (line.hasOption("history"))       selectedTarget = "history";
      else if (line.hasOption("time"))     selectedTarget = "time";
      else if (line.hasOption("configuration")) selectedTarget = "configuration";
      else if (line.hasOption("board"))    selectedTarget = "board";

      return new CmdShow(super.getCtx(), selectedTarget);

    } catch (ParseException e) {
      super.getCtx().showMessage("Invalid show command. Use 'show -help' for details.");
      return null;
    }
  }

  /*public void getDescription() {
    this.getCtx().showMessage("Usage: show [target]\n");
    this.getCtx()
        .showMessage("Description: Displays specific information about the current game state.\n");
    this.getCtx().showMessage("Available targets:\n");
    this.getCtx().showMessage("  - board         : Shows the current hexagonal board state.\n");
    this.getCtx().showMessage("  - history       : Shows the history of all played turns.\n");
    this.getCtx().showMessage("  - time          : Shows the remaining time for each player.\n");
    this.getCtx().showMessage("  - configuration : Shows the current game settings.\n");
  }*/

  private boolean showHistory(MatchManager match) {
    return true;
  }

  private boolean showBoard(MatchManager match){
    super.getCtx().updateBoard(match.getAgonBoard());
    return true;
  }

  private boolean showTime(MatchManager match){
    return true;
  }

  private boolean showConfiguration(MatchManager match){
    return true;
  }
}
