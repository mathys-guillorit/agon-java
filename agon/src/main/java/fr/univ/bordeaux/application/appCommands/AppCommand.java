package fr.univ.bordeaux.application.appCommands;

public class AppCommand {

    private final AppCommandType type;
    private final String arg;

    public AppCommand(AppCommandType type, String arg) {
        this.type = type;
        this.arg = arg;
    }

    public AppCommandType getType() {
        return type;
    }

    public String getArg() {
        return arg;
    }