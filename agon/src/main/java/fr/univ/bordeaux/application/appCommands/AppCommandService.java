package fr.univ.bordeaux.application.appCommands;

import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.server.ServerDiscovery;

public class AppCommandService {

    private final ClientDiscovery discoveryClient = new ClientDiscovery();
    private ServerDiscovery discoveryService;

    private AgonServer server;
    private AgonClient client;

    // Exécute la commande local de l'utilisateur selon son type.
    public void execute(AppCommand command) {
        switch (command.getType()) {
            case SERVER_LIST -> {
            }

            case SERVER_START -> {
            }

            case SERVER_STOP -> {
            }

            case JOIN -> {
            }

            case PING -> {
            }

            case QUIT -> {
            }
        }
    }
}