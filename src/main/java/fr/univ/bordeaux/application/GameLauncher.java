package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.match.ContestMatch;
import fr.univ.bordeaux.technical.config.ConfigParser;
import fr.univ.bordeaux.technical.config.ConfigSerializer;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.technical.utils.LoadLocalFile;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.*;

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
 * @author L'équipe de développement (ou ton nom)
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
   * <p>This method defines flags (like -h, -v) and complex arguments (like -t TIME, -a COLOR). It
   * uses {@link Option.Builder} for complex options to ensure clarity.
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
   * <p>This method parses the raw arguments. If informational flags (-h, -V) are present, it
   * displays the info and returns. Otherwise, it updates the {@link GameConfig} and proceeds to
   * start the game engine. If an invalid argument is provided, it catches the {@link
   * ParseException} and displays the help menu.
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
        String[] fileArg = cmd.getArgs();
        if (fileArg.length > 0) {
          try {
            ContestMatch.executeContest(fileArg[0]);
          } catch (Exception e) {
            System.err.println("[ERROR] Contest mode failed : " + e.getMessage());
          }
        } else {
          System.err.println("Contest mode (-c/--contest) requires a save file.");
          printHelp();
        }
        return;
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
        }
      }
      if (cmd.hasOption("p")) {
        System.out.println("[INFO] Manual guard placement detected.");
        config.setManualPlacement(true);
      }
      String[] fileArg = cmd.getArgs();
      if (fileArg.length > 0) {
        String filePath = fileArg[0];
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
        System.out.println("[INFO] File argument detected: " + filePath);
      }
      startGame(config, cmd);
    } catch (ParseException e) {
      System.err.println("Argument Error : " + e.getMessage());
      printHelp();
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
      System.out.println("No config file found. Creating a default file...");
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
      System.out.println("[INFO] Minimal configuration file created at: " + configPath);
    } catch (IOException e) {
      System.err.println("[ERROR] Failed to save default config: " + e.getMessage());
    }
  }

  /**
   * Initializes the application layers and starts the selected user interface.
   *
   * <p>Chooses between the CLI ({@link fr.univ.bordeaux.ui.cli.AgonShell}) and the GUI ({@link
   * fr.univ.bordeaux.ui.gui.AgonGUI}) based on the provided command-line options.
   *
   * @param config The final configuration to be used by the UI and the engine.
   * @param cmd The parsed command line, used to check for the GUI flag (-g).
   */
  private void startGame(GameConfig config, CommandLine cmd) {
    // MatchManager matchManager = new MatchManager(config);
    if (cmd.hasOption("g")) {
      // AgonGUI agon = new  AgonGUI(config);
    } else {
      // AgonShell agon = new AgonShell(config);
    }
    // agon.setGameEngine(matchManager);
    // agon.start();
  }

  /**
   * Prints the CLI help message.
   *
   * <p>Attempts to read a custom "helpGameLauncher.txt" file. If not found, falls back to the
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
   *
   * <p>Reads from the "version.txt" file or prints a hardcoded fallback string.
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
   * <p>This method is marked as {@code protected} to allow for "Extract and Override" in unit
   * tests, enabling the simulation of {@link IOException} without manipulating physical files.
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
   * <p>This method is marked as {@code protected} to allow for "Extract and Override" in unit
   * tests, enabling the simulation of {@link IOException} to verify the application's fallback
   * behavior.
   *
   * @return The raw String content of the version file.
   * @throws IOException If the file is missing or cannot be accessed.
   */
  protected String getVersionContent() throws IOException {
    return new LoadLocalFile("cmdsInformations/version.txt").getContent();
  }
}
