package fr.univ.bordeaux.application.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.technical.utils.LoadLocalFile;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeParsedLine;
import fr.univ.bordeaux.ui.cli.tools.FakeShell;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import jdk.jfr.Description;
import org.apache.commons.cli.Option;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** test remaining sections/lines that are not tested in specialized commands */
public class CmdTest {

  private FakeCmd fakeCmd;
  private Terminal term;
  private FakeParsedLine fpl;

  @BeforeEach
  void setup() throws Exception {
    this.term = new FakeTerminal(new ByteArrayOutputStream());
    this.fpl = new FakeParsedLine();
    var registrer = new AgonRegister<CmdAction>();
    var shell = new FakeShell(this.term, new LineReaderImpl(this.term), registrer);
    this.fakeCmd = new FakeCmd(shell);
    this.fakeCmd.addOption(new Option("b", "hey", false, null));
    this.fakeCmd.addOption(new Option("h", "help", false, null));
    this.fakeCmd.addOption(new Option("m", null));
    this.fakeCmd.addOption(new Option("r", "reset", false, null));
  }

  @Test
  @Description("check prediction for completeness single character or word")
  void checkWordCompleter() throws Exception {
    String userInputLine;
    String msg;
    Completer completer = this.fakeCmd.getAutoCompleter();
    List<Candidate> candidates = new ArrayList<>();
    completer.complete(new LineReaderImpl(this.term), this.fpl, candidates);
    assertFalse(candidates.isEmpty(), "completer must give -b, -h, -m, --hey, --help");
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-b")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-h")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-m")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-r")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("--hey")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("--help")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("--reset")));
    assertEquals(7, candidates.size());
    candidates.clear();
    FakeLineReader lineReader = new FakeLineReader(this.term);
    lineReader.setCompleter(completer);
    String opt = "--he";
    userInputLine = "cmd " + opt;
    candidates = lineReader.complete(userInputLine);
    StringBuilder finalSb = new StringBuilder();
    StringBuilder finalSb2 = finalSb;
    candidates.forEach(c -> finalSb2.append(c.value()).append(" "));
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("-h")),
        "candidate for \"-h\" must not contain -h but it is: (" + finalSb + ")");
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("--reset")),
        "candidate for \"--reset\" must not contain -h but it is: (" + finalSb + ")");
    assertTrue(
        candidates.stream().anyMatch(c -> c.value().equals("--hey")),
        "options: (" + finalSb + ") must contain : \"--hey\"");
    assertTrue(
        candidates.stream().anyMatch(c -> c.value().equals("--help")),
        "options: (" + finalSb + ") must contain : \"--help\"");
    assertEquals(2, candidates.size());
    opt = "-h";
    userInputLine = "cmd " + opt;
    candidates = lineReader.complete(userInputLine);
    finalSb = new StringBuilder();
    StringBuilder finalSb1 = finalSb;
    candidates.forEach(c -> finalSb1.append(c.value()).append(" "));
    msg = "\"candidate for %s must contain %s but is not: (%s)";
    assertTrue(
        candidates.stream().anyMatch(c -> c.value().equals("-h")),
        String.format(msg, opt, "-h", finalSb));
    msg = "\"candidate for %s must not contain %s but it is: (%s)";
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("-r")),
        String.format(msg, opt, "-r", finalSb));
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("-m")),
        String.format(msg, opt, "-m", finalSb));
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("--reset")),
        String.format(msg, opt, "--reset", finalSb));
    msg = "options: (%s) must not contain : \"%s\"";
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("--hey")),
        String.format(msg, finalSb, "--hey"));
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("--help")),
        String.format(msg, finalSb, "--help"));
    assertEquals(1, candidates.size(), "only option allowed is -h");
  }

  @Test
  @Description("get only long options as result with correct number")
  void testLongOptionsOnly() {
    String opt = "--";
    String userInputLine = "cmd " + opt;
    Completer completer = this.fakeCmd.getAutoCompleter();
    FakeLineReader lineReader = new FakeLineReader(this.term);
    lineReader.setCompleter(completer);
    List<Candidate> candidates = lineReader.complete(userInputLine);
    StringBuilder finalSb = new StringBuilder();
    candidates.forEach(c -> finalSb.append(c.value()).append(" "));
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("-h")),
        "candidate for \"" + opt + "\" must not contain -h but it is: (" + finalSb + ")");
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("-m")),
        "candidate for \"" + opt + "\" must not contain -m but it is: (" + finalSb + ")");
    assertFalse(
        candidates.stream().anyMatch(c -> c.value().equals("-m")),
        "candidate for \"" + opt + "\" must not contain -b but it is: (" + finalSb + ")");
    assertTrue(
        candidates.stream().anyMatch(c -> c.value().equals("--hey")), "options: (" + finalSb + ")");
    assertTrue(
        candidates.stream().anyMatch(c -> c.value().equals("--help")),
        "options: (" + finalSb + ")");
    assertTrue(
        candidates.stream().anyMatch(c -> c.value().equals("--reset")),
        "options: (" + finalSb + ")");
    assertEquals(3, candidates.size());
  }

  @Test
  @Description("when input look like : --something -a_thing --something_else ...")
  void testMultipleOptions() {
    Completer completer = this.fakeCmd.getAutoCompleter();
    FakeLineReader lineReader = new FakeLineReader(this.term);
    lineReader.setCompleter(completer);
    ArrayList<String> opts =
        new ArrayList<>(
            Arrays.asList("--help ", "-h --he", "-", "-h -m -r", "-h -m -r --r", "-h -m -r "));
    String uinput;
    List<Candidate> candidates;
    List<Candidate> candidatesResult;
    final String[][] tmp = {
      {"--hey", "--help", "--reset", "-b", "-h", "-m", "-r"},
      {"--hey", "--help"},
      {"--hey", "--help", "--reset", "-b", "-h", "-m", "-r"},
      {"-r"},
      {"--reset"},
      {"--hey", "--help", "--reset", "-b", "-h", "-m", "-r"}
    };
    short j = 0;
    for (String opt : opts) {
      uinput = "cmd " + opt;
      candidates = lineReader.complete(uinput);
      for (short i = 0; i < tmp[j].length; i++) {
        candidatesResult = candidates.stream().toList();
        short finalI = i;
        short finalJ = j;
        assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals(tmp[finalJ][finalI])),
            "user input is : (\"" + uinput + "\") out is :" + candidatesResult);
        assertEquals(tmp[j].length, candidates.size());
      }
      j++;
    }
    String opt = "--hem -m";
    uinput = "cmd " + opt;
    candidates = lineReader.complete(uinput);
    assertFalse(candidates.stream().anyMatch(c -> c.value().equals(opt)));
  }

  @Test
  @Description("(pattern cannot be predicted because is not in possibilities)")
  void noOptionsWithBadCommand2() throws IOException {
    String input = "non -";
    var flr = new FakeLineReader(this.term);
    flr.setCompleter(this.fakeCmd.getAutoCompleter());
    List<Candidate> candidates = flr.complete(input).stream().toList();
    short optCount = 0;
    final String msg = "we must have %d options defined in `loadOptions()` %s";
    assertEquals(
        optCount, candidates.size(), String.format(msg, optCount, candidates.stream().toList()));
  }

  @Test
  @Description("loading a file description from command")
  void testLoading() throws IOException {
    var registrer = new AgonRegister<CmdAction>();
    var shell = new FakeShell(this.term, new LineReaderImpl(this.term), registrer);
    FakeCmd cmd = new FakeCmd(shell);
    final String path = "/cmdsInformations/desc/test.txt";
    String txt = "this file contains multiple lines as an example for the 1st test\n";
    assertDoesNotThrow(
        () -> {
          new LoadLocalFile(path).getContent().contains("cmdsInformations");
        },
        "resources repertory is not duplicated into test environment");
    assertTrue(Objects.requireNonNull(cmd.loadText("test.txt")).contains(txt));
    assertEquals(new LoadLocalFile(path).getContent(), cmd.loadText("test.txt"));
  }

  @Test
  @Description("to check correct description")
  void descriptionLoading() throws IOException {
    var registrer = new AgonRegister<CmdAction>();
    var shell = new FakeShell(this.term, new LineReaderImpl(this.term), registrer);
    FakeCmd cmd = new FakeCmd(shell);

    assertEquals("Description: default Command", cmd.getDescription());
    String desc = "hello 3";
    cmd.setDesc(desc);
    assertEquals(desc, cmd.getDescription());
    assertTrue(cmd.getDescription().contains("3") && cmd.getDescription().contains(" "));
  }
}
