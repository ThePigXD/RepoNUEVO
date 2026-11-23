package com.example.supabaseandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

/**
 * Pantalla Principal / Menú de Navegación del Módulo de Inventario.
 * @author Carlos M.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MaterialCardView mCardCreate;
    private MaterialCardView mCardRead;
    private MaterialCardView mCardUpdate;
    private MaterialCardView mCardDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        setupWindowInsets();
    }

    private void initViews() {
        mCardCreate = findViewById(R.id.card_btn_create);
        mCardRead = findViewById(R.id.card_btn_read);
        mCardUpdate = findViewById(R.id.card_btn_update);
        mCardDelete = findViewById(R.id.card_btn_delete);
    }

    private void setupListeners() {
        mCardCreate.setOnClickListener(v -> {
            Log.d(TAG, "Abriendo pantalla de creación...");
            startActivity(new Intent(MainActivity.this, CreateActivity.class));
        });

        mCardRead.setOnClickListener(v -> {
            Log.d(TAG, "Abriendo pantalla de consulta...");
            startActivity(new Intent(MainActivity.this, ReadActivity.class));
        });

        mCardUpdate.setOnClickListener(v -> {
            Log.d(TAG, "Abriendo pantalla de edicion...");
            startActivity(new Intent(MainActivity.this, UpdateActivity.class));
        });

        mCardDelete.setOnClickListener(v -> {
            Log.d(TAG, "Abriendo pantalla de eliminacion...");
            startActivity(new Intent(MainActivity.this, DeleteActivity.class));
        });
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
