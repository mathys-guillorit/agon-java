package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import jdk.jfr.Description;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CmdSetTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private GameConfig config;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    config = new GameConfig();
    outContent = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      cmds.register("set", new CmdSet(gameUserInterface, config));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Verify system parameters modification (PARAM=VALUE format)")
  void testSetSystemParams() {
    config.setVerbose(false);
    config.setDebug(false);
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"verbose=true", "debug=true"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertTrue(config.isVerbose());
    assertTrue(config.isDebug());
    assertTrue(outContent.toString().contains("Verbose: true"));
    cmd = cmds.get("set").get().createNew(new String[] {"verbose=false", "debug=false"});
    cmd.execute(null);
    assertFalse(config.isVerbose());
    assertFalse(config.isDebug());
  }

  @Test
  @DisplayName("Verify AI parameters modification")
  void testSetAIParams() {
    CmdAction cmd =
        cmds.get("set")
            .get()
            .createNew(
                new String[] {
                  "aiDepth=8",
                  "aiMode=minimax",
                  "aiTimeLimit=1800",
                  "aiIterativeDeepening=true",
                  "aiHeuristic=mixed",
                  "blitzmode=true",
                  "aiActive=true",
                  "timeout=10"
                });
    cmd.execute(null);

    assertEquals(8, config.getAiDepth());
    assertEquals("minimax", config.getAiMode());
    assertEquals(1800, config.getAiTimeLimit());
    assertTrue(config.isBlitzMode());
    assertTrue(outContent.toString().contains("AI Depth: 8"));
  }

  @Test
  @DisplayName("Verify error handling for invalid numbers")
  void testSetInvalidNumber() {
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"timeout=not_a_number"});
    boolean result = cmd.execute(null);
    assertFalse(result, "Command should fail with bad number format");
    assertTrue(outContent.toString().contains("Error: Numeric value expected"));
  }

  @Test
  @DisplayName("Verify error handling for missing '=' sign")
  void testSetInvalidSyntax() {
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"badformat"});
    boolean result = cmd.execute(null);

    assertTrue(outContent.toString().contains("Invalid format"));
  }

  @Test
  @DisplayName("Verify AI player assignment")
  void testSetPlayerAI() {
    CmdAction cmd =
        cmds.get("set").get().createNew(new String[] {"whiteIsAi=true", "blackIsAi=false"});
    cmd.execute(null);

    assertTrue(config.isWhiteAi());
    assertFalse(config.isBlackAi());
  }

  @Test
  @DisplayName("Verify command description")
  void testDescription() {
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {});
    String desc = cmd.getDescription();

    assertTrue(
        desc.contains("set PARAM=VALUE"), "Description should show the correct format: " + desc);
    assertTrue(desc.contains("set aiDepth=5"), "Description should provide a valid example");
  }

  @Test
  @DisplayName("check correct behavior with different possibilities --something -a --longopt")
  void testOptionCombinations() {
    Terminal term = new FakeTerminal(new ByteArrayOutputStream());
    CmdSet cmd = new CmdSet(this.gameUserInterface, this.config);
    Completer completer = cmd.getAutoCompleter();
    FakeLineReader lineReader = new FakeLineReader(term);
    lineReader.setCompleter(completer);
    ArrayList<String> opts =
        new ArrayList<>(
            Arrays.asList("whiteIsAi=true ", "", "debug", "ai", "time", "time=5 aiMode=MCTS ", ""));
    String uinput;
    List<Candidate> candidates;
    List<Candidate> candidatesResult;
    final String[][] comparator = {
      {
        "verbose",
        "debug",
        "blitzMode",
        "timeout",
        "aiActive",
        "aiMode",
        "aiDepth",
        "aiTimeLimit",
        "aiIterativeDeepening",
        "aiHeuristic",
        "whiteIsAi",
        "blackIsAi"
      },
      {
        "verbose",
        "debug",
        "blitzMode",
        "timeout",
        "aiActive",
        "aiMode",
        "aiDepth",
        "aiTimeLimit",
        "aiIterativeDeepening",
        "aiHeuristic",
        "whiteIsAi",
        "blackIsAi"
      },
      {"debug"},
      {"aiActive", "aiMode", "aiDepth", "aiTimeLimit", "aiIterativeDeepening", "aiHeuristic"},
      {"timeout"},
      {
        "verbose",
        "debug",
        "blitzMode",
        "timeout",
        "aiActive",
        "aiMode",
        "aiDepth",
        "aiTimeLimit",
        "aiIterativeDeepening",
        "aiHeuristic",
        "whiteIsAi",
        "blackIsAi"
      },
      {}
    };
    short j = 0;
    String curropt;
    for (String opt : opts) {
      uinput = "set " + opt;
      if (j == 6) {
        uinput = "something ";
      }
      candidates = lineReader.complete(uinput);
      for (short i = 0; i < comparator[j].length; i++) {
        candidatesResult = candidates.stream().toList();
        short finalI = i;
        short finalJ = j;
        curropt = comparator[finalJ][finalI] + "=";
        String finalCurropt = curropt;
        assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals(finalCurropt)),
            "user input is : (\""
                + uinput
                + "\") out is :"
                + candidatesResult
                + " doesn't contains:"
                + finalCurropt
                + "\"");
      }
      assertEquals(comparator[j].length, candidates.size(), "step: opt n°" + j);
      j++;
    }
  }

  @Test
  @Description("check if the new instance CmdSet have 'set' as name")
  void createNewHasAName() {
    CmdSet cmd = new CmdSet(this.gameUserInterface, this.config);
    assertEquals("set", cmd.createNew(new String[] {"name"}).getName());
  }

  @Test
  @DisplayName("Verify error when no parameter is provided")
  void testSetNoArguments() {
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {});
    boolean result = cmd.execute(null);

    assertFalse(result, "Command should fail when no arguments are provided");
    assertTrue(outContent.toString().contains("No parameters provided"));
  }

  @Test
  @DisplayName("Verify getHelp returns formatted help with options and descriptions")
  void testGetHelp() {
    CmdSet cmd = new CmdSet(this.gameUserInterface, this.config);

    String help = cmd.getHelp();

    assertTrue(help.startsWith("usage: set"), "Help should start with usage: set");
    assertTrue(help.contains("Options"), "Help should contain the Options header");
    assertTrue(help.contains("Description"), "Help should contain the Description header");

    assertTrue(help.contains("verbose=true^false"), "Help should describe verbose option");
    assertTrue(help.contains("debug=true^false"), "Help should describe debug option");
    assertTrue(help.contains("timeout=0..." + Integer.MAX_VALUE), "Help should describe timeout");
    assertTrue(help.contains("aiMode=minimax^mcts^iterative"), "Help should describe aiMode");
    assertTrue(help.contains("whiteIsAi=true^false"), "Help should describe whiteIsAi");

    assertTrue(help.contains("increase verbosity"), "Help should contain verbose description");
    assertTrue(help.contains("to show more messages"), "Help should contain debug description");
  }

  @Test
  @DisplayName("Verify error handling for unknown parameter")
  void testSetUnknownParameter() {
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"unknownParam=true"});
    boolean result = cmd.execute(null);

    assertFalse(result, "Command should fail on unknown parameter");
    assertTrue(outContent.toString().contains("Unknown parameter: unknownParam"));
    assertTrue(outContent.toString().contains("set PARAM=VALUE"), outContent.toString());
  }

  @Test
  @DisplayName("Verify getHelp lists all settable parameters")
  void testGetHelpContainsAllOptions() {
    CmdSet cmd = new CmdSet(this.gameUserInterface, this.config);
    String help = cmd.getHelp();

    String[] expectedOptions = {
      "verbose=true^false",
      "debug=true^false",
      "blitzMode=true^false",
      "timeout=0..." + Integer.MAX_VALUE,
      "aiActive=true^false",
      "aiMode=minimax^mcts^iterative",
      "aiDepth=0..." + Integer.MAX_VALUE,
      "aiTimeLimit=0..." + Integer.MAX_VALUE,
      "aiIterativeDeepening=true^false",
      "aiHeuristic=mixed^centrality^mobility^UCT^ML",
      "whiteIsAi=true^false",
      "blackIsAi=true^false"
    };

    for (String option : expectedOptions) {
      assertTrue(help.contains(option), "Help should contain option: " + option);
    }
  }
}
