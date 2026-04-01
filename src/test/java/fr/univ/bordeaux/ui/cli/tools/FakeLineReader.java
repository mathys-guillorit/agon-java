package fr.univ.bordeaux.ui.cli.tools;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Buffer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.Expander;
import org.jline.reader.Highlighter;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.reader.Widget;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

public class FakeLineReader extends LineReaderImpl implements LineReader {

  private final Queue<String> inputs;
  private final Parser parser = new DefaultParser();
  private final History history = new DefaultHistory();
  private final Map<String, KeyMap<Binding>> keyMaps = new HashMap<>();

  private Completer completer;

  public FakeLineReader(String... lines) {
    super(
            new FakeTerminal(new ByteArrayOutputStream()),
            "agon-test",
            null
    );
    this.inputs = new ArrayDeque<>(List.of(lines));
    this.keyMaps.put(LineReader.MAIN, new KeyMap<>());
  }

  public FakeLineReader(Terminal terminal) {
    super(terminal, "agon-test", null);
    this.inputs = new ArrayDeque<>(List.of(""));
  }

  @Override
  public Map<String, KeyMap<Binding>> defaultKeyMaps() {
    return Map.of();
  }

  @Override
  public String readLine() throws UserInterruptException, EndOfFileException {
    return "";
  }

  @Override
  public String readLine(Character mask) throws UserInterruptException, EndOfFileException {
    return "";
  }

  @Override
  public String readLine(String prompt) {
    if (inputs.isEmpty()) return "";
    return inputs.poll();
  }

  @Override
  public String readLine(String prompt, Character mask)
      throws UserInterruptException, EndOfFileException {
    return "";
  }

  @Override
  public String readLine(String prompt, Character mask, String buffer)
      throws UserInterruptException, EndOfFileException {
    return "";
  }

  @Override
  public String readLine(String prompt, String rightPrompt, Character mask, String buffer)
      throws UserInterruptException, EndOfFileException {
    return "";
  }

  @Override
  public String readLine(
      String prompt, String rightPrompt, MaskingCallback maskingCallback, String buffer)
      throws UserInterruptException, EndOfFileException {
    return "";
  }

  @Override
  public Parser getParser() {
    return parser;
  }

  @Override
  public Highlighter getHighlighter() {
    return null;
  }

  @Override
  public Expander getExpander() {
    return null;
  }

  @Override
  public History getHistory() {
    return history;
  }

  // Toutes les autres méthodes non utilisées
  @Override
  public void callWidget(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, Object> getVariables() {
    return Map.of();
  }

  @Override
  public Object getVariable(String name) {
    return null;
  }

  @Override
  public void setVariable(String name, Object value) {}

  @Override
  public boolean isSet(Option option) {
    return false;
  }

  @Override
  public void setOpt(Option option) {}

  @Override
  public void unsetOpt(Option option) {}

  @Override
  public Terminal getTerminal() {
    return null;
  }

  @Override
  public Map<String, Widget> getWidgets() {
    return new HashMap<>();
  }

  @Override
  public Map<String, Widget> getBuiltinWidgets() {
    return Map.of();
  }

  @Override
  public boolean setKeyMap(String name) {
    return false;
  }

  @Override
  public KeyMap<Binding> getKeys() {
    return null;
  }

  @Override
  public ParsedLine getParsedLine() {
    return null;
  }

  @Override
  public String getSearchTerm() {
    return "";
  }

  @Override
  public RegionType getRegionActive() {
    return null;
  }

  @Override
  public int getRegionMark() {
    return 0;
  }

  @Override
  public void addCommandsInBuffer(Collection<String> commands) {}

  @Override
  public void editAndAddInBuffer(Path file) throws Exception {}

  @Override
  public String getLastBinding() {
    return "";
  }

  @Override
  public String getTailTip() {
    return "";
  }

  @Override
  public void setTailTip(String tailTip) {}

  @Override
  public void setAutosuggestion(SuggestionType type) {}

  @Override
  public SuggestionType getAutosuggestion() {
    return null;
  }

  @Override
  public void zeroOut() {}

  @Override
  public Buffer getBuffer() {
    return null;
  }

  @Override
  public String getAppName() {
    return "";
  }

  @Override
  public void runMacro(String macro) {}

  @Override
  public MouseEvent readMouseEvent() {
    return null;
  }

  @Override
  public void printAbove(String str) {}

  @Override
  public void printAbove(AttributedString str) {}

  @Override
  public boolean isReading() {
    return false;
  }

  @Override
  public LineReader variable(String name, Object value) {
    return null;
  }

  @Override
  public LineReader option(Option option, boolean value) {
    return null;
  }

  @Override
  public Map<String, KeyMap<Binding>> getKeyMaps() {
    return this.keyMaps;
  }

  @Override
  public String getKeyMap() {
    return LineReader.MAIN;
  }

  public void setCompleter(Completer completer) {
    this.completer = completer;
  }

  public List<Candidate> complete(String input){
    if(this.completer == null) return List.of();
    ParsedLine pl = this.parser.parse(input, input.length());
    List<Candidate> candidates = new ArrayList<>();
    this.completer.complete(this,pl,candidates);
    String currentWord = pl.word();
    return candidates.stream()
          .filter(c -> c.value().startsWith(currentWord))
          .toList();
  }
}
