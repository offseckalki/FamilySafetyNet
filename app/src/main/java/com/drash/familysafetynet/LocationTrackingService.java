package com.drash.familysafetynet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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

public class LocationTrackingService extends Service {

    private static final String TAG = "LocationTrackingService";
    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private static final int NOTIFICATION_ID = 12345;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private DatabaseReference databaseRef;
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationTrackingService created");

        createNotificationChannel();
        initializeLocationServices();
        initializeFirebase();
        setupLocationCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationTrackingService started");

        startForeground(NOTIFICATION_ID, createNotification("Starting background location tracking..."));
        startLocationUpdates();

        return START_STICKY; // Restart if killed by system
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "GPS Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks your location in the background for safety monitoring");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Family Safety Net - Active")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_location_on) // Add this icon to drawable
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Ultra-high precision for background tracking
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000) // 3 seconds
                .setMinUpdateIntervalMillis(1500) // Minimum 1.5 seconds
                .setMinUpdateDistanceMeters(2.0f) // Update every 2 meters
                .setMaxUpdateDelayMillis(5000) // Max delay 5 seconds
                .setWaitForAccurateLocation(true)
                .build();
    }

    private void initializeFirebase() {
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        databaseRef = FirebaseDatabase.getInstance().getReference("live_locations").child(deviceId);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        };
    }

    private void handleLocationUpdate(Location location) {
        Log.d(TAG, String.format("Background Location: %.6f, %.6f (±%.1fm)",
                location.getLatitude(), location.getLongitude(), location.getAccuracy()));

        // Update notification with current location
        String locationText = String.format(Locale.getDefault(),
                "Lat: %.4f, Lng: %.4f, Accuracy: %.0fm",
                location.getLatitude(), location.getLongitude(), location.getAccuracy());

        Notification updatedNotification = createNotification("📍 " + locationText);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, updatedNotification);

        // Upload to Firebase
        uploadLocationToFirebase(location);
    }

    private void uploadLocationToFirebase(Location location) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("accuracy", location.getAccuracy());
        locationData.put("provider", location.getProvider());
        locationData.put("speed", location.getSpeed());
        locationData.put("altitude", location.getAltitude());
        locationData.put("bearing", location.getBearing());
        locationData.put("timestamp", System.currentTimeMillis());
        locationData.put("tracking_mode", "background");
        locationData.put("last_seen", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        databaseRef.setValue(locationData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Background location uploaded to Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Firebase upload failed: " + e.getMessage()));
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing location permission");
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Background location updates started"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to start background location updates: " + e.getMessage());
                    stopSelf();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationTrackingService destroyed");

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
