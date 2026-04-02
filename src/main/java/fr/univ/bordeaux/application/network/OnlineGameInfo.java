package fr.univ.bordeaux.application.network;

import fr.univ.bordeaux.agoncore.agonelements.Color;

/**
 * Stores the basic information describing a started online game.
 *
 * <p>This object is created from a NEW_OK or GAME_STARTED network message
 * and is used by the application context to initialize the local visual match.
 */
public class OnlineGameInfo {

    private final int gameId;
    private final Color localColor;
    private final String whitePlayerName;
    private final String blackPlayerName;
    private final boolean myTurn;

    /**
     * Creates a new online game description.
     *
     * @param gameId the unique server game ID
     * @param localColor the color assigned to the local player
     * @param whitePlayerName the display name of the white player
     * @param blackPlayerName the display name of the black player
     * @param myTurn true if it is initially the local player's turn
     */
    public OnlineGameInfo(
            int gameId,
            Color localColor,
            String whitePlayerName,
            String blackPlayerName,
            boolean myTurn) {
        this.gameId = gameId;
        this.localColor = localColor;
        this.whitePlayerName = whitePlayerName;
        this.blackPlayerName = blackPlayerName;
        this.myTurn = myTurn;
    }

    /**
     * Returns the unique game ID assigned by the server.
     *
     * @return the game ID
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Returns the local player's assigned color.
     *
     * @return the local player's color
     */
    public Color getLocalColor() {
        return localColor;
    }

    /**
     * Returns the white player's display name.
     *
     * @return the white player's name
     */
    public String getWhitePlayerName() {
        return whitePlayerName;
    }

    /**
     * Returns the black player's display name.
     *
     * @return the black player's name
     */
    public String getBlackPlayerName() {
        return blackPlayerName;
    }

    /**
     * Indicates whether it is currently the local player's turn.
     *
     * @return true if the local player starts
     */
    public boolean isMyTurn() {
        return myTurn;
    }
}