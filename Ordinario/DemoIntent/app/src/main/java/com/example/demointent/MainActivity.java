package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etRole;
    private EditText etExperience;
    private EditText etTextToAnalyze;
    private EditText etValueToConvert;
    private Spinner spinnerUnitType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Bind UI Components
        etName = findViewById(R.id.etName);
        etRole = findViewById(R.id.etRole);
        etExperience = findViewById(R.id.etExperience);

        etTextToAnalyze = findViewById(R.id.etTextToAnalyze);
        etValueToConvert = findViewById(R.id.etValueToConvert);
        spinnerUnitType = findViewById(R.id.spinnerUnitType);

        MaterialButton btnProfile = findViewById(R.id.btnProfile);
        MaterialButton btnTextAnalyzer = findViewById(R.id.btnTextAnalyzer);
        MaterialButton btnConverter = findViewById(R.id.btnConverter);

        // Setup Spinner
        String[] unitOptions = new String[]{
                getString(R.string.unit_temp),
                getString(R.string.unit_dist),
                getString(R.string.unit_weight)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                unitOptions
        );
        spinnerUnitType.setAdapter(adapter);

        // 1. Launch Profile Activity via Explicit Intent
        btnProfile.setOnClickListener(v -> launchProfileActivity());

        // 2. Launch Text Analyzer Activity via Explicit Intent
        btnTextAnalyzer.setOnClickListener(v -> launchTextAnalyzerActivity());

        // 3. Launch Unit Converter Activity via Explicit Intent
        btnConverter.setOnClickListener(v -> launchUnitConverterActivity());
    }

    private void launchProfileActivity() {
        String inputName = etName.getText().toString().trim();
        String inputRole = etRole.getText().toString().trim();
        String expStr = etExperience.getText().toString().trim();

        String finalName = inputName.isEmpty() ? "Usuario Anónimo" : inputName;
        String finalRole = inputRole.isEmpty() ? "Especialista" : inputRole;

        int expYears = 0;
        if (!expStr.isEmpty()) {
            try {
                expYears = Integer.parseInt(expStr);
            } catch (NumberFormatException ignored) {
            }
        }

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_USER_NAME, finalName);
        intent.putExtra(ProfileActivity.EXTRA_USER_ROLE, finalRole);
        intent.putExtra(ProfileActivity.EXTRA_USER_EXPERIENCE, expYears);
        startActivity(intent);
    }

    private void launchTextAnalyzerActivity() {
        String textToAnalyze = etTextToAnalyze.getText().toString().trim();
        if (textToAnalyze.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un texto a analizar", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, TextAnalyzerActivity.class);
        intent.putExtra(TextAnalyzerActivity.EXTRA_TEXT_INPUT, textToAnalyze);
        startActivity(intent);
    }

    private void launchUnitConverterActivity() {
        String valueStr = etValueToConvert.getText().toString().trim();
        if (valueStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un valor numérico", Toast.LENGTH_SHORT).show();
            return;
        }

        double val;
        try {
            val = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor numérico no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedUnitType = spinnerUnitType.getSelectedItemPosition();

        Intent intent = new Intent(MainActivity.this, UnitConverterActivity.class);
        intent.putExtra(UnitConverterActivity.EXTRA_VALUE, val);
        intent.putExtra(UnitConverterActivity.EXTRA_UNIT_TYPE, selectedUnitType);
        startActivity(intent);
    }
}
