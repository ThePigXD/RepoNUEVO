package com.example.listas;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_videojuegos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Videojuego> listaVideojuegos = obtenerListaVideojuegos();

        VideojuegoAdapter adapter = new VideojuegoAdapter(listaVideojuegos);
        adapter.setOnItemClickListener(videojuego -> 
            Toast.makeText(MainActivity.this, 
                "🎮 " + videojuego.getTitulo() + "\n" +
                "Plataforma: " + videojuego.getPlataforma() + "\n" +
                "Calificación: " + videojuego.getCalificacion() + " ★", 
                Toast.LENGTH_SHORT).show()
        );

        recyclerView.setAdapter(adapter);
    }

    private ArrayList<Videojuego> obtenerListaVideojuegos() {
        ArrayList<Videojuego> lista = new ArrayList<>();
        lista.add(new Videojuego("The Legend of Zelda: Tears of the Kingdom", "Aventura / Acción", "Nintendo Switch", 9.8, 2023));
        lista.add(new Videojuego("Red Dead Redemption 2", "Acción / Mundo Abierto", "Multiplataforma", 9.7, 2018));
        lista.add(new Videojuego("Elden Ring", "ARPG / Soulslike", "Multiplataforma", 9.6, 2022));
        lista.add(new Videojuego("God of War Ragnarök", "Acción / Aventura", "PlayStation 5 / PS4", 9.5, 2022));
        lista.add(new Videojuego("Super Mario Odyssey", "Plataformas 3D", "Nintendo Switch", 9.7, 2017));
        lista.add(new Videojuego("The Witcher 3: Wild Hunt", "RPG de Acción", "Multiplataforma", 9.6, 2015));
        lista.add(new Videojuego("Minecraft", "Sandbox / Supervivencia", "Multiplataforma", 9.4, 2011));
        lista.add(new Videojuego("Cyberpunk 2077: Phantom Liberty", "RPG de Ciencia Ficción", "Multiplataforma", 9.0, 2023));
        lista.add(new Videojuego("Hollow Knight", "Metroidvania", "Multiplataforma", 9.5, 2017));
        lista.add(new Videojuego("Halo: Combat Evolved", "FPS", "Xbox / PC", 9.3, 2001));
        lista.add(new Videojuego("Grand Theft Auto V", "Acción / Mundo Abierto", "Multiplataforma", 9.5, 2013));
        lista.add(new Videojuego("Resident Evil 4 Remake", "Survival Horror", "Multiplataforma", 9.3, 2023));
        lista.add(new Videojuego("Persona 5 Royal", "JRPG", "Multiplataforma", 9.6, 2019));
        lista.add(new Videojuego("Final Fantasy VII Rebirth", "ARPG", "PlayStation 5", 9.4, 2024));
        lista.add(new Videojuego("Chrono Trigger", "JRPG Clásico", "SNES / PC / Mobile", 9.9, 1995));
        lista.add(new Videojuego("Portal 2", "Puzzles / FPS", "Multiplataforma", 9.8, 2011));
        return lista;
    }
}
