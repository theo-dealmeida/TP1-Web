package com.example.mesallumettes.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.mesallumettes.R;

public class Allumettes extends View {
    Drawable allumette;
    int padding=30;
    int largall=30;
    int hautall=200;
    static final float MAXRATIO = 0.15f; // 30 sur 200
    static final float MINRATIO = 0.05f; // 10 sur 200
    int nombreTotalAllumettes = 21;
    int nombreAllumettesVisibles = 21; // pour tester
    int nbSelectionnées = 0;
    Paint pPlein,pTiret;
    public Allumettes(Context context ,AttributeSet attrs) { // dessiner toutes les allumettes
        super(context,attrs);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            allumette = context.getDrawable(R.drawable.allumette);
        } else {
            allumette = context.getResources().getDrawable(R.drawable.allumette);
        }
        pPlein=new Paint();
        pPlein.setColor(Color.rgb(0, 128, 0));
        pPlein.setStrokeWidth(4);
        pPlein.setAntiAlias(true);
        pPlein.setStyle(Paint.Style.STROKE);
        pPlein.setTextSize(18);
        nbSelectionnées=0; // juste pour tester
        DashPathEffect effet = new DashPathEffect(new float[]{10, 25}, 0);
        pTiret = new Paint(pPlein);
        pTiret.setPathEffect(effet);
        pTiret.setStrokeWidth(2);
        pTiret.setColor(Color.GRAY);
    }
    @Override
    protected void onSizeChanged (int w, int h,  int oldw,  int oldh) {
        calculDimensionAllumette();
    }
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String msg="Bienvenue dans le jeu des Allumettes";
        canvas.drawText(msg,(float)padding,(float)padding,pPlein);
        Rect dim = new Rect ();
        pPlein.getTextBounds(msg, 0, msg.length(),dim);
        int x = padding;
        int y = padding;
        float epaisseur = padding/2;// pPlein.getStrokeWidth() / 2;
        //Q0: dessiner juste une allumette
        //allumette.setBounds(padding*2,padding*2,padding*2+largall,padding*2+hautall);
        //allumette.draw(canvas);
        int nb1reLigne = nombreTotalAllumettes / 2 + (nombreTotalAllumettes % 2);
        for (int ligne = 0; ligne < 2; ligne++) {
            x = padding;
            y = y + (hautall + padding) * ligne;

            int debut = nb1reLigne * ligne;
            int fin = nb1reLigne + (nombreTotalAllumettes - nb1reLigne) * ligne;
            int indice_selectionne = nombreAllumettesVisibles - nbSelectionnées;
            int i;
            for (i = debut; (i < fin)&&(i < nombreAllumettesVisibles); i++) { // modif la condition pour distinguer les visibles des enlevees
                int lx = x;
                int ly = y;
                int dx = (x + largall);
                int dy = (y + hautall);


                if (i >= indice_selectionne) {
                canvas.drawRect(lx - epaisseur, ly - epaisseur, dx + epaisseur, dy + epaisseur, pPlein);
                }

                allumette.setBounds(lx, ly, dx, dy);
                allumette.draw(canvas);

                x = x + 2 * largall;
            }
            // on continue là où on s'était arrêté
            // pour dessiner des traces des allumettes enlevées.
            // non obligatoire...
            for (int j = i ; j < fin; j++) {
                int lx = x;
                int ly = y;
                int dx = (x + largall);
                int dy = (y + hautall);

                canvas.drawRect(lx , ly , dx , dy , pTiret);

                x = x + 2 * largall;

            }
        }


                //ce rectangle a une hauter déjà définie par défaut dans le xml
        canvas.drawRect(0, 0, ((getWidth() - 1)<=0 ? 10:(getWidth() - 1)), getHeight() - 1, pPlein);
        Log.d("ALLUMETTES DEBUG", "dessin du rectangle  de largeur "+ (getWidth()-1));

    }
    @Override
    protected  void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        // voir la docu. de onMeasure
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
        Log.d("affiche width ", "avant calcul dimension allumette suggérée " + getWidth());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("affiche width ", "avant calcul dimension allumette suggérée et apres le superonm " + getWidth());
        calculDimensionAllumette();
    }
    protected void calculDimensionAllumette() {
    // attention, ici, on dirait que la getWidth est toujours à 0, tant qu'on 'a rien affiché??. Du coup...
        // a moins de mettre en dur dans le Xml, une taille, on risque de pas bien calculer le
        // format en pixel à utiliser pour chaque allumette.

        Log.d("affiche width", "" + getWidth());
        largall=(getWidth()-2*padding)/nombreTotalAllumettes;
        if (largall<=0)largall=40;
        hautall = (getHeight() - padding * 3) / 2;
        // d'où la suite du calcul étrange
        int nbEmplacementParLigne = nombreTotalAllumettes;  // 1 sur deux, sur deux lignes
        int nbLignes = 2;
        float largallumette,hautallumette; // var locale pour gérer le ratio (float)
        largallumette = ((getWidth() - padding * 2)) / (nbEmplacementParLigne);
        hautallumette = ((getHeight() - padding * 3)) / (nbLignes);

        if ((largallumette > 0) && (hautallumette > 0)) {
            float ratio = largallumette / hautallumette;

            if (ratio > MAXRATIO) largallumette = (hautallumette * MAXRATIO);
            else if (ratio < MINRATIO) hautallumette = (largallumette / MINRATIO);
        }
        largall= (int) largallumette;
        hautall= (int) hautallumette;
    }

    public int obtenirNombreAllumettesSélectionnées() {
        return nbSelectionnées;
    }
    public void sélectionnerAllumettes(int nb) {
        if (nb >= 0)  this.nbSelectionnées = Math.min(nb, nombreAllumettesVisibles);
    }
    public boolean retirerAllumettes(int nb) {
        boolean result = false;

        if ((nb >= 0) && (nombreAllumettesVisibles > 0)) {
            nombreAllumettesVisibles = nombreAllumettesVisibles - nb;
            if (nombreAllumettesVisibles < 0) nombreAllumettesVisibles = 0;
        }
        return result;
    }
    //pour redemarrer une partie, il faut que on reparte avec nb allumettes visibles
    public void changerNombreAllumettes(int nb){
        this.nombreAllumettesVisibles=nb;
    }

    public int obtenirNombreAllumettesVisibles() {
        return nombreAllumettesVisibles;
    }
}

