package com.example.ninjawarrior;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView ninja;
    private Button bExit, bPlay, bScore;
    private Boolean music;
    int enemies;
    private MediaPlayer mp;
    private Toolbar myToolbar;
    private SharedPreferences sharedPreferences, pref;
    private final int DEFAULT = 5;
    private static final String PREFERENCES_NAME = "PrefScores";
    private static final String KEY_PLAYER_NAME = "playerName";
    private static final String KEY_BEST_SCORE = "bestScore";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setToolbar();
        animationImage();
        inicializeAtt();
        startMusic();
        listenerExit();
        listenerScore();
        listenerPlay();
    }

    private void listenerPlay() {
        bPlay.setOnClickListener((view) -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Nombre jugador");
            alert.setMessage("Por favor introduzca su nombre");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String playerName = input.getText().toString();
                    int bestScore = 0;
                    String existingPlayer = sharedPreferences.getString(KEY_PLAYER_NAME, null);

                    // Verificar si el nombre de usuario existe y actualizar puntuacion si es necesario
                    if (existingPlayer != null && !existingPlayer.equals(playerName)) {
                        bestScore = sharedPreferences.getInt(existingPlayer + "_score", 0);
                    } else {
                        bestScore = sharedPreferences.getInt(playerName + "_score", 0);
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_PLAYER_NAME, playerName);
                    editor.putInt(playerName + "_score", bestScore);
                    editor.apply();

                    // Iniciar la nueva actividad
                    Intent intent = new Intent(getApplicationContext(), JocActivity.class);
                    intent.putExtra(KEY_PLAYER_NAME, playerName);
                    startActivity(intent);
                }
            });

            alert.setNegativeButton("Cancelar", null);
            alert.show();
        });
    }





    private boolean isPlayerNameAvailable(String playerName) {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().equals(KEY_PLAYER_NAME)) {
                String existingPlayer = entry.getValue().toString();
                if (existingPlayer.equals(playerName)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Error");
        alert.setMessage(message);
        alert.setPositiveButton("Ok", null);
        alert.show();
    }



    private void listenerScore() {
        bScore.setOnClickListener((view) -> {
            // Obtener todos los nombres de los jugadores y sus puntuaciones
            Map<String, ?> allEntries = sharedPreferences.getAll();
            StringBuilder scores = new StringBuilder();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().contains("_score")) {
                    String playerName = entry.getKey().replace("_score", "");
                    int bestScore = sharedPreferences.getInt(entry.getKey(), 0);
                    scores.append(playerName).append(" - Best Score: ").append(bestScore).append("\n");
                }
            }
            if (scores.length() == 0) {
                scores.append(getString(R.string.score));
            }

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.score));
            alert.setMessage(scores.toString());
            alert.setPositiveButton("Ok", null);
            alert.show();
        });
    }

    private void listenerExit() {
        bExit.setOnClickListener((view) -> {
            System.exit(0);
        });
    }

    private void inicializeAtt() {
        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        bExit = findViewById(R.id.bExit);
        bPlay = findViewById(R.id.bPlay);
        bScore = findViewById(R.id.bScore);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPreferences();

    }

    private void checkPreferences(){
        music = pref.getBoolean("music", true);

        int enemies = pref.getInt("setEnemies", DEFAULT);

        if (music){
            startMusic();
        }else{
            stopMusic();
        }

    }

    private void startMusic(){
        stopMusic();
        mp = MediaPlayer.create(this, R.raw.intromusic);
        mp.start();
    }

    private void stopMusic() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    private void setToolbar() {
        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_info) {
            // Mostrar la información del desarrollador y del juego
            showInfoDialog();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Información");
        builder.setMessage("Desarrollado por Sergi\n\n" +
                "Este es un juego de Ninja Warrior, donde deberás evitar a los enemigos y obtener la mayor puntuación posible.");
        builder.setPositiveButton("Aceptar", null);
        builder.show();
    }


    private void animationImage() {
        ninja = findViewById(R.id.ivNinja);
        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(ninja, "alpha", 1f, 0f);
        fadeAnim.setDuration(5000);
        fadeAnim.setRepeatCount(ValueAnimator.INFINITE);
        fadeAnim.setRepeatMode(ValueAnimator.REVERSE);
        fadeAnim.start();
    }
}