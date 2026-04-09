package fr.univ.bordeaux.application.network.server.player;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.game.ServerPlayerStats;
import java.util.Collection;
import java.util.Locale;

/** Builds textual player-related responses sent back to network clients. */
public class PlayerViewService {

  /** Creates a new player view service. */
  public PlayerViewService() {
    // Default constructor.
  }

  /**
   * Formats the active players list.
   *
   * @param players the collection of active players
   * @return a protocol-compatible multi-line players response
   */
  public String formatPlayersList(final Collection<OnlinePlayer> players) {
    final String playersList;

    if (players.isEmpty()) {
      playersList = "PLAYERS_EMPTY\nEND";
    } else {
      final StringBuilder builder = new StringBuilder(96);
      builder.append("=== PLAYERS ===\n");

      for (OnlinePlayer player : players) {
        appendPlayerLine(builder, player);
      }

      builder.append("===============\nEND");
      playersList = builder.toString();
    }

    return playersList;
  }

  /**
   * Formats the details of a single player.
   *
   * @param player the player to describe
   * @param stats the scoreboard statistics of the player
   * @return a protocol-compatible player details response
   */
  public String formatPlayerDetails(final OnlinePlayer player, final ServerPlayerStats stats) {
    final String details;

    if (player == null) {
      details = "ERROR MESSAGE=PLAYER_NOT_FOUND";
    } else {
      details =
          "PLAYER ID="
              + player.getId()
              + " NAME="
              + player.getName()
              + " STATUS="
              + formatStatus(player.getStatus())
              + " WINS="
              + stats.getWins()
              + " LOSSES="
              + stats.getLosses()
              + " GAMES="
              + stats.getGames();
    }

    return details;
  }

  /**
   * Appends a single player line to the players response buffer.
   *
   * @param builder the target string builder
   * @param player the player to append
   */
  private void appendPlayerLine(final StringBuilder builder, final OnlinePlayer player) {
    builder
        .append("ID=")
        .append(player.getId())
        .append(" NAME=")
        .append(player.getName())
        .append(" STATUS=")
        .append(formatStatus(player.getStatus()))
        .append('\n');
  }

  /**
   * Formats a player status using the network protocol convention.
   *
   * @param status the player status
   * @return the formatted status string
   */
  private String formatStatus(final PlayerStatus status) {
    return status.name().toLowerCase(Locale.ROOT);
  }
}
