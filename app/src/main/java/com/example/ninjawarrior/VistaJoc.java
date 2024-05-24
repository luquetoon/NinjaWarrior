package com.example.ninjawarrior;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;

public class VistaJoc extends View {

    private JocActivity pare;
    private static final double INC_VELOCITAT_GANIVET = 12;
    private int girNinja;
    private float acceleracioNinja;
    private static final int INC_GIR = 5;
    private static final float INC_ACCELERACIO = 0.5f;

    // Dibujo
    private Drawable drawableGanivet, drawEnemie;
    private Drawable[] imgNinjas = new Drawable[3];
    private Grafics ninja, ganivet;
    private Vector<Grafics> objectius;
    private int numObjectius;
    private static int PERIODE_PROCES = 50;
    private long ultimProces = 0;
    private int vidas = 1;

    private int score = 0;

    private final ThreadJoc thread = new ThreadJoc();

    // Controles
    private float mX = 0, mY = 0;
    private boolean llancament = false;
    private boolean ganivetActiu = false;
    private double tempsGanivet;

    // Media player
    MediaPlayer mpLlancament, mpExplosio;

    private Drawable[] drawableObjectiu = new Drawable[8];

    public VistaJoc(Context context, AttributeSet attrs) {
        super(context, attrs);

        score = 0;

        // Cargar los sonido
        mpLlancament = MediaPlayer.create(context, R.raw.llancament);
        mpExplosio = MediaPlayer.create(context, R.raw.explosio);

        // Seleccionar ninja
        imgNinjas[0] = getContext().getDrawable(R.drawable.ninja1);
        imgNinjas[1] = getContext().getDrawable(R.drawable.ninja2);
        imgNinjas[2] = getContext().getDrawable(R.drawable.ninja3);

        drawableObjectiu[0] = context.getResources().getDrawable(R.drawable.cap_ninja, null);
        drawableObjectiu[2] = context.getResources().getDrawable(R.drawable.cua_ninja, null);
        drawableObjectiu[5] = context.getResources().getDrawable(R.drawable.brac_dret, null);
        drawableObjectiu[6] = context.getResources().getDrawable(R.drawable.brac_esquerre, null);
        drawableObjectiu[1] = context.getResources().getDrawable(R.drawable.cos_ninja, null);
        drawableObjectiu[3] = context.getResources().getDrawable(R.drawable.cama_dreta, null);
        drawableObjectiu[4] = context.getResources().getDrawable(R.drawable.cama_esquerra, null);

        numObjectius = Integer.parseInt(MainActivity.objectiusPref);

        drawEnemie = context.getResources().getDrawable(R.drawable.enemie, null);
        drawableGanivet = context.getResources().getDrawable(R.drawable.ganivet, null);

        ganivet = new Grafics(this, drawableGanivet);
        ninja = new Grafics(this, imgNinjas[Integer.parseInt(MainActivity.ninjaPref)]);

        // Crear los objetivos
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

    public void setPare(JocActivity pare) {
        this.pare = pare;
    }


    @Override
    protected void onSizeChanged(int ancho, int alto, int anchoAnter, int altoAnter) {
        super.onSizeChanged(ancho, alto, anchoAnter, altoAnter);

        float centroX = (ancho - ninja.getAmplada()) / 2f;
        float centroY = (alto - ninja.getAltura()) / 2f;
        ninja.setPosX(centroX);
        ninja.setPosY(centroY);

        for (Grafics objectiu : objectius) {
            do{
                objectiu.setPosX(Math.random()*(ancho-objectiu.getAmplada()));
                objectiu.setPosY(Math.random()*(alto-objectiu.getAltura()));
            } while(objectiu.distancia(ninja) < (ancho+alto)/5);

        }
        ultimProces = System.currentTimeMillis();
        thread.start();
    }

    @Override
    synchronized protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ninja.dibuixaGrafic(canvas);
        for (Grafics objectiu : objectius) {
            objectiu.dibuixaGrafic(canvas);
        }
        if (ganivetActiu) {
            ganivet.dibuixaGrafic(canvas);
        }
    }

