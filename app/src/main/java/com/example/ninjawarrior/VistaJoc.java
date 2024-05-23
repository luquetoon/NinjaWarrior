package com.example.ninjawarrior;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.preference.PreferenceManager;

import java.util.Vector;

public class VistaJoc extends View {

    private static final double INC_VELOCITAT_GANIVET = 12;
    private Grafics ninja,ganivet;
    private int girNinja;
    private float acceleracioNinja; // augment de velocitat
    private static final int INC_GIR = 5; // Increment estàndard de gir i acceleració
    private static final float INC_ACCELERACIO = 0.5f;
    private SharedPreferences sharedPreferences;
    private Drawable drawableNinja, drawableGanivet, drawEnemie;
    private Vector<Grafics> objectius;
    private int numObjectius;
    private Drawable[] imgNinjas = new Drawable[3];

    // //// THREAD I TEMPS //////
    private ThreadJoc thread = new ThreadJoc();//Cada quant temps volem processar canvis (ms)
    private static int PERIODE_PROCES = 50;// Quan es va realitzar l'últim procés
    private long ultimProces = 0;
    private int vidas = 3;

    //Controles
    private float mX=0, mY=0;
    private boolean llancament = false;
    private boolean ganivetActiu = false;
    private double tempsGanivet;

    MediaPlayer mpLlancament, mpExplosio;

    private SoundPool soundPool;
    private int soundIdLlancament;
    private int soundIdExplosio;

    private Drawable drawableObjectiu[] = new Drawable[8];

