package fr.univ.bordeaux.application.network.protocol;

import java.util.Map;

// Représente une commande réseau reçue ou envoyée.
public class Command {

    private final CommandType type;
    private final Map<String, String> args;

    // Construit une commande à partir de son type et de ses arguments.
    public Command(CommandType type, Map<String, String> args) {
        this.type = type;
        this.args = args;
    }

    // Retourne le type de la commande.
    public CommandType getType() {
        return type;
    }

    // Retourne la valeur d'un argument donné.
    public String getArg(String key) {
        return args.get(key);
    }
}