package fr.univ.bordeaux.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.ui.cli.AgonShell;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** testing resources loading from testing area (testing repertory) */
public class ResourceLoadingTest {

  @Test
  @DisplayName("resource with URL and '/' path notation")
  void loadResourceUsingURLTest() throws IOException {
    // shows message only on errors
    String reason = "(testing if a the path exists)";
    URL url = getClass().getResource("/cmdsInformations/agonShellMenu.txt");
    assertNotNull(
        url,
        "(invalid path or not existing file at: \""
            + url
            + "\") Resource URL ="
            + url
            + " "
            + reason);
    URL url2 = getClass().getResource("/invalidRepertoryHere/imposssible.path.txt");
    reason = "(testing if a non existing path is valid)";
    assertEquals(null, url2, "error: url is not null: \"" + url2 + "\" it must be null " + reason);
    final String tuple = "new [ARGS] : run a new game";
    final String content = new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
    reason =
        "(if the file exists it must contains a content (at least a part or it's full content))";
    assertTrue(
        content.contains(tuple),
        "error: \"" + content + "\" doesn't contains: \"" + tuple + "\" " + reason);
  }

  @Test
  @DisplayName("resource with \"InputStream\" and '/' path notation")
  void loadResourceUsingInputStreamTest() throws IOException {
    String reason = "(testing if a the path exists)";
    InputStream s = AgonShell.class.getResourceAsStream("/cmdsInformations/agonShellMenu.txt");
    assertNotNull(s, "Resource stream =" + s + " " + reason);
    final String tuple = "new [ARGS] : run a new game";
    reason =
        "(testing if loading a non existing path returns nothing (null) from the InputStream loader)";
    InputStream s2 =
        AgonShell.class.getResourceAsStream("/notExistingPathHere/mustThrowAnError.txt");
    assertEquals(null, s2, "error: url is not null: \"" + s2 + "\" it must be null " + reason);
    final String content = new String(s.readAllBytes(), StandardCharsets.UTF_8);
    assertTrue(
        content.contains(tuple), "error: \"" + content + "\" doesn't contains: \"" + tuple + "\"");
  }
}
