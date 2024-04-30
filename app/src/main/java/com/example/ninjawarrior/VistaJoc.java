package com.example.ninjawarrior;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceManager;

import java.util.Vector;

public class VistaJoc extends View {

    private Grafics ninja;// Gràfic del ninja
    private int girNinja; // Increment de direcció
    private float acceleracioNinja; // augment de velocitat
    // Increment estàndard de gir i acceleració
    private static final int INC_GIR = 5;
    private static final float INC_ACCELERACIO = 0.5f;
    private SharedPreferences sharedPreferences;
    private Drawable drawableNinja, drawableGanivet, drawEnemie;
    private Vector<Grafics> objectius;

    public VistaJoc(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawEnemie = context.getResources().getDrawable(R.drawable.enemie, null);

        //Obtenir el valor de la sharedpref 'setEnemies'
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int numObjectius = sharedPreferences.getInt("setEnemines", 5);

        //Crear els objectius
        objectius = new Vector<Grafics>();
        for (int i = 0; i < numObjectius; i++) {
            Grafics objectiu = new Grafics(this, drawEnemie);
            objectiu.setIncY(Math.random() * 4 - 2);
            objectiu.setIncX(Math.random() * 4 - 2);
            objectiu.setAngle((int) (Math.random() * 360));
            objectiu.setRotacio((int) (Math.random() * 8 - 4));
            objectius.add(objectiu);
        }
    }

    // Métode que ens dóna ample i alt pantalla
    @Override
    protected void onSizeChanged(int ancho, int alto, int anchoAnter, int altoAnter) {
        super.onSizeChanged(ancho, alto, anchoAnter, altoAnter);
        // Una vegada que coneixem el nostre ample i alt situem els objectius de
        // forma aleatória
        for (Grafics objectiu : objectius) {
            objectiu.setPosX(Math.random() * (ancho - objectiu.getAmplada()));
            objectiu.setPosY(Math.random() * (alto - objectiu.getAltura()));
        }
    }
    // Métode que dibuixa la vista
    @Override
    synchronized protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Grafics objetiu : objectius) {
            objetiu.dibuixaGrafic(canvas);
        }
    }

}

