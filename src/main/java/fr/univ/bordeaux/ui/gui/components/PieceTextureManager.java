package fr.univ.bordeaux.ui.gui.components;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.scene.image.Image;

/**
 * * Handles loading, caching, and retrieving piece images for the canvas. Prevents memory leaks by
 * loading the PNG assets only once during instantiation.
 */
public class PieceTextureManager {

  /** A cache storing the loaded JavaFX Images for each piece type. */
  private final Map<String, Image> pieceImages = new HashMap<>();

  /**
   * Default constructor for the PieceTextureManager. Automatically loads and caches all piece
   * textures into memory upon instantiation.
   */
  public PieceTextureManager() {
    loadImages();
  }

  private void loadImages() {
    try {
      pieceImages.put(
          "white_pawn",
          new Image(
              Objects.requireNonNull(getClass().getResourceAsStream("/images/white_pawn.png"))));
      pieceImages.put(
          "black_pawn",
          new Image(
              Objects.requireNonNull(getClass().getResourceAsStream("/images/black_pawn.png"))));
      pieceImages.put(
          "white_queen",
          new Image(
              Objects.requireNonNull(getClass().getResourceAsStream("/images/white_queen.png"))));
      pieceImages.put(
          "black_queen",
          new Image(
              Objects.requireNonNull(getClass().getResourceAsStream("/images/black_queen.png"))));
    } catch (final Exception e) {
      System.err.println(
          "[WARNING] Failed to load piece images. Check the src/main/resources/images/ directory.");
    }
  }

  /**
   * Retrieves the cached image associated with a specific piece type.
   *
   * @param piece The type of the piece (e.g., WHITE_PAWN).
   * @return The corresponding JavaFX Image, or null if the piece is null/not found.
   */
  public Image getImage(final PieceType piece) {
    return piece == null ? null : pieceImages.get(piece.name().toLowerCase());
  }
}
