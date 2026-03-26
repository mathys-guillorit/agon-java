package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;

/** Boolean option. */
public class OptBooleanSpec extends OptTypeSpec {
  /**
   * Option for specified Boolean value.
   */
  public OptBooleanSpec() {
    super(List.of("true", "false"));
  }

}
