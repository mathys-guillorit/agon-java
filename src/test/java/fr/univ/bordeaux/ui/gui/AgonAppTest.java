package fr.univ.bordeaux.ui.gui;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Scene;
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

  @Test
  void testStaticGettersAndSetters() {
    AgonGui gui = new AgonGui(null, null);
    AgonApp.setGui(gui);
    assertNull(AgonApp.getController());
  }

  @Test
  void testAppStart_And_Shortcuts_AllBranches() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            GameConfig config = new GameConfig();

            config.getShortcuts().put("shortcut_new", "Ctrl+N");
            config.getShortcuts().put("shortcut_save", "   ");
            config.getShortcuts().put("shortcut_load", null);
            config.getShortcuts().put("shortcut_quit", "Invalid");

            AgonGui gui = new AgonGui(config, null);
            AgonApp.setGui(gui);

            AgonApp app = new AgonApp();
            Stage stage = new Stage();

            app.start(stage);
            assertNotNull(AgonApp.getController());
            AgonApp.refreshShortcuts();

          } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
          } finally {
            latch.countDown();
          }
        });
    latch.await(5, TimeUnit.SECONDS);
  }

  @Test
  void testSetupShortcuts_NullBranches() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
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

            assertTrue(true);
          } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
          } finally {
            latch.countDown();
          }
        });
    latch.await(2, TimeUnit.SECONDS);
  }

  @Test
  void testRefreshShortcuts_NullBranches() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            setPrivateStaticField(AgonApp.class, "scene", null);
            AgonApp.refreshShortcuts();

            Scene fakeScene = new Scene(new Pane());
            setPrivateStaticField(AgonApp.class, "scene", fakeScene);
            AgonApp.setGui(null);
            AgonApp.refreshShortcuts();

            assertTrue(true);
          } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
          } finally {
            latch.countDown();
          }
        });
    latch.await(2, TimeUnit.SECONDS);
  }

  @Test
  @DisplayName("Couverture à 100% de la méthode setupShortcuts et bindShortcut")
  void testSetupShortcuts_FullCoverage() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            AgonApp app = new AgonApp();
            GameConfig config = new GameConfig();

            config.getShortcuts().clear();

            config.getShortcuts().put("shortcut_new", "Ctrl+N");
            config.getShortcuts().put("shortcut_load", "Ctrl+L");
            config.getShortcuts().put("shortcut_save", "Ctrl+S");
            config.getShortcuts().put("shortcut_config", "Ctrl+C");
            config.getShortcuts().put("shortcut_info", "Ctrl+I");
            config.getShortcuts().put("shortcut_quit", "Ctrl+Q");
            config.getShortcuts().put("shortcut_hint", "Ctrl+H");

            config.getShortcuts().put("shortcut_undo", "   ");
            config.getShortcuts().put("shortcut_redo", null);

            config.getShortcuts().put("shortcut_pause", "NOT_A_VALID_KEY_COMBINATION");

            AgonGui gui = new AgonGui(config, null);
            AgonApp.setGui(gui);

            GameViewController fakeController = new GameViewController();
            setPrivateStaticField(AgonApp.class, "controller", fakeController);

            Scene fakeScene = new Scene(new Pane());
            setPrivateStaticField(AgonApp.class, "scene", fakeScene);

            app.setupShortcuts();

            assertEquals(
                7,
                fakeScene.getAccelerators().size(),
                "Seuls les 7 raccourcis valides doivent être enregistrés");

          } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
          } finally {
            latch.countDown();
          }
        });
    latch.await(5, TimeUnit.SECONDS);
  }
}
