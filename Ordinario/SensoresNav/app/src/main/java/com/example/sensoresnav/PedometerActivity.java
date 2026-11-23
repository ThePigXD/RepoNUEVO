package com.example.sensoresnav;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class PedometerActivity extends BaseActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001;
    private static final int DAILY_GOAL = 5000;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;

    private TextView tvStepsCount, tvGoalPercent, tvSessionSteps, tvDistance, tvCalories, tvPace, tvPedometerStatus;
    private ProgressBar progressStepsGoal;
    private MaterialButton btnResetSteps;

    private int initialSteps = -1;
    private int currentTotalSteps = 0;
    private int sessionSteps = 0;

    private long lastStepTimestamp = 0;
    private final Handler paceResetHandler = new Handler(Looper.getMainLooper());
    private final Runnable resetPaceRunnable = () -> {
        tvPace.setText(R.string.pace_idle);
        tvPace.setTextColor(getResources().getColor(R.color.pedometer_color, getTheme()));
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_pedometer);
        }

        tvStepsCount = findViewById(R.id.tvStepsCount);
        tvGoalPercent = findViewById(R.id.tvGoalPercent);
        tvSessionSteps = findViewById(R.id.tvSessionSteps);
        tvDistance = findViewById(R.id.tvDistance);
        tvCalories = findViewById(R.id.tvCalories);
        tvPace = findViewById(R.id.tvPace);
        tvPedometerStatus = findViewById(R.id.tvPedometerStatus);

        progressStepsGoal = findViewById(R.id.progressStepsGoal);
        btnResetSteps = findViewById(R.id.btnResetSteps);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        btnResetSteps.setOnClickListener(v -> {
            initialSteps = currentTotalSteps;
            sessionSteps = 0;
            updateStepViews();
            Toast.makeText(this, "Contador de sesión reiniciado", Toast.LENGTH_SHORT).show();
        });

        checkPermissionAndRegister();
    }

    private void checkPermissionAndRegister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            } else {
                initSensors();
            }
        } else {
            initSensors();
        }
    }

    private void initSensors() {
        if (stepCounterSensor == null && stepDetectorSensor == null) {
            tvPedometerStatus.setText(R.string.sensor_not_available);
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_LONG).show();
        } else {
            tvPedometerStatus.setText("Sensor de pasos activo y monitoreando");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (stepCounterSensor != null) {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
            }
            if (stepDetectorSensor != null) {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
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
        long now = System.currentTimeMillis();

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            currentTotalSteps = (int) event.values[0];
            if (initialSteps < 0) {
                initialSteps = currentTotalSteps;
            }
            sessionSteps = currentTotalSteps - initialSteps;
            updateStepViews();
            updatePace(now);
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (stepCounterSensor == null) {
                sessionSteps++;
                currentTotalSteps++;
                updateStepViews();
            }
            updatePace(now);
            tvPedometerStatus.setText(R.string.step_detected_event);
        }
    }

    private void updatePace(long now) {
        if (lastStepTimestamp > 0) {
            long timeDiff = now - lastStepTimestamp;
            if (timeDiff < 550) {
                tvPace.setText(R.string.pace_running);
                tvPace.setTextColor(0xFFEF4444); // Red
            } else {
                tvPace.setText(R.string.pace_walking);
                tvPace.setTextColor(0xFF10B981); // Green
            }
        }
        lastStepTimestamp = now;
        paceResetHandler.removeCallbacks(resetPaceRunnable);
        paceResetHandler.postDelayed(resetPaceRunnable, 3000);
    }

    private void updateStepViews() {
        tvStepsCount.setText(String.valueOf(sessionSteps));
        tvSessionSteps.setText(String.format(Locale.getDefault(), "Pasos en sesión: %d (Total sensor: %d)", sessionSteps, currentTotalSteps));

        // Goal percentage
        int percent = (int) ((sessionSteps / (float) DAILY_GOAL) * 100);
        if (percent > 100) percent = 100;
        tvGoalPercent.setText(String.format(Locale.getDefault(), "%d%%", percent));
        progressStepsGoal.setProgress(Math.min(sessionSteps, DAILY_GOAL));

        // Distance: 0.75m per step
        double distanceMeters = sessionSteps * 0.75;
        if (distanceMeters >= 1000) {
            tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceMeters / 1000.0));
        } else {
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f m", distanceMeters));
        }

        // Calories: 0.04 kcal per step
        double calories = sessionSteps * 0.04;
        tvCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", calories));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSensors();
            } else {
                tvPedometerStatus.setText(R.string.permission_required);
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
}