package fr.univ.bordeaux.application.commands;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/// TODO: test if i add two same pairs ("A", CmdQuit(), "A", CmdQuit())
///  into CommandRegister, add this to tests

/**
 * comment ça {@code @warning} never inheritance only composition on this class
 *
 * @param <T> any class that want implement Registry in his property (specific for agon to be
 *     Singleton, too complex for synchronizing some objects between layers)
 */
public class AgonRegister<T> {

  private final HashMap<String, T> correspondences;
  private final boolean caseSensitive;

  /** initialize variables */
  public AgonRegister() {
    this(false);
  }

  public AgonRegister(boolean caseSensitive) {
    this.correspondences = new HashMap<>();
    this.caseSensitive = caseSensitive;
  }

  /**
   * register a {@link T} to be used from an identifier for later access by {@link String} the
   * String is lowercased by the function
   *
   * @param name a name or key
   * @param value {@link T} associated with the key
   */
  public void register(String name, T value) {
    this.correspondences.put(this.normalize(name), value);
  }

  /**
   * can return corresponding {@link T} if found else return null ({@link Optional} show that the
   * result can be null explicitly)
   *
   * @param key associated key to the Object
   * @return the concerned {@link Object} or null
   */
  public Optional<T> get(String key) {
    return Optional.ofNullable(this.correspondences.get(this.normalize(key)));
  }

  public boolean isEmpty() {
    return this.correspondences.isEmpty();
  }

  /** remove all pairs stored (key and value) */
  public void reset() {
    this.correspondences.clear();
  }

  /**
   * remove a value with its key
   *
   * @param key associated
   */
  public void remove(String key) {
    this.correspondences.remove(this.normalize(key));
  }

  /**
   * @return int the number of registered items
   */
  public int size() {
    return this.correspondences.size();
  }

  /**
   * get all keys<br>
   * - duplicate of internal keys
   *
   * @return only keys from pairs
   */
  public Set<String> getKeys() {
    return Set.copyOf(this.correspondences.keySet());
  }

  @Nonnull
  private String normalize(final @Nonnull String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key can't be null");
    }
    return this.caseSensitive ? key : key.toLowerCase();
  }
}
