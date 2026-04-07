package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command used to cancel a sent invitation. */
public class CmdCancel extends Cmd {

  /** Shared application context (client, server, etc.). */
  private final AppContext context;

  /** Command arguments (provided by parser). */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param ui User interface context
   * @param context Application context
   */
  public CmdCancel(GameUserInterface ui, AppContext context) {
    super(ui);
    this.context = context;
    this.setName("cancel");
    this.setDesc("Usage: cancel\n" + "Description: cancels the current sent invitation.\n");
  }

  /** Internal constructor used when the command is executed with arguments. */
  private CmdCancel(GameUserInterface ui, AppContext context, String[] args) {
    this(ui, context);
    this.args = args;
  }

  /** Creates a new instance of the command with parsed arguments. */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdCancel(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match Not used (network command independent from game state)
   */
  @Override
  public boolean execute(MatchManager match) {
    return run(args);
  }

  /**
   * Core logic of the cancel command.
   *
   * @param args Command arguments
   * @return true if execution completed
   */
  @SuppressWarnings("PMD.UnusedFormalParameter")
  private boolean run(String[] args) {
    AgonClient client = context.getClient();

    if (!client.isConnected()) {
      getCtx().showWarn("[CLIENT] Not connected.\n");
      return false;
    }

    if (client.cancelInvitation()) {
      getCtx().showMessage("[CLIENT] Cancel request sent.\n");
    } else {
      getCtx().showError("[CLIENT] Failed to send cancel request.");
    }

    return true;
  }
}
