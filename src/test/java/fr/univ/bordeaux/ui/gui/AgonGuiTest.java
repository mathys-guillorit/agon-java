package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the {@link AgonGui} user interface wrapper.
 * <p>
 * Ensures that the GUI correctly intercepts and routes messages, handles null states gracefully,
 * and manages the JavaFX lifecycle properly during test execution.
 */
public class AgonGuiTest {

    private AgonGui agonGui;
    private GameConfig realConfig;
    private FakeGameViewController fakeController;

    /**
     * Initializes the JavaFX toolkit environment before any tests are run.
     * Prevents implicit exit to avoid JVM shutdown issues during test suites.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for toolkit startup.
     */
    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                latch.countDown();
            });
        } catch (IllegalStateException e) {
            Platform.runLater(() -> Platform.setImplicitExit(false));
            latch.countDown();
        }
        latch.await(5, TimeUnit.SECONDS);
    }


        /**
         * A mock controller used to intercept GUI calls without triggering actual JavaFX rendering.
         */
        static class FakeGameViewController extends GameViewController {
            public String lastMessage = null;
            public String lastInfo = null;
            public String lastError = null;
            public String lastWarn = null;
            public boolean helpCalled = false;
            private HexagonCanvas fakeCanvas = new HexagonCanvas();

            @Override
            public void routeMessage(String m) {
                if (m != null && m.contains("Current Player")) {
                    this.lastMessage = m;
                } else {
                    this.lastInfo = m;
                }
            }

            @Override public void updateMessage(String m) { lastMessage = m; }
            @Override public void showInfo(String m) { lastInfo = m; }
            @Override public void showError(String m) { lastError = m; }
            @Override public void showWarn(String m) { lastWarn = m; }
            @Override public void showHelp() { helpCalled = true; }
            @Override public HexagonCanvas getHexCanvas() { return fakeCanvas; }
        }

    /**
     * Sets up a fresh instance of the GUI and injects the mock controller via reflection
     * before each test.
     *
     * @throws Exception if reflection injection fails.
     */
        @BeforeEach
        void setUp() throws Exception {
            realConfig = new GameConfig();
            agonGui = new AgonGui(realConfig);
            fakeController = new FakeGameViewController();

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    Field controllerField = AgonApp.class.getDeclaredField("controller");
                    controllerField.setAccessible(true);
                    controllerField.set(null, fakeController);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
            latch.await(2, TimeUnit.SECONDS);
        }

    /**
     * Cleans up the mocked controller after each test to ensure test isolation.
     *
     * @throws Exception if reflection cleanup fails.
     */
    @AfterEach
    void tearDown() throws Exception {
        Field controllerField = AgonApp.class.getDeclaredField("controller");
        controllerField.setAccessible(true);
        controllerField.set(null, null);
    }

    /**
     * Utility method to wait for JavaFX Platform.runLater tasks to complete.
     *
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    private void waitForRunLater() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await(2, TimeUnit.SECONDS);
    }

    @Test
    void testGetConfig() {
        assertEquals(realConfig, agonGui.getConfig());
    }

    @Test
    void testCommandQueue_SendAndReceive() {
        agonGui.sendCommand("new --blitz true");
        String receivedCommand = agonGui.getUserInput();
        assertEquals("new --blitz true", receivedCommand);
    }

    @Test
    void testIsRunning() {
        assertTrue(agonGui.isRunning());
    }

    @Test
    void testGettersAndSetters() {
        assertFalse(agonGui.getDebugMode().get());
        assertDoesNotThrow(() -> agonGui.setVerbose(true));
    }


    @Test
    void testShowMessage_CurrentPlayer_GoesToUpdateMessage() throws InterruptedException {
        agonGui.showMessage(">> Current Player is White");
        waitForRunLater();

        assertEquals(">> Current Player is White", fakeController.lastMessage);
        assertNull(fakeController.lastInfo);
    }

    @Test
    void testShowMessage_NormalText_GoesToPopup() throws InterruptedException {
        agonGui.showMessage("Hint: Move pawn to F6");
        waitForRunLater();

        assertEquals("Hint: Move pawn to F6", fakeController.lastInfo);
        assertNull(fakeController.lastMessage);
    }

    @Test
    void testShowInfo_CurrentPlayer_GoesToUpdateMessage() throws InterruptedException {
        agonGui.showInfo("Current Player: Black");
        waitForRunLater();
        assertEquals("Current Player: Black", fakeController.lastMessage);
    }

    @Test
    void testShowInfo_NormalText_GoesToPopup() throws InterruptedException {
        agonGui.showInfo("Configuration saved.");
        waitForRunLater();
        assertEquals("Configuration saved.", fakeController.lastInfo);
    }

    @Test
    void testShowError() throws InterruptedException {
        agonGui.showError("A critical error occurred");
        waitForRunLater();
        assertEquals("A critical error occurred", fakeController.lastError);
    }

    @Test
    void testShowWarn() throws InterruptedException {
        agonGui.showWarn("Be careful!");
        waitForRunLater();
        assertEquals("Be careful!", fakeController.lastWarn);
    }

    @Test
    void testShowHelp() throws InterruptedException {
        agonGui.showHelp();
        waitForRunLater();
        assertTrue(fakeController.helpCalled);
    }


    @Test
    void testMethods_WhenControllerIsNull_DoesNotCrash() throws Exception {
        Field controllerField = AgonApp.class.getDeclaredField("controller");
        controllerField.setAccessible(true);
        controllerField.set(null, null);

        assertDoesNotThrow(() -> {
            agonGui.updateBoard(null);
            agonGui.showMessage("Test");
            agonGui.showInfo("Test");
            agonGui.showError("Test");
            agonGui.showWarn("Test");
            agonGui.showHelp();
            waitForRunLater();
        });
    }

    @Test
    void testMethods_WhenMessageIsNull_DoesNotCrash() throws InterruptedException {
        assertDoesNotThrow(() -> {
            agonGui.showMessage(null);
            agonGui.showInfo(null);
            waitForRunLater();
        });
    }

    @Test
    void testUpdateBoard() throws InterruptedException {
        RestrictedAgonBoard dummyBoard = null;
        agonGui.updateBoard(dummyBoard);
        waitForRunLater();
        assertNotNull(fakeController.getHexCanvas());
    }



    @Test
    void testUpdateBoard_WhenControllerIsNull_DoesNotCrash() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Field controllerField = AgonApp.class.getDeclaredField("controller");
                controllerField.setAccessible(true);
                controllerField.set(null, null); // INJECTION DE NULL
            } catch (Exception e) {}
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
        assertDoesNotThrow(() -> agonGui.updateBoard(null),
                "Method should not crash if canvas is null");
    }

    /**
     * Ensures that updateBoard does not throw NullPointerException if the controller exists
     * but the canvas has not been initialized.
     * * @throws Exception if reflection injection fails.
     */
    @Test
    void testUpdateBoard_WhenCanvasIsNull_DoesNotCrash() throws Exception {
        GameViewController controllerWithoutCanvas = new FakeGameViewController() {
            @Override
            public HexagonCanvas getHexCanvas() {
                return null;
            }
        };
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Field controllerField = AgonApp.class.getDeclaredField("controller");
                controllerField.setAccessible(true);
                controllerField.set(null, controllerWithoutCanvas);
            } catch (Exception e) {}
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
        assertDoesNotThrow(() -> agonGui.updateBoard(null),
                "Method should not crash if canvas is null");
    }
}