package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.application.ai.heuristics.Heuristics;

public class AIFactory {

    public AgonAI createAI(AiDecoder config){
        //Crée le bon model d'ia en fonction des parametres passés dans la config
        return null;
    }

    private Heuristics createEvaluator(String type){
        //Appelé par createAI pour créer la fonction d'évaluation utile au model
        return null;
    }
}
