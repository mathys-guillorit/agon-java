package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.network.CmdAway;
import fr.univ.bordeaux.application.commands.network.CmdBack;
import fr.univ.bordeaux.application.commands.network.CmdJoin;
import fr.univ.bordeaux.application.commands.network.CmdNew;
import fr.univ.bordeaux.application.commands.network.CmdPing;
import fr.univ.bordeaux.application.commands.network.CmdPlayers;
import fr.univ.bordeaux.application.commands.network.CmdScoreboard;
import fr.univ.bordeaux.application.commands.network.CmdServerList;
import fr.univ.bordeaux.application.commands.network.CmdServerStart;
import fr.univ.bordeaux.application.commands.network.CmdServerStatus;
import fr.univ.bordeaux.application.commands.network.CmdServerStop;
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
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.io.config.ConfigBinder;
import fr.univ.bordeaux.technical.io.config.ConfigParser;
import fr.univ.bordeaux.technical.io.config.ConfigSerializer;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.technical.utils.LoadLocalFile;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
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

/** The GameLauncher class is the entry point for the Agon application. */
public class GameLauncher {

  /** The definitions of all allowed command-line options. */
  private final Options options;

  /** The path to the persistent configuration file. */
  private final String configPath = System.getProperty("user.dir") + File.separator + ".agonrc";

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
    options.addOption(
        "a", "ai", true, "Replace the given color by an Ai. Can be both using A for color.");
    options.addOption(
        "c", "contest", false, "Launches contest mode (reads file and outputs move).");
  }

  /**
   * Launches the application by parsing command-line arguments and initializing the game state.
   * This method handles configuration loading, mode selection, and branches into specific execution
   * paths like help display, version info, or contest mode.
   *
   * @param args The command-line arguments provided at startup.
   */
  public void launch(String[] args) {
    GameConfig config = loadInitialConfig();

    GameLogger.getInstance().setDebugMode(config.isDebug());

    CommandLineParser parser = new DefaultParser();
    AgonRegister<CmdAction> cmds = new AgonRegister<>();

    String playerName = askPlayerName();
    AppMode mode = askApplicationMode();
    System.out.println("[INFO] Mode selected: " + mode);

    LocalProfile profile = new LocalProfile(playerName);
    AppContext context = new AppContext(profile);
    context.setMode(mode);

    try {
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        this.fillRegister(cmds, null, null, null, context);
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
          GameLogger.error("The file '" + filePath + "' does not exist or is a directory.");
          return;
        }

        if (cmd.hasOption("c")) {
          GameLogger.info("Contest mode detected.");
          try {
            ContestMatch.executeContest(fileArg[0]);
          } catch (Exception e) {
            GameLogger.error("Contest mode failed : " + e.getMessage());
          }
          return;
        }
        GameLogger.info("File argument detected: " + filePath);

      } else if (cmd.hasOption("c")) {
        GameLogger.error("Contest mode requires a file argument.");
        this.fillRegister(cmds, null, null, null, null);
        printHelp(cmds);
        return;
      }
      startGame(config, cmd, cmds, filePath, context);
    } catch (ParseException e) {
      GameLogger.error("Argument Error : " + e.getMessage());
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
      GameLogger.info("No config file found. Creating a default file...");
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
      GameLogger.info("Minimal configuration file created at: " + configPath);
    } catch (IOException e) {
      GameLogger.error("Failed to save default config: " + e.getMessage());
    }
  }

  private String askPlayerName() {
    Scanner scanner = new Scanner(System.in);

    System.out.print("Enter your player name: ");
    String name = scanner.nextLine().trim();

    while (name.isEmpty()) {
      System.out.print("Name cannot be empty. Enter your player name: ");
      name = scanner.nextLine().trim();
    }

    return name;
  }

  private void startGame(
      GameConfig config,
      CommandLine cmd,
      AgonRegister<CmdAction> cmds,
      String filePathToLoad,
      AppContext context) {
    GameLogger.info("Starting Agon Shell...");
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
        context.setGameEngine(gameEngine);
        gameEngine.setAppContext(context);
        this.fillRegister(cmds, userInterface, config, gameEngine, context);
        ConfigBinder.bindOptionsToConfig(cmd, config, userInterface);

        if (config.isBlitzMode()) {
          CmdCreate create = new CmdCreate(userInterface, config, gameEngine);
          create.execute(null);
        }

        if (filePathToLoad != null) {
          CmdAction loadcmd = cmds.get("load").get().createNew(new String[] {filePathToLoad});
          loadcmd.execute(null);
        }

        gameEngine.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void fillRegister(
      AgonRegister<CmdAction> cmds,
      GameUserInterface ui,
      GameConfig config,
      GameEngine engine,
      AppContext context) {

    cmds.register("new", new CmdNew(ui, context, config, engine));

    cmds.register("hint", new CmdHint(ui));
    cmds.register("show", new CmdShow(ui, config));
    cmds.register("load", new CmdLoad(ui, engine));
    cmds.register("save", new CmdSave(ui));
    cmds.register("set", new CmdSet(ui, config));
    cmds.register("undo", new CmdUndo(ui));
    cmds.register("redo", new CmdRedo(ui));
    cmds.register("help", new CmdHelp(ui, cmds));
    cmds.register("pause", new CmdPause(ui));
    cmds.register("join", new CmdJoin(ui, context));
    cmds.register("ping", new CmdPing(ui, context));
    cmds.register("server_start", new CmdServerStart(ui, context));
    cmds.register("server_stop", new CmdServerStop(ui, context));
    cmds.register("server_list", new CmdServerList(ui, context));
    cmds.register("server_status", new CmdServerStatus(ui, context));
    cmds.register("players", new CmdPlayers(ui, context, new String[0]));
    cmds.register("scoreboard", new CmdScoreboard(ui, context));
    cmds.register("away", new CmdAway(ui, context));
    cmds.register("back", new CmdBack(ui, context));
    cmds.register("quit", new CmdQuit(ui, context));
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
                        System.out.println(
                            String.format("  %-12s : %s", name, cmd.getDescription()));
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

  private AppMode askApplicationMode() {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Select mode:");
    System.out.println("1 - Local");
    System.out.println("2 - Online");
    System.out.print("Your choice: ");

    String input = scanner.nextLine().trim();

    while (!input.equals("1") && !input.equals("2")) {
      System.out.print("Invalid choice. Enter 1 (Local) or 2 (Online): ");
      input = scanner.nextLine().trim();
    }

    if (input.equals("2")) {
      return AppMode.ONLINE;
    }

    return AppMode.LOCAL;
  }
}
