package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.LoadLocalFile;
import java.util.HashMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.reader.UserInterruptException;

/** default mode (restricted) */
public class CmdAgon extends Cmd {

  private final String desc;
  // no switch
  private final HashMap<String, Runnable> optsCorresp;

  /** all options for this specific command */
  private Options options;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx ui context in sub package
   */
  public CmdAgon(AgonShell uictx) {
    super(uictx);
    optsCorresp = new HashMap<>();
    // restricted mode (only in default shell mode "restricted")
    optsCorresp.put("h", this::showHelp);
    optsCorresp.put("q", this::quit);
    optsCorresp.put("V", this::version);
    optsCorresp.put("v", this::toggleVerbose);
    optsCorresp.put("d", this::debug);
    Option help =
        Option.builder("h")
            .longOpt("help")
            .argName("cmd")
            .desc("Show help (optionally for a specific command")
            .get();
    Option version = Option.builder("V").longOpt("version").desc("show program version").get();
    Option verbose = Option.builder("v").longOpt("verbose").desc("add more text information").get();
    Option debug = Option.builder("d").longOpt("debug").desc("show debug messages").get();
    Option quit = Option.builder("q").longOpt("quit").desc("leave the cli").get();
    this.addOption(version);
    this.addOption(verbose);
    this.addOption(debug);
    this.addOption(help);
    this.addOption(quit);
    this.desc = "main command";
  }

  /** where commands manage its options and go things with them... */
  @Override
  public void execute() {
    // special here only for shell/CLI not gui
    // because some methods are required here are not defined in
    // the higher abstraction levels, so it requires a clean cast with Java 16 improvements
    GameUserInterface ctx = this.getCtx();
    CommandLineParser parser = new DefaultParser();
    try {
      if (ctx.getTxtOptions().length == 0) { // no args
        this.getCtx().showHelp();
        return;
      }
      CommandLine cmdOpts = parser.parse(this.options, ctx.getTxtOptions());
      this.optsCorresp
          .keySet()
          .forEach(
              (optName) -> {
                if (cmdOpts.hasOption(optName)) this.optsCorresp.get(optName).run();
                /// @WARNING: if one option is included it's continuing to next options
              });
    } catch (UserInterruptException e) {
      ctx.showMessage("\nbye\n");
    } catch (ParseException e) {
      ctx.showError(e.getMessage());
      ctx.showHelp();
    } catch (Exception e) {
      ctx.showError("unexpected error occurred");
      ctx.showError(e.getMessage());
      e.printStackTrace();
      ctx.showHelp();
    }
  }

  private void quit() {
    this.getCtx().quitGame();
  }

  private void version() {
    try {
      String vFile = new LoadLocalFile("cmdsInformations/version.txt").getContent();
      this.getCtx().showMessage(vFile);
    } catch (Exception e) {
      this.getCtx().showError(e.getMessage());
    }
  }

  private void toggleVerbose() {
    if (!(this.getCtx() instanceof AgonShell ctx)) return;
    // specific to CLI/shell only
    ctx.setVerbose();
  }

  private void debug() {
    ///  TODO : do a better/stronger debug mode
    this.getCtx().showMessage("\nmove to debug mode\n");
  }

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }

  ///  ////////////////// GETTERS & SETTERS //////////////////

  @Override
  public String getName() {
    return "agon";
  }
}
