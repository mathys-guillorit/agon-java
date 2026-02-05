package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.application.ai.interfaces.AgonAI;
import fr.univ.bordeaux.application.ai.interfaces.BoardEvaluator;

public class AIFactory {

    public AgonAI createAI(AiDecoder config){
        //Crée le bon model d'ia en fonction des parametres passés dans la config
        return null;
    }

    private BoardEvaluator createEvaluator(String type){
        //Appelé par createAI pour créer la fonction d'évaluation utile au model
        return null;
    }
}
