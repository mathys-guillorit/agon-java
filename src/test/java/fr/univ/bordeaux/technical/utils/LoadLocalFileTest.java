package fr.univ.bordeaux.technical.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

public class LoadLocalFileTest {

  @Test
  @Description("loading a file and check behavior when file exists or not")
  void regularUseIncludingExceptionsTest() throws Exception {
    assertThrows(IOException.class, () -> new LoadLocalFile("this is a test of loading a file"));
    final String path = "/cmdsInformations/desc/test.txt";
    String content = new LoadLocalFile(path).getContent();
    assertTrue(true, "file loading failed");
    assertFalse(content.isEmpty());
    assertTrue(content.contains("multiple lines as an"));
    assertTrue(content.contains("1"));
    assertTrue(content.contains("2"));
  }
}
