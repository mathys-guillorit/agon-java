package fr.univ.bordeaux.application.network.server.game;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.io.IOException;

/** Sends end-of-game notifications to connected players. */
public class GameResultNotifier {

  /** Creates a new game result notifier. */
  public GameResultNotifier() {
    // Explicit constructor required by PMD.
  }

  /**
   * Sends a GAME_OVER message to one player.
   *
   * @param receiver the player receiving the message
   * @param opponent the opponent player referenced in the message
   * @param reason the game end reason
   * @param winner indicates whether the receiver won the game
   */
  public void notifyGameResult(
      final OnlinePlayer receiver,
      final OnlinePlayer opponent,
      final String reason,
      final boolean winner) {
    final ClientHandler receiverHandler = getHandler(receiver);

    if (receiver == null || opponent == null || receiverHandler == null) {
      return;
    }

    final String result = winner ? "WIN" : "LOSS";
    final String message =
        "GAME_OVER RESULT=" + result + " REASON=" + reason + " OPPONENT=" + opponent.getName();

    try {
      receiverHandler.sendFromServer(message);
    } catch (IOException exception) {
      GameLogger.error("[SERVER] Failed to send GAME_OVER message.");
    }
  }

  /**
   * Returns the handler associated with a player.
   *
   * @param player the player to inspect
   * @return the player handler, or {@code null} if unavailable
   */
  private ClientHandler getHandler(final OnlinePlayer player) {
    ClientHandler handler = null;

    if (player != null) {
      handler = player.getHandler();
    }

    return handler;
  }
}
