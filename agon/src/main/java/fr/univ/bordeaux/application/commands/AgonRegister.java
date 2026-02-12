package fr.univ.bordeaux.application.commands;

import java.util.HashMap;
import java.util.Optional;

///  TODO: test if i add two same pairs ("A", CmdQuit(), "A", CmdQuit())
///  into CommandRegister, add this to tests

/**
 * {@code @warning} never inheritance only composition on this class
 * @param <T> any class that want implement Registry in his property
 * (specific for agon to be Singleton, too complex for synchronizing some objects between layers)
 */
public abstract class AgonRegister<T> {

  private final HashMap<String, T> correspondences;

  /** initialize variables */
  public AgonRegister() {
    this.correspondences = new HashMap<>();
  }

  /**
   * register a {@link T} to be used from an identifier for later access by {@link String} the
   * String is lowercased by the function
   *
   * @param name a name or key
   * @param type {@link T} associated with the key
   */
  public void register(String name, T type) {
    this.correspondences.put(name, type);
  }

  /**
   * can return corresponding {@link T} if found else return null ({@link Optional} show that the
   * result can be null explicitly)
   *
   * @param key associated key to the Object
   * @return the concerned {@link ICmd} or null
   */
  public Optional<T> get(String key) {
    return Optional.ofNullable(this.correspondences.get(key.toLowerCase()));
  }

  public boolean isEmpty() {
    return this.correspondences.isEmpty();
  }
}
