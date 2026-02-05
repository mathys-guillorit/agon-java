package fr.univ.bordeaux.application.appCommands;

public class AppCommand {

    private final AppCommandType type;
    private final String arg;

    // Construit une commande à partir de son type et de ses arguments.
    public AppCommand(AppCommandType type, String arg) {
        this.type = type;
        this.arg = arg;
    }

    // Retourne le type de la commande.
    public AppCommandType getType() {
        return type;
    }

    // Retourne la valeur d'un argument donné.
    public String getArg() {
        return arg;
    }
}