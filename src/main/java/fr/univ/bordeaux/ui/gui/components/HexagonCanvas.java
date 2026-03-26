package fr.univ.bordeaux.ui.gui.components;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A custom JavaFX Canvas responsible for rendering the hexagonal Agon game board.
 * It handles the drawing of the grid, the pieces, and captures mouse interactions.
 */
public class HexagonCanvas extends Canvas {

    private static final double HEX_SIZE = 35.0;
    private static final double SQRT_3 = Math.sqrt(3);
    private RestrictedAgonBoard currentBoard;
    private final Map<String, Image> pieceImages = new HashMap<>();

    /**
     * Constructs a new HexagonCanvas.
     * Initializes piece images and sets up the mouse click event listener.
     */
    public HexagonCanvas() {
        loadImages();
        this.setOnMouseClicked(event -> {
            double clickX = event.getX();
            double clickY = event.getY();
            System.out.println("Click detected at X: " + clickX + " | Y: " + clickY);
        });
    }

    /**
     * Updates the internal board state and triggers a complete redraw of the canvas.
     *
     * @param board The current restricted view of the Agon board.
     */
    public void updateBoard(RestrictedAgonBoard board) {
        this.currentBoard = board;
        draw();
    }

    /**
     * Main rendering loop.
     * Clears the canvas, draws the hexagonal grid using axial coordinates,
     * and maps these coordinates to the internal Bitboard to draw the pieces.
     */
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#2b2b2b"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        int boardRadius = 5;

        for (int q = -boardRadius; q <= boardRadius; q++) {
            int r1 = Math.max(-boardRadius, -q - boardRadius);
            int r2 = Math.min(boardRadius, -q + boardRadius);

            for (int r = r1; r <= r2; r++) {
                double xOffset = HEX_SIZE * SQRT_3 * (q + r / 2.0);
                double yOffset = HEX_SIZE * 1.5 * r;
                double hexX = centerX + xOffset;
                double hexY = centerY + yOffset;
                Color fillColor = Color.LIGHTGRAY;

                if ((Math.abs(q) + Math.abs(r) + Math.abs(-q-r)) / 2 % 2 == 0) {
                    fillColor = Color.web("#e0e0e0");
                }
                drawHexagon(gc, hexX, hexY, HEX_SIZE, fillColor, Color.BLACK);

                if (currentBoard != null) {
                    try {
                        char rowChar = (char) (65 + (5 - r));
                        int logicalCol = q + 6;
                        int index = fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper.toIndex(rowChar, logicalCol);
                        PieceType piece = currentBoard.getPieceAt(index);

                        if (piece != null) {
                            drawPiece(gc, hexX, hexY, piece);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Draws a single pointy-topped hexagon at the specified coordinates.
     *
     * @param gc      The graphics context used for drawing.
     * @param centerX The X coordinate of the hexagon's center.
     * @param centerY The Y coordinate of the hexagon's center.
     * @param size    The outer radius (size) of the hexagon.
     * @param fill    The interior color of the hexagon.
     * @param stroke  The border color of the hexagon.
     */
    private void drawHexagon(GraphicsContext gc, double centerX, double centerY, double size, Color fill, Color stroke) {
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];
        for (int i = 0; i < 6; i++) {
            double angle_deg = 60 * i - 30;
            double angle_rad = Math.PI / 180 * angle_deg;
            xPoints[i] = centerX + size * Math.cos(angle_rad);
            yPoints[i] = centerY + size * Math.sin(angle_rad);
        }
        gc.setFill(fill);
        gc.fillPolygon(xPoints, yPoints, 6);
        gc.setStroke(stroke);
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, 6);
    }

    /**
     * Renders a game piece on the board.
     * Automatically scales the image to fit proportionally within the hexagon.
     *
     * @param gc      The graphics context used for drawing.
     * @param centerX The X coordinate of the target hexagon's center.
     * @param centerY The Y coordinate of the target hexagon's center.
     * @param piece   The PieceType to be drawn.
     */
    private void drawPiece(GraphicsContext gc, double centerX, double centerY, PieceType piece) {
        String pieceName = piece.name().toLowerCase();
        Image img = pieceImages.get(pieceName);

        if (img != null) {
            double originalW = img.getWidth();
            double originalH = img.getHeight();
            double pieceSizeMultiplier = 3.5;
            double scale = (HEX_SIZE * pieceSizeMultiplier) / Math.max(originalW, originalH);
            double drawW = originalW * scale;
            double drawH = originalH * scale;
            double drawX = centerX - drawW / 2;
            double drawY = centerY - drawH / 2;
            gc.drawImage(img, drawX, drawY, drawW, drawH);
        } else {
            double pieceRadius = HEX_SIZE * 0.5;
            gc.setFill(Color.RED);
            gc.fillOval(centerX - pieceRadius, centerY - pieceRadius, pieceRadius * 2, pieceRadius * 2);
        }
    }

    /**
     * Loads the piece textures from the resources directory into memory.
     */
    private void loadImages() {
        try {
            pieceImages.put("white_pawn", new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_pawn.png"))));
            pieceImages.put("black_pawn", new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_pawn.png"))));
            pieceImages.put("white_queen", new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/white_queen.png"))));
            pieceImages.put("black_queen", new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/black_queen.png"))));
        } catch (Exception e) {
            System.err.println("[WARNING] Failed to load piece images. Check the src/main/resources/images/ directory.");
        }
    }

    @Override
    public boolean isResizable() { return true; }
    @Override
    public double prefWidth(double height) { return getWidth(); }
    @Override
    public double prefHeight(double width) { return getHeight(); }
}