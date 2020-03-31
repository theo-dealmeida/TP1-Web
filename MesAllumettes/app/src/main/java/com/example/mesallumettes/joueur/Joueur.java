package com.example.mesallumettes.joueur;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Joueur {
    String nom = "Joeur Type 1";
    public Joueur() {

    }
    public void changerNom(String n) {
        nom = n;
    }

    public String obtenirNom() {
        return nom;
    }

    public String toString() {
        return ""+obtenirNom();
    }
    public void temporiser() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1000);
    }
    Random générateur = new Random();

    public int jouer(int nbRestantes) {
        int résultat = générateur.nextInt(Math.min(nbRestantes, 3))+1;
        return résultat;
    }


}
