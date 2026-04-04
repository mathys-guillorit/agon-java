package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;

/** TODO: complete here. */
public class StandardMatch extends Match {

  /**
   * Create default match where two players play together.
   *
   * @param agonBoard {@link AgonBoard} Board to play on.
   * @param player1 {@link Player} first player.
   * @param player2 {@link Player} second player.
   */
  public StandardMatch(AgonBoard agonBoard, Player player1, Player player2, GameConfig gameConfig) {
    super(agonBoard, player1, player2, gameConfig);
  }

  /**
   * Create default match where two players play together.
   *
   * @param agonBoard {@link AgonBoard} Board to play on.
   * @param player1 {@link Player} first player.
   * @param player2 {@link Player} second player.
   * @param gameConfig {@link GameConfig} game configuration.
   * @param startingColor {@link Color} starting color.
   */
  public StandardMatch(
      AgonBoard agonBoard,
      Player player1,
      Player player2,
      GameConfig gameConfig,
      Color startingColor) {
    super(agonBoard, player1, player2, gameConfig, startingColor);
  }

  /** Completed Actions when beginning a turn. */
  @Override
  public void startActions() {}

  /** Actions done when a turn is about to end. */
  @Override
  public void endActions() {
    super.switchPlayer();
  }
}
