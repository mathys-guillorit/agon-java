package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.jline.reader.Completer;

import javax.annotation.Nonnull;

public class FakeCmd extends Cmd {

  public FakeCmd(GameUserInterface ui) {
    super(ui);
  }

  @Override
  public boolean execute(MatchManager match) {
    return true;
  }

  @Override
  public CmdAction createNew(String[] args) {
    return this;
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return super.getAutoCompleter();
  }

}
