package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.network.*;
import fr.univ.bordeaux.application.commands.specialized.*;
import fr.univ.bordeaux.application.match.ContestMatch;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.io.config.ConfigBinder;
import fr.univ.bordeaux.technical.io.config.ConfigParser;
import fr.univ.bordeaux.technical.io.config.ConfigSerializer;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.technical.utils.LoadLocalFile;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.gui.AgonGui;
import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * The GameLauncher class is the entry point for the Agon application.
 * It handles command-line arguments, initializes the configuration,
 * and bootstraps either the CLI or GUI environment.
 */
public class GameLauncher {

  /** Command-line options defined for the application launch. */
  private final Options options;

  /** Path to the local configuration file (.agonrc). */
  private final String configPath = System.getProperty("user.dir") + File.separator + ".agonrc";

  /**
   * Constructs a new GameLauncher and initializes supported command-line options.
   */
  public GameLauncher() {
    this.options = new Options();
    this.setupOptions();
  }

  /**
   * Configures the available command-line options using Apache Commons CLI.
   * Includes help, version, verbose, debug, gui, and contest modes.
   */
  private void setupOptions() {
    options.addOption("h", "help", false, "Displays this help message.");
    options.addOption("V", "version", false, "Displays version information");
    options.addOption("v", "verbose", false, "Enables verbose output.");
    options.addOption("d", "debug", false, "Enables debug mode.");
    options.addOption("g", "gui", false, "Starts the graphical interface.");
    options.addOption(
        "c", "contest", false, "Launches contest mode (reads file and outputs move).");
    ConfigBinder.fillOptions(options);
  }

