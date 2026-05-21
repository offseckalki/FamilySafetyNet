package com.drash.familysafetynet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PreciseLocationTracker";
    private static final int FOREGROUND_PERMISSION_REQUEST_CODE = 1001;
    private static final int BACKGROUND_PERMISSION_REQUEST_CODE = 1002;

    private WebView webView;
    private TextView locationStatusText;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DatabaseReference databaseRef;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        locationStatusText = findViewById(R.id.locationStatusText);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        databaseRef = FirebaseDatabase.getInstance().getReference("live_locations").child(deviceId);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .setMinUpdateDistanceMeters(1.0f)
                .setMaxUpdateDelayMillis(3000)
                .setWaitForAccurateLocation(true)
                .build();

        setupLocationCallback();
        checkAndRequestPermissions();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    handleNewLocation(location);
                }
            }
        };
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, "=== HIGH-PRECISION LOCATION ===");
        Log.d(TAG, "Latitude: " + location.getLatitude());
        Log.d(TAG, "Longitude: " + location.getLongitude());
        Log.d(TAG, "Accuracy: " + location.getAccuracy() + "m");
        Log.d(TAG, "Provider: " + location.getProvider());

        updateLocationUI(location);
        uploadLocationToFirebase(location);

        // Push to WebView JS and Firestore
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        String js = "window.currentLocation = { lat: " + lat + ", lon: " + lon + " };";
        runOnUiThread(() -> {
            if (webView != null) {
                webView.evaluateJavascript(js, null);
                webView.evaluateJavascript(
                        "if (typeof updateDeviceStatus === 'function') updateDeviceStatus();", null
                );
            }
        });
    }

    private void updateLocationUI(Location location) {
        String locationInfo = String.format(Locale.getDefault(),
                "📍 LIVE LOCATION\n" +
                        "Lat: %.6f\n" +
                        "Lng: %.6f\n" +
                        "Accuracy: %.1fm\n" +
                        "Provider: %s\n" +
                        "Speed: %.1f km/h\n" +
                        "Updated: %s",
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                location.getProvider(),
                location.getSpeed() * 3.6,
                new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date())
        );
        locationStatusText.setText(locationInfo);
    }

    private void uploadLocationToFirebase(Location location) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("accuracy", location.getAccuracy());
        locationData.put("provider", location.getProvider());
        locationData.put("timestamp", System.currentTimeMillis());
        locationData.put("device_model", Build.MANUFACTURER + " " + Build.MODEL);
        locationData.put("last_seen", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        databaseRef.setValue(locationData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location uploaded to Firebase successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload location: " + e.getMessage()));
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestForegroundLocationPermission();
        } else {
            checkBackgroundLocationPermission();
            startHighPrecisionTracking();  // Start automatically when permissions are there!
        }
    }

    private void requestForegroundLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionExplanationDialog("Location Access Required",
                    "This app needs precise location access to provide accurate tracking.",
                    () -> ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            }, FOREGROUND_PERMISSION_REQUEST_CODE));
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, FOREGROUND_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestBackgroundLocationPermission();
            } else {
                Log.d(TAG, "All permissions granted - ready for tracking");
            }
        }
    }

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showPermissionExplanationDialog("Background Location Required",
                    "This app needs 'Allow all the time' location permission.",
                    () -> ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            BACKGROUND_PERMISSION_REQUEST_CODE));
        }
    }

    private void showPermissionExplanationDialog(String title, String message, Runnable onAccept) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Grant Permission", (dialog, which) -> onAccept.run())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Location permission is required for tracking", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FOREGROUND_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Foreground location permission granted");
                    checkBackgroundLocationPermission();
                    startHighPrecisionTracking();  // Start immediately after permission!
                } else {
                    Toast.makeText(this, "Location permission denied - app cannot function", Toast.LENGTH_LONG).show();
                }
                break;
            case BACKGROUND_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Background location permission granted");
                } else {
                    Toast.makeText(this, "Background permission denied - tracking limited to foreground", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void startHighPrecisionTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "High-precision location tracking started");
                    // Start/background service for always-on tracking:
                    Intent serviceIntent = new Intent(this, LocationTrackingService.class);
                    ContextCompat.startForegroundService(this, serviceIntent);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to start location tracking: " + e.getMessage());
                    Toast.makeText(this, "Failed to start tracking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Tracking continues via foreground service!
    }
}
