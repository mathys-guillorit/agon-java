package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerPlayerStatsTest {

  @Test
  @DisplayName("Constructor initializes stats with zero values")
  void constructor_initializes_zero_values() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    assertEquals("Alice", stats.getPlayerName());
    assertEquals(0, stats.getWins());
    assertEquals(0, stats.getLosses());
    assertEquals(0, stats.getGames());
  }

  @Test
  @DisplayName("AddWin increments wins and games")
  void add_win() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    stats.addWin();

    assertEquals(1, stats.getWins());
    assertEquals(0, stats.getLosses());
    assertEquals(1, stats.getGames());
  }

  @Test
  @DisplayName("AddLoss increments losses and games")
  void add_loss() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    stats.addLoss();

    assertEquals(0, stats.getWins());
    assertEquals(1, stats.getLosses());
    assertEquals(1, stats.getGames());
  }

  @Test
  @DisplayName("AddWin and AddLoss both update totals correctly")
  void add_win_and_loss() {
    ServerPlayerStats stats = new ServerPlayerStats("Alice");

    stats.addWin();
    stats.addLoss();

    assertEquals(1, stats.getWins());
    assertEquals(1, stats.getLosses());
    assertEquals(2, stats.getGames());
  }
}
