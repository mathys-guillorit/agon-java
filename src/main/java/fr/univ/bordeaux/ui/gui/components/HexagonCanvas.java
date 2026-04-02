package fr.univ.bordeaux.ui.gui.components;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.ui.gui.components.states.CanvasInteractionState;
import fr.univ.bordeaux.ui.gui.components.states.CanvasInterface;
import fr.univ.bordeaux.ui.gui.components.states.IdleCanvasState;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.function.Consumer;

/**
 * A custom JavaFX Canvas responsible for rendering the hexagonal Agon game board.
 * It handles the drawing of the grid, the pieces, and captures mouse interactions.
 */
public class HexagonCanvas extends Canvas implements CanvasInterface {

    private static final double HEX_SIZE = 30.0;
    private static final double SQRT_3 = Math.sqrt(3);
    private RestrictedAgonBoard currentBoard;
    private final Map<String, Image> pieceImages = new HashMap<>();
    private final Map<String, PieceType> boardSnapshot = new HashMap<>();
    private CanvasInteractionState currentState = new IdleCanvasState();
    private String selectedHex = null;
    private Consumer<String> moveRequestListener;
    private PieceType draggedPiece = null;
    private double mouseX = 0;
    private double mouseY = 0;



    /**
     * Constructs a new HexagonCanvas.
     * Initializes piece images and sets up the mouse click event listeners.
     */
    public HexagonCanvas() {
        loadImages();
        setupMouseListener();
    }

    /**
     * Sets the listener that will handle game moves requested via the UI.
     *
     * @param moveRequestListener A consumer accepting a move command string.
     */
    public void setMoveRequestListener(Consumer<String> moveRequestListener) {
        this.moveRequestListener = moveRequestListener;
    }

    /**
     * Registers mouse event handlers (press, drag, release) for canvas interaction.
     */
    private void setupMouseListener() {
        this.setOnMousePressed(event -> {
            if (currentBoard == null) return;
            String coord = pixelToHex(event.getX(), event.getY());
            currentState.handleMousePressed(this, coord, event.getX(), event.getY());
        });

        this.setOnMouseDragged(event -> {
            if (currentBoard == null) return;
            currentState.handleMouseDragged(this, event.getX(), event.getY());
        });

        this.setOnMouseReleased(event -> {
            if (currentBoard == null) return;
            String coord = pixelToHex(event.getX(), event.getY());
            currentState.handleMouseReleased(this, coord, event.getX(), event.getY());
        });
    }

    /**
     * Converts a 2D pixel coordinate into a standard Agon hexagonal coordinate (e.g., "F6").
     *
     * @param x The X pixel coordinate.
     * @param y The Y pixel coordinate.
     * @return The hexagonal coordinate string, or null if the click is outside the board.
     */
    private String pixelToHex(double x, double y) {
        double ptX = x - (getWidth() / 2);
        double ptY = y - (getHeight() / 2);
        double qFrac = (SQRT_3 / 3.0 * ptX - 1.0 / 3.0 * ptY) / HEX_SIZE;
        double rFrac = (2.0 / 3.0 * ptY) / HEX_SIZE;

        int[] rounded = axialRound(qFrac, rFrac);
        int q = rounded[0];
        int r = rounded[1];

        if (Math.abs(q) <= 5 && Math.abs(r) <= 5 && Math.abs(-q - r) <= 5) {
            char rowChar = (char) (65 + (5 - r));
            int logicalCol = q + 6;
            return "" + rowChar + logicalCol;
        }
        return null;
    }

    public void setState(CanvasInteractionState newState) {
        this.currentState = newState;
    }

    public void setSelectedHex(String hex) {
        this.selectedHex = hex;
    }

    /**
     * Triggers a move request via the assigned listener.
     *
     * @param moveCommand The formatted move command (e.g., "F6G7").
     */
    public void requestMove(String moveCommand) {
        if (moveRequestListener != null) {
            moveRequestListener.accept(moveCommand);
        }
    }

    /**
     * Captures the current state of pieces from the actual game board model.
     *
     * @param board The current restricted board instance.
     * @return A map linking hexagonal coordinates to the pieces placed there.
     */
    public Map<String, PieceType> takeSnapshot(RestrictedAgonBoard board) {
        Map<String, PieceType> snap = new HashMap<>();
        if (board != null) {
            for (int[] coord : getBoardCoordinates()) {
                int q = coord[0];
                int r = coord[1];
                char rowChar = (char) (65 + (5 - r));
                int logicalCol = q + 6;
                try {
                    int index = CoordinateMapper.toIndex(rowChar, logicalCol);
                    PieceType piece = board.getPieceAt(index);
                    if (piece != null) {
                        snap.put("" + rowChar + logicalCol, piece);
                    }
                } catch (Exception e) {
                    // Ignore out of bounds
                }
            }
        }
        return snap;
    }

