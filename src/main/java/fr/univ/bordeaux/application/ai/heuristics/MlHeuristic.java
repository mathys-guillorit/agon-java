package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsNode;
import fr.univ.bordeaux.technical.utils.GameLogger;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;

/**
 * MCTS selection heuristic powered by a Machine Learning model (TensorFlow).
 */
public class MlHeuristic implements MctsSelectionHeuristic {

    /** The exploration constant used in the PUCT formula to balance exploration and exploitation. */
    private final double explorationParam;

    /** The loaded TensorFlow model used to evaluate the board positions. */
    private SavedModelBundle model;

    /**
     * Constructs the Machine Learning heuristic and loads the TensorFlow model from the disk.
     *
     * @param explorationParam The exploration constant used in the PUCT formula.
     */
    public MlHeuristic(final double explorationParam) {
        this.explorationParam = explorationParam;

        try {
            this.model = SavedModelBundle.load("tfdata/saved_model", "serve");
        } catch (RuntimeException e) {
                GameLogger.error("[ML Heuristic] WARNING: Failed to load the model.");
                GameLogger.error("[ML Heuristic] Error details: " + e.getMessage());
        }
    }

    @Override
    public double evaluateNode(final MctsNode parent, final MctsNode child, final AgonBoard board) {
        final double result;

        if (child.getVisitCount() == 0) {
            result = Double.MAX_VALUE;
        } else {
            final float[] features = extractFeatures(board);
            final double winProb = predictWithTensorFlow(features);

            final double explore = explorationParam * Math.sqrt(Math.log(parent.getVisitCount()) / child.getVisitCount());

            result = winProb + explore;
        }

        return result;
    }

    private double predictWithTensorFlow(final float[] features) {
        if (this.model == null) {
            throw new IllegalStateException("[ML Heuristic] Cannot predict without model.");
        }

        final float[][] inputMatrix = {features};

        try (TFloat32 inputTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(inputMatrix))) {

            try (Tensor outputTensor = model.session().runner()
                    .feed("xInput", inputTensor)
                    .fetch("prediction")
                    .run()
                    .get(0)) {

                return outputTensor.asRawTensor().data().asFloats().getFloat(0);

            } catch (RuntimeException e) {
                throw new IllegalStateException("[ML Heuristic] Failed to predict.", e);
            }
        }
    }

    private float[] extractFeatures(final AgonBoard board) {
        float wQueenDist = 6f;
        float bQueenDist = 6f;

        float wPawns = 0f;
        float bPawns = 0f;
        float wCentSum = 0f;
        float bCentSum = 0f;

        for (int i = 0; i <= 120; i++) {
            final PieceType piece = board.getPieceAt(i);

            if (piece == null) {
                continue;
            }

            if (piece == PieceType.WHITE_QUEEN) {
                wQueenDist = board.getCentrality(i);
            } else if (piece == PieceType.BLACK_QUEEN) {
                bQueenDist = board.getCentrality(i);
            } else if (piece == PieceType.WHITE_PAWN) {
                wPawns++;
                wCentSum += board.getCentrality(i);
            } else if (piece == PieceType.BLACK_PAWN) {
                bPawns++;
                bCentSum += board.getCentrality(i);
            }
        }

        final float posAdvantage = wCentSum - bCentSum;
        final float captureAdv = wPawns - bPawns;
        final float whiteMob = board.generateLegalMoves(Color.WHITE).size();
        final float blackMob = board.generateLegalMoves(Color.BLACK).size();

        return new float[] {
                wQueenDist,
                bQueenDist,
                posAdvantage,
                captureAdv,
                whiteMob,
                blackMob
        };
    }
}