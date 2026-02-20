package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import org.apache.commons.cli.Option;

public class CmdSet extends Cmd {

  private final String desc;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdSet(AbstractGameUI uictx) {
    super(uictx);
    this.addOption(
        Option.builder()
            .longOpt("key")
            .hasArg()
            .argName("value")
            .desc("Set an arbitrary key to a value, syntax: key=value")
            .get());
    this.desc = "change current configuration, example: \"debug=true\"";
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
  public void execute() {}
}
