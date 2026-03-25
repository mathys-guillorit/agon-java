package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.ui.GameUserInterface;

import java.util.List;

/**
 * Command used to list available servers on the local network.
 *
 * <p>Usage:
 * <ul>
 *     <li>{@code server_list}</li>
 * </ul>
 *
 * <p>This command:
 * <ul>
 *     <li>Ensures discovery is running</li>
 *     <li>Retrieves detected servers</li>
 *     <li>Displays them to the user</li>
 * </ul>
 */
public class CmdServerList extends Cmd {

    private final AppContext context;

    public CmdServerList(GameUserInterface ui, AppContext context) {
        super(ui);
        this.context = context;
        this.setName("server_list");
        this.setDesc(
                "Usage: server list\n"
                        + "Description: displays available servers on the local network.\n"
                        + "Uses UDP discovery to detect active servers.\n"
        );
    }

    @Override
    public CmdAction createNew(String[] args) {
        return new CmdServerList(getCtx(), context);
    }

    @Override
    public boolean execute(MatchManager match) {

        // Ensure discovery process is active
        try {
            context.ensureDiscoveryStarted();
        } catch (Exception e) {
            getCtx().showError("[SERVER] Discovery error: " + e.getMessage());
            return false;
        }

        List<ServerInfo> servers = context.getDiscovery().getServers();

        // Display results
        if (servers.isEmpty()) {
            getCtx().showWarn("[SERVER] No servers found on the network.");
        } else {
            for (ServerInfo s : servers) {
                getCtx().showMessage(s.name + " @ " + s.ip + ":" + s.tcpPort + "\n");
            }
        }

        return true;
    }
}