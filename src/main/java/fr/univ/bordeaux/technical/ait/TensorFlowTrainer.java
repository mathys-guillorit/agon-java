package fr.univ.bordeaux.technical.ait;

import org.tensorflow.Graph;
import org.tensorflow.Operand;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Assign;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.op.core.Variable;
import org.tensorflow.Signature;
import org.tensorflow.types.TFloat32;
import org.tensorflow.SavedModelBundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone script responsible for training the TensorFlow Machine Learning model purely in Java.
 * * <p>It reads the dataset, trains a Logistic Regression model via gradient descent,
 * and exports the trained model (.pb format) to be utilized by the MCTS heuristic.
 */
public class TensorFlowTrainer {

    /** The file path to the training dataset. */
    private static final String CSV_PATH = "tfdata/agon_dataset.csv";

    /** The directory path where the trained model will be exported. */
    private static final String MODEL_EXPORT_DIR = "tfdata/saved_model";

    /**
     * The entry point of the training script.
     * Cleans the previous export directory, loads the dataset, and triggers the training process.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("=== Starting TensorFlow training (100% Java) ===");

        deleteDirectory(new File(MODEL_EXPORT_DIR));

        float[][][] data = loadDataFromCsv(CSV_PATH);
        if (data == null) return;

        float[][] features = data[0];
        float[][] labels = data[1];

        System.out.println("Data loaded: " + features.length + " games found.");

        trainAndSaveModel(features, labels);
    }

    /**
     * Constructs the computational graph, executes the training loop using Mean Squared Error (MSE),
     * and exports the model using the high-level Signature API.
     *
     * @param features A 2D array of input features (shape: [N, 6]).
     * @param labels   A 2D array of target labels (shape: [N, 1]).
     */
    private static void trainAndSaveModel(float[][] features, float[][] labels) {
        try (Graph graph = new Graph();
             TFloat32 xTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(features));
             TFloat32 yTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(labels))) {

            Ops tf = Ops.create(graph);

            Placeholder<TFloat32> xInput = tf.withName("xInput").placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, 6)));
            Placeholder<TFloat32> yInput = tf.withName("yInput").placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, 1)));

            Variable<TFloat32> weights = tf.variable(tf.zeros(tf.constant(Shape.of(6, 1)), TFloat32.class));
            Variable<TFloat32> bias = tf.variable(tf.zeros(tf.constant(Shape.of(1)), TFloat32.class));

            Assign<TFloat32> initWeights = tf.assign(weights, tf.zeros(tf.constant(Shape.of(6, 1)), TFloat32.class));
            Assign<TFloat32> initBias = tf.assign(bias, tf.zeros(tf.constant(Shape.of(1)), TFloat32.class));

            Operand<TFloat32> logits = tf.math.add(tf.linalg.matMul(xInput, weights), bias);

            Operand<TFloat32> prediction = tf.withName("prediction").math.sigmoid(logits);

            Operand<TFloat32> diff = tf.math.sub(prediction, yInput);
            Operand<TFloat32> squareDiff = tf.math.square(diff);
            Operand<TFloat32> loss = tf.math.mean(squareDiff, tf.constant(new int[]{0}));

            Operand<TFloat32> learningRate = tf.constant(0.01f);
            var gradients = tf.gradients(loss, java.util.Arrays.asList(weights, bias));
            Operand<TFloat32> weightUpdate = tf.assignSub(weights, tf.math.mul(learningRate, gradients.dy(0)));
            Operand<TFloat32> biasUpdate = tf.assignSub(bias, tf.math.mul(learningRate, gradients.dy(1)));


            try (Session session = new Session(graph)) {
                session.runner().addTarget(initWeights).addTarget(initBias).run();

                int epochs = 100;
                System.out.println("Starting training for " + epochs + " epochs...");

                for (int i = 1; i <= epochs; i++) {
                    session.runner()
                            .feed(xInput, xTensor)
                            .feed(yInput, yTensor)
                            .addTarget(weightUpdate)
                            .addTarget(biasUpdate)
                            .run();

                    if (i % 10 == 0) {
                        try (org.tensorflow.Tensor lossValue = session.runner()
                                .feed(xInput, xTensor)
                                .feed(yInput, yTensor)
                                .fetch(loss)
                                .run().get(0)) {
                            System.out.println("Epoch " + i + " | Loss: " + lossValue.asRawTensor().data().asFloats().getFloat(0));
                        }
                    }
                }
                System.out.println("Training completed!");

                System.out.println("Creating high-level Signature for export...");

                Signature signature = Signature.builder()
                        .input("xInput", xInput)
                        .output("prediction", prediction)
                        .build();

                System.out.println("Exporting the model...");

                SavedModelBundle.exporter(MODEL_EXPORT_DIR)
                        .withSession(session)
                        .withTags("serve")
                        .withSignature(signature)
                        .export();

                System.out.println("===> SUCCESS: Model physically saved to " + MODEL_EXPORT_DIR);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the dataset from a specified CSV file and extracts the features and labels.
     *
     * @param filePath The path to the CSV dataset file.
     * @return A 3D array where index 0 contains the features array and index 1 contains the labels array,
     * or null if an error occurs during file reading.
     */
    private static float[][][] loadDataFromCsv(String filePath) {
        List<float[]> featureList = new ArrayList<>();
        List<float[]> labelList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != 7) continue;
                float[] x = new float[6];
                for (int i = 0; i < 6; i++) x[i] = Float.parseFloat(values[i]);
                featureList.add(x);

                float[] y = new float[1];
                y[0] = Float.parseFloat(values[6]);
                labelList.add(y);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
            return null;
        }

        return new float[][][] { featureList.toArray(new float[0][0]), labelList.toArray(new float[0][0]) };
    }

    /**
     * Recursively deletes a directory and all of its contents.
     *
     * @param directoryToBeDeleted The File object representing the directory to be deleted.
     */
    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}