package fr.univ.bordeaux.technical.ait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TensorFlowTrainerTest {

    @Test
    void testLoadDataFromCsv(@TempDir Path tempDir) throws Exception {
        File tempFile = tempDir.resolve("test_dataset.csv").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("f1,f2,f3,f4,f5,f6,label\n");
            writer.write("1.0,2.0,3.0,4.0,5.0,6.0,1\n");
            writer.write("0.5,0.5,0.5,0.5,0.5,0.5,0\n");
        }

        Method method = TensorFlowTrainer.class.getDeclaredMethod("loadDataFromCsv", String.class);
        method.setAccessible(true);

        float[][][] result = (float[][][]) method.invoke(null, tempFile.getAbsolutePath());

        assertNotNull(result);
        assertEquals(2, result[0].length);
        assertEquals(2, result[1].length);
        assertEquals(6, result[0][0].length);
        assertEquals(1, result[1][0].length);
        assertEquals(1.0f, result[0][0][0]);
        assertEquals(1f, result[1][0][0]);
        assertEquals(0.5f, result[0][1][0]);
        assertEquals(0f, result[1][1][0]);
    }

    @Test
    void testLoadDataFromCsvWithInvalidData(@TempDir Path tempDir) throws Exception {
        File tempFile = tempDir.resolve("invalid_dataset.csv").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("f1,f2,f3,f4,f5,f6,label\n");
            writer.write("1.0,2.0\n");
            writer.write("0.5,0.5,0.5,0.5,0.5,0.5,0\n");
        }

        Method method = TensorFlowTrainer.class.getDeclaredMethod("loadDataFromCsv", String.class);
        method.setAccessible(true);

        float[][][] result = (float[][][]) method.invoke(null, tempFile.getAbsolutePath());

        assertNotNull(result);
        assertEquals(1, result[0].length);
        assertEquals(1, result[1].length);
    }

    @Test
    void testDeleteDirectory(@TempDir Path tempDir) throws Exception {
        File subDir = tempDir.resolve("subfolder").toFile();
        assertTrue(subDir.mkdir());
        File tempFile = new File(subDir, "dummy.txt");
        assertTrue(tempFile.createNewFile());

        assertTrue(subDir.exists());
        assertTrue(tempFile.exists());

        Method method = TensorFlowTrainer.class.getDeclaredMethod("deleteDirectory", File.class);
        method.setAccessible(true);

        method.invoke(null, subDir);

        assertFalse(tempFile.exists());
        assertFalse(subDir.exists());
    }
}