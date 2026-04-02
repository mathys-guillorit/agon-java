package fr.univ.bordeaux.technical.io.config;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigBinderTest {

  private GameConfig config;
  private GameUserInterface ui;
  private ByteArrayOutputStream out; // Sera maintenant correctement rempli
  private Options options;
  private CommandLineParser parser = new DefaultParser();

  @BeforeEach
  void setUp() {
    config = new GameConfig();
    out = new ByteArrayOutputStream(); // Initialisation avant l'UI
    LineReader reader = new FakeLineReader("n");

    try {
      // CORRECTION : Utiliser FakeTerminal avec notre flux 'out'
      Terminal terminal = new FakeTerminal(out);
      ui = new AgonShell(terminal, reader, new AgonRegister<>());

      options = new Options();
      options.addOption("b", "blitz", false, "blitz");
      options.addOption("t", "time", true, "time");
      options.addOption("a", "ai", true, "ai");

      // Définition des options longues
      options.addOption(null, "ai-mode", true, "mode");
      options.addOption(null, "ai-minimax-depth", true, "depth");
      options.addOption(null, "ai-minimax-scoring", true, "scoring");
      options.addOption(null, "ai-time", true, "aitime");
      options.addOption("v", "verbose", false, "verbose");
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Test
  void testTimeoutWithoutBlitz() throws Exception {
    CommandLine cmd = parser.parse(options, new String[]{"-t", "5"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);

    assertFalse(config.isBlitzMode());
    // out contient maintenant les données grâce à FakeTerminal(out)
    assertTrue(out.toString().contains("ignored"));
  }

  @Test
  void testAiActivationColors() throws Exception {
    // Test "white"
    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[]{"-a", "white"}), config, ui);
    assertTrue(config.isWhiteAi());

    // Test "all"
    config = new GameConfig();
    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[]{"-a", "all"}), config, ui);
    assertTrue(config.isWhiteAi() && config.isBlackAi());

    // Test valeur nulle (option présente mais sans argument)
    config = new GameConfig();
    options.getOption("a").setOptionalArg(true);
    CommandLine cmd = parser.parse(options, new String[]{"-a"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertTrue(config.isBlackAi());
    assertTrue(out.toString().contains("No Color given"));
  }

  @Test
  void testAiActivationInvalidColor() throws Exception {
    CommandLine cmd = parser.parse(options, new String[]{"-a", "purple"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertTrue(config.isBlackAi());
    assertTrue(out.toString().contains("Invalid Color"));
  }

  @Test
  void testAiModes() throws Exception {
    // Utilisation correcte des options longues pour Apache CLI dans les tests : "--ai-mode"
    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[]{"--ai-mode", "minimax"}), config, ui);
    assertEquals("minimax", config.getAiMode());

    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[]{"--ai-mode", "iterative"}), config, ui);
    assertEquals("iterative", config.getAiMode());
    assertTrue(config.isAiIterativeDeepening());
  }

  @Test
  void testAiMinimaxDepth() throws Exception {
    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[]{"--ai-minimax-depth", "4"}), config, ui);
    assertEquals(4, config.getAiDepth());
  }

  @Test
  void testAiHeuristics() throws Exception {
    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[]{"--ai-minimax-scoring", "centrality"}), config, ui);
    assertEquals("centrality", config.getAiHeuristic());
  }

  @Test
  void testAiTimeLimit() throws Exception {
    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[]{"--ai-time", "1000"}), config, ui);
    assertEquals(1000, config.getAiTimeLimit());
  }
}