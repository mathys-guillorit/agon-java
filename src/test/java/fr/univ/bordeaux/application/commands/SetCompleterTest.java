package fr.univ.bordeaux.application.commands;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

public class SetCompleterTest {

  @Test
  @Description("complete a word if is in all possible options")
  void correctCompletWord() {
    // CmdSet cmds = new CmdSet();

  }

  @Test
  @Description("")
  void noWords() {}

  @Test
  @Description("if options have only short name")
  void optsAsFirstName() {}

  @Test
  @Description("only long option names")
  void optsAreLong() {}
}
