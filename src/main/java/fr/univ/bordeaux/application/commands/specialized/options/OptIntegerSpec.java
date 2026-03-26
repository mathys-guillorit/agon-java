package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;

/** Integer option. */
public class OptIntegerSpec extends OptTypeSpec {

  /** Option for specified Integers value. */
  public OptIntegerSpec() {
    super(List.of());
  }

  /**
   * Check if the type input is valid with the completer.
   *
   * @param value user value (terminal)
   * @return true if it's valid else false
   */
  @Override
  public boolean validate(String value) {
    try {
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
