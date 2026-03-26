package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;

/**
 * Typing completer.
 */
public class OptTypeSpec implements OptValueSpec {

  private List<String> allowed;

  /**
   * Create a typing completer.
   *
   * @param allowed possibles words
   */
  public OptTypeSpec(List<String> allowed) {
    this.allowed = allowed;
  }

  /**
   * Advanced completer for types.
   *
   * @param prefix prediction text
   *
   * @return possibilities
   */
  @Override
  public List<String> complete(String prefix) {
    return this.allowed.stream().filter(v -> v.startsWith(prefix)).toList();
  }

  /**
   * Check if the content contains the value.
   *
   * @param value to check
   *
   * @return true is included in the message else false
   */
  @Override
  public boolean validate(String value) {
    return this.allowed.contains(value);
  }
}
