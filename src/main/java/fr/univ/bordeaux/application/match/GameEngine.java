package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;

public class GameEngine {
    private final GameUserInterface ui;
    private MatchManager matchManager;
    private final AgonRegister<CmdAction> Cmds;

    public GameEngine(GameUserInterface ui, AgonRegister<CmdAction> Cmds) {
      this.ui = ui;
      this.Cmds = Cmds;
    }

    public void start() {
      while (ui.isRunning()) {
        CmdAction action;

        // Logique de sélection de l'action
        if (this.matchManager == null || this.matchManager.isMatchOver()) {
          // Phase Hors-Match (Menu)
          String input = ui.getUserInput();
          if (input == null) continue;
          action = UIPromptParser.parse(input, this.Cmds,ui);
        } else {
          // Phase En-Match
          Player p = matchManager.getCurrentPlayer();
          ui.showMessage("Current Player: " + p.getName() + " (" + p.getColor() + ")\n");
          action = p.getAction(this.Cmds);
        }
        // Exécution de l'action
        if (action != null) {
          // L'action s'exécute sur le manager
          action.execute(this.matchManager);
          // Mise à jour visuelle
          if (matchManager != null) {
            ui.updateBoard(matchManager.getAgonBoard());
          }
        }else{
          ui.showMessage("Unknown command use the help command to see more details on commandes\n");
        }
      }
    }
  public void setMatchManager(MatchManager matchManager) {
    this.matchManager = matchManager;
  }
  }
