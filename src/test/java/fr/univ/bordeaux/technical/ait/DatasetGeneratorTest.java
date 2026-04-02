package fr.univ.bordeaux.technical.ait;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class DatasetGeneratorTest {

    @Test
    void testExtractFeaturesReturnsValidCsvString() throws Exception {
        AgonBoardImpl board = new AgonBoardImpl();
        board.initBaseConfiguration();

        Method method = DatasetGenerator.class.getDeclaredMethod("extractFeatures", AgonBoard.class);
        method.setAccessible(true);

        String csvResult = (String) method.invoke(null, board);

        assertNotNull(csvResult);
        assertFalse(csvResult.trim().isEmpty());

        String[] values = csvResult.split(",");
        assertTrue(values.length >= 6);

        assertDoesNotThrow(() -> {
            for (String val : values) {
                Float.parseFloat(val);
            }
        });
    }
}