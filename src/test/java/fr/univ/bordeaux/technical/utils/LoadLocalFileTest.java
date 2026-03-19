package fr.univ.bordeaux.technical.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class LoadLocalFileTest {

  /**
   * Verifies that the loader correctly applies the fallback mechanism when a resource file cannot
   * be found.
   *
   * <p>This test ensures that if the input stream is null (e.g., the file does not exist), the
   * provided default content is used instead.
   */
  @Test
  public void testLoadLocalFileWithNullStream() throws IOException {
    LoadLocalFile loader = new LoadLocalFile("missing_resource.txt");
    assertEquals(
        "",
        loader.getContent(),
        "The default content should be used when the resource stream is null.");
  }
}
