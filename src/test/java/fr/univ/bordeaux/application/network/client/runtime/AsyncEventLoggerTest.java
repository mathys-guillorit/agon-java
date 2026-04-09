package fr.univ.bordeaux.application.network.client.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

class AsyncEventLoggerTest {

  @Test
  void logMethodsDoNotThrow() {
    assertDoesNotThrow(() -> AsyncEventLogger.logInfo("info message"));
    assertDoesNotThrow(() -> AsyncEventLogger.logWarn("warn message"));
    assertDoesNotThrow(() -> AsyncEventLogger.logError("error message"));
  }

  @Test
  void privateConstructorIsCovered() throws Exception {
    Constructor<AsyncEventLogger> constructor = AsyncEventLogger.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    assertDoesNotThrow(() -> constructor.newInstance());
  }
}
