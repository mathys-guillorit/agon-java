package fr.univ.bordeaux.ui.gui.components;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import java.lang.reflect.Field;
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
 * logic branches, and piece visualization rules. Fully PMD compliant (final variables, single
 * returns).
 */
public class HexagonCanvasTest {

  private HexagonCanvas canvas;

  @BeforeAll
  static void initJFX() throws InterruptedException {
    System.setProperty("IS_TEST_ENV", "true");
    final CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(
          () -> {
            Platform.setImplicitExit(false);
            latch.countDown();
          });
    } catch (IllegalStateException e) {
      Platform.runLater(
          () -> {
            Platform.setImplicitExit(false);
            latch.countDown();
          });
    }
    latch.await(2, TimeUnit.SECONDS);
  }

  @BeforeEach
  void setUp() {
    canvas = new HexagonCanvas();
  }

  private MouseEvent createMouseEvent(
      final javafx.event.EventType<MouseEvent> type, final double x, final double y) {
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
    final PieceType testPiece = PieceType.WHITE_PAWN;
    canvas.setSelectedHex("F6");
    canvas.setDraggedPiece(testPiece);
    canvas.setMousePosition(100.5, 200.5);
    assertDoesNotThrow(() -> canvas.draw());
  }

  @Test
  void testApplySnapshot() {
    final Map<String, PieceType> snapshot = new HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    snapshot.put("A1", PieceType.BLACK_QUEEN);

    final RestrictedAgonBoard dummyBoard = null;
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
    final RestrictedAgonBoard dummyBoard =
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

    final double centerX = canvas.getWidth() / 2;
    final double centerY = canvas.getHeight() / 2;
    for (double angle = 0; angle < Math.PI * 2; angle += 0.1) {
      final double x = centerX + Math.cos(angle) * 35;
      final double y = centerY + Math.sin(angle) * 35;
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

    final RestrictedAgonBoard exceptionBoard =
        (RestrictedAgonBoard)
            Proxy.newProxyInstance(
                RestrictedAgonBoard.class.getClassLoader(),
                new Class<?>[] {RestrictedAgonBoard.class},
                (proxy, method, args) -> {
                  Object result = null;
                  if ("getPieceAt".equals(method.getName())) {
                    final int index = (Integer) args[0];
                    if (index % 2 == 0) {
                      throw new RuntimeException("Force Catch Block!");
                    }
                    result = PieceType.WHITE_PAWN;
                  }
                  return result;
                });

    final Map<String, PieceType> snap = canvas.takeSnapshot(exceptionBoard);
    assertFalse(snap.isEmpty());
  }

  @Test
  void testDraw_FallbackAndDraggedPiece() throws Exception {
    final Field tmField = HexagonCanvas.class.getDeclaredField("textureManager");
    tmField.setAccessible(true);
    final Object manager = tmField.get(canvas);

    final Field piField = manager.getClass().getDeclaredField("pieceImages");
    piField.setAccessible(true);
    ((Map<?, ?>) piField.get(manager)).clear();

    canvas.setSelectedHex("F6");
    canvas.setDraggedPiece(PieceType.WHITE_PAWN);
    canvas.setMousePosition(150, 150);

    final Map<String, PieceType> snapshot = new HashMap<>();
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
    final RestrictedAgonBoard dummyBoard =
        (RestrictedAgonBoard)
            Proxy.newProxyInstance(
                RestrictedAgonBoard.class.getClassLoader(),
                new Class<?>[] {RestrictedAgonBoard.class},
                (proxy, method, args) -> null);

    canvas.applySnapshot(dummyBoard, new HashMap<>());
    final double centerX = canvas.getWidth() / 2;
    final double centerY = canvas.getHeight() / 2;

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
    final String result =
        HexMath.pixelToHex(
            canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight());
    assertNotNull(result);
    assertEquals("F6", result);
  }

  @Test
  void testDraw_WithPiecesAndSelection() {
    final Map<String, PieceType> snapshot = new HashMap<>();
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
  void testPixelToHex_BoundaryConditions() {
    final String centerHex =
        HexMath.pixelToHex(
            canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight());
    assertEquals("F6", centerHex);

    final String outHex =
        HexMath.pixelToHex(10000.0, 10000.0, canvas.getWidth(), canvas.getHeight());
    assertNull(outHex);
  }

  @Test
  void testTakeImmediateSnapshot_WithActualPiece() {
    final int f6Index = CoordinateMapper.toIndex('F', 6);
    final RestrictedAgonBoard boardWithPiece =
        new RestrictedAgonBoard() {
          @Override
          public List<Move> generateLegalMoves(final Color color) {
            return Collections.emptyList();
          }

          @Override
          public PieceType getPieceAt(final int index) {
            PieceType type = null;
            if (index == f6Index) {
              type = PieceType.WHITE_QUEEN;
            }
            return type;
          }
        };

    final Map<String, PieceType> snapshot = canvas.takeSnapshot(boardWithPiece);
    assertTrue(snapshot.containsKey("F6"), "Snapshot must contain hex F6");
    assertEquals(
        PieceType.WHITE_QUEEN, snapshot.get("F6"), "The piece on F6 must be a WHITE_QUEEN");
  }

  @Test
  void testDraw_PieceVisibilityLogic() {
    final Map<String, PieceType> snapshot = new HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    canvas.applySnapshot(null, snapshot);
    canvas.setDraggedPiece(null);
    assertDoesNotThrow(() -> canvas.draw());

    canvas.setSelectedHex("F6");
    canvas.setDraggedPiece(PieceType.WHITE_PAWN);
    canvas.setMousePosition(200, 200);
    assertDoesNotThrow(() -> canvas.draw());
  }

  @Test
  void testPixelToHex_FullBranchCoverage() {
    canvas.setWidth(1000);
    canvas.setHeight(1000);

    final String inside = HexMath.pixelToHex(500.0, 500.0, 1000.0, 1000.0);
    assertEquals("F6", inside, "Center (500, 500) should be detected as F6");

    final String outside = HexMath.pixelToHex(0.0, 0.0, 1000.0, 1000.0);
    assertNull(outside, "A click at (0,0) is out of bounds and must return null");
  }

  @Test
  void testDraw_PieceVisibilityBranches() {
    final Map<String, PieceType> snapshot = new HashMap<>();
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
    final Map<String, PieceType> snapshot = new HashMap<>();
    snapshot.put("F6", PieceType.WHITE_PAWN);
    canvas.applySnapshot(null, snapshot);
    assertDoesNotThrow(() -> canvas.draw());
  }
}
