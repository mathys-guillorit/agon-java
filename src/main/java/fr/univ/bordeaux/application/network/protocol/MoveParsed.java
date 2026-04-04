package fr.univ.bordeaux.application.network.protocol;

/**
 * Represents a parsed compact move received through the network protocol.
 *
 * <p>Examples of supported move text:
 *
 * <ul>
 *   <li>e2e4
 *   <li>e10f11
 * </ul>
 *
 * <p>This object stores both:
 *
 * <ul>
 *   <li>the original source/destination coordinates as text,
 *   <li>the corresponding internal board indices.
 * </ul>
 */
public class MoveParsed {

  /** Source coordinate as text (e.g. "e2" or "e10"). */
  private final String sourceText;

  /** Destination coordinate as text (e.g. "e4" or "f11"). */
  private final String destinationText;

  private final int fromIndex;
  private final int toIndex;

  /**
   * Creates a parsed move object.
   *
   * @param sourceText source coordinate as text
   * @param destinationText destination coordinate as text
   * @param fromIndex internal source index
   * @param toIndex internal destination index
   */
  public MoveParsed(String sourceText, String destinationText, int fromIndex, int toIndex) {
    this.sourceText = sourceText;
    this.destinationText = destinationText;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
  }

  /**
   * Returns the source coordinate as text.
   *
   * @return source coordinate
   */
  public String getSourceText() {
    return sourceText;
  }

  /**
   * Returns the destination coordinate as text.
   *
   * @return destination coordinate
   */
  public String getDestinationText() {
    return destinationText;
  }

  /**
   * Returns the internal source index.
   *
   * @return source index
   */
  public int getFromIndex() {
    return fromIndex;
  }

  /**
   * Returns the internal destination index.
   *
   * @return destination index
   */
  public int getToIndex() {
    return toIndex;
  }

  public boolean hasSource() {
    return fromIndex >= 0 && sourceText != null && !sourceText.isBlank();
  }
}
