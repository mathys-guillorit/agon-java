package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;

/**
 * Base implementation of the {@link Player} interface.
 *
 * <p>This abstract class maintains the common state for any player type (Human or AI), including
 * their identifier name and assigned piece color.
 */
public abstract class AbstractPlayer implements Player {

  /** The unique name or identifier of the player. */
  protected String name;

  /** The color (White or Black) assigned to this player for the match. */
  protected Color color;

  /**
   * Constructs a new player with a specific name and color.
   *
   * @param name The player's display name.
   * @param color The {@link Color} assigned to the player.
   */
  public AbstractPlayer(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  /**
   * Requests an action (move or command) from the player.
   *
   * @param cmds The registry of available commands to choose from.
   * @return A {@link CmdAction} representing the player's chosen move or command.
   */
  @Override
  public abstract CmdAction getAction(AgonRegister<CmdAction> cmds);

  /**
   * Gets the color assigned to this player.
   *
   * @return The player's {@link Color}.
   */
  @Override
  public Color getColor() {
    return this.color;
  }

  /**
   * Gets the name of the player.
   *
   * @return The player's name as a {@link String}.
   */
  @Override
  public String getName() {
    return this.name;
  }
}
