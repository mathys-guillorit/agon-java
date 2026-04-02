package fr.univ.bordeaux.ui.gui.components;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the {@link HexagonCanvas} component.
 *
 * <p>Verifies the mathematical calculations for the hexagonal grid, mouse event handling, rendering
 * logic branches, and piece visualization rules.
 */
public class HexagonCanvasTest {

  private HexagonCanvas canvas;

  /**
   * Initializes the JavaFX toolkit environment before any tests are run.
   *
   * @throws InterruptedException if the thread is interrupted while waiting for toolkit startup.
   */
  @BeforeAll
  static void initJFX() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(latch::countDown);
    } catch (IllegalStateException e) {
      // JavaFX is already initialized
    }
    latch.await(2, TimeUnit.SECONDS);
  }

  /** Initializes a fresh canvas before each test. */
  @BeforeEach
  void setUp() {
    canvas = new HexagonCanvas();
  }

  /**
   * Helper method to generate dummy mouse events for testing interaction.
   *
   * @param type The type of mouse event (e.g., MOUSE_PRESSED).
   * @param x The simulated X coordinate on the canvas.
   * @param y The simulated Y coordinate on the canvas.
   * @return A constructed {@link MouseEvent}.
   */
  private MouseEvent createMouseEvent(javafx.event.EventType<MouseEvent> type, double x, double y) {
    return new MouseEvent(
        type,
        x,
        y,
        x,
        y,
        MouseButton.PRIMARY,
        1,
        false,
        false,
        false,
        false,
        true,
        false,
        false,
        true,
        false,
        false,
        null);
  }

  @Test
  void testStateManagementUtils() {
    PieceType testPiece = PieceType.WHITE_PAWN;
    canvas.setSelectedHex("F6");
    canvas.setDraggedPiece(testPiece);
    canvas.setMousePosition(100.5, 200.5);
    assertDoesNotThrow(() -> canvas.draw());
  }

  @Test
  void testApplySnapshot() {
    Map<String, PieceType> snapshot = new HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    snapshot.put("A1", PieceType.BLACK_QUEEN);

    RestrictedAgonBoard dummyBoard = null;
    canvas.applySnapshot(dummyBoard, snapshot);

    assertTrue(canvas.hasPieceAt("F6"));
    assertEquals(PieceType.WHITE_PAWN, canvas.getPieceAt("F6"));
    assertTrue(canvas.hasPieceAt("A1"));
    assertEquals(PieceType.BLACK_QUEEN, canvas.getPieceAt("A1"));
    assertFalse(canvas.hasPieceAt("G7"));
  }

  @Test
  void testMoveRequestCallback() {
    final String[] receivedCommand = {null};
    canvas.setMoveRequestListener(command -> receivedCommand[0] = command);
    canvas.requestMove("F6G7");
    assertEquals("F6G7", receivedCommand[0]);
  }

  @Test
  void testMouseEvents_WithNullBoard_ReturnsEarly() {
    assertDoesNotThrow(
        () -> {
          canvas.getOnMousePressed().handle(createMouseEvent(MouseEvent.MOUSE_PRESSED, 100, 100));
          canvas.getOnMouseDragged().handle(createMouseEvent(MouseEvent.MOUSE_DRAGGED, 100, 100));
          canvas.getOnMouseReleased().handle(createMouseEvent(MouseEvent.MOUSE_RELEASED, 100, 100));
        });
  }

  @Test
  void testPixelToHex_MathAndOutOfBounds() {
    RestrictedAgonBoard dummyBoard =
        (RestrictedAgonBoard)
            Proxy.newProxyInstance(
                RestrictedAgonBoard.class.getClassLoader(),
                new Class<?>[] {RestrictedAgonBoard.class},
                (proxy, method, args) -> null);
    canvas.applySnapshot(dummyBoard, new HashMap<>());
    assertDoesNotThrow(
        () -> {
          canvas
              .getOnMousePressed()
              .handle(createMouseEvent(MouseEvent.MOUSE_PRESSED, 10000, 10000));
        });

    double centerX = canvas.getWidth() / 2;
    double centerY = canvas.getHeight() / 2;
    for (double angle = 0; angle < Math.PI * 2; angle += 0.1) {
      double x = centerX + Math.cos(angle) * 35;
      double y = centerY + Math.sin(angle) * 35;
      canvas.getOnMousePressed().handle(createMouseEvent(MouseEvent.MOUSE_PRESSED, x, y));
    }
  }

  @Test
  void testRequestMove_NullListener() {
    canvas.setMoveRequestListener(null);
    assertDoesNotThrow(() -> canvas.requestMove("A1B2"));
  }

  @Test
  void testTakeImmediateSnapshot_WithExceptions() {
    assertTrue(canvas.takeSnapshot(null).isEmpty());
    RestrictedAgonBoard exceptionBoard =
        (RestrictedAgonBoard)
            Proxy.newProxyInstance(
                RestrictedAgonBoard.class.getClassLoader(),
                new Class<?>[] {RestrictedAgonBoard.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("getPieceAt")) {
                    int index = (Integer) args[0];
                    if (index % 2 == 0) throw new RuntimeException("Force Catch Block!");
                    return PieceType.WHITE_PAWN;
                  }
                  return null;
                });
    Map<String, PieceType> snap = canvas.takeSnapshot(exceptionBoard);
    assertFalse(snap.isEmpty());
  }

  @Test
  void testLoadImages_CatchBlock() throws Exception {
    Field field = HexagonCanvas.class.getDeclaredField("pieceImages");
    field.setAccessible(true);
    Map<?, ?> originalMap = (Map<?, ?>) field.get(canvas);
    field.set(canvas, Collections.emptyMap());
    Method method = HexagonCanvas.class.getDeclaredMethod("loadImages");
    method.setAccessible(true);
    assertDoesNotThrow(() -> method.invoke(canvas));
    field.set(canvas, originalMap);
  }

  @Test
  void testDraw_FallbackAndDraggedPiece() throws Exception {
    Field field = HexagonCanvas.class.getDeclaredField("pieceImages");
    field.setAccessible(true);
    ((Map<?, ?>) field.get(canvas)).clear();

    canvas.setSelectedHex("F6");
    canvas.setDraggedPiece(PieceType.WHITE_PAWN);
    canvas.setMousePosition(150, 150);

    Map<String, PieceType> snapshot = new HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    snapshot.put("G7", PieceType.BLACK_PAWN);
    canvas.applySnapshot(null, snapshot);
    assertDoesNotThrow(() -> canvas.draw());
  }

  @Test
  void testSizingMethods() {
    assertEquals(canvas.getWidth(), canvas.prefWidth(100));
    assertEquals(canvas.getHeight(), canvas.prefHeight(100));
    assertEquals(0, canvas.minWidth(100));
    assertEquals(0, canvas.minHeight(100));
    assertEquals(Double.MAX_VALUE, canvas.maxWidth(100));
    assertEquals(Double.MAX_VALUE, canvas.maxHeight(100));
    assertTrue(canvas.isResizable());
  }

  @Test
  void testMouseInteractionWithBoard() {
    RestrictedAgonBoard dummyBoard =
        (RestrictedAgonBoard)
            Proxy.newProxyInstance(
                RestrictedAgonBoard.class.getClassLoader(),
                new Class<?>[] {RestrictedAgonBoard.class},
                (proxy, method, args) -> null);
    canvas.applySnapshot(dummyBoard, new HashMap<>());
    double centerX = canvas.getWidth() / 2;
    double centerY = canvas.getHeight() / 2;
    assertDoesNotThrow(
        () -> {
          canvas
              .getOnMouseDragged()
              .handle(createMouseEvent(MouseEvent.MOUSE_DRAGGED, centerX, centerY));
          canvas
              .getOnMouseReleased()
              .handle(createMouseEvent(MouseEvent.MOUSE_RELEASED, centerX, centerY));
        });
  }

  @Test
  void testPixelToHex_InsideBoundaries() {
    try {
      Method method =
          HexagonCanvas.class.getDeclaredMethod("pixelToHex", double.class, double.class);
      method.setAccessible(true);
      String result = (String) method.invoke(canvas, canvas.getWidth() / 2, canvas.getHeight() / 2);
      assertNotNull(result);
      assertEquals("F6", result);
    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  @Test
  void testDraw_WithPiecesAndSelection() {
    Map<String, PieceType> snapshot = new HashMap<>();
    snapshot.put("F6", PieceType.WHITE_QUEEN);
    snapshot.put("G7", PieceType.BLACK_PAWN);
    canvas.applySnapshot(null, snapshot);
    canvas.setDraggedPiece(null);
    assertDoesNotThrow(() -> canvas.draw());
    canvas.setSelectedHex("G7");
    canvas.setDraggedPiece(PieceType.BLACK_PAWN);
    assertDoesNotThrow(() -> canvas.draw());
  }

  @Test
  void testPixelToHex_BoundaryConditions() throws Exception {
    Method method = HexagonCanvas.class.getDeclaredMethod("pixelToHex", double.class, double.class);
    method.setAccessible(true);
    String centerHex =
        (String) method.invoke(canvas, canvas.getWidth() / 2, canvas.getHeight() / 2);
    assertEquals("F6", centerHex);
    String outHex = (String) method.invoke(canvas, 10000.0, 10000.0);
    assertNull(outHex);
  }

  @Test
  void testTakeImmediateSnapshot_WithActualPiece() {
    final int f6Index = CoordinateMapper.toIndex('F', 6);
    RestrictedAgonBoard boardWithPiece =
        new RestrictedAgonBoard() {
          @Override
          public List<Move> generateLegalMoves(Color color) {
            return List.of();
          }

          @Override
          public PieceType getPieceAt(int index) {
            if (index == f6Index) return PieceType.WHITE_QUEEN;
            return null;
          }
        };
    Map<String, PieceType> snapshot = canvas.takeSnapshot(boardWithPiece);
    assertTrue(snapshot.containsKey("F6"), "Snapshot must contain hex F6");
    assertEquals(
        PieceType.WHITE_QUEEN, snapshot.get("F6"), "The piece on F6 must be a WHITE_QUEEN");
  }

  @Test
  void testDraw_PieceVisibilityLogic() {
    Map<String, PieceType> snapshot = new HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    canvas.applySnapshot(null, snapshot);
    canvas.setDraggedPiece(null);
    assertDoesNotThrow(() -> canvas.draw());
    canvas.setSelectedHex("F6");
    canvas.setDraggedPiece(PieceType.WHITE_PAWN);
    canvas.setMousePosition(200, 200);
    assertDoesNotThrow(() -> canvas.draw());
  }

  /**
   * Tests full branch coverage for math coordinates calculation. Ensures an out-of-bounds click
   * correctly returns null.
   */
  @Test
  void testPixelToHex_FullBranchCoverage() throws Exception {
    canvas.setWidth(1000);
    canvas.setHeight(1000);
    java.lang.reflect.Method method =
        HexagonCanvas.class.getDeclaredMethod("pixelToHex", double.class, double.class);
    method.setAccessible(true);
    String inside = (String) method.invoke(canvas, 500.0, 500.0);
    assertEquals("F6", inside, "Center (500, 500) should be detected as F6");
    String outside = (String) method.invoke(canvas, 0.0, 0.0);
    assertNull(outside, "A click at (0,0) is out of bounds and must return null");
  }

  @Test
  void testDraw_PieceVisibilityBranches() {
    Map<String, PieceType> snapshot = new java.util.HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    canvas.applySnapshot(null, snapshot);
    canvas.setDraggedPiece(null);
    assertDoesNotThrow(() -> canvas.draw());
    canvas.setSelectedHex("F6");
    canvas.setDraggedPiece(PieceType.WHITE_PAWN);
    canvas.setMousePosition(100, 100);
    assertDoesNotThrow(() -> canvas.draw());
  }

  @Test
  void testDraw_PieceNullAndNotNull() {
    Map<String, PieceType> snapshot = new java.util.HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    canvas.applySnapshot(null, snapshot);
    assertDoesNotThrow(() -> canvas.draw());
  }
}
