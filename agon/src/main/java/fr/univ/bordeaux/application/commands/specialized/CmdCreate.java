package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import java.io.IOException;
import org.apache.commons.cli.Option;

/** Create a new game. Command representation in cli : "new [ARGS]" */
public class CmdCreate extends Cmd {

  private String desc;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx context
   */
  public CmdCreate(AbstractGameUI uictx) {
    super(uictx);
    Option aiAndColor =
        Option.builder("a")
            .longOpt("ai")
            .hasArg()
            .argName("COLOR")
            .desc("Specify the AI color")
            .required(false) // optional arg
            .get();
    Option invitPlayer = Option.builder("PLAYER_ID").desc("Specify the player ID").get();
    this.addOption(invitPlayer);
    this.addOption(aiAndColor);
    final String filename = "CmdCreate.txt";
    try {
      this.desc = this.loadText(filename);
    } catch (IOException e) {
      this.desc = "description cannot be loaded for more info type: \"set debug=true\"";
      // if (someobjet.getDebug())
      // this.getCtx().showError("problem when reading description file: \""+filename+"\" for
      // loading description");
    } catch (NullPointerException e) {
      this.desc = "description cannot be loaded for more info: \"set debug=true\"";
      // if (someobjet.getDebug())
      // this.getCtx().showError("missing file at: \""+filename+"\" for loading description");
    }
  }

  @Override
  public String getName() {
    return "new";
  }

  @Override
  public void execute() {}

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }
}
