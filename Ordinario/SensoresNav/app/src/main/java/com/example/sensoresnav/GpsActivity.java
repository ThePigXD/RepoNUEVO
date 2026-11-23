package com.example.sensoresnav;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class GpsActivity extends BaseActivity implements LocationListener {

    private static final int PERMISSION_REQUEST_LOCATION = 1003;

    private LocationManager locationManager;
    private Location lastLocation = null;

    private TextView tvLatitude, tvLongitude, tvDmsCoords, tvAltitude, tvSpeed, tvAccuracy, tvProvider, tvGpsStatus;
    private TextView tvGpsDistance, tvGpsTimer;
    private MaterialButton btnToggleGps, btnOpenMaps;

    private boolean isTracking = false;
    private float totalDistanceMeters = 0.0f;
    private long startTimeMillis = 0;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTracking) {
                long elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
                long hours = elapsedSeconds / 3600;
                long minutes = (elapsedSeconds % 3600) / 60;
                long seconds = elapsedSeconds % 60;
                tvGpsTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_gps);
        }

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvDmsCoords = findViewById(R.id.tvDmsCoords);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvProvider = findViewById(R.id.tvProvider);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);

        tvGpsDistance = findViewById(R.id.tvGpsDistance);
        tvGpsTimer = findViewById(R.id.tvGpsTimer);

        btnToggleGps = findViewById(R.id.btnToggleGps);
        btnOpenMaps = findViewById(R.id.btnOpenMaps);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnToggleGps.setOnClickListener(v -> {
            if (isTracking) {
                stopGpsTracking();
            } else {
                checkPermissionAndStartGps();
            }
        });

        btnOpenMaps.setOnClickListener(v -> openInExternalMaps());
    }

    private void checkPermissionAndStartGps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            startGpsTracking();
        }
    }

    private void startGpsTracking() {
        if (locationManager == null) return;

        try {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "El servicio de Ubicación / GPS está desactivado en el dispositivo", Toast.LENGTH_LONG).show();
                tvGpsStatus.setText("GPS desactivado en ajustes");
                return;
            }

            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
            }

            isTracking = true;
            totalDistanceMeters = 0.0f;
            lastLocation = null;
            startTimeMillis = System.currentTimeMillis();

            btnToggleGps.setText(R.string.btn_stop_gps);
            tvGpsStatus.setText(R.string.gps_searching);
            timerHandler.post(timerRunnable);

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown == null) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnown != null) {
                onLocationChanged(lastKnown);
            }

        } catch (SecurityException e) {
            Toast.makeText(this, "Error de permisos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopGpsTracking() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        isTracking = false;
        timerHandler.removeCallbacks(timerRunnable);
        btnToggleGps.setText(R.string.btn_start_gps);
        tvGpsStatus.setText(R.string.gps_stopped);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracking) {
            stopGpsTracking();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (lastLocation != null) {
            float dist = lastLocation.distanceTo(location);
            if (dist > 0.5f) { // Filter out minor GPS jitter
                totalDistanceMeters += dist;
            }
        }
        lastLocation = location;

        // Update Distance
        if (totalDistanceMeters >= 1000) {
            tvGpsDistance.setText(String.format(Locale.getDefault(), "%.2f km", totalDistanceMeters / 1000.0f));
        } else {
            tvGpsDistance.setText(String.format(Locale.getDefault(), "%.1f m", totalDistanceMeters));
        }

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        tvLatitude.setText(String.format(Locale.getDefault(), "Latitud: %.6f°", lat));
        tvLongitude.setText(String.format(Locale.getDefault(), "Longitud: %.6f°", lng));
        tvDmsCoords.setText(String.format(Locale.getDefault(), "DMS: %s, %s", convertToDMS(lat, true), convertToDMS(lng, false)));

        tvAltitude.setText(String.format(Locale.getDefault(), "Altitud: %.1f m", location.getAltitude()));

        float speedKmH = location.getSpeed() * 3.6f;
        float speedMph = speedKmH * 0.621371f;
        tvSpeed.setText(String.format(Locale.getDefault(), "Velocidad: %.1f km/h (%.1f mph)", speedKmH, speedMph));

        tvAccuracy.setText(String.format(Locale.getDefault(), "Precisión: ±%.1f m", location.getAccuracy()));
        tvProvider.setText(String.format(Locale.getDefault(), "Proveedor: %s", location.getProvider().toUpperCase(Locale.getDefault())));

        tvGpsStatus.setText("Posición GPS fijada y en seguimiento activo");
    }

    private String convertToDMS(double coordinate, boolean isLatitude) {
        String direction = isLatitude ? (coordinate >= 0 ? "N" : "S") : (coordinate >= 0 ? "E" : "O");
        coordinate = Math.abs(coordinate);
        int degrees = (int) coordinate;
        coordinate = (coordinate - degrees) * 60;
        int minutes = (int) coordinate;
        double seconds = (coordinate - minutes) * 60;
        return String.format(Locale.getDefault(), "%d°%02d'%04.1f\"%s", degrees, minutes, seconds, direction);
    }

    private void openInExternalMaps() {
        if (lastLocation == null) {
            Toast.makeText(this, "Obtén una ubicación GPS primero para ver en Mapas", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmIntentUri = Uri.parse("geo:" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + "?q=" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + "(Mi+Ubicación)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No se encontró aplicación de Mapas", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGpsTracking();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
}