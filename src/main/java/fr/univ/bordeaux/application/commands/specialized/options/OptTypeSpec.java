package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;

public class OptTypeSpec implements OptValueSpec {

  private List<String> allowed;

  public OptTypeSpec(List<String> allowed) {
    this.allowed = allowed;
  }

  @Override
  public List<String> complete(String prefix) {
    return this.allowed.stream().filter(v -> v.startsWith(prefix)).toList();
  }

  @Override
  public boolean validate(String value) {
    return this.allowed.contains(value);
  }
}
