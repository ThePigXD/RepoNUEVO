package com.example.sensoresnav;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MagnetometerActivity extends BaseActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;

    private TextView tvMagX, tvMagY, tvMagZ, tvMagTotal, tvCompassHeading, tvMagStatus;
    private ProgressBar progressMagField;
    private ImageView imgCompass;

    private final float[] lastAccelerometer = new float[3];
    private final float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    private float smoothedAzimuthDegrees = 0f;
    private static final float ALPHA = 0.15f; // Low-pass filter smoothing coefficient

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetometer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_magnetometer);
        }

        tvMagX = findViewById(R.id.tvMagX);
        tvMagY = findViewById(R.id.tvMagY);
        tvMagZ = findViewById(R.id.tvMagZ);
        tvMagTotal = findViewById(R.id.tvMagTotal);
        tvCompassHeading = findViewById(R.id.tvCompassHeading);
        tvMagStatus = findViewById(R.id.tvMagStatus);

        progressMagField = findViewById(R.id.progressMagField);
        imgCompass = findViewById(R.id.imgCompass);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (magnetometer == null) {
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (magnetometer != null) {
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
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
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double totalField = Math.sqrt(x * x + y * y + z * z);

            tvMagX.setText(String.format(Locale.getDefault(), "Campo X: %.2f µT", x));
            tvMagY.setText(String.format(Locale.getDefault(), "Campo Y: %.2f µT", y));
            tvMagZ.setText(String.format(Locale.getDefault(), "Campo Z: %.2f µT", z));
            tvMagTotal.setText(String.format(Locale.getDefault(), "Intensidad Total: %.2f µT", totalField));

            int magProgress = (int) totalField;
            if (magProgress > 200) magProgress = 200;
            progressMagField.setProgress(magProgress);

            // Magnetic Anomaly Alert (Earth average is ~30 to 60 uT)
            if (totalField > 90.0) {
                tvMagStatus.setText(R.string.mag_anomaly);
                tvMagStatus.setTextColor(0xFFEF4444); // Red alert
                tvMagStatus.setBackgroundColor(0xFFFEE2E2);
            } else {
                tvMagStatus.setText(R.string.mag_normal);
                tvMagStatus.setTextColor(0xFF10B981); // Emerald green
                tvMagStatus.setBackgroundColor(0xFFECFDF5);
            }
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

                if (azimuthInDegrees < 0) {
                    azimuthInDegrees += 360f;
                }

                // Low-pass filter for smooth needle movement
                smoothedAzimuthDegrees = smoothAngle(smoothedAzimuthDegrees, azimuthInDegrees, ALPHA);
                imgCompass.setRotation(-smoothedAzimuthDegrees);

                String directionName = get16PointDirectionName(smoothedAzimuthDegrees);
                tvCompassHeading.setText(String.format(Locale.getDefault(), "%.0f° %s", smoothedAzimuthDegrees, directionName));
            }
        }
    }

    private float smoothAngle(float current, float target, float alpha) {
        float diff = target - current;
        while (diff < -180f) diff += 360f;
        while (diff >= 180f) diff -= 360f;
        return current + alpha * diff;
    }

    private String get16PointDirectionName(float degrees) {
        if (degrees >= 348.75 || degrees < 11.25) return "Norte (N)";
        if (degrees >= 11.25 && degrees < 33.75) return "Norte-Noreste (NNE)";
        if (degrees >= 33.75 && degrees < 56.25) return "Noreste (NE)";
        if (degrees >= 56.25 && degrees < 78.75) return "Este-Noreste (ENE)";
        if (degrees >= 78.75 && degrees < 101.25) return "Este (E)";
        if (degrees >= 101.25 && degrees < 123.75) return "Este-Sureste (ESE)";
        if (degrees >= 123.75 && degrees < 146.25) return "Sureste (SE)";
        if (degrees >= 146.25 && degrees < 168.75) return "Sur-Sureste (SSE)";
        if (degrees >= 168.75 && degrees < 191.25) return "Sur (S)";
        if (degrees >= 191.25 && degrees < 213.75) return "Sur-Suroeste (SSO)";
        if (degrees >= 213.75 && degrees < 236.25) return "Suroeste (SO)";
        if (degrees >= 236.25 && degrees < 258.75) return "Oeste-Suroeste (OSO)";
        if (degrees >= 258.75 && degrees < 281.25) return "Oeste (O)";
        if (degrees >= 281.25 && degrees < 303.75) return "Oeste-Noroeste (ONO)";
        if (degrees >= 303.75 && degrees < 326.25) return "Noroeste (NO)";
        if (degrees >= 326.25 && degrees < 348.75) return "Norte-Noroeste (NNO)";
        return "";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}