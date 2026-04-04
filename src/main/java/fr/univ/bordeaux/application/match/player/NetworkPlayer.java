package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;

/**
 * Player implementation used for online/network matches.
 *
 * <p>This player is only used by the server-side match engine. It does not read commands from the
 * UI.
 */
public class NetworkPlayer extends AbstractPlayer {

  public NetworkPlayer(String name, Color color) {
    super(name, color);
  }

  /**
   * Returns the next action to perform for this player.
   *
   * <p>This implementation is unused for network players, as their moves are received from the
   * server rather than generated locally.
   *
   * @param cmds command registry (unused)
   * @return always null
   */
  @Override
  public CmdAction getAction(AgonRegister<CmdAction> cmds) {
    return null;
  }

  /**
   * Returns the color assigned to this player.
   *
   * @return the player's color
   */
  @Override
  public Color getColor() {
    return color;
  }

  /**
   * Returns the name of this player.
   *
   * @return the player's name
   */
  @Override
  public String getName() {
    return name;
  }
}
