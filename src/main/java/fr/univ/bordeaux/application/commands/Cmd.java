package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.LoadLocalFile;
import fr.univ.bordeaux.ui.cli.OptCompleterAdapter;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.jline.reader.Completer;

/**
 * represent the fixed code for all different Commands
 * each command knows his options only <br> (to make easier auto-complete)
 */
public abstract class Cmd implements CmdAction {

  private Options options;
  private GameUserInterface ui;
  private static String prompt = null;

  /**
   * description from sub commands
   */
  private String desc;

  private String name;

  public Cmd(GameUserInterface ui) {
    this.ui = ui;
    this.options = new Options();
    this.desc = "Description: default Command";
    this.name = "cmd";
  }

  public GameUserInterface getCtx() {
    return this.ui;
  }

  /**
   * auto-completer for commands
   *
   * <pre>
   * - split and transform Options (given by context) from Commons-cli to JLine format
   * - simple version
   * </pre>
   *
   * @return (by JLine) with tab, matching command
   */
  @Nonnull
  public Completer getAutoCompleter() {

    return new OptCompleterAdapter(this.options).getCompleter(this.getName());
  }

  public void addOption(Option option) {
    this.options.addOption(option);
  }

  public Options getOptions() {
    return this.options;
  }

  /**
   * load display content from files directly from path concerned by commands
   *
   * @param filePath {@link String} sub file path from "desc/"
   * @return {@link String} text obtained
   */
  @Nullable
  public String loadText(String filePath) throws IOException, NullPointerException {
    final String finalPath = "/cmdsInformations/desc/" + filePath;
    LoadLocalFile txt = new LoadLocalFile(finalPath);
    return txt.getContent();
  }

 /* public void getDescription() {
    HelpFormatter formatter = HelpFormatter.builder().get();
    try {
      formatter.printHelp(this.getName(), this.getDescription(), this.getOptions(), "", true);
    } catch (Exception e) {
      this.getCtx().showError("help not found for this command");
      this.getCtx().showError(e.getMessage());
      /// TODO: uncomment this area and replace: "???"
      // if (???.getDebug()) // when debug mode available to get on a unknown object
      //  e.printStackTrace(); // by default
    }
  }*/

  /**
   * get a simple description for the helper (linked to Commons Cli)
   *
   * @return a line or more
   */
  /*public String getDescription() {
    /// TODO: add i18n later here (or in constructor)
    return this.desc;
  }*/

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
