package com.example.staudodge

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat.getMainExecutor
import com.google.android.gms.location.*

/**
 * This class handles getting the location from the users. For some reason, at least on my emulator,
 * the deprecated way of fetching location goes way faster and doesn't seem to fail as the other one
 * does from time to time.
 */
class LocationServiceClass : android.location.LocationListener {
    var lastLocation: Location? = null

    private lateinit var context: Context
    private lateinit var activity: MainActivity

    private lateinit var locationManager: LocationManager

    private lateinit var locationRequestBuilder: LocationRequest.Builder
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    //Initializes so this class have reference to activity and context.
    fun initialize(activity: MainActivity) {
        this.activity = activity
        context = activity.baseContext
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    /**
     * This function have two different methods of fetching the location from the user based on the
     * SDK version. If below API 30 it uses a deprecated way of doing it otherwise a better one.
     * The "MissingPermission" is fine to suppress since there is no way of getting here without permission
     */
    @SuppressLint("MissingPermission")
    fun updateLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                LocationManager.GPS_PROVIDER,
                null,
                getMainExecutor(context)
            ) { location ->
                lastLocation = location
                activity.updateIncidents()
            }
        } else {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null)
        }
    }

    //Where the location result come from "requestSingleUpdate"
    override fun onLocationChanged(location: Location) {
        lastLocation = location
        activity.updateIncidents()
    }

    //The proper way of the getting location but for some reason I can't get this to work.
    @SuppressLint("MissingPermission")
    private fun initializeFusedLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        locationRequestBuilder =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000)

        locationRequest = locationRequestBuilder.build()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                for (location in locationResult.locations) {
                    val lat = locationResult.lastLocation?.latitude
                    val lng = locationResult.lastLocation?.longitude
                    lastLocation = locationResult.lastLocation
                    activity.updateIncidents()
                }
            }
        }

        Looper.myLooper()?.let {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                it
            )
        }
    }
}