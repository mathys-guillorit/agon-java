package fr.univ.bordeaux.application;


import fr.univ.bordeaux.application.appCommands.AppCommand;
import fr.univ.bordeaux.application.appCommands.AppCommandResult;
//import fr.univ.bordeaux.ui.IAppController;

/**
 * Application entry point for UI actions.
 * It creates a command and sends it here via dispatch().
 */
public class AppController { //implements IAppController {

    private final AppContext context;

    public AppController(AppContext context) {
        this.context = context;
    }

   /* @Override
    public CommandResult dispatch(AppCommand command) {
        if (command == null) {
            return CommandResult.error("Null command.");
        }
        try {
            return command.execute(context);
        } catch (Exception e) {
            // Keep the UI safe: convert exceptions into a readable error.
            return CommandResult.error("Command failed: " + e.getMessage());
        }
    }*/
}