  /**
   * Main entry point for launching the application logic.
   * @param args Array of command-line arguments provided at startup.
   */
  public void launch(final String... args) {
    final AgonRegister<CmdAction> cmds = new AgonRegister<>();
    final CommandLineParser parser = new DefaultParser();

    try {
      final CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("h") || cmd.hasOption("V")) {
        handleInfoOptions(cmd, cmds);
      } else {
        setupAndStartGame(cmd, cmds);
      }
    } catch (ParseException e) {
      GameLogger.error("Argument Error : " + e.getMessage());
      printHelp(cmds);
    }
  }

  /**
   * Handles information-only requests such as displaying help or version info.
   * @param cmd  The parsed command line.
   * @param cmds The registry of commands.
   */
  private void handleInfoOptions(final CommandLine cmd, final AgonRegister<CmdAction> cmds) {
    if (cmd.hasOption("h")) {
      this.fillRegister(cmds, null, null, null, new AppContext(new LocalProfile("Temp")));
      printHelp(cmds);
    } else if (cmd.hasOption("V")) {
      printVersion();
    }
  }

  /**
   * Initializes game configuration and context before starting the appropriate interface.
   * @param cmd  The parsed command line.
   * @param cmds The registry of commands.
   */
  private void setupAndStartGame(final CommandLine cmd, final AgonRegister<CmdAction> cmds) {
    final GameConfig config = loadInitialConfig();
    applyConfigOptions(cmd, config);

    final AppContext context = createAppContext(cmd);

    String filePathToLoad = null;
    if (cmd.getArgs().length > 0) {
      filePathToLoad = cmd.getArgs()[0];
    }

    if (processArgumentsAndContest(cmd, cmds, context, filePathToLoad)) {
      startGame(config, cmd, cmds, filePathToLoad, context);
    }
  }

  /**
   * Updates the game configuration based on verbose and debug flags.
   * @param cmd    The parsed command line.
   * @param config The game configuration to update.
   */
  private void applyConfigOptions(final CommandLine cmd, final GameConfig config) {
    if (cmd.hasOption("v")) {
      config.setVerbose(true);
      GameLogger.info("[INFO] Verbose mode enabled.");
    }
    if (cmd.hasOption("d")) {
      config.setDebug(true);
      GameLogger.info("[DEBUG] Debug mode enabled.");
    }
  }

  /**
   * Creates the application context, determining the player name and execution mode.
   * @param cmd The parsed command line.
   * @return The initialized {@link AppContext}.
   */
  private AppContext createAppContext(final CommandLine cmd) {
    String playerName = System.getProperty("user.name");
    if (playerName == null || playerName.isBlank()) {
      playerName = "Player";
    }

    final AppContext context = new AppContext(new LocalProfile(playerName));
    context.setMode(AppMode.LOCAL);
    return context;
  }

  /**
   * Processes file arguments and handles the specialized contest mode.
   * @param cmd      The parsed command line.
   * @param cmds     The command registry.
   * @param context  The application context.
   * @param filePath The path of the file to load, if provided.
   * @return true if the game should proceed to start, false if it should stop (e.g., contest finished).
   */
  private boolean processArgumentsAndContest(
      final CommandLine cmd,
      final AgonRegister<CmdAction> cmds,
      final AppContext context,
      final String filePath) {

    if (filePath == null) {
      if (cmd.hasOption("c")) {
        GameLogger.error("[ERROR] Contest mode requires a file argument.");
        this.fillRegister(cmds, null, null, null, context);
        printHelp(cmds);
        return false;
      }
    } else {
      final File file = new File(filePath);

      if (!file.exists() || file.isDirectory()) {
        GameLogger.error("[ERROR] The file '" + filePath + "' does not exist or is a directory.");
        return false;
      } else if (cmd.hasOption("c")) {
        GameLogger.info("[INFO] Contest mode detected.");
        try {
          ContestMatch.executeContest(filePath);
        } catch (Exception e) {
          GameLogger.error("[ERROR] Contest mode failed : " + e.getMessage());
        }
        return false;
      } else {
        GameLogger.info("[INFO] File argument detected: " + filePath);
      }
    }

    return true;
  }

  /**
   * Loads the configuration from the .agonrc file or creates a default one if missing.
   * @return The loaded {@link GameConfig}.
   */
  private GameConfig loadInitialConfig() {
    final ConfigParser configParser = new ConfigParser();
    GameConfig config;
    try {
      config = configParser.parse(configPath);
    } catch (IOException e) {
      System.out.println("No config file found. Creating a default file...");
      createDefaultConfigFile();
      config = new GameConfig();
    }
    return config;
  }

  /**
   * Generates a default configuration file on the local disk.
   */
  private void createDefaultConfigFile() {
    final ConfigSerializer serializer = new ConfigSerializer();
    try {
      serializer.createDefault(configPath);
      GameLogger.info("[INFO] Minimal configuration file created at: " + configPath);
    } catch (IOException e) {
      GameLogger.error("[ERROR] Failed to save default config: " + e.getMessage());
    }
  }

  /**
   * Prompts the user via terminal to enter their player name.
   * @return The non-empty player name.
   */
  protected String askPlayerName(LineReader reader) {
    final String prompt = "\u001B[35m[AGON]\u001B[0m> ";
    String name = reader.readLine(prompt + "Enter your player name: ").trim();
    while (name.isEmpty()) {
      name = reader.readLine(prompt + "Name cannot be empty. Enter your player name: ").trim();
    }
    return name;
  }

  /**
   * Prompts the user to choose between Local and Online application modes.
   * @return The selected {@link AppMode}.
   */
  protected AppMode askApplicationMode(LineReader reader) {
    final String prompt = "\u001B[35m[AGON]\u001B[0m> ";
    reader.getTerminal().writer().println(prompt + "Select mode:");
    reader.getTerminal().writer().println("1 - Local");
    reader.getTerminal().writer().println("2 - Online");
    reader.getTerminal().writer().flush();

    String input = reader.readLine(prompt + "Your choice: ").trim();
    while (!"1".equals(input) && !"2".equals(input)) {
      input = reader.readLine(prompt + "Invalid choice. Enter 1 (Local) or 2 (Online): ").trim();
    }
    return "2".equals(input) ? AppMode.ONLINE : AppMode.LOCAL;
  }

  /**
   * Starts either the GUI or the CLI based on startup arguments.
   * @param config         The game configuration.
   * @param cmd            The parsed command line.
   * @param cmds           The command registry.
   * @param filePathToLoad The file to load at startup, if any.
   * @param context        The application context.
   */
  protected void startGame(
      final GameConfig config,
      final CommandLine cmd,
      final AgonRegister<CmdAction> cmds,
      final String filePathToLoad,
      final AppContext context) {

    if (cmd.hasOption("g")) {
      System.out.println("Starting Agon GUI...");
      launchGUI(config, cmd, cmds, filePathToLoad, context);
    } else {
      System.out.println("Starting Agon Shell...");
      launchCLI(config, cmd, cmds, filePathToLoad, context);
    }
  }

  /**
   * Initializes and starts the JavaFX Graphical User Interface.
   * @param config  The game configuration.
   * @param cmds    The command registry.
   * @param cmd     The parsed command line.
   * @param context The application context.
   */
  protected void launchGUI(
      GameConfig config,
      CommandLine cmd,
      AgonRegister<CmdAction> cmds,
      String filePathToLoad,
      AppContext context) {
    final AgonGui gui = new AgonGui(config, context);

    try {
      ConfigBinder.bindOptionsToConfig(cmd, config, gui);
    } catch (Exception e) {
      GameLogger.error("[ERROR] Failed to bind config to GUI: " + e.getMessage());
    }

    final GameEngine gameEngine = new GameEngine(gui, cmds);
    context.setGameEngine(gameEngine);
    gameEngine.setAppContext(context);
    this.fillRegister(cmds, gui, config, gameEngine, context);

    new Thread(
            () -> {
              boolean fxReady = false;
              while (!fxReady) {
                try {
                  Platform.runLater(() -> {});
                  fxReady = true;
                } catch (IllegalStateException e) {
                  try {
                    Thread.sleep(800);
                  } catch (InterruptedException ignored) {
                  }
                }
              }

              Platform.runLater(
                  () -> {
                    if (config.isBlitzMode()) {
                      CmdCreate create = new CmdCreate(gui, config, gameEngine);
                      create.execute(null);
                    }

                    if (filePathToLoad != null && !cmd.hasOption("c")) {
                      cmds.get("load")
                          .ifPresent(l -> l.createNew(new String[] {filePathToLoad}).execute(null));
                    }
                  });
            })
        .start();

    gui.start();
    gameEngine.start();
  }

  /**
   * Initializes and starts the interactive Command Line Interface using JLine.
   * @param config         The game configuration.
   * @param cmd            The parsed command line.
   * @param cmds           The command registry.
   * @param filePathToLoad The file to load at startup, if any.
   * @param context        The application context.
   */
  protected void launchCLI(
      GameConfig config,
      CommandLine cmd,
      AgonRegister<CmdAction> cmds,
      String filePathToLoad,
      AppContext context) {
    try {
      final AgonShell[] shellRef = new AgonShell[1];

      final Completer strategyCompleter =
          (reader, line, candidates) -> {
            if (shellRef[0] != null) {
              shellRef[0].globalCompleter(reader, line, candidates);
            }
          };

      final Terminal terminal = TerminalBuilder.builder().dumb(true).build();
      final LineReader reader =
          LineReaderBuilder.builder().terminal(terminal).completer(strategyCompleter).build();

      final AgonShell userInterface = new AgonShell(terminal, reader, cmds);
      shellRef[0] = userInterface;

      final GameEngine gameEngine = new GameEngine(userInterface, cmds);
      context.setGameEngine(gameEngine);
      gameEngine.setAppContext(context);

      this.fillRegister(cmds, userInterface, config, gameEngine, context);

      ConfigBinder.bindOptionsToConfig(cmd, config, userInterface);

      if (config.isBlitzMode()) {
        CmdCreate create = new CmdCreate(userInterface, config, gameEngine);
        create.execute(null);
      }

      if (filePathToLoad != null && !cmd.hasOption("c")) {
        CmdAction loadcmd = cmds.get("load").get().createNew(new String[] {filePathToLoad});
        loadcmd.execute(null);
      }

      final boolean isTestEnv = "true".equals(System.getProperty("IS_TEST_ENV"));
      if (!isTestEnv && !cmd.hasOption("c")) {
        userInterface.initializeSession(context);
      }

      gameEngine.start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Populates the command register with all available game and network commands.
   *  @param cmds          The registry to populate.
   * @param userInterface The active user interface (CLI or GUI).
   * @param config        The game configuration.
   * @param engine        The game engine.
   * @param context       The application context.
   */
  private void fillRegister(
      final AgonRegister<CmdAction> cmds,
      final GameUserInterface userInterface,
      final GameConfig config,
      final GameEngine engine,
      final AppContext context) {

    cmds.register("new", new CmdNew(userInterface, context, config, engine));
    cmds.register("hint", new CmdHint(userInterface));
    cmds.register("show", new CmdShow(userInterface, config));
    cmds.register("load", new CmdLoad(userInterface, engine));
    cmds.register("save", new CmdSave(userInterface));
    cmds.register("set", new CmdSet(userInterface, config));
    cmds.register("undo", new CmdUndo(userInterface));
    cmds.register("redo", new CmdRedo(userInterface));
    cmds.register("pause", new CmdPause(userInterface));
    cmds.register("help", new CmdHelp(userInterface, cmds));

    cmds.register("join", new CmdJoin(userInterface, context));
    cmds.register("ping", new CmdPing(userInterface, context));
    cmds.register("server_start", new CmdServerStart(userInterface, context));
    cmds.register("server_stop", new CmdServerStop(userInterface, context));
    cmds.register("server_list", new CmdServerList(userInterface, context));
    cmds.register("server_status", new CmdServerStatus(userInterface, context));
    cmds.register("players", new CmdPlayers(userInterface, context, new String[0]));
    cmds.register("scoreboard", new CmdScoreboard(userInterface, context));
    cmds.register("away", new CmdAway(userInterface, context));
    cmds.register("back", new CmdBack(userInterface, context));
    cmds.register("accept", new CmdAccept(userInterface, context));
    cmds.register("decline", new CmdDecline(userInterface, context));
    cmds.register("cancel", new CmdCancel(userInterface, context));
    cmds.register("mode", new CmdMode(userInterface, context));

    cmds.register("quit", new CmdQuit(userInterface, context));
  }

  /**
   * Prints the help message to the console, listing CLI options and shell commands.
   *  @param cmds The registry containing available shell commands.
   */
  private void printHelp(final AgonRegister<CmdAction> cmds) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("agon [OPTIONS]", options);

    System.out.println("\nCOMMANDES DISPONIBLES DANS LE SHELL :");
    cmds.getKeys()
        .forEach(
            name ->
                cmds.get(name)
                    .ifPresent(
                        cmd -> System.out.printf("  %-12s : %s%n", name, cmd.getDescription())));

    System.out.println(
        "\nHow to move your pieces : \n\n"
            + "[letter1][col1][letter2][col2] to move "
            + "your piece from letter1-col1 to letter2-col2\n"
            + "if you have a piece to relocate you have to enter the tile"
            + "where you want to put it [letter][col]\n"
            + "Exemples: a1a2, f5g6 and for relocation a1, f10\n");
  }

  /**
   * Displays the application version information by reading the version.txt file.
   */
  private void printVersion() {
    try {
      System.out.println(getVersionContent());
    } catch (IOException e) {
      GameLogger.error("[WARNING] version.txt not found.");
      System.out.println(
          "Agon Game - CLI Launcher\n(c) 2026 University of Bordeaux\nversion 1.0.0");
    }
  }

  /**
   * Loads the content of the help documentation from a local file.
   *  @return The help content as a String.
   * @throws IOException If the file cannot be read.
   */
  protected String getHelpContent() throws IOException {
    return new LoadLocalFile("/cmdsInformations/helpGameLauncher.txt").getContent();
  }

  /**
   * Loads the content of the version information from a local file.
   *  @return The version content as a String.
   * @throws IOException If the file cannot be read.
   */
  protected String getVersionContent() throws IOException {
    return new LoadLocalFile("/cmdsInformations/version.txt").getContent();
  }
}