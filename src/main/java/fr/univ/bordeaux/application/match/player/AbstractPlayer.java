package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;

/**
 * Base implementation of the {@link Player} interface. *
 *
 * <p>This abstract class provides common storage for player attributes like name and color, which
 * are shared by both human and AI player implementations.
 */
public abstract class AbstractPlayer implements Player {

  /** The display name of the player. */
  protected final String name;

  /** The team color assigned to the player. */
  protected final Color color;

  /**
   * Constructs a player with a name and a color.
   *
   * @param name The player's name.
   * @param color The {@link Color} assigned to the player.
   */
  public AbstractPlayer(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  /**
   * Prompts the player (or the AI) to provide the next action to execute.
   *
   * @param cmds The registry of available commands to choose from.
   * @return The {@link CmdAction} selected by the player.
   */
  @Override
  public abstract CmdAction getAction(AgonRegister<CmdAction> cmds);

  /**
   * Retrieves the name of the player.
   *
   * @return A {@link String} representing the player's display name.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Retrieves the color assigned to the player for the current match.
   *
   * @return The {@link Color} (e.g., WHITE or BLACK) used by this player.
   */
  @Override
  public Color getColor() {
    return color;
  }
}
