package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.*;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.LoadLocalFile;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class GameLauncher {
  private final Options options;

  public GameLauncher() {
    this.options = new Options();
    options.addOption("h", "help", false, "Displays this help message.");
    options.addOption("V", "version", false, "Displays version information");
    options.addOption("v", "verbose", false, "Enables verbose output.");
    options.addOption("d", "debug", false, "Enables debug mode");
  }

  public void launch(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("help")) {
        printHelp();
        System.exit(0);
      }
      if (cmd.hasOption("version")) {
        printVersion();
        System.exit(0);
      }

      boolean verbose = cmd.hasOption("verbose");
      boolean debug = cmd.hasOption("debug");

      if (verbose) System.out.println("[INFO] Verbose mode enabled.");
      if (debug) System.out.println("[DEBUG] Debug mode enabled.");

      startGame();
    } catch (ParseException e) {
      System.err.println("Argument Error : " + e.getMessage());
      System.exit(1);
    }
  }

  private void startGame() {
    System.out.println("Starting Agon Shell...");
    // Lien avec le shell de ton collègue
    String mainMenu = null;
    try {
      final String path = "/cmdsInformations/agonShellMenu.txt";
      mainMenu = new LoadLocalFile(path).getContent();
    } catch (IOException | NullPointerException e) {
      // if the file doesn't exist
      System.out.println("error when loading menu: " + e);
    }
    try {
      Terminal terminal = TerminalBuilder.builder().system(true).build(); // IOException
      var shell = new AgonShell(terminal);
      var cmds = new AgonRegister<CmdAction>();
      cmds.register("quit", new CmdQuit(shell));
      cmds.register("new", new CmdCreate(shell));
      cmds.register("load", new CmdLoad(shell));
      cmds.register("help", new CmdHelp(shell));
      cmds.register("save", new CmdSave(shell));
      cmds.register("pause", new CmdPause(shell));
      cmds.register("hint", new CmdHint(shell));
      cmds.register("set", new CmdSet(shell));
      if (mainMenu != null) shell.loadMainMenu(mainMenu);
      shell.initCmds(cmds);
      shell.start();
    } catch (IOException e) {
      System.out.println("terminal instance (from JLine cannot be created)");
      e.printStackTrace();
    }
  }

  private void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    String header = "\nAgon Game - CLI Launcher\n";
    String footer = "\nUniversity of Bordeaux - PDP 2026";
    formatter.printHelp("agon", header, this.options, footer, true);
  }

  private void printVersion() {
    System.out.println("Agon version 1.0-SNAPSHOT");
    System.out.println("(c) 2026 University of Bordeaux");
  }
}
