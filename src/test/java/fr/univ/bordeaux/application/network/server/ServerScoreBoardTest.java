package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.server.game.ServerPlayerStats;
import fr.univ.bordeaux.application.network.server.game.ServerScoreboard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerScoreBoardTest {

  @Test
  @DisplayName("Scoreboard is empty initially")
  void scoreboardEmptyInitially() {
    ServerScoreboard scoreboard = new ServerScoreboard();

    assertTrue(scoreboard.isEmpty());
    assertTrue(scoreboard.getAllStats().isEmpty());
  }

  @Test
  @DisplayName("GetOrCreateStats creates a new player stats entry")
  void getOrCreateStatsCreatesEntry() {
    ServerScoreboard scoreboard = new ServerScoreboard();

    ServerPlayerStats stats = scoreboard.getOrCreateStats("Alice");

    assertNotNull(stats);
    assertEquals("Alice", stats.getPlayerName());
    assertFalse(scoreboard.isEmpty());
    assertEquals(1, scoreboard.getAllStats().size());
  }

  @Test
  @DisplayName("GetOrCreateStats returns same entry for same name ignoring case")
  void getOrCreateStatsSameNameCaseInsensitive() {
    ServerScoreboard scoreboard = new ServerScoreboard();

    ServerPlayerStats s1 = scoreboard.getOrCreateStats("Alice");
    ServerPlayerStats s2 = scoreboard.getOrCreateStats("alice");

    assertSame(s1, s2);
    assertEquals(1, scoreboard.getAllStats().size());
  }

  @Test
  @DisplayName("RecordWin updates player stats correctly")
  void recordWin() {
    ServerScoreboard scoreboard = new ServerScoreboard();

    scoreboard.recordWin("Alice");

    ServerPlayerStats stats = scoreboard.getOrCreateStats("Alice");
    assertEquals(1, stats.getWins());
    assertEquals(0, stats.getLosses());
    assertEquals(1, stats.getGames());
  }

  @Test
  @DisplayName("RecordLoss updates player stats correctly")
  void recordLoss() {
    ServerScoreboard scoreboard = new ServerScoreboard();

    scoreboard.recordLoss("Alice");

    ServerPlayerStats stats = scoreboard.getOrCreateStats("Alice");
    assertEquals(0, stats.getWins());
    assertEquals(1, stats.getLosses());
    assertEquals(1, stats.getGames());
  }

  @Test
  @DisplayName("GetAllStats returns all created player stats")
  void getAllStats() {
    ServerScoreboard scoreboard = new ServerScoreboard();

    scoreboard.recordWin("Alice");
    scoreboard.recordLoss("Bob");

    assertEquals(2, scoreboard.getAllStats().size());
  }
}
