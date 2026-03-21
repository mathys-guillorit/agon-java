package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;

/**
 * specifications for option types, when a type is required for a specific variable assignments it
 * provides controls over it
 */
public interface OptValueSpec {

  List<String> complete(String prefix);

  boolean validate(String value);
}
