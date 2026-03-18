package fr.univ.bordeaux.technical.io.storage.states;

import fr.univ.bordeaux.technical.io.storage.GameSaveBuilder;

/**
 * The initial state of the parser before any section header is encountered.
 * It simply ignores all lines.
 */
public class IdleState implements SaveParserState {

    @Override
    public void parseLine(String line, GameSaveBuilder builder) {
    }
}