package com.example.fragmentado;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnInicio = findViewById(R.id.btn_inicio);
        Button btnNovedades = findViewById(R.id.btn_novedades);
        Button btnPerfil = findViewById(R.id.btn_perfil);

        btnInicio.setOnClickListener(v -> cargarFragmento(new InicioFragment()));
        btnNovedades.setOnClickListener(v -> cargarFragmento(new NovedadesFragment()));
        btnPerfil.setOnClickListener(v -> cargarFragmento(new PerfilFragment()));

        if (savedInstanceState == null) {
            cargarFragmento(new InicioFragment());
        }
    }

    private void cargarFragmento(Fragment fragmento) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragmento)
                .commit();
    }
}
