package com.example.ejemploandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView tvResultado;
    Button btnActualizar;
    EditText etEntrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tvResultado = findViewById(R.id.tvResultado);
        btnActualizar = findViewById(R.id.btnActualizar);
        etEntrada = findViewById(R.id.etEntrada);

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String entrada = etEntrada.getText().toString();
                tvResultado.setText(entrada);
            }
        });
    }
}
