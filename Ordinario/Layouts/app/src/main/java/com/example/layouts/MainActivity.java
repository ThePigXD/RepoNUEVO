package com.example.layouts;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton btnNavLinear = findViewById(R.id.btnNavLinear);
        MaterialButton btnNavRelative = findViewById(R.id.btnNavRelative);
        MaterialButton btnNavFrame = findViewById(R.id.btnNavFrame);
        MaterialButton btnNavTable = findViewById(R.id.btnNavTable);

        btnNavLinear.setOnClickListener(v -> {
            Intent intentLinear = new Intent(MainActivity.this, LinearActivity.class);
            startActivity(intentLinear);
        });

        btnNavRelative.setOnClickListener(v -> {
            Intent intentRelative = new Intent(MainActivity.this, RelativeActivity.class);
            startActivity(intentRelative);
        });

        btnNavFrame.setOnClickListener(v -> {
            Intent intentFrame = new Intent(MainActivity.this, FrameActivity.class);
            startActivity(intentFrame);
        });

        btnNavTable.setOnClickListener(v -> {
            Intent intentTable = new Intent(MainActivity.this, TableActivity.class);
            startActivity(intentTable);
        });
    }
}