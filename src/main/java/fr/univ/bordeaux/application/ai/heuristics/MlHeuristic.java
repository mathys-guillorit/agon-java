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
 *
 * <p>Implements a variant of the PUCT (Predictor + UCB) algorithm popularized by AlphaZero. Instead
 * of relying solely on random simulation statistics (rollouts), this heuristic uses a neural
 * network to predict the win probability of a given state, while preserving the exploration factor
 * (C) of the UCT formula.
 */
public class MlHeuristic implements MctsSelectionHeuristic {

  /** The exploration parameter determining the weight of exploration. */
  private final double explorationParam;

  /** The compiled TensorFlow model loaded into memory for inference. */
  private SavedModelBundle model;

  /**
   * Constructs the Machine Learning heuristic and loads the TensorFlow model from the disk.
   *
   * @param explorationParam The exploration constant used in the PUCT formula (typically √2).
   */
  public MlHeuristic(double explorationParam) {
    this.explorationParam = explorationParam;

    try {
      this.model = SavedModelBundle.load("tfdata/saved_model", "serve");
    } catch (Exception e) {
      GameLogger.error(
          "[ML Heuristic] WARNING: Failed to load the model. Ensure the directory exists and contains a valid .pb file.");
      GameLogger.error("[ML Heuristic] Error details: " + e.getMessage());
      this.model = null;
    }
  }

  /**
   * Evaluates a child node relative to its parent using the PUCT algorithm combined with the
   * TensorFlow model prediction.
   *
   * @param parent The parent node providing context such as the total visit count.
   * @param child The child node to be evaluated.
   * @param board The current game board representing the exact state of the child node.
   * @return The calculated selection score combining the model's win probability and the
   *     exploration bonus.
   */
  @Override
  public double evaluateNode(MctsNode parent, MctsNode child, AgonBoard board) {
    if (child.getVisitCount() == 0) {
      return Double.MAX_VALUE;
    }

    float[] features = extractFeatures(board);

    double winProbability = predictWithTensorFlow(features);

    double exploit = winProbability;
    double explore =
        explorationParam
            * Math.sqrt(Math.log(parent.getVisitCount()) / (double) child.getVisitCount());

    return exploit + explore;
  }

  /**
   * Calls the in-memory TensorFlow model to obtain a win probability prediction for the current
   * board state.
   *
   * @param features The array of 6 numerical features extracted from the game board.
   * @return The predicted probability of winning, ranging between 0.0 and 1.0. Returns 0.5 as a
   *     fallback if the model fails.
   */
  private double predictWithTensorFlow(float[] features) {
    if (this.model == null) {
      throw new RuntimeException("[ML Heuristic] Cannot predict without model.");
    }

    float[][] inputMatrix = new float[][] {features};

    try (TFloat32 inputTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(inputMatrix))) {

      try (Tensor outputTensor =
          model.session().runner().feed("xInput", inputTensor).fetch("prediction").run().get(0)) {

        return outputTensor.asRawTensor().data().asFloats().getFloat(0);

      } catch (Exception e) {
        throw new RuntimeException("[ML Heuristic] Failed to predict.");
      }
    }
  }

  /**
   * Translates the board state into an array of six numerical features required by the TensorFlow
   * model.
   *
   * @param board The current state of the game board.
   * @return A float array containing the calculated features: white queen distance, black queen
   *     distance, guard position advantage, capture advantage, white mobility, and black mobility.
   */
  private float[] extractFeatures(AgonBoard board) {
    float whiteQueenDist = 6f;
    float blackQueenDist = 6f;
    float whiteGuardsOnBoard = 0f;
    float blackGuardsOnBoard = 0f;
    float whiteGuardsCentralitySum = 0f;
    float blackGuardsCentralitySum = 0f;

    for (int i = 0; i <= 120; i++) {
      PieceType piece = board.getPieceAt(i);
      if (piece == null) continue;

      if (piece == PieceType.WHITE_QUEEN) {
        whiteQueenDist = board.getCentrality(i);
      } else if (piece == PieceType.BLACK_QUEEN) {
        blackQueenDist = board.getCentrality(i);
      } else if (piece == PieceType.WHITE_PAWN) {
        whiteGuardsOnBoard++;
        whiteGuardsCentralitySum += board.getCentrality(i);
      } else if (piece == PieceType.BLACK_PAWN) {
        blackGuardsOnBoard++;
        blackGuardsCentralitySum += board.getCentrality(i);
      }
    }

    float guardPositionAdvantage = whiteGuardsCentralitySum - blackGuardsCentralitySum;
    float captureAdvantage = whiteGuardsOnBoard - blackGuardsOnBoard;
    float whiteMobility = board.generateLegalMoves(Color.WHITE).size();
    float blackMobility = board.generateLegalMoves(Color.BLACK).size();

    return new float[] {
      whiteQueenDist,
      blackQueenDist,
      guardPositionAdvantage,
      captureAdvantage,
      whiteMobility,
      blackMobility
    };
  }
}
