package com.example.ninjawarrior;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Vector;

public class VistaJoc extends View {

    private Grafics ninja; // Gràfic del ninja
    private int girNinja; // Increment de direcció
    private float acceleracioNinja; // augment de velocitat
    // Increment estàndard de gir i acceleració
    private static final int INC_GIR = 5;
    private static final float INC_ACCELERACIO = 0.5f;
    private SharedPreferences sharedPreferences;
    private Drawable drawableNinja, drawableGanivet, drawEnemie;
    private Vector<Grafics> objectius;
    private int numObjectius;

    private Drawable[] imgNinjas = new Drawable[3];

    public VistaJoc(Context context, AttributeSet attrs) {
        super(context, attrs);



        // Obtener las preferencias
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Seleccionar ninja
        imgNinjas[0] =  getContext().getDrawable(R.drawable.ninja1);
        imgNinjas[1] =  getContext().getDrawable(R.drawable.ninja2);
        imgNinjas[2] =  getContext().getDrawable(R.drawable.ninja3);

        numObjectius = Integer.parseInt(MainActivity.objectiusPref);

        drawEnemie = context.getResources().getDrawable(R.drawable.enemie, null);

        ninja = new Grafics(this, imgNinjas[Integer.parseInt(MainActivity.ninjaPref)]);

        // Crear los objectivos
        objectius = new Vector<>();
        for (int i = 0; i < numObjectius; i++) {
            Grafics objectiu = new Grafics(this, drawEnemie);
            objectiu.setIncY(Math.random() * 4 - 2);
            objectiu.setIncX(Math.random() * 4 - 2);
            objectiu.setAngle((int) (Math.random() * 360));
            objectiu.setRotacio((int) (Math.random() * 8 - 4));
            objectius.add(objectiu);
        }
    }

    // Método que nos da ancho y alto pantalla
    @Override
    protected void onSizeChanged(int ancho, int alto, int anchoAnter, int altoAnter) {
        super.onSizeChanged(ancho, alto, anchoAnter, altoAnter);
        // Una vez que conocemos nuestro ancho y alto situamos los objetivos de
        // forma aleatoria
        for (Grafics objectiu : objectius) {
            objectiu.setPosX(Math.random() * (ancho - objectiu.getAmplada()));
            objectiu.setPosY(Math.random() * (alto - objectiu.getAltura()));
        }
    }

    // Método que dibuja la vista
    @Override
    synchronized protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Grafics objectiu : objectius) {
            objectiu.dibuixaGrafic(canvas);
        }
    }
}
