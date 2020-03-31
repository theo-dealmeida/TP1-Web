package com.example.mesallumettes;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mesallumettes.controle.Controleur;
import com.example.mesallumettes.controle.InteractionTactile;
import com.example.mesallumettes.joueur.JoueurHumain;
import com.example.mesallumettes.moteur.JeuxDesAllumettes;
import com.example.mesallumettes.joueur.Joueur;
import com.example.mesallumettes.view.Allumettes;

public class MainActivity extends Activity {
    private Allumettes all;
    private JeuxDesAllumettes règles = new JeuxDesAllumettes();
    private Controleur controleur;
    private Joueur[] joueurs=new Joueur[2];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allumettes);
        Log.d("ALLUMETTES DEBUG", "création");
        all = findViewById(R.id.allumettes);
        //all.sélectionnerAllumettes(4); // pour tester la Q1

        TextView msg = findViewById(R.id.message);
        ScrollView scroll = findViewById(R.id.scroll);
        Button demarrer = (Button) findViewById(R.id.demarrer);
        Button valider = (Button) findViewById(R.id.valider);
        JeuxDesAllumettes regles=new JeuxDesAllumettes();
        controleur=new Controleur(regles,all,msg,scroll);

        joueurs[0]=new Joueur();
        joueurs[0].changerNom("Théo");
        regles.ajouterJoueur(joueurs[0]);
        InteractionTactile it = new InteractionTactile(all, valider);
        JoueurHumain jh = new JoueurHumain(it);
        joueurs[1]= jh;
        joueurs[1].changerNom("Emilie");
        regles.ajouterJoueur(joueurs[1]);
    }
    public void démarrer(View v) {
        Log.d("ALLUMETTES DEBUG", "bouton démarrerPartie");
        controleur.demarrerPartie();
    }

}
