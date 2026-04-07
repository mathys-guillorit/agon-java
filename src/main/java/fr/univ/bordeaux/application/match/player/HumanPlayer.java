package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UiPromptParser;

/**
 * Represents a human participant in the Agon match.
 *
 * <p>This class handles human behavior by interacting with the user interface to retrieve input and
 * delegating the parsing of that input to the {@link UiPromptParser} to generate game actions.
 */
public class HumanPlayer extends AbstractPlayer {

  /** The user interface used to interact with the human player. */
  private final GameUserInterface ui;

  /**
   * Constructs a new HumanPlayer.
   *
   * @param name The display name of the player.
   * @param color The {@link Color} assigned to this player.
   * @param ui The {@link GameUserInterface} implementation used for input/output.
   */
  public HumanPlayer(String name, Color color, GameUserInterface ui) {
    super(name, color);
    this.ui = ui;
  }

  /**
   * Returns the color associated with this human player.
   *
   * @return The player's {@link Color}.
   */
  @Override
  public Color getColor() {
    return color;
  }

  /**
   * Prompts the user for input and converts it into a command action.
   *
   * <p>This method retrieves a raw string from the UI. If the input is null, empty, or contains
   * only whitespace, it returns null. Otherwise, it uses the {@link UiPromptParser} to map the
   * input string to a specific {@link CmdAction}.
   *
   * @param cmds The registry of available commands to match against user input.
   * @return The interpreted {@link CmdAction}, or {@code null} if input is invalid or empty.
   */
  @Override
  public CmdAction getAction(AgonRegister<CmdAction> cmds) {
    String input = ui.getUserInput();
    if (input == null || input.trim().isEmpty()) {
      return null;
    }
    // Static call to the parser
    return UiPromptParser.parse(input, cmds, ui);
  }

  /**
   * Returns the name of the human player.
   *
   * @return A {@link String} representing the player's name.
   */
  @Override
  public String getName() {
    return name;
  }
}
