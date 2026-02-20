package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import org.apache.commons.cli.Option;

public class CmdShow extends Cmd {

  private String desc;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdShow(AbstractGameUI uictx) {
    super(uictx);
    this.addOption(Option.builder().longOpt("board").desc("Show the current board state").get());
    this.addOption(Option.builder().longOpt("history").desc("Show game turns history").get());
    this.addOption(Option.builder().longOpt("time").desc("Show time left for each player").get());
    this.addOption(Option.builder().longOpt("configuration").desc("Explicit configuration").get());
    this.desc = "display various information";
  }

  @Override
  public String getName() {
    return "show";
  }

  @Override
  public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }

  @Override
  public void execute() {}
}
