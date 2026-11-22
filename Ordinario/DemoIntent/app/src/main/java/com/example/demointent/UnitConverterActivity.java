package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class UnitConverterActivity extends AppCompatActivity {

    public static final String EXTRA_VALUE = "com.example.demointent.EXTRA_VALUE";
    public static final String EXTRA_UNIT_TYPE = "com.example.demointent.EXTRA_UNIT_TYPE";

    public static final int TYPE_TEMPERATURE = 0;
    public static final int TYPE_DISTANCE = 1;
    public static final int TYPE_WEIGHT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unit_converter);

        TextView tvBaseValueDisplay = findViewById(R.id.tvBaseValueDisplay);
        TextView tvResult1Label = findViewById(R.id.tvResult1Label);
        TextView tvResult1Value = findViewById(R.id.tvResult1Value);
        TextView tvResult2Label = findViewById(R.id.tvResult2Label);
        TextView tvResult2Value = findViewById(R.id.tvResult2Value);
        TextView tvResult3Label = findViewById(R.id.tvResult3Label);
        TextView tvResult3Value = findViewById(R.id.tvResult3Value);
        MaterialButton btnBackConverter = findViewById(R.id.btnBackConverter);

        Intent intent = getIntent();
        double baseValue = intent.getDoubleExtra(EXTRA_VALUE, 0.0);
        int unitType = intent.getIntExtra(EXTRA_UNIT_TYPE, TYPE_TEMPERATURE);

        switch (unitType) {
            case TYPE_TEMPERATURE:
                tvBaseValueDisplay.setText(String.format(Locale.getDefault(), "%.2f °C", baseValue));

                double fahrenheit = (baseValue * 9.0 / 5.0) + 32.0;
                double kelvin = baseValue + 273.15;
                double rankine = (baseValue + 273.15) * 9.0 / 5.0;

                tvResult1Label.setText("Grados Fahrenheit");
                tvResult1Value.setText(String.format(Locale.getDefault(), "%.2f °F", fahrenheit));

                tvResult2Label.setText("Grados Kelvin");
                tvResult2Value.setText(String.format(Locale.getDefault(), "%.2f K", kelvin));

                tvResult3Label.setText("Grados Rankine");
                tvResult3Value.setText(String.format(Locale.getDefault(), "%.2f °R", rankine));
                break;

            case TYPE_DISTANCE:
                tvBaseValueDisplay.setText(String.format(Locale.getDefault(), "%.2f Km", baseValue));

                double miles = baseValue * 0.621371;
                double meters = baseValue * 1000.0;
                double feet = baseValue * 3280.84;

                tvResult1Label.setText("Millas (mi)");
                tvResult1Value.setText(String.format(Locale.getDefault(), "%.3f mi", miles));

                tvResult2Label.setText("Metros (m)");
                tvResult2Value.setText(String.format(Locale.getDefault(), "%.1f m", meters));

                tvResult3Label.setText("Pies (ft)");
                tvResult3Value.setText(String.format(Locale.getDefault(), "%.2f ft", feet));
                break;

            case TYPE_WEIGHT:
            default:
                tvBaseValueDisplay.setText(String.format(Locale.getDefault(), "%.2f Kg", baseValue));

                double pounds = baseValue * 2.20462;
                double grams = baseValue * 1000.0;
                double ounces = baseValue * 35.274;

                tvResult1Label.setText("Libras (lbs)");
                tvResult1Value.setText(String.format(Locale.getDefault(), "%.2f lbs", pounds));

                tvResult2Label.setText("Gramos (g)");
                tvResult2Value.setText(String.format(Locale.getDefault(), "%.1f g", grams));

                tvResult3Label.setText("Onzas (oz)");
                tvResult3Value.setText(String.format(Locale.getDefault(), "%.2f oz", ounces));
                break;
        }

        btnBackConverter.setOnClickListener(v -> finish());
    }
}
