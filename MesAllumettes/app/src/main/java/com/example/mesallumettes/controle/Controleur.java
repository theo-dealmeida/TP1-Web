package com.example.mesallumettes.controle;

import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mesallumettes.moteur.JeuxDesAllumettes;
import com.example.mesallumettes.joueur.Joueur;
import com.example.mesallumettes.view.Allumettes;

public class Controleur implements TextWatcher {

    JeuxDesAllumettes regles;
    Tache t; // la tache en background pour dérouler le jeu qui a comme but de
    // simuler le choix de 1,2 ou 3 allumettes par le joueur courant, et donc modifie le nb total
    // restant d'allumettes encore dispo.
    Allumettes vueall;
    TextView texte;
    ScrollView defile;


    public Controleur(JeuxDesAllumettes regles, Allumettes vue, TextView dialogue, ScrollView scroll) {
        this.regles = regles;
        this.vueall = vue;
        this.texte = dialogue;
        this.defile = scroll;
        texte.addTextChangedListener(this);
    }
    public void demarrerPartie(){
        vueall.changerNombreAllumettes(regles.obtenirNbTotalAllumettes());
        regles.démarrer(true); // suppose tjs que le 1er joueur des 2 commence
        t = new Tache();
        t.execute();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
    @Override
    public void afterTextChanged(Editable s) {
        defile.fullScroll(View.FOCUS_DOWN);
    }

    private class Tache extends AsyncTask<Void, Tache.MessageDeProgression, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                // pour démarrer, on affiche l'état du jeu
                MessageDeResolution messageResolution = new MessageDeResolution(regles.aQuiDeJouer(), -1, regles.obtenirNbAllumettesRestantes());
                publishProgress(messageResolution);

                // boucle de jeu : tant qu'on n'a pas de gagnant pour la partie
                while (regles.obtenirGagnant() == null) {
                    // découpe du coup en deux : sélection puis application

                    // sélection
                    MessageDeCoup messageCoup = new MessageDeCoup(regles.aQuiDeJouer(), regles.jouerUnTour());
                    publishProgress(messageCoup);

                    // temps d'attente pour laisser le temps (à l'utilisateur) de voir
                    // si c'est un joueur "artitificiel" (tour actuel)
                    messageCoup.getJoueur().temporiser();

                    // application du coup, changement de joueur
                    MessageDeResolution messageResolution2 = new MessageDeResolution(regles.aQuiDeJouer(), messageCoup.getNbJouées(), regles.obtenirNbAllumettesRestantes());
                    publishProgress(messageResolution2);

                    // temps d'attente pour laisser le temps (à l'utilisateur) de voir
                    // si c'est un joueur "artitificiel" (prochain tour, s'il y a)
                    messageCoup.getJoueur().temporiser();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // fin de partie
            String résultat = "Partie finie ou interrompue";
            if (regles.obtenirGagnant() != null)
                résultat = regles.obtenirGagnant().toString() + " a gagné !";

            return résultat;
        }


        @Override
        protected void onProgressUpdate(MessageDeProgression... msg) {
            msg[0].miseAJour();
        }

        @Override
        protected void onPostExecute(String s) {
            //encours = false;
            texte.append(s);
            //vueall.changerEtatInitial(true);
            vueall.invalidate();
        }

        private abstract class MessageDeProgression {
            Joueur j;
            int nbJouées;
            int nbRestantes;

            public Joueur getJoueur() {
                return j;
            }

            public void setJoueur(Joueur j) {
                this.j = j;
            }

            public int getNbJouées() {
                return nbJouées;
            }

            public void setNbJouées(int nb) {
                this.nbJouées = nb;
            }

            public int getNbRestantes() {
                return nbRestantes;
            }

            public void setNbRestantes(int nb) {
                this.nbRestantes = nb;
            }

            abstract void miseAJour(); // de la view Allumettes et de la zone textuelle
        }

        private class MessageDeCoup extends MessageDeProgression {

            protected MessageDeCoup(Joueur j, int nbJouées) {
                setJoueur(j);
                setNbJouées(nbJouées);
            }

            public String toString() {
                return j.toString() + " a joué : " + getNbJouées() + "\n";
            }

            @Override
            void miseAJour() {
                vueall.sélectionnerAllumettes(getNbJouées());
                vueall.invalidate();

                texte.append(this.toString());
                texte.invalidate();

            }
        }

        private class MessageDeResolution extends MessageDeProgression {


            protected MessageDeResolution(Joueur j, int nbJouées, int nbRestantes) {
                setJoueur(j);
                setNbJouées(nbJouées);
                setNbRestantes(nbRestantes);
            }

            public String toString() {
                String résultat = "il reste " + getNbRestantes() + " allumette(s). ";
                if (j == null) {
                    résultat += "C'est fini ! \n";
                } else {
                    résultat += "C'est à " + j + " de jouer\n";
                }
                return résultat;
            }

            @Override
            void miseAJour() {
                vueall.retirerAllumettes(getNbJouées());
                vueall.sélectionnerAllumettes(0);
                vueall.invalidate();

                texte.append(this.toString());
                texte.invalidate();
            }
        }

    }

}