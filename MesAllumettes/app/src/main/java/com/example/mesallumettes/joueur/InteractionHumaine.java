package com.example.mesallumettes.joueur;

public interface InteractionHumaine {

    public int obtenirNbChoisi(); // pour voir combien d’allumettes ont été choisies
    public Object getSynchro(); // pour fournir un objet de synchronisation, en vue d’un wait

}
