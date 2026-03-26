package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;

/**
 * Specifications for option types, when a type is required for a specific variable assignments it
 * provides controls over it.
 */
public interface OptValueSpec {

  /**
   * Advanced completer for types.
   *
   * @param prefix prediction text
   * @return possibilities
   */
  List<String> complete(String prefix);

  /**
   * Check if the content contains the value.
   *
   * @param value to check
   * @return true is included in the message else false
   */
  boolean validate(String value);
}
