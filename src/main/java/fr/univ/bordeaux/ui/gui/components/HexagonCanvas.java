package fr.univ.bordeaux.ui.gui.components;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.ui.gui.components.states.CanvasInteractionState;
import fr.univ.bordeaux.ui.gui.components.states.CanvasInterface;
import fr.univ.bordeaux.ui.gui.components.states.IdleCanvasState;
import java.util.*;
import java.util.function.Consumer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * A custom JavaFX Canvas responsible for rendering the hexagonal Agon game board. It handles the
 * drawing of the grid, the pieces, and captures mouse interactions by delegating them to the
 * current {@link CanvasInteractionState}.
 */
public class HexagonCanvas extends Canvas implements CanvasInterface {

  /** The current state of the game board. */
  private RestrictedAgonBoard currentBoard;

  /** The utility responsible for loading and providing piece images. */
  private final PieceTextureManager textureManager = new PieceTextureManager();

  /** A snapshot map linking coordinates to piece types for efficient rendering. */
  private final Map<String, PieceType> boardSnapshot = new HashMap<>();

  /** The current interaction state of the canvas (e.g., Idle or Dragging). */
  private CanvasInteractionState currentState = new IdleCanvasState();

  /** The logical coordinate of the currently selected hexagon (e.g., "F6"). */
  private String selectedHex = null;

  /** The callback triggered when a user attempts to make a move. */
  private Consumer<String> moveRequestListener;

  /** The piece currently being dragged by the mouse. */
  private PieceType draggedPiece = null;

  /** The current X pixel coordinate of the mouse pointer. */
  private double mouseX = 0;

  /** The current Y pixel coordinate of the mouse pointer. */
  private double mouseY = 0;

  /** Default constructor for the Hexagon Canvas. Initializes mouse event listeners. */
  public HexagonCanvas() {
    setupMouseListener();
  }

  /**
   * Sets the listener that will be triggered when a move is requested by the user via the UI.
   *
   * @param moveRequestListener A consumer accepting the move command string (e.g., "F6G7").
   */
  public void setMoveRequestListener(final Consumer<String> moveRequestListener) {
    this.moveRequestListener = moveRequestListener;
  }

  private void setupMouseListener() {
    this.setOnMousePressed(
        event -> {
          if (currentBoard == null) {
            return;
          }
          final String coord =
              HexMath.pixelToHex(event.getX(), event.getY(), getWidth(), getHeight());
          currentState.handleMousePressed(this, coord, event.getX(), event.getY());
        });

    this.setOnMouseDragged(
        event -> {
          if (currentBoard == null) {
            return;
          }
          currentState.handleMouseDragged(this, event.getX(), event.getY());
        });

    this.setOnMouseReleased(
        event -> {
          if (currentBoard == null) {
            return;
          }
          final String coord =
              HexMath.pixelToHex(event.getX(), event.getY(), getWidth(), getHeight());
          currentState.handleMouseReleased(this, coord, event.getX(), event.getY());
        });
  }

  /** {@inheritDoc} */
  @Override
  public void setState(final CanvasInteractionState newState) {
    this.currentState = newState;
  }

  /** {@inheritDoc} */
  @Override
  public void setSelectedHex(final String hex) {
    this.selectedHex = hex;
  }

  /** {@inheritDoc} */
  @Override
  public void requestMove(final String moveCommand) {
    if (moveRequestListener != null) {
      moveRequestListener.accept(moveCommand);
    }
  }

  /**
   * Captures the current state of the board pieces to render them efficiently.
   *
   * @param board The current restricted board state.
   * @return A map linking logical coordinates (e.g., "F6") to their occupying PieceType.
   */
  public Map<String, PieceType> takeSnapshot(final RestrictedAgonBoard board) {
    final Map<String, PieceType> snap = new HashMap<>();
    if (board != null) {
      for (final int[] coord : HexMath.getBoardCoordinates()) {
        final int q = coord[0];
        final int r = coord[1];
        final char rowChar = (char) (65 + 5 - r);
        final int logicalCol = q + 6;
        try {
          final int index = CoordinateMapper.toIndex(rowChar, logicalCol);
          final PieceType piece = board.getPieceAt(index);
          if (piece != null) {
            snap.put("" + rowChar + logicalCol, piece);
          }
        } catch (Exception ignored) {
        }
      }
    }
    return snap;
  }

  /**
   * Applies a new board state snapshot to the canvas and triggers a redraw.
   *
   * @param boardRef The reference to the core board.
   * @param safeSnapshot The mapped coordinates and pieces to render.
   */
  public void applySnapshot(
      final RestrictedAgonBoard boardRef, final Map<String, PieceType> safeSnapshot) {
    this.currentBoard = boardRef;
    this.boardSnapshot.clear();
    this.boardSnapshot.putAll(safeSnapshot);
    draw();
  }

