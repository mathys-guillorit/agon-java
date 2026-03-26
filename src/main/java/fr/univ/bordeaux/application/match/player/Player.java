package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;

/** Represent general player's behavior. */
public interface Player {

  /**
   * Get the command from the user.
   *
   * @param cmds {@link AgonRegister}
   * @return {@link CmdAction}
   */
  CmdAction getAction(AgonRegister<CmdAction> cmds);

  /**
   * get the player's name.
   *
   * @return {@link String}
   */
  String getName();

  /**
   * Get player's color.
   *
   * @return {@link Color}
   */
  Color getColor();

  /**
   * get if the player is an AI or not.
   *
   * @return true | false
   */
  boolean isAi();
}
