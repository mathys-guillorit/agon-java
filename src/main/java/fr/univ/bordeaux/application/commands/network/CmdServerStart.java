package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.server.AgonServer;

import java.util.Scanner;

/**
 * server start [PORT]
 * Starts a simple TCP game server on the given port (default: 12345).
 * If the port is already used, prints an explicit error.
 */
public class CmdServerStart extends Cmd {

    private final AppContext context;
    private static final int DEFAULT_PORT = 12345;

    /**
     * Creates a new server start command.
     *
     * @param context the shared application context
     */
    public CmdServerStart(AppContext context) {
        this.context = context;
    }

    @Override
    public void execute() {
    }


    /**
     * Executes the server start command.
     *
     * @param args optional port argument
     */
    @Override
    public void execute(String[] args) {
        int port = DEFAULT_PORT;

        if (args != null && args.length >= 1) {
            String raw = args[0].trim();
            if (!raw.isEmpty()) {
                try {
                    port = Integer.parseInt(raw);
                } catch (NumberFormatException e) {
                    System.out.println("[SERVER] Invalid port: " + raw);
                    return;
                }
            }
        }

        AgonServer server = new AgonServer(port);
        boolean ok = server.start();
        if (!ok) {
            System.out.println("[SERVER] ERROR: port " + port + " is already in use (or cannot be opened).");
            return;
        }

        context.setServer(server);
        System.out.println("[SERVER] Started on port " + port);
    }

}
