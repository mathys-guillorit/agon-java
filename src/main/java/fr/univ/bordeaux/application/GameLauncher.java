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
import java.util.Scanner;
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
 *
 * <p>It acts as the bootstrapper that handles:
 * <ul>
 * <li>Defining valid command-line options (CLI).
 * <li>Loading persistent settings from the local configuration file (.agonrc).
 * <li>Parsing arguments provided by the user at startup.
 * <li>Resolving conflicts between file loading and manual options.
 * <li>Initializing the appropriate User Interface (CLI or GUI).
 * </ul>
 */
public class GameLauncher {

    private final Options options;
    private final String configPath = System.getProperty("user.dir") + File.separator + ".agonrc";

    public GameLauncher() {
        this.options = new Options();
        this.setupOptions();
    }

    private void setupOptions() {
        options.addOption("h", "help", false, "Displays this help message.");
        options.addOption("V", "version", false, "Displays version information");
        options.addOption("v", "verbose", false, "Enables verbose output.");
        options.addOption("d", "debug", false, "Enables debug mode.");
        options.addOption("g", "gui", false, "Starts the graphical interface.");
        options.addOption("b", "blitz", false, "Launches the game in blitz mode.");
        options.addOption("t", "time", true, "Sets the time limit for each player (in minutes).");
        options.addOption("a", "ai", true, "Replace the given color by an Ai. Can be both using A for color.");
        options.addOption("c", "contest", false, "Launches contest mode (reads file and outputs move).");
    }

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

    private void handleInfoOptions(final CommandLine cmd, final AgonRegister<CmdAction> cmds) {
        if (cmd.hasOption("h")) {
            this.fillRegister(cmds, null, null, null, new AppContext(new LocalProfile("Temp")));
            printHelp(cmds);
        } else if (cmd.hasOption("V")) {
            printVersion();
        }
    }

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

    private AppContext createAppContext(final CommandLine cmd) {
        final AppMode mode;
        final String playerName;

        if (cmd.hasOption("g")) {
            mode = AppMode.LOCAL;
            playerName = "GUIPlayer";
        } else {
            playerName = askPlayerName();
            mode = askApplicationMode();
            GameLogger.info("[INFO] Mode selected: " + mode);
        }

        final AppContext context = new AppContext(new LocalProfile(playerName));
        context.setMode(mode);

        return context;
    }

    private boolean processArgumentsAndContest(
            final CommandLine cmd, final AgonRegister<CmdAction> cmds, final AppContext context, final String filePath) {

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

    private GameConfig loadInitialConfig() {
        final ConfigParser configParser = new ConfigParser();
        GameConfig config;
        try {
            config = configParser.parse(configPath);
        } catch (IOException e) {
            GameLogger.info("No config file found. Creating a default file...");
            createDefaultConfigFile();
            config = new GameConfig();
        }
        return config;
    }

    private void createDefaultConfigFile() {
        final ConfigSerializer serializer = new ConfigSerializer();
        try {
            serializer.createDefault(configPath);
            GameLogger.info("[INFO] Minimal configuration file created at: " + configPath);
        } catch (IOException e) {
            GameLogger.error("[ERROR] Failed to save default config: " + e.getMessage());
        }
    }

    protected String askPlayerName() {
        final Scanner scanner = new Scanner(System.in);
        GameLogger.info("Enter your player name: ");
        String name = scanner.nextLine().trim();

        while (name.isEmpty()) {
            GameLogger.info("Name cannot be empty. Enter your player name: ");
            name = scanner.nextLine().trim();
        }
        return name;
    }

    protected AppMode askApplicationMode() {
        final Scanner scanner = new Scanner(System.in);
        GameLogger.info("Select mode:");
        GameLogger.info("1 - Local");
        GameLogger.info("2 - Online");
        GameLogger.info("Your choice: ");

        String input = scanner.nextLine().trim();
        while (!"1".equals(input) && !"2".equals(input)) {
            GameLogger.info("Invalid choice. Enter 1 (Local) or 2 (Online): ");
            input = scanner.nextLine().trim();
        }
        return "2".equals(input) ? AppMode.ONLINE : AppMode.LOCAL;
    }

    protected void startGame(
            final GameConfig config,
            final CommandLine cmd,
            final AgonRegister<CmdAction> cmds,
            final String filePathToLoad,
            final AppContext context) {

        GameLogger.info("Starting Agon Shell...");

        if (cmd.hasOption("g")) {
            GameLogger.info("[INFO] Starting Agon GUI...");
            launchGUI(config, cmds, context);
        } else {
            launchCLI(config, cmd, cmds, filePathToLoad, context);
        }
    }

    protected void launchGUI(GameConfig config, AgonRegister<CmdAction> cmds, AppContext context) {
        final AgonGui gui = new AgonGui(config, context);
        final GameEngine gameEngine = new GameEngine(gui, cmds);
        context.setGameEngine(gameEngine);
        gameEngine.setAppContext(context);
        this.fillRegister(cmds, gui, config, gameEngine, context);
        gui.start();
        gameEngine.start();
    }

    protected void launchCLI(GameConfig config, CommandLine cmd, AgonRegister<CmdAction> cmds, String filePathToLoad, AppContext context) {
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

            gameEngine.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void printHelp(final AgonRegister<CmdAction> cmds) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("agon [OPTIONS]", options);

        GameLogger.info("\nCOMMANDES DISPONIBLES DANS LE SHELL :");
        cmds.getKeys()
                .forEach(
                        name ->
                                cmds.get(name)
                                        .ifPresent(
                                                cmd -> System.out.printf("  %-12s : %s%n", name, cmd.getDescription())));

        GameLogger.info(
                "\nHow to move your pieces : \n\n"
                        + "[letter1][col1][letter2][col2] to move "
                        + "your piece from letter1-col1 to letter2-col2\n"
                        + "if you have a piece to relocate you have to enter the tile"
                        + "where you want to put it [letter][col]\n"
                        + "Exemples: a1a2, f5g6 and for relocation a1, f10\n");
    }

    private void printVersion() {
        try {
            GameLogger.info(getVersionContent());
        } catch (IOException e) {
            GameLogger.error("[WARNING] version.txt not found.");
            GameLogger.info(
                    "Agon Game - CLI Launcher\n(c) 2026 University of Bordeaux\nversion 1.0.0");
        }
    }

    protected String getHelpContent() throws IOException {
        return new LoadLocalFile("/cmdsInformations/helpGameLauncher.txt").getContent();
    }

    protected String getVersionContent() throws IOException {
        return new LoadLocalFile("/cmdsInformations/version.txt").getContent();
    }
}