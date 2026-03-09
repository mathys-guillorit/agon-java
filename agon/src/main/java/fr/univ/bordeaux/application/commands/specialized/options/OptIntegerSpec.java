package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;

/** Integer option */
public class OptIntegerSpec extends OptTypeSpec {

  public OptIntegerSpec() {
    super(List.of());
  }

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
