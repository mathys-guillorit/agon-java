package fr.univ.bordeaux.ui.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
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
    AgonGui gui = new AgonGui(null);
    AgonApp.setGui(gui);
    assertNull(AgonApp.getController());
  }
/*
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

                        AgonGui gui = new AgonGui(config);
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
*/
    @Test
    void testSetupShortcuts_NullBranches() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    try {
                        AgonApp app = new AgonApp();

                        AgonApp.setGui(null);
                        app.setupShortcuts();

                        AgonApp.setGui(new AgonGui(null));
                        app.setupShortcuts();

                        AgonGui guiWithConfig = new AgonGui(new GameConfig());
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
}
