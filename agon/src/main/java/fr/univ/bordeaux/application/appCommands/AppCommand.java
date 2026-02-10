package fr.univ.bordeaux.application.appCommands;

import fr.univ.bordeaux.application.AppContext;


/**
 * Application-level command (Command Pattern).
 */
public interface AppCommand {

    /**
     * Executes the command.
     *
     * @param context shared application context (services/state)
     */
    AppCommandResult execute(AppContext context);

}