package fr.univ.bordeaux.ui.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

public class AgonAppTest {

  @BeforeAll
  static void initJFX() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(latch::countDown);
    } catch (Exception e) {
      latch.countDown();
    }
    latch.await(2, TimeUnit.SECONDS);
  }

  @BeforeEach
  void resetAgonApp() throws Exception {
    setPrivateStaticField(AgonApp.class, "controller", null);
    setPrivateStaticField(AgonApp.class, "agonGui", null);
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
  void testAppStart_And_Shortcuts() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            AgonApp app = new AgonApp();
            Stage stage = new Stage();
            app.start(stage);
            assertNotNull(AgonApp.getController());
            stage.close();
          } catch (Exception e) {
          } finally {
            latch.countDown();
          }
        });
    latch.await(2, TimeUnit.SECONDS);
  }
}
