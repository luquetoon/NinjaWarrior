package com.example.ninjawarrior;

import static com.example.ninjawarrior.MainActivity.PREFERENCES_NAME;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class JocActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private int MaxScore;
    private Bundle bundel;
    private int bestScore;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joc);

        VistaJoc vistaJoc = findViewById(R.id.VistaJoc);
        vistaJoc.setPare(this);

        pref = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }


    public void setScoreEndGame(int score){
        bundel = this.getIntent().getExtras();
        name = bundel.getString("playerName", "");
        bestScore = pref.getInt(name,0);

        if (score > bestScore) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("bestScore", score); // Actualizar la puntuación máxima
            editor.apply();
            bestScore = score; // Actualizar la variable bestScore
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("¡Fin del juego!");
        alertDialogBuilder.setMessage("¡Enhorabuena " + name + "!\n" +
                "Tu puntuación actual es: " + score + "\n" +
                "Tu mejor puntuación es: " + bestScore);
        alertDialogBuilder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        alertDialogBuilder.setPositiveButton("Reintentar", ((dialog, which) -> {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }));

        alertDialogBuilder.show();
    }
}