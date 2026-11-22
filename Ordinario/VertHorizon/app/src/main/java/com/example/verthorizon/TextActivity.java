package com.example.verthorizon;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TextActivity extends AppCompatActivity {

    private static final String TAG = "VertHorizon_Text";
    private static final String KEY_TEXTO_GUARDADO = "KEY_TEXTO_GUARDADO";

    private EditText cuadro;
    private TextView input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_texto);

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

        logAndToast(getString(R.string.msg_created, "TextActivity"));

        cuadro = findViewById(R.id.editTextText);
        input = findViewById(R.id.textView);
        Button boton = findViewById(R.id.button3);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_TEXTO_GUARDADO)) {
            String textoGuardado = savedInstanceState.getString(KEY_TEXTO_GUARDADO);
            if (textoGuardado != null && !textoGuardado.isEmpty()) {
                input.setText(textoGuardado);
            }
        }

        boton.setOnClickListener(v -> {
            String mensaje = cuadro.getText() != null ? cuadro.getText().toString().trim() : "";
            if (mensaje.isEmpty()) {
                cuadro.setError(getString(R.string.texto_error_empty));
                Toast.makeText(TextActivity.this, getString(R.string.texto_error_empty), Toast.LENGTH_SHORT).show();
            } else {
                cuadro.setError(null);
                input.setText(mensaje);
                cuadro.setText("");
                Toast.makeText(TextActivity.this, getString(R.string.texto_success), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        logAndToast(getString(R.string.msg_started, "TextActivity"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        logAndToast(getString(R.string.msg_resumed, "TextActivity"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        logAndToast(getString(R.string.msg_paused, "TextActivity"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        logAndToast(getString(R.string.msg_stopped, "TextActivity"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logAndToast(getString(R.string.msg_destroyed, "TextActivity"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TEXTO_GUARDADO, input.getText().toString());
    }

    private void logAndToast(String mensaje) {
        Log.d(TAG, mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}