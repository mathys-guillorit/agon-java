package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.application.ai.heuristics.Heuristic;

public class AIFactory {

  public AgonAI createAI() {
    // Crée le bon model d'ia en fonction des parametres passés dans la config
    return null;
  }

  private Heuristic createHeuristic(String type) {
    // Appelé par createAI pour créer la fonction d'évaluation utile au model
    return null;
  }
}
