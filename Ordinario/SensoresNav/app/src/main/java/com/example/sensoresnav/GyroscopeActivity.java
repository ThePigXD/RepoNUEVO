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

public class GyroscopeActivity extends BaseActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscope;

    private TextView tvGyroX, tvGyroY, tvGyroZ;
    private TextView tvAngleX, tvAngleY, tvAngleZ, tvGyroState;
    private ProgressBar progressGyroX, progressGyroY, progressGyroZ;
    private MaterialButton btnCalibrateGyro;

    private long lastTimestamp = 0;
    private float angleX = 0f;
    private float angleY = 0f;
    private float angleZ = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_gyroscope);
        }

        tvGyroX = findViewById(R.id.tvGyroX);
        tvGyroY = findViewById(R.id.tvGyroY);
        tvGyroZ = findViewById(R.id.tvGyroZ);

        tvAngleX = findViewById(R.id.tvAngleX);
        tvAngleY = findViewById(R.id.tvAngleY);
        tvAngleZ = findViewById(R.id.tvAngleZ);
        tvGyroState = findViewById(R.id.tvGyroState);

        progressGyroX = findViewById(R.id.progressGyroX);
        progressGyroY = findViewById(R.id.progressGyroY);
        progressGyroZ = findViewById(R.id.progressGyroZ);
        btnCalibrateGyro = findViewById(R.id.btnCalibrateGyro);

        btnCalibrateGyro.setOnClickListener(v -> {
            angleX = 0f;
            angleY = 0f;
            angleZ = 0f;
            updateAngleViews();
            Toast.makeText(this, "Ángulos calibrados a 0.0°", Toast.LENGTH_SHORT).show();
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        if (gyroscope == null) {
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
        }
        lastTimestamp = 0;
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
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float xRad = event.values[0]; // Pitch
            float yRad = event.values[1]; // Roll
            float zRad = event.values[2]; // Yaw

            float xDeg = (float) Math.toDegrees(xRad);
            float yDeg = (float) Math.toDegrees(yRad);
            float zDeg = (float) Math.toDegrees(zRad);

            tvGyroX.setText(String.format(Locale.getDefault(), "Pitch (Eje X): %.2f rad/s (%.1f°/s)", xRad, xDeg));
            tvGyroY.setText(String.format(Locale.getDefault(), "Roll (Eje Y): %.2f rad/s (%.1f°/s)", yRad, yDeg));
            tvGyroZ.setText(String.format(Locale.getDefault(), "Yaw (Eje Z): %.2f rad/s (%.1f°/s)", zRad, zDeg));

            progressGyroX.setProgress(clampProgress(xRad));
            progressGyroY.setProgress(clampProgress(yRad));
            progressGyroZ.setProgress(clampProgress(zRad));

            // Integrate rotation angles over elapsed time dt
            if (lastTimestamp != 0) {
                float dt = (event.timestamp - lastTimestamp) * 1.0f / 1000000000.0f; // ns to s
                if (dt < 1.0f) { // Ignore giant jumps
                    angleX += xDeg * dt;
                    angleY += yDeg * dt;
                    angleZ += zDeg * dt;
                    updateAngleViews();
                }
            }
            lastTimestamp = event.timestamp;

            // Rotation status classification based on total angular speed
            double totalSpeed = Math.sqrt(xRad * xRad + yRad * yRad + zRad * zRad);
            if (totalSpeed > 3.0) {
                tvGyroState.setText(R.string.gyro_status_fast);
                tvGyroState.setTextColor(0xFFF59E0B);
                tvGyroState.setBackgroundColor(0xFFFEF3C7);
            } else if (totalSpeed > 0.3) {
                tvGyroState.setText(R.string.gyro_status_moving);
                tvGyroState.setTextColor(0xFF3B82F6);
                tvGyroState.setBackgroundColor(0xFFEFF6FF);
            } else {
                tvGyroState.setText(R.string.gyro_status_idle);
                tvGyroState.setTextColor(0xFF10B981);
                tvGyroState.setBackgroundColor(0xFFECFDF5);
            }
        }
    }

    private void updateAngleViews() {
        tvAngleX.setText(String.format(Locale.getDefault(), "%.1f°", angleX));
        tvAngleY.setText(String.format(Locale.getDefault(), "%.1f°", angleY));
        tvAngleZ.setText(String.format(Locale.getDefault(), "%.1f°", angleZ));
    }

    private int clampProgress(float radVal) {
        int progress = (int) (radVal * 2 + 10);
        if (progress < 0) return 0;
        if (progress > 20) return 20;
        return progress;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}