  /**
   * Clears the canvas and completely redraws the background, the hexagonal grid, the coordinate
   * labels, and all active pieces.
   */
  @Override
  public void draw() {
    final GraphicsContext gc = getGraphicsContext2D();
    gc.setFill(Color.web("#2b2b2b"));
    gc.fillRect(0, 0, getWidth(), getHeight());

    final double centerX = getWidth() / 2;
    final double centerY = getHeight() / 2;

    for (final int[] coord : HexMath.getBoardCoordinates()) {
      final int q = coord[0];
      final int r = coord[1];

      final double offsetX = HexMath.HEX_SIZE * HexMath.SQRT_3 * (q + r / 2.0);
      final double offsetY = HexMath.HEX_SIZE * 1.5 * r;
      final double hexX = centerX + offsetX;
      final double hexY = centerY + offsetY;

      final char rowChar = (char) (65 + 5 - r);
      final int logicalCol = q + 6;
      final String currentCoord = "" + rowChar + logicalCol;

      Color fillColor =
          ((Math.abs(q) + Math.abs(r) + Math.abs(-q - r)) / 2 % 2 == 0)
              ? Color.web("#e0e0e0")
              : Color.LIGHTGRAY;
      Color strokeColor = Color.BLACK;
      double lineWidth = 2.0;

      if (selectedHex != null && selectedHex.equals(currentCoord)) {
        fillColor = Color.web("#fff59d");
        strokeColor = Color.web("#fbc02d");
        lineWidth = 4.0;
      }

      drawHexagon(gc, hexX, hexY, HexMath.HEX_SIZE, fillColor, strokeColor, lineWidth);

      final PieceType piece = boardSnapshot.get(currentCoord);
      if (piece != null && (!currentCoord.equals(selectedHex) || draggedPiece == null)) {
        drawPiece(gc, hexX, hexY, piece);
      }
    }

    gc.setFill(Color.web("#a0a0a0"));
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setTextBaseline(VPos.CENTER);

    final int boardRadius = 5;
    for (int r = -boardRadius; r <= boardRadius; r++) {
      final char rowChar = (char) (65 + 5 - r);
      final int minQ = Math.max(-boardRadius, -boardRadius - r);
      final double finalX =
          centerX
              + (HexMath.HEX_SIZE * HexMath.SQRT_3 * (minQ + r / 2.0))
              - (HexMath.SQRT_3 * HexMath.HEX_SIZE);
      final double finalY = centerY + (HexMath.HEX_SIZE * 1.5 * r);
      gc.fillText(String.valueOf(rowChar), finalX, finalY);
    }

    for (int q = -boardRadius; q <= boardRadius; q++) {
      final int maxR = Math.min(boardRadius, boardRadius - q);
      final double finalX =
          centerX
              + (HexMath.HEX_SIZE * HexMath.SQRT_3 * (q + maxR / 2.0))
              + (HexMath.HEX_SIZE * HexMath.SQRT_3 * 0.5);
      final double finalY = centerY + (HexMath.HEX_SIZE * 1.5 * maxR) + (HexMath.HEX_SIZE * 1.5);
      gc.fillText(String.valueOf(q + 6), finalX, finalY);
    }

    if (draggedPiece != null) {
      drawPiece(gc, mouseX, mouseY, draggedPiece);
    }
  }

  private void drawHexagon(
      final GraphicsContext gc,
      final double centerX,
      final double centerY,
      final double size,
      final Color fill,
      final Color stroke,
      final double lineWidth) {
    final double[] pointsX = new double[6];
    final double[] pointsY = new double[6];
    for (int i = 0; i < 6; i++) {
      final double angle_rad = Math.PI / 180 * (60 * i - 30);
      pointsX[i] = centerX + size * Math.cos(angle_rad);
      pointsY[i] = centerY + size * Math.sin(angle_rad);
    }
    gc.setFill(fill);
    gc.fillPolygon(pointsX, pointsY, 6);
    gc.setStroke(stroke);
    gc.setLineWidth(lineWidth);
    gc.strokePolygon(pointsX, pointsY, 6);
  }

  private void drawPiece(
      final GraphicsContext gc, final double centerX, final double centerY, final PieceType piece) {
    final Image img = textureManager.getImage(piece);
    if (img != null) {
      final double scale = HexMath.HEX_SIZE * 3.5 / Math.max(img.getWidth(), img.getHeight());
      final double drawW = img.getWidth() * scale;
      final double drawH = img.getHeight() * scale;
      gc.drawImage(img, centerX - drawW / 2, centerY - drawH / 2, drawW, drawH);
    } else {
      final double r = HexMath.HEX_SIZE * 0.5;
      gc.setFill(Color.RED);
      gc.fillOval(centerX - r, centerY - r, r * 2, r * 2);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasPieceAt(final String hex) {
    return boardSnapshot.containsKey(hex);
  }

  /** {@inheritDoc} */
  @Override
  public PieceType getPieceAt(final String hex) {
    return boardSnapshot.get(hex);
  }

  /** {@inheritDoc} */
  @Override
  public void setDraggedPiece(final PieceType piece) {
    this.draggedPiece = piece;
  }

  /** {@inheritDoc} */
  @Override
  public void setMousePosition(final double x, final double y) {
    this.mouseX = x;
    this.mouseY = y;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isResizable() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public double prefWidth(double height) {
    return getWidth();
  }

  /** {@inheritDoc} */
  @Override
  public double prefHeight(double width) {
    return getHeight();
  }

  /** {@inheritDoc} */
  @Override
  public double minWidth(double height) {
    return 0;
  }

  /** {@inheritDoc} */
  @Override
  public double minHeight(double width) {
    return 0;
  }

  /** {@inheritDoc} */
  @Override
  public double maxWidth(double height) {
    return Double.MAX_VALUE;
  }

  /** {@inheritDoc} */
  @Override
  public double maxHeight(double width) {
    return Double.MAX_VALUE;
  }
}
