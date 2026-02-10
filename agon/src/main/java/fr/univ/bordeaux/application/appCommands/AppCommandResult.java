package fr.univ.bordeaux.application.appCommands;

/**
 * Standard result object returned by commands.
 * Keeps UI simple: it only needs to display success/error and a message.
 */
public class AppCommandResult {

    private final boolean ok;
    private final String message;

    private AppCommandResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public static AppCommandResult ok(String message) {
        return new AppCommandResult(true, message);
    }

    public static AppCommandResult error(String message) {
        return new AppCommandResult(false, message);
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }
}
