package org.descartae.android.presenter;

import android.annotation.SuppressLint;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Created by lucasmontano on 2/20/18.
 */

public abstract class BaseLocationPresenter implements OnSuccessListener<Location> {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    FusedLocationProviderClient mFusedLocationClient;

    private Location currentLocation;

    protected BaseLocationPresenter(FusedLocationProviderClient fusedLocationClient) {
        mFusedLocationClient = fusedLocationClient;
    }

    protected abstract void updateCurrentLocation(Location currentLocation);

    public Location getCurrentLocation() {
        return currentLocation;
    }

    @SuppressLint("MissingPermission")
    protected void requestLocation() {

        LocationCallback mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                updateCurrentLocation(lastLocation);
                currentLocation = lastLocation;
                mFusedLocationClient.flushLocations();
            }
        };
        LocationRequest mRequestingLocationUpdates = new LocationRequest();
        mRequestingLocationUpdates.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mRequestingLocationUpdates.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mRequestingLocationUpdates.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        // Update Location Once
        mRequestingLocationUpdates.setNumUpdates(1);

        mFusedLocationClient.requestLocationUpdates(mRequestingLocationUpdates, mLocationCallback, null /* Looper */);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this);
    }

    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            updateCurrentLocation(location);
            currentLocation = location;
        }
    }
}
