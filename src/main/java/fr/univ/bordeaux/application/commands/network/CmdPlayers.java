package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to display the list of connected players on the server.
 *
 * <p>This command sends a "PLAYERS" request and displays the server response.
 */
public class CmdPlayers extends Cmd {

    /** Application context */
    private final AppContext context;

    /**
     * Constructor.
     *
     * @param ui user interface
     * @param context application context
     */
    public CmdPlayers(GameUserInterface ui, AppContext context) {
        super(ui);
        this.context = context;

        this.setName("players");
        this.setDesc(
                "Usage: players\n"
                        + "Description: displays the list of connected players on the server.\n"
                        + "Requires an active connection.\n"
        );
    }

    /**
     * Creates a new instance of the command.
     *
     * @param args command arguments
     * @return new CmdPlayers instance
     */
    @Override
    public CmdAction createNew(String[] args) {
        return new CmdPlayers(getCtx(), context);
    }

    /**
     * Executes the command.
     *
     * @param match current match manager (unused here)
     * @return true if execution succeeds
     */
    @Override
    public boolean execute(MatchManager match) {

        AgonClient client = context.getClient();

        // Check connection
        if (!client.isConnected()) {
            getCtx().showWarn("[CLIENT] Not connected. Use join first.");
            return false;
        }

        // Request players list
        String response = client.requestPlayers();

        if (response != null) {
            getCtx().showMessage(response + "\n");
        } else {
            getCtx().showError("[CLIENT] Failed to retrieve players.");
        }

        return true;
    }
}