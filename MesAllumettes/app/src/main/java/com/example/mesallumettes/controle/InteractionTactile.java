package com.example.mesallumettes.controle;

import android.app.NotificationManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mesallumettes.joueur.InteractionHumaine;
import com.example.mesallumettes.view.Allumettes;

public class  InteractionTactile implements View.OnClickListener, InteractionHumaine {

    Allumettes all;
    Button val;
    int nbChoisis;
    Object objetsynchro=new Object(); // instancié qu'une seule fois
    // donc, toute thread recevant la référence de cet objet, pourra s'en servir
    // pour s'y endormir en attendant qu'une autre thread s'en serve pour la réveiller.
    // La condition est que ces appels à wait et notify soient bien initiés dans des
    // sections de code taggé synchronized.

    public InteractionTactile(Allumettes all, Button val) {
        this.val = val;
        this.all = all;
        this.val.setOnClickListener(this);//pour que le onclick enclenche prise en compte choix
        this.all.setOnClickListener(this);//pour que le onclick enclenche comptage nb all selec
    }

    @Override
    public void onClick(View v) {
        if (v == all) { //clic dans la partie correspondant à view allumettes
            //Faut verifier que nbChoisis n'excede ni 3, ni le nb all restantes affichées
            if (nbChoisis < ((Allumettes) all).obtenirNombreAllumettesVisibles())
                nbChoisis = (nbChoisis + 1) % 4;
            all.sélectionnerAllumettes(nbChoisis); //force affichage choix all selectionnees
            all.invalidate(); // force le redessin de la view allumettes
            Log.println(Log.DEBUG, "ALLUMETTES DEBUG", "Une all. de plus choisie " + nbChoisis);
        } else if (v == val) {
            Log.println(Log.DEBUG, "ALLUMETTES DEBUG", "Va t on, reveiller un humain ?");
            //clic sur le bouton valider
            // A tester: Si c'est le bon moment, cad, c'est bien pendant la partie
            // Reveiller la thread de jeu endormie dans le moniteur de objetsynchro
            synchronized (objetsynchro) {
                if (nbChoisis > 0) {// y'a bien au moins 1 allumette choisie
                    objetsynchro.notify();
                    //ne pas remettre encore nbChoisis à 0, car pas encore récupéré par thread notifiée
                    Log.println(Log.DEBUG, "ALLUMETTES DEBUG", "Réveil de l'humain ayant choisi " + nbChoisis + " allumettes");
                }
            }
        }
    }

    @Override
    public int obtenirNbChoisi() {
        int nbChoisisCourant = nbChoisis;
        nbChoisis = 0;
        return nbChoisisCourant;
    }

    @Override
    public Object getSynchro() {
        return objetsynchro;
    }

    public void setNbChoisis(int nbChoisis) {
        this.nbChoisis = nbChoisis;
    }
}
