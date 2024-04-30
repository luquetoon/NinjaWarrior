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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView ninja;
    private Button bExit, bPlay, bScore;
    private Boolean music;
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
        initializeAttributes();
        startMusic();
        setListeners();
        //Quitar al entregar
        setRandomValuesSharedPref();
    }


    //Test orden de Scores
    private void setRandomValuesSharedPref() {
        Random random = new Random();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("Marta", random.nextInt(100));
        editor.putInt("Samu", random.nextInt(100));
        editor.putInt("Adri", random.nextInt(100));
        editor.putInt("Ariel", random.nextInt(100));
        editor.putInt("Sasa", random.nextInt(100));
        editor.apply(); // Aplica los cambios

    }

    private void setListeners() {
        listenerExit();
        listenerScore();
        listenerPlay();
    }

    // Iniciar la nueva actividad
                /*Intent intent = new Intent(getApplicationContext(), JocActivity.class);
                intent.putExtra(KEY_PLAYER_NAME, playerName);
                startActivity(intent);*/
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
                    int bestScore;

                    if (sharedPreferences.contains(playerName)) {
                        // Si el jugador ya existe, obtener su puntaje
                        bestScore = sharedPreferences.getInt(playerName, 0);
                    } else {
                        // Si el jugador no existe, establecer su puntaje como 0
                        bestScore = 0;
                        // Además, guardar el nuevo jugador en las preferencias
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(playerName, bestScore);
                        editor.apply();
                    }

                    // Mostrar el puntaje actual del jugador
                    AlertDialog.Builder scoreAlert = new AlertDialog.Builder(MainActivity.this);
                    scoreAlert.setTitle("Puntuación de " + playerName);
                    scoreAlert.setMessage("Tu puntuación actual es: " + bestScore);
                    scoreAlert.setPositiveButton("Ok", null);
                    scoreAlert.show();
                }
            });

            alert.setNegativeButton("Cancelar", null);
            alert.show();
        });
    }






    private void listenerScore() {
        bScore.setOnClickListener((view) -> {
            // Obtener todos los nombres de los jugadores y sus puntuaciones
            Map<String, ?> allEntries = sharedPreferences.getAll();
            // Crear una lista para almacenar las puntuaciones de los jugadores
            List<PlayerScore> playerScores = new ArrayList<>();

            // Iterar sobre todas las entradas y agregarlas a la lista
            int maxNameLength = iteratorScoresName(allEntries, playerScores);

            // Ordenar la lista de puntuaciones de los jugadores de mayor a menor
            Collections.sort(playerScores, Collections.reverseOrder());
            // Mostrar solo las cinco puntuaciones más altas
            StringBuilder scores = new StringBuilder();


            // Iterar sobre las puntuaciones de los jugadores
            iteratorScorePlayers(playerScores, maxNameLength, scores);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getString(R.string.score));
            alert.setMessage(scores.length() > 0 ? scores.toString() : getString(R.string.score));
            alert.setPositiveButton("Ok", null);
            AlertDialog dialog = alert.create();

            dialog.show();

        });
    }

    private static int iteratorScoresName(Map<String, ?> allEntries, List<PlayerScore> playerScores) {
        int maxNameLength = 0;

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {

            if (!entry.getKey().equals(KEY_PLAYER_NAME) && !entry.getKey().equals(KEY_BEST_SCORE)) {
                String playerName = entry.getKey();
                int bestScore = Integer.parseInt(entry.getValue().toString());
                playerScores.add(new PlayerScore(playerName, bestScore));
                // longitud máxima del nombre de jugador
                maxNameLength = Math.max(maxNameLength, playerName.length());
            }
        }
        return maxNameLength;
    }

    private static void iteratorScorePlayers(List<PlayerScore> playerScores, int maxNameLength, StringBuilder scores) {
        int count = 0;
        for (PlayerScore playerScore : playerScores) {
            String playerName = String.format("%-" + maxNameLength + "s", playerScore.getPlayerName());
            String score = String.format("%3d", playerScore.getScore());
            scores.append(playerName).append("............................").append(score).append("\n");

            count++;
            if (count == 5) {
                break;
            }
        }
    }


    //Controlar SahredPref
    private void savePlayerScore(String playerName, int score) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(playerName, score);
        editor.apply();
    }

    private int getPlayerScore(String playerName) {
        return sharedPreferences.getInt(playerName, 0);
    }

    private void listenerExit() {
        bExit.setOnClickListener((view) -> {
            System.exit(0);
        });
    }

    private void initializeAttributes() {
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

    private void checkPreferences() {
        music = pref.getBoolean("music", true);
        if (music) {
            startMusic();
        } else {
            stopMusic();
        }
    }

    private void startMusic() {
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
        builder.setMessage("Desarrollado por Sergi\n\n" + "Este es un juego de Ninja Warrior," +
                " donde deberás evitar a los enemigos y obtener la mayor puntuación posible.");
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
