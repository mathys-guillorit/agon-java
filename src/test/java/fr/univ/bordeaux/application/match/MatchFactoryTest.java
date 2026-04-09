package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchFactoryTest {

  @Test
  @DisplayName("createOnlineMatch returns StandardMatch when blitzMode is false")
  void createOnlineMatchStandard() {
    Match match = MatchFactory.createOnlineMatch("Alice", "Bob", false);

    assertNotNull(match);
    assertInstanceOf(StandardMatch.class, match);
    assertEquals("Alice", match.getWhitePlayer().getName());
    assertEquals("Bob", match.getBlackPlayer().getName());
  }

  @Test
  @DisplayName("createOnlineMatch returns BlitzMatch when blitzMode is true")
  void createOnlineMatchBlitz() {
    Match match = MatchFactory.createOnlineMatch("Alice", "Bob", true);

    assertNotNull(match);
    assertInstanceOf(BlitzMatch.class, match);
    assertEquals("Alice", match.getWhitePlayer().getName());
    assertEquals("Bob", match.getBlackPlayer().getName());
  }
}
