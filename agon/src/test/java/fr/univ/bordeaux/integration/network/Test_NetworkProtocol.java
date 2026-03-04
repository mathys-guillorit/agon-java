package fr.univ.bordeaux.integration.network;

import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandParser;
import fr.univ.bordeaux.application.network.protocol.CommandType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_NetworkProtocol {

    // -----------------
    // CommandType
    // -----------------

    @Test
    void type_null() {
        assertEquals(CommandType.UNKNOWN, CommandType.convertCommandType(null));
    }

    @Test
    void type_empty() {
        assertEquals(CommandType.UNKNOWN, CommandType.convertCommandType("   "));
    }

    @Test
    void type_valid() {
        assertEquals(CommandType.PING, CommandType.convertCommandType("PING"));
    }

    @Test
    void type_invalid() {
        assertEquals(CommandType.UNKNOWN, CommandType.convertCommandType("HELLO"));
    }

    // -----------------
    // CommandParser
    // -----------------

    @Test
    void parse_null() {
        Command c = CommandParser.parse(null);
        assertEquals(CommandType.UNKNOWN, c.getType());
    }

    @Test
    void parse_blank() {
        Command c = CommandParser.parse("   ");
        assertEquals(CommandType.UNKNOWN, c.getType());
    }

    @Test
    void parse_no_args() {
        Command c = CommandParser.parse("PING");
        assertEquals(CommandType.PING, c.getType());
        assertNull(c.getArg("x"));
    }

    @Test
    void parse_args_good_and_bad() {
        Command c = CommandParser.parse("PONG a=1 bad =x b=2");

        assertEquals(CommandType.PONG, c.getType());
        assertEquals("1", c.getArg("a"));
        assertEquals("2", c.getArg("b"));

        assertNull(c.getArg("bad"));
        assertNull(c.getArg(""));
    }
}