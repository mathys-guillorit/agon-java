package fr.univ.bordeaux.application.ai.strategy;

/** Exception thrown when the AI mode and heuristic are incompatible. */
public class IncompatibleAiConfigurationException extends RuntimeException {
  public IncompatibleAiConfigurationException(String message) {
    super(message);
  }
}
