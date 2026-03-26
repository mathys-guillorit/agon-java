package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.network.CmdJoin;
import fr.univ.bordeaux.application.commands.network.CmdPing;
import fr.univ.bordeaux.application.commands.network.CmdServerList;
import fr.univ.bordeaux.application.commands.network.CmdServerStart;
import fr.univ.bordeaux.application.commands.network.CmdServerStatus;
import fr.univ.bordeaux.application.commands.network.CmdServerStop;
import fr.univ.bordeaux.application.commands.network.CmdPlayers;
import fr.univ.bordeaux.application.commands.specialized.CmdCreate;
import fr.univ.bordeaux.application.commands.specialized.CmdHelp;
import fr.univ.bordeaux.application.commands.specialized.CmdHint;
import fr.univ.bordeaux.application.commands.specialized.CmdLoad;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
import fr.univ.bordeaux.application.commands.specialized.CmdRedo;
import fr.univ.bordeaux.application.commands.specialized.CmdSave;
import fr.univ.bordeaux.application.commands.specialized.CmdSet;
import fr.univ.bordeaux.application.commands.specialized.CmdShow;
import fr.univ.bordeaux.application.commands.specialized.CmdUndo;
import fr.univ.bordeaux.application.match.ContestMatch;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.config.ConfigParser;
import fr.univ.bordeaux.technical.config.ConfigSerializer;
import fr.univ.bordeaux.technical.config.GameConfig;
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
 * @author L'équipe de développement
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
   * <p>This method defines flags (like -h, -v) and complex arguments (like -t TIME, -a COLOR).
   */
  private void setupOptions() {
    options.addOption("h", "help", false, "Displays this help message.");
    options.addOption("V", "version", false, "Displays version information");
    options.addOption("v", "verbose", false, "Enables verbose output.");
    options.addOption("d", "debug", false, "Enables debug mode.");
    options.addOption("g", "gui", false, "Starts the graphical interface.");
    options.addOption("b", "blitz", false, "Launches the game in blitz mode.");
    options.addOption(
            "c", "contest", false, "Launches contest mode (reads file and outputs move).");
    options.addOption("p", "placement", false, "Manual placement of guards.");
    options.addOption(
            Option.builder("t")
                    .longOpt("time")
                    .hasArg(true)
                    .argName("TIME")
                    .desc("Time limit in minutes for blitz mode (default: 30).")
                    .build());
    options.addOption(
            Option.builder("a")
                    .longOpt("ai")
                    .hasArg(true)
                    .optionalArg(true)
                    .argName("COLOR")
                    .desc("Replaces a player with AI (Colors: B, W, A).")
                    .build());
  }

  /**
   * Main launch sequence that processes arguments, updates configuration, and starts the game.
   *
   * @param args The raw command-line arguments passed at startup.
   */
  public void launch(String[] args) {
    GameConfig config = loadInitialConfig();
    CommandLineParser parser = new DefaultParser();

    try {
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        printHelp();
        return;
      }
      if (cmd.hasOption("V")) {
        printVersion();
        return;
      }
      if (cmd.hasOption("v")) {
        config.setVerbose(true);
        System.out.println("[INFO] Verbose mode enabled.");
      }
      if (cmd.hasOption("d")) {
        config.setDebug(true);
        System.out.println("[DEBUG] Debug mode enabled.");
      }
      if (cmd.hasOption("p")) {
        System.out.println("[INFO] Manual guard placement detected.");
        config.setManualPlacement(true);
      }
      if (cmd.hasOption("t") && !cmd.hasOption("b")) {
        System.out.println(
                "[WARNING] The '-t' / '--time' option is ignored because blitz mode (-b) is not active");
      } else if (cmd.hasOption("b")) {
        config.setBlitzMode(true);
        int time = 30;
        if (cmd.hasOption("t")) {
          try {
            time = Integer.parseInt(cmd.getOptionValue("t"));
          } catch (NumberFormatException e) {
            System.err.println("[ERROR] Invalid time format. Using default 30 mins.");
          }
        }
        config.setTimeout(time * 60);
        System.out.println("[INFO] Blitz mode activated: " + time + " minutes");
      }
      if (cmd.hasOption("c")) {
        System.out.println("[INFO] Contest mode detected.");
      }
      if (cmd.hasOption("a")) {
        config.setAi(true);
        String color = cmd.getOptionValue("a", "DEFAULT");
        if ("W".equalsIgnoreCase(color)) {
          config.setWhiteAI(true);
          config.setBlackAI(false);
          System.out.println("[INFO] AI configured to play White.");
        } else if ("B".equalsIgnoreCase(color)) {
          config.setWhiteAI(false);
          config.setBlackAI(true);
          System.out.println("[INFO] AI configured to play Black.");
        } else if ("A".equalsIgnoreCase(color)) {
          config.setWhiteAI(true);
          config.setBlackAI(true);
          System.out.println("[INFO] AI configured to play Both sides.");
        } else {
          config.setWhiteAi(false);
          config.setBlackAi(true);
          System.out.println("[INFO] AI defaults configuration (Black).");
        }
      }

      String[] fileArg = cmd.getArgs();
      String filePath = null;

      if (fileArg.length > 0) {
        filePath = fileArg[0];
        File file = new File(filePath);

        if (!file.exists() || file.isDirectory()) {
          System.err.println(
                  "[ERROR] The file '" + filePath + "' does not exist or is a directory.");
          return;
        }

        if (cmd.hasOption("p")) {
          System.out.println(
                  "[WARNING] Option '-p' (Manual Placement) is ignored because a save file is loaded.");
        }

        if (cmd.hasOption("c")) {
          System.out.println("[INFO] Contest mode detected.");
          try {
            ContestMatch.executeContest(fileArg[0]);
          } catch (Exception e) {
            System.err.println("[ERROR] Contest mode failed : " + e.getMessage());
          }
          return;
        }

        System.out.println("[INFO] File argument detected: " + filePath);

      } else if (cmd.hasOption("c")) {
        System.err.println("[ERROR] Contest mode requires a file argument.");
        printHelp();
        return;
      }

      startGame(config, cmd, filePath);

    } catch (ParseException e) {
      System.err.println("Argument Error : " + e.getMessage());
      printHelp();
    }
  }

  /**
   * Loads the initial configuration from the .agonrc file.
   *
   * @return A {@link GameConfig} object populated with file settings or default values.
   */
  private GameConfig loadInitialConfig() {
    ConfigParser configParser = new ConfigParser();
    try {
      return configParser.parse(configPath);
    } catch (IOException e) {
      System.out.println("No config file found. Creating a default file...");
      createDefaultConfigFile();
      return new GameConfig();
    }
  }

  /**
   * Persists a default configuration to the disk using the serializer.
   */
  private void createDefaultConfigFile() {
    ConfigSerializer serializer = new ConfigSerializer();
    try {
      serializer.createDefault(configPath);
      System.out.println("[INFO] Minimal configuration file created at: " + configPath);
    } catch (IOException e) {
      System.err.println("[ERROR] Failed to save default config: " + e.getMessage());
    }
  }

  /**
   * Asks the user for a local player name before starting the application.
   *
   * @return a non-empty player name
   */
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

  /**
   * Initializes the application layers and starts the selected user interface.
   *
   * @param config The final configuration to be used by the UI and the engine.
   * @param cmd The parsed command line, used to check for the GUI flag (-g).
   * @param filePathToLoad The path to the save file to load automatically, or null if none.
   */
  private void startGame(GameConfig config, CommandLine cmd, String filePathToLoad) {
    System.out.println("Starting Agon Shell...");
    AgonRegister<CmdAction> cmds = new AgonRegister<>();

    String playerName = askPlayerName();
    LocalProfile profile = new LocalProfile(playerName);
    AppContext context = new AppContext(profile);

    GameUserInterface userInterface;

    if (cmd.hasOption("g")) {
      // AgonGUI agon = new AgonGUI(config);
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
        shellRef[0] = (AgonShell) userInterface;

        GameEngine gameEngine = new GameEngine(userInterface, cmds);

        // =========================
        // Local / gameplay commands
        // =========================
        cmds.register("new", new CmdCreate(userInterface, config, gameEngine));
        cmds.register("hint", new CmdHint(userInterface));
        cmds.register("show", new CmdShow(userInterface, config));
        cmds.register("load", new CmdLoad(userInterface));
        cmds.register("save", new CmdSave(userInterface));
        cmds.register("set", new CmdSet(userInterface, config));
        cmds.register("undo", new CmdUndo(userInterface));
        cmds.register("redo", new CmdRedo(userInterface));
        cmds.register("help", new CmdHelp(userInterface, cmds));

        // =========================
        // Network commands
        // =========================
        cmds.register("join", new CmdJoin(userInterface, context));
        cmds.register("ping", new CmdPing(userInterface, context));
        cmds.register("server_start", new CmdServerStart(userInterface, context));
        cmds.register("server_stop", new CmdServerStop(userInterface, context));
        cmds.register("server_list", new CmdServerList(userInterface, context));
        cmds.register("server_status", new CmdServerStatus(userInterface, context));
        cmds.register("players", new CmdPlayers(userInterface, context));

        // =========================
        // Context-aware quit
        // =========================
        cmds.register("quit", new CmdQuit(userInterface, context));

        /* if (filePathToLoad != null) {
          loadCmd.execute(null);
        } */

        gameEngine.start();

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Prints the CLI help message.
   *
   * <p>Attempts to read a custom help file. If not found, falls back to the
   * standard Apache CLI formatter.
   */
  private void printHelp() {
    try {
      System.out.println(getHelpContent());
    } catch (IOException e) {
      System.err.println("[WARNING] agonShellMenu.txt not found. Displaying default help:");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("agon [OPTIONS] [FILE]", "\nAgon Game\n", this.options, "", true);
    }
  }

  /**
   * Prints the current application version and credits.
   */
  private void printVersion() {
    try {
      System.out.println(getVersionContent());
    } catch (IOException e) {
      System.err.println("[WARNING] version.txt not found.");
      System.out.println(
              "Agon Game - CLI Launcher\n(c) 2026 University of Bordeaux\nversion 1.0.0");
    }
  }

  /**
   * Retrieves the content of the help information file from the local resources.
   *
   * @return The raw String content of the help file.
   * @throws IOException If the file is missing or cannot be accessed.
   */
  protected String getHelpContent() throws IOException {
    return new LoadLocalFile("cmdsInformations/helpGameLauncher.txt").getContent();
  }

  /**
   * Retrieves the application version and credits from the local resources.
   *
   * @return The raw String content of the version file.
   * @throws IOException If the file is missing or cannot be accessed.
   */
  protected String getVersionContent() throws IOException {
    return new LoadLocalFile("cmdsInformations/version.txt").getContent();
  }
}