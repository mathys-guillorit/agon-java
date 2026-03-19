package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;

public interface Player {

  CmdAction getAction(AgonRegister<CmdAction> cmds);

  String getName();

  Color getColor();

  boolean isAI();
}
