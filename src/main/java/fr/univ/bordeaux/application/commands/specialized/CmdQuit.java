package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command responsible for safely exiting the Agon application. */
public class CmdQuit extends Cmd {

  /**
   * Constructs a new Quit command.
   *
   * @param uictx The user interface context to close upon execution.
   */
  public CmdQuit(GameUserInterface uictx) {
    super(uictx);
    this.setName("quit");
  }

  /**
   * Provides the usage and description for the quit command.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Usage: quit (or Ctrl+C)\n");
    sb.append("Description: Exits the game.");
    sb.append(" You will be prompted to save your current ");
    sb.append("progress before leaving.\n");
    return sb.toString();
  }

  /**
   * Executes the shutdown sequence.
   *
   * @param match The manager for the current game session.
   * @return true always, as the command successfully initiates the exit.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (match != null && !match.isMatchOver() && !match.isSaved()) {
      boolean resolved = false;
      while (!resolved) {
        this.getCtx().showMessage("Save the game before quitting? [y/N] \n");
        String response = this.getCtx().getUserInput();
        System.out.println("la reponse utilisateur est : " + response);
        if (response != null && (response.equalsIgnoreCase("y"))) {
          this.getCtx().showMessage("Enter filename: \n");
          String filename = this.getCtx().getUserInput();
          if (filename == null || filename.trim().isEmpty()) {
            filename = "default_save";
          }
          CmdSave saveCmd = new CmdSave(this.getCtx());
          saveCmd.createNew(new String[]{filename}).execute(match);
          if (match.isSaved()) {
            resolved = true;
          } else {
            this.getCtx().showMessage("Save failed. Try again.\n");
          }
        } else {
          resolved = true;
        }
      }
      match.quit();
    }
    this.getCtx().quit();
    return true;
  }

  /**
   * Factory method to create an executable instance of the quit command.
   *
   * @param args Arguments passed in CLI (ignored for quit).
   * @return A new CmdQuit instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdQuit(super.getCtx());
  }
}