    public VistaJoc(Context context, AttributeSet attrs) {
        super(context, attrs);

        //SoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }

        // Carga los sonidos en el SoundPool y guarda sus IDs
        soundIdLlancament = soundPool.load(context, R.raw.llancament, 1);
        soundIdExplosio = soundPool.load(context, R.raw.explosio, 1);


        // Obtener las preferencias
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

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
        drawableGanivet = context.getResources().getDrawable(R.drawable.ganivet,null);

        ganivet = new Grafics(this, drawableGanivet);
        ninja = new Grafics(this, imgNinjas[Integer.parseInt(MainActivity.ninjaPref)]);

        mpLlancament = MediaPlayer.create(context,R.raw.llancament);
        mpExplosio = MediaPlayer.create(context,R.raw.explosio);

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

    // Método para reproducir el sonido de lanzamiento
    private void reproducirLlancament() {
        soundPool.play(soundIdLlancament, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    // Método para reproducir el sonido de explosión
    private void reproducirExplosio() {
        soundPool.play(soundIdExplosio, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    // Método que nos da ancho y alto pantalla
    @Override
    protected void onSizeChanged(int ancho, int alto, int anchoAnter, int altoAnter) {
        super.onSizeChanged(ancho, alto, anchoAnter, altoAnter);

        float centroX = (ancho - ninja.getAmplada()) / 2f;
        float centroY = (alto - ninja.getAltura()) / 2f;
        // Establecer la posición del ninja en el centro
        ninja.setPosX(centroX);
        ninja.setPosY(centroY);

        // Una vez que conocemos nuestro ancho y alto, situamos los objetivos de
        // forma aleatoria
        for (Grafics objectiu : objectius) {
            objectiu.setPosX(Math.random() * (ancho - objectiu.getAmplada()));
            objectiu.setPosY(Math.random() * (alto - objectiu.getAltura()));
        }
        ultimProces= System.currentTimeMillis();
        thread.start();

    }

    // Método que dibuja la vista
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

    class ThreadJoc extends Thread {
        @Override
        public void run() {
            while (true) {
                actualitzaMoviment();
            }
        }
    }
    synchronized protected void actualitzaMoviment() {
        long instant_actual = System.currentTimeMillis();
        // No facis res si el període de procés no s'ha complert.
        if(ultimProces + PERIODE_PROCES > instant_actual) {
            return;
        }
        // Per una execució en temps real calculem retard
        double retard = (instant_actual - ultimProces) / PERIODE_PROCES;
        ultimProces = instant_actual; // Per a la propera vegada
        // Actualitzem velocitat i direcció del personatge Ninja a partir de
        // girNinja i acceleracioNinja (segons l'entrada del jugador)
        ninja.setAngle((int) (ninja.getAngle() + girNinja * retard));
        double nIncX = ninja.getIncX() + acceleracioNinja *
                Math.cos(Math.toRadians(ninja.getAngle())) * retard;
        double nIncY = ninja.getIncY() + acceleracioNinja *
                Math.sin(Math.toRadians(ninja.getAngle())) * retard;
        // Actualitzem si el módul de la velocitat no és més gran que el màxim
        if (Math.hypot(nIncX,nIncY) <= Grafics.MAX_VELOCITAT){
            ninja.setIncX(nIncX);
            ninja.setIncY(nIncY);
        }
        // Actualitzem posicions X i Y
        ninja.incrementaPos(retard);
        for(Grafics objectiu : objectius) {
            objectiu.incrementaPos(retard);
        }

        if (ganivetActiu) {
            ganivet.incrementaPos(retard);
            tempsGanivet -=retard;
            if (tempsGanivet < 0) {
                ganivetActiu = false;
            } else {
                for (int i = 0; i < objectius.size(); i++)
                    if (ganivet.verificaColision(objectius.elementAt(i))) {
                        destrueixObjectiu(i);
                        break;
                    }
            }
        }
        for (Grafics objectiu : objectius) {
            if (ninja.verificaColision(objectiu)) {
                vidas--;
                if (vidas <= 0) {
                    //Game OVER
                    endGame();
                }
            }
        }
    }
    private void endGame() {
        Context context = getContext();
        if(context instanceof Activity){
            ((Activity) context).finish();
        }
    }



    private void destrueixObjectiu(int i) {
        mpExplosio.start();
        int numParts = 3;
        if(objectius.get(i).getDrawable() == drawEnemie){
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
        ganivetActiu =false;
    }
    private void disparaGanivet() {
        mpLlancament.start();
        ganivet.setPosX(ninja.getPosX()+ ninja.getAmplada()/2-ganivet.getAmplada()/2);
        ganivet.setPosY(ninja.getPosY()+ ninja.getAltura()/2-ganivet.getAltura()/2);
        ganivet.setAngle(ninja.getAngle());
        ganivet.setIncX(Math.cos(Math.toRadians(ganivet.getAngle())) *
                INC_VELOCITAT_GANIVET);
        ganivet.setIncY(Math.sin(Math.toRadians(ganivet.getAngle())) *
                INC_VELOCITAT_GANIVET);
        tempsGanivet = (int) Math.min(this.getWidth() / Math.abs( ganivet.getIncX()),
                this.getHeight() / Math.abs(ganivet.getIncY())) - 2;
        ganivetActiu = true;
    }

    //Movimiento
    @Override
    public boolean onTouchEvent (MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                llancament =true;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dy<6 && dx>6){
                    girNinja = Math.round((x - mX) / 2);
                    llancament = false;
                } else if (dx<6 && dy>6){
                    acceleracioNinja = Math.round((mY - y) / 25);
                    llancament = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                girNinja = 0;
                acceleracioNinja = 0;
                if (llancament){
                    disparaGanivet();
                }
                break;
        }
        mX=x; mY=y;
        return true;
    }

    @Override
    public boolean onKeyDown(int codiTecla, KeyEvent event) {
        super.onKeyDown(codiTecla, event);
        // Suposem que processarem la pulsació
        boolean procesada = true;
        switch (codiTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                acceleracioNinja = +INC_ACCELERACIO;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                acceleracioNinja = -INC_ACCELERACIO;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                girNinja = -INC_GIR;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                girNinja = +INC_GIR;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                    disparaGanivet();
                break;
            default:
                // Si estem aquí, no hi ha pulsació que ens interessi
                procesada = false;
                break;
        }
        return procesada;
    }
    @Override
    public boolean onKeyUp(int codigoTecla, KeyEvent evento) {
        super.onKeyUp(codigoTecla, evento);
        // Suposem que processarem la pulsació
        boolean procesada = true;
        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                acceleracioNinja = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                acceleracioNinja = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                girNinja = 0;
                break;
            default:
                // Si estem aquí, no hi ha pulsació que ens interessi
                procesada = false;
                break;
        }
        return procesada;
    }
}
