package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.player.NetworkPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerGameSessionTest {

  private OnlinePlayer whitePlayer() {
    return new OnlinePlayer(1, "cid-white", "Alice", PlayerStatus.IDLE, null);
  }

  private OnlinePlayer blackPlayer() {
    return new OnlinePlayer(2, "cid-black", "Bob", PlayerStatus.IDLE, null);
  }

  private static class FakeMatch extends Match {

    private Player forcedCurrentPlayer;
    private boolean replacementRequired;
    private boolean matchOver;
    private boolean moveResult = true;
    private Move lastMove;

    public FakeMatch(Player white, Player black, Color currentColor) {
      super(null, white, black, null, currentColor);
      this.forcedCurrentPlayer = currentColor == Color.WHITE ? white : black;
    }

    public void setForcedCurrentPlayer(Player p) {
      this.forcedCurrentPlayer = p;
    }

    public void setReplacementRequired(boolean replacementRequired) {
      this.replacementRequired = replacementRequired;
    }

    public void setMatchOver(boolean matchOver) {
      this.matchOver = matchOver;
    }

    public void setMoveResult(boolean moveResult) {
      this.moveResult = moveResult;
    }

    public Move getLastMove() {
      return lastMove;
    }

    @Override
    public Player getCurrentPlayer() {
      return forcedCurrentPlayer;
    }

    @Override
    public boolean move(Move move) {
      this.lastMove = move;
      return moveResult;
    }

    @Override
    public boolean isReplacementMoveRequired(Color color) {
      return replacementRequired;
    }

    @Override
    public boolean isMatchOver() {
      return matchOver;
    }

    @Override
    public void startActions() {}

    @Override
    public void endActions() {}
  }

  @Test
  @DisplayName("Constructor and getters return expected values")
  void constructorAndGetters() {
    OnlinePlayer white = whitePlayer();
    OnlinePlayer black = blackPlayer();

    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);

    ServerGameSession session = new ServerGameSession(42, white, black, match);

    assertEquals(42, session.getGameId());
    assertEquals(white, session.getwhitePlayer());
    assertEquals(black, session.getblackPlayer());
    assertEquals(match, session.getMatch());
  }

  @Test
  @DisplayName("ContainsPlayer returns true only for session players")
  void containsPlayer() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    ServerGameSession session =
        new ServerGameSession(
            1,
            whitePlayer(),
            blackPlayer(),
            new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE));

    assertTrue(session.containsPlayer(1));
    assertTrue(session.containsPlayer(2));
    assertFalse(session.containsPlayer(99));
  }

  @Test
  @DisplayName("GetOpponent returns the other player or null")
  void getOpponent() {
    OnlinePlayer white = whitePlayer();
    OnlinePlayer black = blackPlayer();

    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    ServerGameSession session =
        new ServerGameSession(
            1, white, black, new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE));

    assertEquals(black, session.getOpponent(1));
    assertEquals(white, session.getOpponent(2));
    assertNull(session.getOpponent(99));
  }

  @Test
  @DisplayName("GetColorOfPlayer returns expected colors")
  void getColorOfPlayer() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    ServerGameSession session =
        new ServerGameSession(
            1,
            whitePlayer(),
            blackPlayer(),
            new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE));

    assertEquals(Color.WHITE, session.getColorOfPlayer(1));
    assertEquals(Color.BLACK, session.getColorOfPlayer(2));
    assertNull(session.getColorOfPlayer(99));
  }

  @Test
  @DisplayName("GetRoleLabel returns WHITE BLACK or UNKNOWN")
  void getRoleLabel() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    ServerGameSession session =
        new ServerGameSession(
            1,
            whitePlayer(),
            blackPlayer(),
            new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE));

    assertEquals("WHITE", session.getRoleLabel(1));
    assertEquals("BLACK", session.getRoleLabel(2));
    assertEquals("UNKNOWN", session.getRoleLabel(99));
  }

  @Test
  @DisplayName("IsPlayersTurn returns false when match is null")
  void isPlayersTurnMatchNull() {
    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), null);

    assertFalse(session.isPlayersTurn(1));
  }

  @Test
  @DisplayName("IsPlayersTurn returns false for unknown player")
  void isPlayersTurnUnknownPlayer() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    assertFalse(session.isPlayersTurn(99));
  }

  @Test
  @DisplayName("IsPlayersTurn returns true only for the current color")
  void isPlayersTurn() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    assertTrue(session.isPlayersTurn(1));
    assertFalse(session.isPlayersTurn(2));
  }

  @Test
  @DisplayName("DescribeRoles returns formatted role string")
  void describeRoles() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    ServerGameSession session =
        new ServerGameSession(
            1,
            whitePlayer(),
            blackPlayer(),
            new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE));

    assertEquals("WHITE=Alice BLACK=Bob", session.describeRoles());
  }

  @Test
  @DisplayName("PlayMove returns false when player is not in session")
  void playMovePlayerNotInSession() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    assertFalse(session.playMove(99, "e2e4"));
    assertNull(match.getLastMove());
  }

  @Test
  @DisplayName("PlayMove returns false when match is null")
  void playMoveMatchNull() {
    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), null);

    assertFalse(session.playMove(1, "e2e4"));
  }

  @Test
  @DisplayName("PlayMove returns false when it is not the player's turn")
  void playMoveNotPlayersTurn() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.BLACK);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    assertFalse(session.playMove(1, "e2e4"));
    assertNull(match.getLastMove());
  }

  @Test
  @DisplayName("PlayMove returns false for invalid raw move")
  void playMoveInvalidRawMove() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    assertFalse(session.playMove(1, "invalid"));
    assertNull(match.getLastMove());
  }

  @Test
  @DisplayName("PlayMove applies a normal move when valid")
  void playMoveNormalSuccess() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);
    match.setReplacementRequired(false);
    match.setMoveResult(true);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    boolean result = session.playMove(1, "e2e4");

    assertTrue(result);
    assertNotNull(match.getLastMove());
    assertTrue(match.getLastMove().getFrom() >= 0);
    assertTrue(match.getLastMove().getDestination() >= 0);
    assertEquals(Color.WHITE, match.getLastMove().getColor());
  }

  @Test
  @DisplayName("PlayMove rejects short move during normal phase")
  void playMoveRejectsShortMoveInNormalPhase() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);
    match.setReplacementRequired(false);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    boolean result = session.playMove(1, "e3");

    assertFalse(result);
    assertNull(match.getLastMove());
  }

  @Test
  @DisplayName("PlayMove applies replacement move when required")
  void playMoveReplacementSuccess() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);
    match.setReplacementRequired(true);
    match.setMoveResult(true);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    boolean result = session.playMove(1, "e3");

    assertTrue(result);
    assertNotNull(match.getLastMove());
    assertEquals(-1, match.getLastMove().getFrom());
    assertTrue(match.getLastMove().getDestination() >= 0);
    assertEquals(Color.WHITE, match.getLastMove().getColor());
  }

  @Test
  @DisplayName("PlayMove rejects full move during replacement phase")
  void playMoveRejectsFullMoveInReplacementPhase() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);
    match.setReplacementRequired(true);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    boolean result = session.playMove(1, "e2e4");

    assertFalse(result);
    assertNull(match.getLastMove());
  }

  @Test
  @DisplayName("PlayMove returns false when match refuses the move")
  void playMoveMatchRefusesMove() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);
    match.setReplacementRequired(false);
    match.setMoveResult(false);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    boolean result = session.playMove(1, "e2e4");

    assertFalse(result);
    assertNotNull(match.getLastMove());
  }

  @Test
  @DisplayName("IsGameOver returns false when match is null")
  void isGameOverMatchNull() {
    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), null);

    assertFalse(session.isGameOver());
  }

  @Test
  @DisplayName("IsGameOver delegates to match state")
  void isGameOver() {
    NetworkPlayer whiteMatchPlayer = new NetworkPlayer("Alice", Color.WHITE);
    NetworkPlayer blackMatchPlayer = new NetworkPlayer("Bob", Color.BLACK);

    FakeMatch match = new FakeMatch(whiteMatchPlayer, blackMatchPlayer, Color.WHITE);
    match.setMatchOver(true);

    ServerGameSession session = new ServerGameSession(1, whitePlayer(), blackPlayer(), match);

    assertTrue(session.isGameOver());
  }
}