    synchronized protected void actualitzaMoviment() {
        long instant_actual = System.currentTimeMillis();
        if (ultimProces + PERIODE_PROCES > instant_actual) {
            return;
        }
        double retard = (instant_actual - ultimProces) / PERIODE_PROCES;
        ultimProces = instant_actual;

        ninja.setAngle((int) (ninja.getAngle() + girNinja * retard));
        double nIncX = ninja.getIncX() + acceleracioNinja *
                Math.cos(Math.toRadians(ninja.getAngle())) * retard;
        double nIncY = ninja.getIncY() + acceleracioNinja *
                Math.sin(Math.toRadians(ninja.getAngle())) * retard;

        if (Math.hypot(nIncX, nIncY) <= Grafics.MAX_VELOCITAT) {
            ninja.setIncX(nIncX);
            ninja.setIncY(nIncY);
        }

        ninja.incrementaPos(retard);
        for (Grafics objectiu : objectius) {
            objectiu.incrementaPos(retard);
        }

        //Colision ninja
        for (int i = 0; i < objectius.size(); i++) {
            if (objectius.elementAt(i).verificaColision(ninja)) {
                vidas =- 1 ;
                if(vidas <= 0){
                    vidas = 0;
                    break;
                }
            }
        }

        if (ganivetActiu) {
            ganivet.incrementaPos(retard);
            tempsGanivet -= retard;
            if (tempsGanivet < 0) {
                ganivetActiu = false;
            } else {
                for (int i = 0; i < objectius.size(); i++) {
                    if (ganivet.verificaColision(objectius.elementAt(i))) {
                        destruyeObjectiu(i);
                        break;
                    }
                }
            }
        }
    }

    private void destruyeObjectiu(int i) {
        score += 30;
        int numParts = 7;
        if(objectius.get(i).getDrawable()== drawEnemie){
            for(int n = 0; n < numParts; n++){
                Grafics objectiu = new Grafics(this, drawableObjectiu[n]);
                objectiu.setPosX(objectius.get(i).getPosX());
                objectiu.setPosY(objectius.get(i).getPosY());
                objectiu.setIncX(Math.random()*7-3);
                objectiu.setIncY(Math.random()*7-3);
                objectiu.setAngle((int)(Math.random()*360));
                objectiu.setRotacio((int)(Math.random()*8-4));
                objectius.add(objectiu);
            }
        }
        objectius.remove(i);
        mpExplosio.start();
        ganivetActiu = false;
    }

    @Override
    public boolean onKeyDown(int codigoTecla, KeyEvent evento) {
        super.onKeyDown(codigoTecla, evento);
        boolean pulsacion = true;
        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                acceleracioNinja = +INC_ACCELERACIO;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                girNinja = -INC_GIR;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                girNinja = +INC_GIR;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                llancament = true;
                break;
            default:
                pulsacion = false;
                break;
        }
        return pulsacion;
    }

    @Override
    public boolean onKeyUp(int codigoTecla, KeyEvent evento) {
        super.onKeyUp(codigoTecla, evento);
        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                acceleracioNinja = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                girNinja = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (llancament) {
                    activaGanivet();
                }
                break;
        }
        return true;
    }

    private void activaGanivet() {
        ganivet.setPosX(ninja.getPosX() + ninja.getAmplada() / 2 - ganivet.getAmplada() / 2);
        ganivet.setPosY(ninja.getPosY() + ninja.getAltura() / 2 - ganivet.getAltura() / 2);
        ganivet.setAngle(ninja.getAngle());
        ganivet.setIncX(Math.cos(Math.toRadians(ganivet.getAngle())) * INC_VELOCITAT_GANIVET);
        ganivet.setIncY(Math.sin(Math.toRadians(ganivet.getAngle())) * INC_VELOCITAT_GANIVET);
        tempsGanivet = (int) Math.min(this.getWidth() / Math.abs(ganivet.getIncX()), this.getHeight() / Math.abs(ganivet.getIncY())) - 2;
        ganivetActiu = true;
        mpLlancament.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent evento) {
        super.onTouchEvent(evento);
        float x = evento.getX();
        float y = evento.getY();
        switch (evento.getAction()) {
            case MotionEvent.ACTION_DOWN:
                llancament = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dy < 6 && dx > 6) {
                    girNinja = Math.round((x - mX) / 2);
                    llancament = false;
                } else if (dx < 6 && dy > 6) {
                    acceleracioNinja = Math.round((mY - y) / 25);
                    llancament = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                girNinja = 0;
                acceleracioNinja = 0;
                if (llancament) {
                    activaGanivet();
                }
                break;
        }
        mX = x;
        mY = y;
        return true;
    }

    class ThreadJoc extends Thread {
        @Override
        public void run() {
            while (vidas != 0) {
                actualitzaMoviment();
            }
            VistaJoc.this.post(() -> {
                pare.setScoreEndGame(score);
            });
        }
    }

}
