package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdCreate;
import fr.univ.bordeaux.application.commands.specialized.CmdHelp;
import fr.univ.bordeaux.application.commands.specialized.CmdHint;
import fr.univ.bordeaux.application.commands.specialized.CmdLoad;
import fr.univ.bordeaux.application.commands.specialized.CmdPause;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
import fr.univ.bordeaux.application.commands.specialized.CmdRedo;
import fr.univ.bordeaux.application.commands.specialized.CmdSave;
import fr.univ.bordeaux.application.commands.specialized.CmdSet;
import fr.univ.bordeaux.application.commands.specialized.CmdShow;
import fr.univ.bordeaux.application.commands.specialized.CmdUndo;
import fr.univ.bordeaux.application.match.ContestMatch;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.technical.io.config.ConfigBinder;
import fr.univ.bordeaux.technical.io.config.ConfigParser;
import fr.univ.bordeaux.technical.io.config.ConfigSerializer;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.LoadLocalFile;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * The GameLauncher class is the entry point for the Agon application.
 *
 * <p>It acts as the bootstrapper that handles:
 *
 * <ul>
 *   <li>Defining valid command-line options (CLI).
 *   <li>Loading persistent settings from the local configuration file (.agonrc).
 *   <li>Parsing arguments provided by the user at startup.
 *   <li>Resolving conflicts between file loading and manual options.
 *   <li>Initializing the appropriate User Interface (CLI or GUI).
 * </ul>
 *
 * @version 1.0
 * @see GameConfig
 */
public class GameLauncher {
  /** The definitions of all allowed command-line options. */
  private final Options options;

  /** The path to the persistent configuration file, located in the user's working directory. */
  private final String configPath = System.getProperty("user.dir") + File.separator + ".agonrc";

  /**
   * Constructs a new GameLauncher.
   *
   * <p>Initializes the option definitions immediately upon creation.
   */
  public GameLauncher() {
    this.options = new Options();
    this.setupOptions();
  }

  /**
   * Configures the available command-line options.
   *
   * <p>This method defines flags (like -h, -v). It uses {@link Option.Builder} for complex options
   * to ensure clarity.
   */
  private void setupOptions() {
    options.addOption("h", "help", false, "Displays this help message.");
    options.addOption("V", "version", false, "Displays version information");
    options.addOption("v", "verbose", false, "Enables verbose output.");
    options.addOption("d", "debug", false, "Enables debug mode.");
    options.addOption("g", "gui", false, "Starts the graphical interface.");
    options.addOption("b", "blitz", false, "Starts the game in Blitz mode.");
    options.addOption("t", "time", true, "Sets the time limit for each player (in minutes).");
    options.addOption("a", "ai", true, "Replace the given color by an Ai. Can be both using A for color.");
    options.addOption(
        "c", "contest", false, "Launches contest mode (reads file and outputs move).");
  }

