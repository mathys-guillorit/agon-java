package fr.univ.bordeaux.technical.io.storage.states;

import fr.univ.bordeaux.technical.io.config.ConfigParser;
import fr.univ.bordeaux.technical.io.storage.GameSaveBuilder;
import java.io.IOException;

/** Parses settings lines (key=value) and feeds them into the builder's GameConfig. */
public class SettingsState implements SaveParserState {

  private final ConfigParser configHelper = new ConfigParser();

  @Override
  public void parseLine(final String line, final GameSaveBuilder builder) throws IOException {
    configHelper.parseLine(line, builder.getConfig());
  }
}
