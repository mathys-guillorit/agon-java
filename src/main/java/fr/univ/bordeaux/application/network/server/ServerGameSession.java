package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.application.network.player.OnlinePlayer;

/**
 * Represents a single active game session on the server.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>linking two connected players to a game instance,</li>
 *   <li>holding the associated {@link MatchManager},</li>
 *   <li>providing utility methods to identify players in the session.</li>
 * </ul>
 */
public class ServerGameSession {

    private final int gameId;

    private final OnlinePlayer whitePlayer;

    private final OnlinePlayer blackPlayer;

    private final Match match;

    /**
     * Creates a new game session between two players.
     *
     * @param gameId unique identifier of the game
     * @param whitePlayer first player
     * @param blackPlayer second player
     * @param match the match associated with this game
     */
    public ServerGameSession(int gameId, OnlinePlayer whitePlayer, OnlinePlayer blackPlayer, Match match) {
        this.gameId = gameId;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.match = match;
    }

    /**
     * Returns the unique identifier of this game session.
     *
     * @return the game ID
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Returns the first player.
     *
     * @return player 1
     */
    public OnlinePlayer getwhitePlayer() {
        return whitePlayer;
    }

    /**
     * Returns the second player.
     *
     * @return player 2
     */
    public OnlinePlayer getblackPlayer() {
        return blackPlayer;
    }

    /**
     * Returns the match associated with this game.
     *
     * @return the match
     */
    public Match getMatch() {
        return match;
    }

    /**
     * Checks whether a player is part of this game session.
     *
     * @param playerId the player ID to check
     * @return true if the player belongs to this session, false otherwise
     */
    public boolean containsPlayer(int playerId) {
        return whitePlayer.getId() == playerId || blackPlayer.getId() == playerId;
    }

    /**
     * Returns the opponent of the given player in this game session.
     *
     * <p>If the provided player ID corresponds to the white player,
     * the black player is returned, and vice versa.
     *
     * @param playerId the ID of the player
     * @return the opponent player, or null if the player is not part of this session
     */
    public OnlinePlayer getOpponent(int playerId) {
        if (whitePlayer.getId() == playerId) {
            return blackPlayer;
        }
        if (blackPlayer.getId() == playerId) {
            return whitePlayer;
        }
        return null;
    }

    /**
     * Returns the color associated with a given player in this session.
     *
     * <p>The color is determined by the role assigned at game creation:
     * one player is white and the other is black.
     *
     * @param playerId the ID of the player
     * @return {@link Color#WHITE}, {@link Color#BLACK}, or null if the player is not part of this session
     */
    public Color getColorOfPlayer(int playerId) {
        if (whitePlayer.getId() == playerId) {
            return Color.WHITE;
        }
        if (blackPlayer.getId() == playerId) {
            return Color.BLACK;
        }
        return null;
    }

    /**
     * Returns a human-readable label describing the role of a player.
     *
     * <p>This method converts the player's color into a string representation
     * used in the network protocol (e.g., "WHITE" or "BLACK").
     *
     * @param playerId the ID of the player
     * @return "WHITE", "BLACK", or "UNKNOWN" if the player is not part of this session
     */
    public String getRoleLabel(int playerId) {
        Color color = getColorOfPlayer(playerId);

        if (color == Color.WHITE) {
            return "WHITE";
        }
        if (color == Color.BLACK) {
            return "BLACK";
        }

        return "UNKNOWN";
    }

    /**
     * Indicates whether it is currently the turn of the given player.
     *
     * <p>This method compares the player's assigned color with the current player
     * in the underlying {@link Match} instance.
     *
     * @param playerId the ID of the player
     * @return true if it is this player's turn, false otherwise
     */
    public boolean isPlayersTurn(int playerId) {
        if (match == null) {
            return false;
        }

        Color color = getColorOfPlayer(playerId);
        if (color == null) {
            return false;
        }

        Player current = match.getCurrentPlayer();
        return current != null && current.getColor() == color;
    }

    /**
     * Returns a formatted string describing the role distribution in this game.
     *
     * <p>This is mainly used for debugging or initial client notification,
     * showing which player is assigned to white and which to black.
     *
     * @return a string formatted as "WHITE=name BLACK=name"
     */
    public String describeRoles() {
        return "WHITE=" + whitePlayer.getName() + " BLACK=" + blackPlayer.getName();
    }
}