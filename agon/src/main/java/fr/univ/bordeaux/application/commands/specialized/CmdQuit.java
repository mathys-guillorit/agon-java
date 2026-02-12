package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.client.AgonClient;

/**
 * Command to handle quitting the application or disconnecting from a server.
 */
public class CmdQuit extends Cmd {
  private final AppContext context;

  /**
   * Constructs a Quit command.
   * @param context The application context.
   */
  public CmdQuit(AppContext context) { this.context = context; }

  /**
   * Determines if the command should trigger the shell loop termination.
   */
  @Override
  public boolean isQuit() {
    return !context.isConnected();
  }

  /**
   * Executes the quit logic. If connected, it disconnects from the server.
   * @param args Command arguments (unused).
   */
  @Override
  public void execute(String[] args) {
    AgonClient client = context.getClient();

    if (client != null && client.isConnected()) {
      client.quit();
      System.out.println("[CLIENT] Disconnected from server. Returning to local mode.");
      return;
    }

    System.out.println("Goodbye.");
  }
}

