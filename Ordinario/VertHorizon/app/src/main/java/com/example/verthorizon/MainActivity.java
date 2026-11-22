package com.example.verthorizon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "VertHorizon_Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int defaultPadding = (int) (24 * getResources().getDisplayMetrics().density);
            v.setPadding(
                    systemBars.left + defaultPadding,
                    systemBars.top + defaultPadding,
                    systemBars.right + defaultPadding,
                    systemBars.bottom + defaultPadding
            );
            return insets;
        });

        logAndToast(getString(R.string.msg_created, "MainActivity"));

        Button boton = findViewById(R.id.button);
        boton.setOnClickListener(v -> {
            Intent textoActividad = new Intent(MainActivity.this, TextActivity.class);
            startActivity(textoActividad);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        logAndToast(getString(R.string.msg_started, "MainActivity"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        logAndToast(getString(R.string.msg_resumed, "MainActivity"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        logAndToast(getString(R.string.msg_paused, "MainActivity"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        logAndToast(getString(R.string.msg_stopped, "MainActivity"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logAndToast(getString(R.string.msg_destroyed, "MainActivity"));
    }

    private void logAndToast(String mensaje) {
        Log.d(TAG, mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}