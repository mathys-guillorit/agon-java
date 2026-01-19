import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import univ.bordeaux.fr.*;

class ExampleTest {

    @Test
    @DisplayName("division")
    void division(){
        var tmp = new Example();
        final int v = 5;
        tmp.setValue(v);
        tmp.compute();
        assertEquals(tmp.getValue(), tmp.getRd()/v);
    }

    /**
     * <code>ParameterizedTest</code> indicate that the test method is going to be<br>
     * executed many times with différent arguments. Numbers are the<br>
     * indexes of passed values as arguments<br>
     * <codeCsvSource></code> indicate the arguments are passed as CSV format
     */
    @ParameterizedTest(name = "{0} + {1} = {2}", quoteTextArguments = false)
    @CsvSource(textBlock = """
        0,    1,   1
        1,    2,   3
        49,  51, 100
        1,  100, 101
    """)
    void add(int first, int second, int expectedResult) {
        Example calculator = new Example();
        int result = calculator.add(first, second);
        assertEquals(
            expectedResult,
            result,
            () -> first + " + " + second + " should equal " + expectedResult);
    }

}