    /**
     * Applies a snapshot of the board state and requests a re-render.
     *
     * @param boardRef     A reference to the active board.
     * @param safeSnapshot The extracted map of piece positions.
     */
    public void applySnapshot(RestrictedAgonBoard boardRef, Map<String, PieceType> safeSnapshot) {
        this.currentBoard = boardRef;
        this.boardSnapshot.clear();
        this.boardSnapshot.putAll(safeSnapshot);
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

        for (int[] coord : getBoardCoordinates()) {
            int q = coord[0];
            int r = coord[1];

            double xOffset = HEX_SIZE * SQRT_3 * (q + r / 2.0);
            double yOffset = HEX_SIZE * 1.5 * r;
            double hexX = centerX + xOffset;
            double hexY = centerY + yOffset;

            char rowChar = (char) (65 + (5 - r));
            int logicalCol = q + 6;
            String currentCoord = "" + rowChar + logicalCol;

            Color fillColor = Color.LIGHTGRAY;
            if ((Math.abs(q) + Math.abs(r) + Math.abs(-q - r)) / 2 % 2 == 0) {
                fillColor = Color.web("#e0e0e0");
            }

            Color strokeColor = Color.BLACK;
            double lineWidth = 2.0;

            if (selectedHex != null && selectedHex.equals(currentCoord)) {
                fillColor = Color.web("#fff59d");
                strokeColor = Color.web("#fbc02d");
                lineWidth = 4.0;
            }

            drawHexagon(gc, hexX, hexY, HEX_SIZE, fillColor, strokeColor, lineWidth);

            PieceType piece = boardSnapshot.get(currentCoord);
            if (piece != null) {
                if (!currentCoord.equals(selectedHex) || draggedPiece == null) {
                    drawPiece(gc, hexX, hexY, piece);
                }
            }
        }

        gc.setFill(Color.web("#a0a0a0"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        int boardRadius = 5;
        for (int r = -boardRadius; r <= boardRadius; r++) {
            char rowChar = (char) (65 + (5 - r));
            int minQ = Math.max(-boardRadius, -boardRadius - r);

            double xOffset = HEX_SIZE * SQRT_3 * (minQ + r / 2.0);
            double yOffset = HEX_SIZE * 1.5 * r;

            double finalX = centerX + xOffset - (SQRT_3 * HEX_SIZE);
            double finalY = centerY + yOffset;

            gc.fillText(String.valueOf(rowChar), finalX, finalY);
        }

        for (int q = -boardRadius; q <= boardRadius; q++) {
            int logicalCol = q + 6;
            int maxR = Math.min(boardRadius, boardRadius - q);
            double xOffset = HEX_SIZE * SQRT_3 * (q + maxR / 2.0);
            double yOffset = HEX_SIZE * 1.5 * maxR;
            double finalX = centerX + xOffset + (HEX_SIZE * SQRT_3 * 0.5);
            double finalY = centerY + yOffset + (HEX_SIZE * 1.5);
            gc.fillText(String.valueOf(logicalCol), finalX, finalY);
        }
        if (draggedPiece != null) {
            drawPiece(gc, mouseX, mouseY, draggedPiece);
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
    private void drawHexagon(GraphicsContext gc, double centerX, double centerY, double size, Color fill, Color stroke, double lineWidth) {
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
        gc.setLineWidth(lineWidth);
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

    /**
     * Converts floating-point axial coordinates back to the nearest integer hex coordinates.
     * Uses the cubic coordinate rounding algorithm.
     */
    private int[] axialRound(double qFrac, double rFrac) {
        double sFrac = -qFrac - rFrac;

        int q = (int) Math.round(qFrac);
        int r = (int) Math.round(rFrac);
        int s = (int) Math.round(sFrac);

        double qDiff = Math.abs(q - qFrac);
        double rDiff = Math.abs(r - rFrac);
        double sDiff = Math.abs(s - sFrac);

        if (qDiff > rDiff && qDiff > sDiff) {
            q = -r - s;
        } else if (rDiff > sDiff) {
            r = -q - s;
        }

        return new int[]{q, r};
    }

    /**
     * Calculates and returns the list of all valid (q, r) coordinates on the board.
     * This avoids duplicating the complex hexagonal grid boundaries logic.
     * * @return A list of integer arrays [q, r] for each valid hexagon.
     */
    private List<int[]> getBoardCoordinates() {
        List<int[]> coords = new ArrayList<>();
        int boardRadius = 5;

        for (int q = -boardRadius; q <= boardRadius; q++) {
            int r1 = Math.max(-boardRadius, -q - boardRadius);
            int r2 = Math.min(boardRadius, -q + boardRadius);
            for (int r = r1; r <= r2; r++) {
                coords.add(new int[]{q, r});
            }
        }
        return coords;
    }

    public boolean hasPieceAt(String hex) { return boardSnapshot.containsKey(hex); }
    public PieceType getPieceAt(String hex) { return boardSnapshot.get(hex); }
    public void setDraggedPiece(PieceType piece) { this.draggedPiece = piece; }
    public void setMousePosition(double x, double y) { this.mouseX = x; this.mouseY = y; }

    @Override
    public boolean isResizable() { return true; }
    @Override
    public double prefWidth(double height) { return getWidth(); }
    @Override
    public double prefHeight(double width) { return getHeight(); }
    @Override
    public double minWidth(double height) { return 0; }
    @Override
    public double minHeight(double width) { return 0; }
    @Override
    public double maxWidth(double height) { return Double.MAX_VALUE; }
    @Override
    public double maxHeight(double width) { return Double.MAX_VALUE; }
}