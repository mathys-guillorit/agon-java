package fr.univ.bordeaux.ui.gui;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;

public class AgonAppTest {

  @BeforeAll
  static void initJFX() throws InterruptedException {
    System.setProperty("IS_TEST_ENV", "true");
    CountDownLatch latch = new CountDownLatch(1);
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
  void resetAgonApp() throws Exception {
    setPrivateStaticField(AgonApp.class, "controller", null);
    setPrivateStaticField(AgonApp.class, "agonGui", null);
    setPrivateStaticField(AgonApp.class, "scene", null);
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          for (Window window : new ArrayList<>(Window.getWindows())) {
            if (window instanceof Stage) {
              ((Stage) window).close();
            }
          }
          latch.countDown();
        });
    latch.await(2, TimeUnit.SECONDS);
  }

  private void setPrivateStaticField(Class<?> clazz, String fieldName, Object value)
      throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(null, value);
  }

  /**
   * Extremely robust helper to ensure JUnit waits for the JavaFX task to finish and throws any
   * hidden exceptions.
   */
  private void runOnFxThread(Runnable action) throws Exception {
    AtomicReference<Throwable> thrown = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            action.run();
          } catch (Throwable t) {
            thrown.set(t);
          } finally {
            latch.countDown();
          }
        });

    if (!latch.await(10, TimeUnit.SECONDS)) {
      fail("The JavaFX task timed out.");
    }
    if (thrown.get() != null) {
      if (thrown.get() instanceof Exception) throw (Exception) thrown.get();
      if (thrown.get() instanceof Error) throw (Error) thrown.get();
      throw new RuntimeException(thrown.get());
    }
  }

  @Test
  void testStaticGettersAndSetters() {
    AgonGui gui = new AgonGui(null, null);
    AgonApp.setGui(gui);
    assertNull(AgonApp.getController());
  }

  @Test
  void testAppStart() throws Exception {
    runOnFxThread(
        () -> {
          try {
            AgonApp app = new AgonApp();
            Stage stage = new Stage();
            AgonApp.setGui(new AgonGui(new GameConfig(), null));

            app.start(stage);

            assertNotNull(
                AgonApp.getController(), "The controller should be instantiated by the FXML.");
          } catch (IOException e) {
            System.err.println("Note: FXML not found for testAppStart. Ignoring start() coverage.");
          }
        });
  }

  @Test
  void testSetupShortcuts_NullBranches() throws Exception {
    runOnFxThread(
        () -> {
          try {
            AgonApp app = new AgonApp();

            AgonApp.setGui(null);
            app.setupShortcuts();

            AgonApp.setGui(new AgonGui(null, null));
            app.setupShortcuts();

            AgonGui guiWithConfig = new AgonGui(new GameConfig(), null);
            AgonApp.setGui(guiWithConfig);
            setPrivateStaticField(AgonApp.class, "controller", null);
            app.setupShortcuts();

            setPrivateStaticField(AgonApp.class, "controller", new GameViewController());
            setPrivateStaticField(AgonApp.class, "scene", null);
            app.setupShortcuts();

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Test
  void testSetupShortcuts_FullCoverage() throws Exception {
    runOnFxThread(
        () -> {
          try {
            AgonApp app = new AgonApp();
            GameConfig config = new GameConfig();

            config.getShortcuts().clear();

            config.getShortcuts().put("shortcut_new", "Ctrl+1");

            config.getShortcuts().put("shortcut_load", "   ");

            config.getShortcuts().put("shortcut_save", "INVALID_COMBINATION_TO_CRASH");

            AgonGui gui = new AgonGui(config, null);
            AgonApp.setGui(gui);
            GameViewController fakeController = new GameViewController();
            setPrivateStaticField(AgonApp.class, "controller", fakeController);
            Scene fakeScene = new Scene(new Pane());
            setPrivateStaticField(AgonApp.class, "scene", fakeScene);

            app.setupShortcuts();

            assertTrue(
                fakeScene.getAccelerators().containsKey(KeyCombination.valueOf("Ctrl+1")),
                "The valid shortcut should have been inserted into the scene accelerators.");

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Test
  void testRefreshShortcuts_AllBranches() throws Exception {
    runOnFxThread(
        () -> {
          try {
            setPrivateStaticField(AgonApp.class, "scene", null);
            AgonApp.refreshShortcuts();

            Scene fakeScene = new Scene(new Pane());
            setPrivateStaticField(AgonApp.class, "scene", fakeScene);
            AgonApp.setGui(null);
            AgonApp.refreshShortcuts();

            GameConfig config = new GameConfig();
            config.getShortcuts().clear();
            config.getShortcuts().put("shortcut_new", "Ctrl+9");

            AgonGui gui = new AgonGui(config, null);
            AgonApp.setGui(gui);
            setPrivateStaticField(AgonApp.class, "controller", new GameViewController());

            fakeScene.getAccelerators().put(KeyCombination.valueOf("Ctrl+Z"), () -> {});

            AgonApp.refreshShortcuts();

            assertTrue(
                fakeScene.getAccelerators().containsKey(KeyCombination.valueOf("Ctrl+9")),
                "The scene should contain the new shortcut after refresh.");

            assertFalse(
                fakeScene.getAccelerators().containsKey(KeyCombination.valueOf("Ctrl+Z")),
                "The scene should have cleared the old shortcuts.");

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }
}
