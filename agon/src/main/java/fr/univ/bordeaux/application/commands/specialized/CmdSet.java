package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;
import java.io.IOException;
import java.util.HashMap;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.jline.reader.Completer;
import org.jline.reader.UserInterruptException;

public class CmdSet extends Cmd {

  /** execute commands with its name */
  private HashMap<String, Runnable> optsCorresp;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx {@link AbstractGameUI} UI item to pass
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
    this.setDesc("change current configuration, example: \"debug=true\"");
    this.setName("set");
  }

  private void debug() {
    ///  TODO : do a better/stronger debug mode
    this.getCtx().showMessage("\nmove to debug mode\n");
  }

  private void setVerbose() {
    this.getCtx().showMessage("\nset verbose mode\nto: ");
    this.getCtx().setVerbose(true);
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    try {
      return new SetCompleter(this.getOptions());
    } catch (UserInterruptException e) {
      // need to see this type of error to treat it cleanly
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      ///  TODO : improve error capturing
      this.getCtx().showError("missing \"longOpt\" in one or more of Option returned from: ");
      this.getCtx().showError("CmdSet().getOptions(), constructor must fill all values");
    }
    /// TODO: require improvements here (we must not go in final return)
    // return the bad Completer by default
    return super.getAutoCompleter();
  }

  @Override
  public void showHelp() {
    final String filepath = "cmdSet.txt";
    try {
      this.getCtx().showMessage(this.loadText(filepath));
    } catch (IOException e) {
      this.getCtx()
          .showError(
              "help for this command is not available (file broken or other problem related to it)");
      /// TODO: uncomment this area and replace: "???"
      // if (???.getDebug())
      //  e.printStackTrace(); // by default
    } catch (NullPointerException e) {
      this.getCtx().showError("help for this command is not available (file missing)");
    }
  }

  @Override
  public void execute() {
    GameUserInterface ctx = this.getCtx();
    CommandLineParser parser = new DefaultParser();
    try {
      final var tmp = new UIPromptParser(ctx.getUserPrompt());
      if (tmp.getUserOptions().length == 0) { // no args
        this.getCtx().showHelp();
        return;
      }
      CommandLine cmdOpts = parser.parse(this.getOptions(), tmp.getTxtOptions());
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
