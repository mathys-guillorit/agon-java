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
import fr.univ.bordeaux.ui.gui.AgonApp;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javafx.application.Application;
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

    /** The path to the persistent configuration file. */
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
        options.addOption("b", "blitz", false, "Starts the game in Blitz mode.");
        options.addOption("t", "time", true, "Sets the time limit for each player (in minutes).");
        options.addOption("a", "ai", true, "Replace the given color by an Ai. Can be both using A for color.");
        options.addOption("c", "contest", false, "Launches contest mode (reads file and outputs move).");
    }

    public void launch(final String[] args) {
        final GameConfig config = loadInitialConfig();

       GameLogger.getInstance().setDebugMode(config.isDebug());

        final CommandLineParser parser = new DefaultParser();
        final AgonRegister<CmdAction> cmds = new AgonRegister<>();
        final CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                this.fillRegister(cmds, null, config, null, new AppContext(new LocalProfile("Default")));
                printHelp(cmds);
                return;
            }

            if (cmd.hasOption("V")) {
                printVersion();
                return;
            }

            if (cmd.hasOption("g")) {
                GameLogger.info("Starting Graphical User Interface (GUI)...");
                Application.launch(AgonApp.class, args);
                return;
            }

            final String playerName = askPlayerName();
            final AppMode mode = askApplicationMode();
            GameLogger.info("[INFO] Mode selected: " + mode);

            final LocalProfile profile = new LocalProfile(playerName);
            final AppContext context = new AppContext(profile);
            context.setMode(mode);

            final String[] fileArg = cmd.getArgs();
            String filePath = null;

            if (fileArg.length > 0) {
                filePath = fileArg[0];
                final File file = new File(filePath);

                if (!file.exists() || file.isDirectory()) {
                    GameLogger.error("The file '" + filePath + "' does not exist or is a directory.");
                    return;
                }

                if (cmd.hasOption("c")) {
                    GameLogger.info("Contest mode detected.");
                    try {
                        ContestMatch.executeContest(filePath);
                    } catch (Exception e) {
                        GameLogger.error("Contest mode failed : " + e.getMessage());
                    }
                    return;
                }
                GameLogger.info("File argument detected: " + filePath);

            } else if (cmd.hasOption("c")) {
                GameLogger.error("Contest mode requires a file argument.");
                this.fillRegister(cmds, null, config, null, null);
                printHelp(cmds);
                return;
            }

            startGame(config, cmd, cmds, filePath, context);

        } catch (ParseException e) {
            GameLogger.error("Argument Error : " + e.getMessage());
            printHelp(cmds);
        }
    }

    private GameConfig loadInitialConfig() {
        final ConfigParser configParser = new ConfigParser();
        try {
            return configParser.parse(configPath);
        } catch (IOException e) {
            GameLogger.info("No config file found. Creating a default file...");
            createDefaultConfigFile();
            return new GameConfig();
        }
    }

    private void createDefaultConfigFile() {
        final ConfigSerializer serializer = new ConfigSerializer();
        try {
            serializer.createDefault(configPath);
            GameLogger.info("Minimal configuration file created at: " + configPath);
        } catch (IOException e) {
            GameLogger.error("Failed to save default config: " + e.getMessage());
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

    private void startGame(
            final GameConfig config,
            final CommandLine cmd,
            final AgonRegister<CmdAction> cmds,
            final String filePathToLoad,
            final AppContext context) {
        GameLogger.info("Starting Agon Shell...");
        final AgonShell userInterface;

        try {
            final AgonShell[] shellRef = new AgonShell[1];

            Completer strategyCompleter = (reader, line, candidates) -> {
                if (shellRef[0] != null) {
                    shellRef[0].globalCompleter(reader, line, candidates);
                }
            };

            Terminal terminal = TerminalBuilder.builder().dumb(true).build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).completer(strategyCompleter).build();

            userInterface = new AgonShell(terminal, reader, cmds);
            shellRef[0] = userInterface;

            final GameEngine gameEngine = new GameEngine(userInterface, cmds);
            context.setGameEngine(gameEngine);
            gameEngine.setAppContext(context);

            this.fillRegister(cmds, userInterface, config, gameEngine, context);

            ConfigBinder.bindOptionsToConfig(cmd, config, userInterface);

            if (config.isBlitzMode()) {
                final CmdCreate create = new CmdCreate(userInterface, config, gameEngine);
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

    private void fillRegister(
            final AgonRegister<CmdAction> cmds,
            final GameUserInterface ui,
            final GameConfig config,
            final GameEngine engine,
            final AppContext context) {

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
        cmds.register("accept", new CmdAccept(ui, context));
        cmds.register("decline", new CmdDecline(ui, context));
        cmds.register("cancel", new CmdCancel(ui, context));
        cmds.register("mode", new CmdMode(ui, context));
        cmds.register("quit", new CmdQuit(ui, context));
    }

    private void printHelp(final AgonRegister<CmdAction> cmds) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("agon [OPTIONS]", options);
        GameLogger.info("\nAVAILABLE COMMAND IN CLI MODE :");
        cmds.getKeys().forEach(
                name -> {
                    cmds.get(name).ifPresent(
                            cmd -> {
                                GameLogger.info(String.format("  %-12s : %s", name, cmd.getDescription()));
                            });
                });
        GameLogger.info(
                "\nHow to move your pieces : "
                        + "\n\n[letter1][col1][letter2][col2] to move your piece "
                        + "from letter1-col1 to letter2-col2\n"
                        + "if you have a piece to relocate you have to enter the tile "
                        + "where you want to put it [letter][col]\n"
                        + "Exemples: a1a2, f5g6 and for relocation a1, f10\n");
    }

    private void printVersion() {
        try {
            GameLogger.info(getVersionContent());
        } catch (IOException e) {
            GameLogger.error("[WARNING] version.txt not found.");
        }
    }

    protected String getHelpContent() throws IOException {
        return new LoadLocalFile("/cmdsInformations/helpGameLauncher.txt").getContent();
    }

    protected String getVersionContent() throws IOException {
        return new LoadLocalFile("/cmdsInformations/version.txt").getContent();
    }
}