package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.server.game.ServerPlayerStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerPlayerStatsTest {

  @Test
  @DisplayName("Constructor initializes stats with zero values")
  void constructorInitializesZeroValues() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    assertEquals("Alice", stats.getPlayerName());
    assertEquals(0, stats.getWins());
    assertEquals(0, stats.getLosses());
    assertEquals(0, stats.getGames());
  }

  @Test
  @DisplayName("AddWin increments wins and games")
  void addWin() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    stats.addWin();

    assertEquals(1, stats.getWins());
    assertEquals(0, stats.getLosses());
    assertEquals(1, stats.getGames());
  }

  @Test
  @DisplayName("AddLoss increments losses and games")
  void addLoss() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    stats.addLoss();

    assertEquals(0, stats.getWins());
    assertEquals(1, stats.getLosses());
    assertEquals(1, stats.getGames());
  }

  @Test
  @DisplayName("AddWin and AddLoss both update totals correctly")
  void addWinAndLoss() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    stats.addWin();
    stats.addLoss();

    assertEquals(1, stats.getWins());
    assertEquals(1, stats.getLosses());
    assertEquals(2, stats.getGames());
  }
}
