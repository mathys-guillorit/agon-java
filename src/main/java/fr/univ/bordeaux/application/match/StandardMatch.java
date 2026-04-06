package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;

/**
 * Represents a standard match of Agon.
 *
 * <p>This class implements the basic rules of the game where players take turns without time
 * constraints. It handles the transition between player turns by simply switching the active player
 * at the end of each action phase.
 */
public class StandardMatch extends Match {

  /**
   * Constructs a standard match with the specified board and players. By default, the White player
   * starts the match.
   *
   * @param agonBoard The {@link AgonBoard} instance to play on.
   * @param player1 The first {@link Player}.
   * @param player2 The second {@link Player}.
   * @param gameConfig The {@link GameConfig} defining the match settings.
   */
  public StandardMatch(AgonBoard agonBoard, Player player1, Player player2, GameConfig gameConfig) {
    super(agonBoard, player1, player2, gameConfig);
  }

  /**
   * Constructs a standard match with the specified board, players, and starting color.
   *
   * @param agonBoard The {@link AgonBoard} instance to play on.
   * @param player1 The first {@link Player}.
   * @param player2 The second {@link Player}.
   * @param gameConfig The {@link GameConfig} defining the match settings.
   * @param startingColor The {@link Color} of the player who takes the first turn.
   */
  public StandardMatch(
      AgonBoard agonBoard,
      Player player1,
      Player player2,
      GameConfig gameConfig,
      Color startingColor) {
    super(agonBoard, player1, player2, gameConfig, startingColor);
  }

  /**
   * * Performs actions required at the beginning of a turn.
   *
   * <p>In a standard match, no specific actions are required before the player moves.
   */
  @Override
  public void startActions() {}

  /**
   * * Performs actions required at the end of a turn.
   *
   * <p>This method triggers the player switch logic to pass the turn to the opponent.
   */
  @Override
  public void endActions() {
    super.switchPlayer();
  }
}
