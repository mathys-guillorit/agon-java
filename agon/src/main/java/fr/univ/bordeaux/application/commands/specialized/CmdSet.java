package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.specialized.options.OptBooleanSpec;
import fr.univ.bordeaux.application.commands.specialized.options.OptValueSpec;
import fr.univ.bordeaux.application.commands.specialized.options.ValueSuggester;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.*;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.UserInterruptException;

import javax.annotation.Nonnull;

public class CmdSet extends Cmd {

  private final String desc;
  /** execute commands with its name */
  private HashMap<String, Runnable> optsCorresp;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdSet(AbstractGameUI uictx) {
    super(uictx);
    this.optsCorresp = new HashMap<>();
    optsCorresp.put("v", this::setVerbose);
    optsCorresp.put("d", this::debug);
    Option verbose = Option.builder("v").longOpt("verbose").desc("add more text information").get();
    Option debug = Option.builder("d").longOpt("debug").desc("show debug messages").get();
    this.addOption(verbose);
    this.addOption(debug);
    this.desc = "change current configuration, example: \"debug=true\"";
  }

  private void debug() {
    ///  TODO : do a better/stronger debug mode
    this.getCtx().showMessage("\nmove to debug mode\n");
  }

  private void setVerbose() {
    this.getCtx().setVerbose(true);
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    try {
      return new SetCompleter(this.getOptions());
    } catch (UserInterruptException e) {
      ///  TODO : improve error capturing
      this.getCtx().showError("missing \"longOpt\" in one or more of Option returned from: ");
      this.getCtx().showError("CmdSet().getOptions(), constructor must fill all values");
    }
    return super.getAutoCompleter();
  }

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return desc;
  }

  @Override
  public String getName() {
    return "set";
  }

  @Override
  public void execute() {
    GameUserInterface ctx = this.getCtx();
    CommandLineParser parser = new DefaultParser();
    try {
      if (ctx.getTxtOptions().length == 0) { // no args
        this.getCtx().showHelp();
        return;
      }
      CommandLine cmdOpts = parser.parse(this.getOptions(), ctx.getTxtOptions());
      this.optsCorresp
          .keySet()
          .forEach(
              (optName) -> {
                if (cmdOpts.hasOption(optName)) this.optsCorresp.get(optName).run();
                /// @WARNING: if one option is included it's continuing to next options
              });
    } catch (UserInterruptException ignored) {
      System.out.println("user typed ctrl+c or something");
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
}
