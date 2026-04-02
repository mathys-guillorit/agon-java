package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;

/**
 * Player implementation used for online/network matches.
 *
 * <p>This player is only used by the server-side match engine.
 * It does not read commands from the UI.
 */
public class NetworkPlayer extends AbstractPlayer {

    public NetworkPlayer(String name, Color color) {
        super(name, color);
    }

    @Override
    public CmdAction getAction(AgonRegister<CmdAction> cmds) {
        return null;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String getName() {
        return name;
    }

}