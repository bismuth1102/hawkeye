package com.skillion.hawkeye.location;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.io.File;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location location;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;
    private static final String TAG = LocationService.class.getSimpleName();
    private Handler mServiceHandler;

    public static final String file = ("data.txt");
    public static File myData = null;
    public OutputStream fo;
    private String logInfo;


    @Override
    public void onCreate() {
        Log.d("Location: ", "Location service onCreate");
        super.onCreate();
        mRequestingLocationUpdates = true;
        mLastUpdateTime = "";
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }


    private void createLocationRequest() {
        Log.d("Location: ", "create location request");

        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        Log.d("Location: ", "create location callback");

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d("Location", "Accuracy: " + locationResult.getLastLocation().getAccuracy());
                /**
                 * TODO: Implement Accuracy bounds
                 */
                logInfo = "Location:\n" + locationResult.getLastLocation().getLongitude() +
                        " | " + locationResult.getLastLocation().getLatitude() +
                        "\nAccuracy: " + locationResult.getLastLocation().getAccuracy() +
                        "\nSpeed: " + locationResult.getLastLocation().getSpeed() +
                        "\nTime: " + locationResult.getLastLocation().getTime() + "\n\n";

                Log.d("Location", logInfo);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                //updateLocationUI();
            }
        };
    }


    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }


    private void startLocationUpdates() {
        Log.d("Location: ", "start updates");

        // Begin by checking if the device has the necessary location settings.
        if (checkPermissions()) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    //    here to request the missing permissions, and then overriding
                    //    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                           int[] grantResults)
                    //    to handle the case where the user grants the permission. See the documentation
                    //    for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }catch(Exception e){
                Log.d("LOG", String.valueOf(e));
            }
        } else {
            Log.d("Location", "checkPermissions failed");

        }
    }


    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d("TAG", "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(task -> {
                    mRequestingLocationUpdates = false;
                });
    }



    /** When we start the service from main, we create it above and then start here*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Location: ", "Location service onStart");
        Log.d("Location: ", "mRequestingLocationUpdates: " + mRequestingLocationUpdates + " | checkPermissions: " + checkPermissions());
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        stopLocationUpdates();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
