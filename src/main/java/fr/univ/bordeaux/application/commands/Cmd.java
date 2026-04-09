package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.technical.utils.LoadLocalFile;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.OptCompleterAdapter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

/**
 * Abstract base class for all game commands.
 *
 * <p>This class provides common functionality for command management, including CLI options
 * handling, UI context access, and automatic JLine completer generation. Each sub-command is
 * responsible for defining its own logic, name, and options.
 *
 * @author fr.univ.bordeaux
 * @version 1.0
 */
public abstract class Cmd implements CmdAction {

  /** The CLI options associated specifically with this command. */
  private Options options;

  /** The user interface context for command interaction and output. */
  private GameUserInterface ui;

  /** A brief text description of the command's purpose and usage. */
  private String desc;

  /** The unique trigger name of the command (e.g., "new", "help"). */
  private String name;

  /**
   * Constructs a new command with a reference to the UI context. Initializes default values for
   * name, options, and description.
   *
   * @param ui The {@link GameUserInterface} context.
   */
  public Cmd(GameUserInterface ui) {
    this.ui = ui;
    this.options = new Options();
    this.desc = "Description: default Command";
    this.name = "cmd";
  }

  /**
   * Provides access to the current UI context. * @return The {@link GameUserInterface} instance.
   */
  public GameUserInterface getCtx() {
    return this.ui;
  }

  /**
   * Generates a JLine {@link Completer} for this command.
   *
   * <p>This implementation uses an {@link OptCompleterAdapter} to bridge Commons-CLI options with
   * the JLine completion system.
   *
   * @return A non-null {@link Completer} adapted to the command's options.
   */
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return new OptCompleterAdapter(this.options).getCompleter(this.getName());
  }

  /**
   * Adds a new CLI option to the command.
   *
   * @param option The {@link Option} to register.
   */
  public void addOption(Option option) {
    this.options.addOption(option);
  }

  /**
   * Retrieves all CLI options defined for this command.
   *
   * @return The {@link Options} container.
   */
  @Override
  public Options getOptions() {
    return this.options;
  }

  /**
   * Loads text content from a local file located in the command information directory. Useful for
   * loading long descriptions or ASCII art.
   *
   * @param filePath The sub-path under "/cmdsInformations/desc/".
   * @return The string content of the file, or {@code null} if loading fails.
   * @throws IOException If the file cannot be read.
   * @throws NullPointerException If the file path is invalid.
   */
  @Nullable
  public String loadText(String filePath) throws IOException, NullPointerException {
    final String finalPath = "/cmdsInformations/desc/" + filePath;
    LoadLocalFile txt = new LoadLocalFile(finalPath);
    return txt.getContent();
  }

  /**
   * Returns the command's description.
   *
   * <p>This string is typically used by the help command to inform the user.
   *
   * @return The description string.
   */
  @Override
  public String getDescription() {
    return this.desc;
  }

  /**
   * Updates the command's description.
   *
   * @param desc The new description string.
   */
  protected final void setDesc(String desc) {
    this.desc = desc;
  }

  /**
   * Updates the command's trigger name.
   *
   * @param name The new name string.
   */
  protected final void setName(String name) {
    this.name = name;
  }

  /**
   * Retrieves the command's trigger name.
   *
   * @return The name string (e.g., "load", "quit").
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Show Help information about how to use the command (detailed).
   *
   * @see <a href="https://jline.org/docs/architecture/">jline.org/docs/architecture </a>
   */
  @Override
  public String getHelp() {
    // create a flow to catch the output
    var baos = new ByteArrayOutputStream();
    PrintStream psOriginalOut = System.out;
    try {
      // put System.out to our flow
      System.setOut(new PrintStream(baos));
      HelpFormatter formatter = HelpFormatter.builder().get();
      formatter.printHelp(this.getName(), "", this.getOptions(), "", true);
      return baos.toString().trim(); // get String from flow
    } finally {
      // restore original output
      System.setOut(psOriginalOut);
    }
  }
}
