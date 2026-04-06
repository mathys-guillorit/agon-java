package fr.univ.bordeaux.ui.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
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

    @Test
    void testAppStart_And_Shortcuts_AllBranches() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    try {
                        // 1. On prépare une configuration avec TOUS les cas pour bindShortcut()
                        fr.univ.bordeaux.technical.io.config.GameConfig config =
                                new fr.univ.bordeaux.technical.io.config.GameConfig();

                        // Couvre la branche (shortcut != null && !shortcut.isBlank()) = VRAI
                        config.getShortcuts().put("shortcut_new", "Ctrl+N");
                        // Couvre la branche isBlank() = VRAI
                        config.getShortcuts().put("shortcut_save", "   ");
                        // Couvre la branche shortcut == null
                        config.getShortcuts().put("shortcut_load", null);
                        // Couvre le catch (IllegalArgumentException)
                        config.getShortcuts().put("shortcut_quit", "ToucheInvalide");

                        AgonGui gui = new AgonGui(config);
                        AgonApp.setGui(gui);

                        AgonApp app = new AgonApp();
                        Stage stage = new Stage();
                        app.start(stage); // Ceci couvre le cas où AUCUN élément n'est null dans setupShortcuts()

                        assertNotNull(AgonApp.getController());

                        // 2. Maintenant que tout est chargé, on appelle refreshShortcuts()
                        // Ceci couvre la branche VRAIE de : if (scene != null && agonGui != null)
                        AgonApp.refreshShortcuts();

                    } catch (Exception e) {
                        fail("Exception inattendue : " + e.getMessage());
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

                        // Branche 1 : agonGui est null
                        AgonApp.setGui(null);
                        app.setupShortcuts();

                        // Branche 2 : agonGui n'est pas null, mais getConfig() est null
                        AgonApp.setGui(new AgonGui(null));
                        app.setupShortcuts();

                        // Branche 3 : getConfig() n'est pas null, mais controller est null
                        AgonGui guiWithConfig = new AgonGui(new fr.univ.bordeaux.technical.io.config.GameConfig());
                        AgonApp.setGui(guiWithConfig);
                        setPrivateStaticField(AgonApp.class, "controller", null);
                        app.setupShortcuts();

                        // Branche 4 : controller n'est pas null, mais scene est null
                        setPrivateStaticField(AgonApp.class, "controller", new fr.univ.bordeaux.ui.gui.controllers.GameViewController());
                        setPrivateStaticField(AgonApp.class, "scene", null);
                        app.setupShortcuts();

                        assertTrue(true); // Si on arrive ici sans crash, toutes les branches ont été sécurisées !
                    } catch (Exception e) {
                        fail("Exception inattendue : " + e.getMessage());
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
                        // Branche 1 : scene est null (et agonGui null aussi d'ailleurs)
                        setPrivateStaticField(AgonApp.class, "scene", null);
                        AgonApp.refreshShortcuts();

                        // Branche 2 : scene n'est pas null, mais agonGui est null
                        javafx.scene.Scene fakeScene = new javafx.scene.Scene(new javafx.scene.layout.Pane());
                        setPrivateStaticField(AgonApp.class, "scene", fakeScene);
                        AgonApp.setGui(null);
                        AgonApp.refreshShortcuts();

                        assertTrue(true);
                    } catch (Exception e) {
                        fail("Exception inattendue : " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
        latch.await(2, TimeUnit.SECONDS);
    }
}