  /**
   * Main launch sequence that processes arguments, updates configuration, and starts the game.
   *
   * <p>This method parses the raw arguments. If informational flags (-h, -V) are present, it
   * displays the info and returns. Otherwise, it updates the {@link GameConfig} and proceeds to
   * start the game engine. If an invalid argument is provided, it catches the {@link
   * ParseException} and displays the help menu.
   *
   * @param args The raw command-line arguments passed at startup.
   */
  public void launch(String[] args) {
    GameConfig config = loadInitialConfig();

    // Initialize Logger with default config
    fr.univ.bordeaux.technical.utils.GameLogger.getInstance().setDebugMode(config.isDebug());

    CommandLineParser parser = new DefaultParser();
    AgonRegister<CmdAction> cmds = new AgonRegister<>();
    try {
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        this.fillRegister(cmds, null, null, null);
        printHelp(cmds);
        return;
      }
      if (cmd.hasOption("V")) {
        printVersion();
        return;
      }

      String[] fileArg = cmd.getArgs();
      String filePath = null;
      if (fileArg.length > 0) {
        filePath = fileArg[0];
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
          fr.univ.bordeaux.technical.utils.GameLogger.error(
              "The file '" + filePath + "' does not exist or is a directory.");
          return;
        }
        if (cmd.hasOption("c")) {
          fr.univ.bordeaux.technical.utils.GameLogger.info("Contest mode detected.");
          try {
            ContestMatch.executeContest(fileArg[0]);
          } catch (Exception e) {
            fr.univ.bordeaux.technical.utils.GameLogger.error("Contest mode failed : " + e.getMessage());
          }
          return;
        }
        fr.univ.bordeaux.technical.utils.GameLogger.info("File argument detected: " + filePath);

      } else if (cmd.hasOption("c")) {
        fr.univ.bordeaux.technical.utils.GameLogger.error("Contest mode requires a file argument.");
        this.fillRegister(cmds, null, null, null);
        printHelp(cmds);
        return;
      }
      startGame(config, cmd, cmds, filePath);
    } catch (ParseException e) {
      fr.univ.bordeaux.technical.utils.GameLogger.error("Argument Error : " + e.getMessage());
      printHelp(cmds);
    }
  }

  /**
   * Loads the initial configuration from the .agonrc file.
   *
   * <p>If the file is missing or unreadable, a default configuration file is created and a default
   * GameConfig object is returned.
   *
   * @return A {@link GameConfig} object populated with file settings or default values.
   */
  private GameConfig loadInitialConfig() {
    ConfigParser configParser = new ConfigParser();
    try {
      return configParser.parse(configPath);
    } catch (IOException e) {
      fr.univ.bordeaux.technical.utils.GameLogger.info("No config file found. Creating a default file...");
      createDefaultConfigFile();
      return new GameConfig();
    }
  }

  /**
   * Persists a default configuration to the disk using the Serializer.
   *
   * <p>This ensures the user has a base template to modify for future runs.
   */
  private void createDefaultConfigFile() {
    ConfigSerializer serializer = new ConfigSerializer();
    try {
      serializer.createDefault(configPath);
      fr.univ.bordeaux.technical.utils.GameLogger.info("Minimal configuration file created at: " + configPath);
    } catch (IOException e) {
      fr.univ.bordeaux.technical.utils.GameLogger.error("Failed to save default config: " + e.getMessage());
    }
  }

  /**
   * Initializes the application layers and starts the selected user interface.
   *
   * <p>Chooses between the CLI ({@link fr.univ.bordeaux.ui.cli.AgonShell}) and the GUI ({@link
   * fr.univ.bordeaux.ui.gui}) based on the provided command-line options.
   *
   * @param config The final configuration to be used by the UI and the engine.
   * @param cmd The parsed command line, used to check for the GUI flag (-g).
   * @param filePathToLoad The path to the save file to load automatically, or null if none.
   */
  private void startGame(
      GameConfig config, CommandLine cmd, AgonRegister<CmdAction> cmds, String filePathToLoad) {
    fr.univ.bordeaux.technical.utils.GameLogger.info("Starting Agon Shell...");
    AgonShell userInterface;
    if (cmd.hasOption("g")) {
      // AgonGUI agon = new  AgonGUI(config);
    } else {
      try {
        final AgonShell[] shellRef = new AgonShell[1];

        Completer strategyCompleter =
            (reader, line, candidates) -> {
              if (shellRef[0] != null) {
                shellRef[0].globalCompleter(reader, line, candidates);
              }
            };
        Terminal terminal = TerminalBuilder.builder().dumb(true).build();
        LineReader reader =
            LineReaderBuilder.builder().terminal(terminal).completer(strategyCompleter).build();

        userInterface = new AgonShell(terminal, reader, cmds);
        shellRef[0] = userInterface;
        GameEngine gameEngine = new GameEngine(userInterface, cmds);
        this.fillRegister(cmds, userInterface, config, gameEngine);
        // Bind CLI options to config
        ConfigBinder.bindOptionsToConfig(cmd, config, userInterface);

        if (config.isBlitzMode()) {
          CmdCreate create = new CmdCreate(userInterface, config, gameEngine);
          create.execute(null);
        }

        if (filePathToLoad != null) {
          CmdAction loadcmd=cmds.get("load").get().createNew(new String[]{filePathToLoad});
          loadcmd.execute(null);
        }

        gameEngine.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void fillRegister(
      AgonRegister<CmdAction> cmds, GameUserInterface ui, GameConfig config, GameEngine engine) {
    cmds.register("new", new CmdCreate(ui, config, engine));
    cmds.register("quit", new CmdQuit(ui));
    cmds.register("hint", new CmdHint(ui));
    cmds.register("show", new CmdShow(ui, config));
    cmds.register("load", new CmdLoad(ui, engine));
    cmds.register("save", new CmdSave(ui));
    cmds.register("set", new CmdSet(ui, config));
    cmds.register("undo", new CmdUndo(ui));
    cmds.register("redo", new CmdRedo(ui));
    cmds.register("help", new CmdHelp(ui, cmds));
    cmds.register("pause", new CmdPause(ui));
  }

  /**
   * Prints the CLI help message.
   *
   * <p>Attempts to read a custom "helpGameLauncher.txt" file. If not found, falls back to the
   * standard Apache CLI formatter.
   */
  private void printHelp(AgonRegister<CmdAction> cmds) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("agon [OPTIONS]", options);
    System.out.println("\nAVAILABLE COMMAND IN CLI MODE :");
    cmds.getKeys()
        .forEach(
            name -> {
              cmds.get(name)
                  .ifPresent(
                      cmd -> {
                        System.out.println(String.format("  %-12s : %s", name, cmd.getDescription()));
                      });
            });
    System.out.println(
        "\nHow to move your pieces : \n\n[letter1][col1][letter2][col2] to move your piece from letter1-col1 to letter2-col2\n"
            + "if you have a piece to relocate you have to enter the tile where you want to put it [letter][col]\n"
            + "Exemples: a1a2, f5g6 and for relocation a1, f10\n");
    ;
  }

  /**
   * Prints the current application version and credits.
   *
   * <p>Reads from the "version.txt" file or prints a hardcoded fallback string.
   */
  private void printVersion() {
    try {
      System.out.println(getVersionContent());
    } catch (IOException e) {
      System.err.println("[WARNING] version.txt not found.");
    }
  }

  /**
   * Retrieves the content of the help information file from the local resources.
   *
   * <p>This method is marked as {@code protected} to allow for "Extract and Override" in unit
   * tests, enabling the simulation of {@link IOException} without manipulating physical files.
   *
   * @return The raw String content of the help file.
   * @throws IOException If the file is missing or cannot be accessed.
   */
  protected String getHelpContent() throws IOException {
    return new LoadLocalFile("/cmdsInformations/helpGameLauncher.txt").getContent();
  }

  /**
   * Retrieves the application version and credits from the local resources.
   *
   * <p>This method is marked as {@code protected} to allow for "Extract and Override" in unit
   * tests, enabling the simulation of {@link IOException} to verify the application's fallback
   * behavior.
   *
   * @return The raw String content of the version file.
   * @throws IOException If the file is missing or cannot be accessed.
   */
  protected String getVersionContent() throws IOException {
    return new LoadLocalFile("/cmdsInformations/version.txt").getContent();
  }
}
