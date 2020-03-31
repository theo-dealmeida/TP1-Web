package com.example.mesallumettes.joueur;

import android.util.Log;

public class JoueurHumain extends Joueur {
    private final InteractionHumaine ih;

    public JoueurHumain(InteractionHumaine interact){
        this.ih = interact;
    }

    public void temporiser() throws InterruptedException {}

    public int jouer(int nbRestantes) {
        Object synchro=ih.getSynchro(); // un objet "normal" mais dont adresse connue de tous ceux
        // toutes les threads - voulant l'utiliser pour se synchroniser
        synchronized(synchro) { // qu'on  considère ayant  un moniteur associé (avec motclef synchronized)
            Log.println(Log.DEBUG,"ALLUMETTES DEBUG", "avant de s'endormir");
            try{
                synchro.wait(); // afin que lathread s'endorme dans la queue de threads du moniteur
            } catch (InterruptedException e) {} // si par malheur on est interrompu, on verra ...
        }
        //si ici, la thread du joueur a été débloquée, car il a validé selection des allumettes, on continue:
        // Si on a bien fait le notify quand le nombre d'allumettes est effectivement déjà
        // déterminé, pas de risque que obtenirNbChoisi bugge en renvoyant 0 allumettes
        int nbc=ih.obtenirNbChoisi(); // on a fait en sorte que ce nbc sera choisi parmi allumettes restantes, tel qu'on le
        // voit sur la GUI
        return nbc;
    }
}
