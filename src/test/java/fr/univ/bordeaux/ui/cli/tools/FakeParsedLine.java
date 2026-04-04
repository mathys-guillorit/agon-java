package fr.univ.bordeaux.ui.cli.tools;

import java.util.List;
import org.jline.reader.ParsedLine;

public class FakeParsedLine implements ParsedLine {

  @Override
  public String word() {
    return "";
  }

  @Override
  public int wordCursor() {
    return 0;
  }

  @Override
  public int wordIndex() {
    return 1;
  }

  @Override
  public List<String> words() {
    return List.of("cmd", "");
  }

  @Override
  public String line() {
    return "cmd ";
  }

  @Override
  public int cursor() {
    return 4;
  }
}
