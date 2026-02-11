package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.client.AgonClient;

public class CmdQuit extends Cmd {
  private final AppContext context;

  public CmdQuit(AppContext ctx) { this.context = ctx; }

  @Override
  public boolean isQuit() {
    return !context.isConnected();
  }

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

