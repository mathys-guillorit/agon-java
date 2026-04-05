package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.utils.GameLogger; // Import ajouté
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jline.keymap.KeyMap;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * The AgonShell class acts as the primary Command Line Interface (CLI) manager for the Agon game.
 */
public class AgonShell implements GameUserInterface, MatchObserver {

  private AtomicBoolean running;
  private Terminal terminal;
  private LineReader reader;
  private String line;

  private final String msgHa;
  private final String msgBi;
  private final String msgBw;
  private final String msgBe;

  private String mainMenuAscii = "No default Menu set";
  private String userPrompt = "> ";
  private AgonRegister<CmdAction> cmds;
  private boolean verbose;
  private AtomicBoolean debug;

  private void init() {
    this.verbose = false;
    this.debug = new AtomicBoolean(false);
    this.running = new AtomicBoolean(true);
    this.userPrompt = this.msgHa + "> ";
    GameLogger.info("AgonShell: CLI components initialized.");
  }

  public AgonShell(Terminal term, LineReader reader, AgonRegister<CmdAction> cmds) {
    super();
    this.terminal = term;
    this.reader = reader;
    this.msgHa = this.cliLayer();
    this.msgBi = this.cliInfo();
    this.msgBw = this.cliWarn();
    this.msgBe = this.cliError();
    this.init();
    this.cmds = cmds;
    reader
        .getKeyMaps()
        .get(LineReader.MAIN)
        .bind(new Reference(LineReader.HISTORY_INCREMENTAL_SEARCH_BACKWARD), KeyMap.ctrl('R'));
    GameLogger.info("AgonShell: Terminal session started.");
  }

  public void loadMainMenu(String mainMenu) {
    this.mainMenuAscii = mainMenu;
    GameLogger.debug("AgonShell: Main menu ASCII loaded.");
  }

  public void leave() {
    GameLogger.info("AgonShell: Requesting application shutdown.");
    this.running.set(false);
    this.safeCloseTerminal();
  }

  public boolean isRunning() {
    return this.running.get();
  }

  public String getUserInput() {
    try {
      String readLine = this.reader.readLine(this.userPrompt);

      // Cas du Ctrl+D (EOF)
      if (readLine == null) {
        GameLogger.debug("AgonShell: EOF received (null input).");
        return "quit";
      }

      // --- CORRECTION : TRIM ET VÉRIFICATION ---
      line = readLine.trim();
      if (line.isEmpty()) {
        return null; // Retourne null pour les lignes vides (espaces inclus)
      }
      // ------------------------------------------

      this.reader.getHistory().add(line);
      GameLogger.debug("AgonShell: User entered command: " + line);
      return line;

    } catch (UserInterruptException e) {
      if (Thread.currentThread().isInterrupted()) {
        GameLogger.debug("AgonShell: Input interrupted by match timer.");
        Thread.interrupted(); // Nettoie le flag d'interruption
        return null;
      }
      GameLogger.info("AgonShell: User interrupted (Ctrl+C).");
      return "quit";

    } catch (org.jline.reader.EndOfFileException e) {
      return "quit";

    } catch (Exception e) {
      if (e.getMessage() != null) {
        GameLogger.error("AgonShell: Unexpected error: " + e.getMessage());
      }
      return null;
    }
  }
  public void safeCloseTerminal() {
    try {
      this.cliWln("System: Terminal session closed. Bye!");
      this.terminal.close();
      GameLogger.info("AgonShell: Terminal closed successfully.");
    } catch (IOException e) {
      GameLogger.error("AgonShell: Failed to close terminal: " + e.getMessage());
      this.showError("Failed to close terminal: " + e.getMessage());
    }
  }

