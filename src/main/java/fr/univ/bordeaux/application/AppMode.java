package fr.univ.bordeaux.application;

/**
 * Defines the current operating mode of the application.
 *
 * <p>The mode determines how some commands behave:
 * <ul>
 *   <li>LOCAL: commands act on a local game instance,</li>
 *   <li>ONLINE: commands interact with a remote server.</li>
 * </ul>
 */
public enum AppMode {
    /** Local game mode. */
    LOCAL,

    /** Online multiplayer mode. */
    ONLINE
}