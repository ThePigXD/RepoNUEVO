package com.example.demointent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_NAME = "com.example.demointent.EXTRA_USER_NAME";
    public static final String EXTRA_USER_ROLE = "com.example.demointent.EXTRA_USER_ROLE";
    public static final String EXTRA_USER_EXPERIENCE = "com.example.demointent.EXTRA_USER_EXPERIENCE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        TextView tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        TextView tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvProfileRole = findViewById(R.id.tvProfileRole);
        TextView tvExperienceBadge = findViewById(R.id.tvExperienceBadge);
        TextView tvYearsExp = findViewById(R.id.tvYearsExp);
        MaterialButton btnShareProfile = findViewById(R.id.btnShareProfile);
        MaterialButton btnBackProfile = findViewById(R.id.btnBackProfile);

        Intent intent = getIntent();
        String rawName = intent.getStringExtra(EXTRA_USER_NAME);
        String rawRole = intent.getStringExtra(EXTRA_USER_ROLE);
        int expYears = intent.getIntExtra(EXTRA_USER_EXPERIENCE, 0);

        String name = (rawName != null && !rawName.trim().isEmpty()) ? rawName : "Usuario Anónimo";
        String role = (rawRole != null && !rawRole.trim().isEmpty()) ? rawRole : "Especialista";

        tvProfileName.setText(name);
        tvProfileRole.setText(role);
        tvYearsExp.setText(String.format(Locale.getDefault(), "%d año(s)", expYears));

        // Generate initials
        String initials = extractInitials(name);
        tvAvatarInitials.setText(initials);

        // Experience Badge calculation
        String badgeText;
        if (expYears < 2) {
            badgeText = String.format(Locale.getDefault(), "Nivel Junior (%d año/s)", expYears);
        } else if (expYears <= 5) {
            badgeText = String.format(Locale.getDefault(), "Nivel Mid-Senior (%d años)", expYears);
        } else {
            badgeText = String.format(Locale.getDefault(), "Nivel Senior Leader (%d años)", expYears);
        }
        tvExperienceBadge.setText(badgeText);

        btnShareProfile.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Perfil de " + name);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Perfil Profesional:\nNombre: " + name + "\nPuesto: " + role + "\nExperiencia: " + expYears + " años.");
            try {
                startActivity(Intent.createChooser(shareIntent, "Compartir perfil con"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No hay aplicaciones para compartir", Toast.LENGTH_SHORT).show();
            }
        });

        btnBackProfile.setOnClickListener(v -> finish());
    }

    private String extractInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        if (parts.length > 0 && !parts[0].isEmpty()) {
            sb.append(parts[0].charAt(0));
        }
        if (parts.length > 1 && !parts[1].isEmpty()) {
            sb.append(parts[1].charAt(0));
        }
        return sb.toString().toUpperCase();
    }
}
