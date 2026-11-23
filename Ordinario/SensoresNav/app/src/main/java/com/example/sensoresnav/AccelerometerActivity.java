package com.example.sensoresnav;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class AccelerometerActivity extends BaseActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView tvAccelX, tvAccelY, tvAccelZ;
    private TextView tvGForce, tvPitchAngle, tvRollAngle, tvPeakGForce, tvShakeStatus;
    private ProgressBar progressGForce, progressX, progressY, progressZ;
    private MaterialButton btnResetPeak;

    private float peakGForce = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_accelerometer);
        }

        tvAccelX = findViewById(R.id.tvAccelX);
        tvAccelY = findViewById(R.id.tvAccelY);
        tvAccelZ = findViewById(R.id.tvAccelZ);
        tvGForce = findViewById(R.id.tvGForce);
        tvPitchAngle = findViewById(R.id.tvPitchAngle);
        tvRollAngle = findViewById(R.id.tvRollAngle);
        tvPeakGForce = findViewById(R.id.tvPeakGForce);
        tvShakeStatus = findViewById(R.id.tvShakeStatus);

        progressGForce = findViewById(R.id.progressGForce);
        progressX = findViewById(R.id.progressX);
        progressY = findViewById(R.id.progressY);
        progressZ = findViewById(R.id.progressZ);
        btnResetPeak = findViewById(R.id.btnResetPeak);

        btnResetPeak.setOnClickListener(v -> {
            peakGForce = 1.0f;
            tvPeakGForce.setText(String.format(Locale.getDefault(), "%.2f G", peakGForce));
            Toast.makeText(this, "Pico de aceleración reiniciado", Toast.LENGTH_SHORT).show();
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_SHORT).show();
            tvShakeStatus.setText(R.string.sensor_not_available);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            tvAccelX.setText(String.format(Locale.getDefault(), "Eje X: %.2f m/s²", x));
            tvAccelY.setText(String.format(Locale.getDefault(), "Eje Y: %.2f m/s²", y));
            tvAccelZ.setText(String.format(Locale.getDefault(), "Eje Z: %.2f m/s²", z));

            progressX.setProgress(clampProgress(x));
            progressY.setProgress(clampProgress(y));
            progressZ.setProgress(clampProgress(z));

            // Magnitude & G-Force
            double magnitude = Math.sqrt(x * x + y * y + z * z);
            float gForce = (float) (magnitude / SensorManager.GRAVITY_EARTH);
            tvGForce.setText(String.format(Locale.getDefault(), "%.2f G", gForce));

            int gProgress = (int) (gForce * 100);
            if (gProgress > 300) gProgress = 300;
            progressGForce.setProgress(gProgress);

            // Peak G-Force tracker
            if (gForce > peakGForce) {
                peakGForce = gForce;
                tvPeakGForce.setText(String.format(Locale.getDefault(), "%.2f G", peakGForce));
            }

            // Inclinometer: Pitch & Roll calculation
            double pitchRad = Math.atan2(y, Math.sqrt(x * x + z * z));
            double rollRad = Math.atan2(-x, z);

            double pitchDeg = Math.toDegrees(pitchRad);
            double rollDeg = Math.toDegrees(rollRad);

            tvPitchAngle.setText(String.format(Locale.getDefault(), "%.1f°", pitchDeg));
            tvRollAngle.setText(String.format(Locale.getDefault(), "%.1f°", rollDeg));

            // Motion Status Classification
            if (gForce > 2.2f) {
                tvShakeStatus.setText(R.string.shake_high);
                tvShakeStatus.setTextColor(0xFFEF4444); // Red
                tvShakeStatus.setBackgroundColor(0xFFFEE2E2);
            } else if (gForce > 1.4f) {
                tvShakeStatus.setText(R.string.shake_detected);
                tvShakeStatus.setTextColor(0xFFF59E0B); // Amber
                tvShakeStatus.setBackgroundColor(0xFFFEF3C7);
            } else if (gForce > 1.15f) {
                tvShakeStatus.setText(R.string.shake_light);
                tvShakeStatus.setTextColor(0xFF3B82F6); // Blue
                tvShakeStatus.setBackgroundColor(0xFFEFF6FF);
            } else {
                tvShakeStatus.setText(R.string.shake_none);
                tvShakeStatus.setTextColor(0xFF10B981); // Emerald Green
                tvShakeStatus.setBackgroundColor(0xFFECFDF5);
            }
        }
    }

    private int clampProgress(float val) {
        int progress = (int) (val + 20);
        if (progress < 0) return 0;
        if (progress > 40) return 40;
        return progress;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}