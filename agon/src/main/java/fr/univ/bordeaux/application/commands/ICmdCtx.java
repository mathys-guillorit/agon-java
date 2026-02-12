package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.cli.ICmdShellDelegate;


/**
 * implements all methods that depends on his delegates to remove layered calls
 * like "ctx.getSomethingDelegate.getDisplay.showMessage(message)"
 * <br>
 * - allow to add delegates without breaking lower concretion levels
 */
public interface ICmdCtx extends ICmdShellDelegate{
  // require to extends "INETDelegate" and "IGUIDelegate" later...

  ICmdShellDelegate getICmdShellDelegate();
  INETDelegate getINETDelegate();
  IGUIDelegate getGUIDelegate();

}