  public void globalCompleter(LineReader reader, ParsedLine line, List<Candidate> candidates) {
    List<String> words = line.words();
    if (words.isEmpty() || words.getFirst().isEmpty()) {
      this.cmds.getKeys().forEach(key -> candidates.add(new Candidate(key)));
      return;
    }

    String firstWord = words.getFirst();
    Optional<CmdAction> cmdOpt = this.cmds.get(firstWord.toLowerCase());

    if (cmdOpt.isEmpty()) {
      this.cmds.getKeys().stream()
          .filter(key -> key.startsWith(firstWord.toLowerCase()))
          .forEach(key -> candidates.add(new Candidate(key)));
      return;
    }

    CmdAction cmd = cmdOpt.get();
    if (cmd.getAutoCompleter() != null) {
      cmd.getAutoCompleter().complete(reader, line, candidates);
    }
  }

  private String cliError() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.RED))
        .append("ERROR")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  private String cliLayer() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.MAGENTA))
        .append("AGON")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  private String cliInfo() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.BLUE))
        .append("INFO")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  private String cliWarn() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW))
        .append("WARNING")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  @Override
  public void showError(String msg) {
    GameLogger.error("AgonShell (UI Display): " + msg);
    this.cliW(this.msgHa + this.msgBe + " " + msg + "\n");
  }

  @Override
  public void showInfo(String msg) {
    // On ne loggue pas systématiquement en INFO ici car c'est souvent de l'affichage pur
    // pour l'utilisateur, mais on peut le mettre en DEBUG
    GameLogger.debug("AgonShell (UI Display Info): " + msg);
    this.cliW(this.msgHa + this.msgBi + " " + msg + "\n");
  }

  @Override
  public void showWarn(String msg) {
    GameLogger.debug("AgonShell (UI Display Warning): " + msg);
    this.cliW(this.msgHa + this.msgBw + " " + msg + "\n");
  }

  private void cliWln(String msg) {
    this.terminal.writer().println(msg);
    this.terminal.flush();
  }

  private void cliW(String msg) {
    this.terminal.writer().print(msg);
    this.terminal.flush();
  }

  public void showHelp() {
    GameLogger.debug("AgonShell: Displaying help menu.");
    this.cliWln(this.mainMenuAscii);
  }

  @Override
  public void quit() {
    this.leave();
  }

  @Override
  public void onMatchUpdate(ReadOnlyMatch match) {
    this.cliWln(ConsoleRenderer.getBoardRepresentation(match.getAgonBoard()));
    if (match.isMatchOver()) {
      Player winner = match.getWinner();
      String winnerInfo = (winner != null) ? winner.getColor().toString() : "UNKNOWN";
      GameLogger.info("AgonShell: Match over. Winner: " + winnerInfo);
      this.showInfo("MATCH FINISHED! Winner: " + winnerInfo);
    } else {
      String[] timers = match.getAllPlayersRemainingTime();
      if (timers != null) {
        this.showInfo("Current turn: " + match.getCurrentPlayer().getColor() + " Remaining time : White " + timers[0] + " Black " + timers[1]);
      } else {
        this.showInfo("Current turn: " + match.getCurrentPlayer().getColor());
      }
    }
  }

  @Override
  public void showMessage(String message) {
    this.cliW(message);
  }

  public AtomicBoolean getRunning() {
    return running;
  }

  public void displayHistory(List<MoveDtO> moves) {
    GameLogger.debug("AgonShell: Displaying history with " + moves.size() + " moves.");
    if (moves.isEmpty()) {
      this.showInfo("The history is currently empty.");
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[history]\n");

    for (int i = 0; i < moves.size(); i += 2) {
      MoveDtO moveO = moves.get(i);
      sb.append("O ")
          .append(moveO.from().toLowerCase())
          .append(" ")
          .append(moveO.to().toLowerCase())
          .append(";");

      if (i + 1 < moves.size()) {
        MoveDtO moveX = moves.get(i + 1);
        sb.append(" X ")
            .append(moveX.from().toLowerCase())
            .append(" ")
            .append(moveX.to().toLowerCase())
            .append(";");
      }
      sb.append("\n");
    }

    this.showMessage(sb.toString());
  }
}