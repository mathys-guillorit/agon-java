package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.LoadLocalFile;
import fr.univ.bordeaux.ui.cli.OptCompleterAdapter;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.jline.reader.Completer;


public abstract class Cmd implements CmdAction {

  private Options options;
  private AbstractGameUI ctx;
  // may require a GUI delegate here for later (example : IGUIDelegate)
  // mau require a delegate here for the NETwork for later (or
  // juste create one without args in the constructor) (example: INETDelegate)
  private static String prompt = null;


  public Cmd(AbstractGameUI uictx) {
    this.ctx = uictx;
    this.options = new Options();
  }

  public AbstractGameUI getCtx() {
    return this.ctx;
  }


  public boolean requiresInput() {
    return false;
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
   * get a simple description for the helper (linked to Commons Cli)
   *
   * @return a line or more
   */
  public abstract String getDescription();

  /**
   * load display content from files directly from path concerned by commands
   *
   * @param filePath {@link String} sub file path from "desc/"
   * @return {@link String} text obtained
   */
  public String loadText(String filePath) {
    final StringBuilder result = new StringBuilder();
    final String finalPath = "cmdsInformations/desc/" + filePath;
    try {
      LoadLocalFile txt = new LoadLocalFile(finalPath);
      result.append(txt.getContent());
    } catch (IOException e) {
      result.append(e.getMessage());
      this.getCtx().showError(e.getMessage());
      /// TODO: uncomment this area and replace: "???"
      // if (???.getDebug())
      //  e.printStackTrace(); // by default
    } catch (NullPointerException e) {
      result.append("no file found");
      this.getCtx().showError(e.getMessage());
    }
    return result.toString();
  }

  public void showHelp() {
    HelpFormatter formatter = HelpFormatter.builder().get();
    try {
      formatter.printHelp(this.getName(), this.getDescription(), this.getOptions(), "", true);
    } catch (Exception e) {
      this.getCtx().showError("help not found for this command");
      this.getCtx().showError(e.getMessage());
      /// TODO: uncomment this area and replace: "???"
      // if (???.getDebug())
      //  e.printStackTrace(); // by default
    }
  }
}
