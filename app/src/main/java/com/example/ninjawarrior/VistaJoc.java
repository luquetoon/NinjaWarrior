package com.example.ninjawarrior;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import java.util.Vector;

public class VistaJoc extends View {

    private Grafics ninja;// Gràfic del ninja
    private int girNinja; // Increment de direcció
    private float acceleracioNinja; // augment de velocitat
    // Increment estàndard de gir i acceleració
    private static final int INC_GIR = 5;
    private static final float INC_ACCELERACIO = 0.5f;

    private Drawable drawableNinja, drawableGanivet, drawableEnemic;
    private Vector<Grafics> objectius;

    public VistaJoc(Context context) {
        super(context);

        Drawable drawableNinja, drawableGanivet, drawableEnemic;
        // Obtenim referència al recurs ninja_enemic guardat en carpeta Res
        drawableEnemic = context.getResources().getDrawable(R.drawable.enemie, null);

        // Creem els objectius o blancs i inicialitzem la seva velocitat, angle i
        // rotació. La posició inicial no la podem obtenir
        // fins a conèixer ample i alt pantalla
        objectius = new Vector<Grafics>();
        for (int i = 0; i < objectius.size() ; i++) {
            Grafics objectiu = new Grafics(this, drawableEnemic);
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

