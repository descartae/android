package org.descartae.android.presenter

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnSuccessListener

/**
 * The desired interval for location updates. Inexact. Updates may be more or less frequent.
 */
private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

/**
 * The fastest rate for active location updates. Exact. Updates will never be more frequent
 * than this value.
 */
private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

abstract class BaseLocationPresenter protected constructor(internal var mFusedLocationClient: FusedLocationProviderClient) : OnSuccessListener<Location> {

    var currentLocation: Location? = null

    protected abstract fun updateCurrentLocation(currentLocation: Location)

    @SuppressLint("MissingPermission")
    fun requestLocation() {

        val mLocationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {

                // If location already fetched, ignore and flush
                if (currentLocation == null) {
                    val lastLocation = locationResult!!.lastLocation
                    updateCurrentLocation(lastLocation)
                    currentLocation = lastLocation
                }

                mFusedLocationClient.flushLocations()
            }
        }
        val mRequestingLocationUpdates = LocationRequest()
        mRequestingLocationUpdates.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mRequestingLocationUpdates.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mRequestingLocationUpdates.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS

        // Update Location Once
        mRequestingLocationUpdates.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(mRequestingLocationUpdates, mLocationCallback, null/* Looper */)
        mFusedLocationClient.lastLocation.addOnSuccessListener(this)
    }

    override fun onSuccess(location: Location?) {

        location?.let {
            currentLocation = it
            updateCurrentLocation(it)
        }
    }
}
