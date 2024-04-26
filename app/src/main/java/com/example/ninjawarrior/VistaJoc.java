package com.example.ninjawarrior;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import java.util.Vector;

public class VistaJoc extends View {

    private Drawable drawableNinja, drawableGanivet, drawableEnemic;
    private Vector<Grafics> objectius;

    private Grafics ninja;// Gràfic del ninja
    private int girNinja; // Increment de direcció
    private float acceleracioNinja; // augment de velocitat
    // Increment estàndard de gir i acceleració
    private static final int INC_GIR = 5;
    private static final float INC_ACCELERACIO = 0.5f;


    public VistaJoc(Context context) {
        super(context);

        // Obtener referencia al recurso ninja_enemic guardado en la carpeta Res
        drawableEnemic = context.getResources().getDrawable(R.drawable.enemie, null);

        int numObjectius = 10; // Número de objetivos
        objectius = new Vector<>();
        for (int i = 0; i < numObjectius; i++) {
            Grafics objectiu = new Grafics(this, drawableEnemic);
            objectiu.setIncY(Math.random() * 4 - 2);
            objectiu.setIncX(Math.random() * 4 - 2);
            objectiu.setAngle((int) (Math.random() * 360));
            objectiu.setRotacio((int) (Math.random() * 8 - 4));
            objectius.add(objectiu);
        }
    }
}
